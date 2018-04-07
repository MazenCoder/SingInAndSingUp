package com.codedroid.singinandsingup;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SingUpActivity extends AppCompatActivity {

    private final String TAG = SingUpActivity.class.getSimpleName();
    private ProgressBar progressBar;

    private EditText nameUser, emailUser, passUser, confirmPassUser;
    private TextInputLayout til_name, til_email, til_pass, til_confirmPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);
        setUpView();
    }

    private void setUpView() {

        progressBar     = (ProgressBar)findViewById(R.id.progress_account);

        // EditeText
        nameUser        = (EditText)findViewById(R.id.et_sig_name);
        emailUser       = (EditText)findViewById(R.id.et_sig_email);
        passUser        = (EditText)findViewById(R.id.et_sig_pass);
        confirmPassUser = (EditText)findViewById(R.id.et_sig_confirm_pass);

        // TextInputLayou
        til_name        = (TextInputLayout)findViewById(R.id.til_name);
        til_email       = (TextInputLayout)findViewById(R.id.til_email);
        til_pass        = (TextInputLayout)findViewById(R.id.til_pass);
        til_confirmPass = (TextInputLayout)findViewById(R.id.til_confirm_pass);

    }

    public void RegisterUser(View view) {

        if (Check.isNotEmpty(nameUser)) {
            til_name.setErrorEnabled(false);

            if (Check.isNotEmpty(emailUser)) {
//                    til_email.setErrorEnabled(false);

                if (Check.isValidEmail(emailUser.getText().toString().toLowerCase().trim())) {
                    til_email.setErrorEnabled(false);

                    if (Check.isNotEmpty(passUser)) {

                        if (Check.isValidPassword(passUser.getText().toString().trim())) {
                            til_pass.setErrorEnabled(false);

                            if (Check.isNotEmpty(confirmPassUser)) {

                                if (Check.isValidPassword(confirmPassUser.getText().toString().trim())) {

                                    if (Check.doStringsMatch(passUser.getText().toString().trim(), confirmPassUser.getText().toString().trim())) {
                                        til_confirmPass.setErrorEnabled(false);
                                        // Register New User

                                        // send data to firebase
                                        registerNewEmail(emailUser.getText().toString().toLowerCase().trim(),
                                                passUser.getText().toString().trim());

                                    }else {
                                        til_confirmPass.setError(getString(R.string.your_password_do_not_match));

                                    }
                                }else {
                                    til_confirmPass.setError(getString(R.string.please_check_your_pass));
                                }
                            }else {
                                til_confirmPass.setError(getString(R.string.please_enter_your_pass));
                            }
                        }else {
                            til_pass.setError(getString(R.string.please_check_your_pass));
                        }
                    }else {
                        til_pass.setError(getString(R.string.please_enter_your_pass));
                    }
                }else {
                    til_email.setError(getString(R.string.please_check_your_email));
                }
            }else til_email.setError(getString(R.string.please_enter_your_email));
        }else {
            til_name.setError(getString(R.string.please_enter_your_name));
        }
    }

    private void registerNewEmail(final String email, final String pass) {

        showDialog();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
//                            Toast.makeText(getApplicationContext(), nameUser.getText().toString(), Toast.LENGTH_LONG).show();
                            sendVerificationEmail();
                            setUserDetails(nameUser.getText().toString());
                            FirebaseAuth.getInstance().signOut();

                            Intent i = new Intent(getApplicationContext(), SingInActivity.class);
                            i.putExtra(Check.KEY_EMAIL, email);
                            i.putExtra(Check.KEY_PASS, pass);
                            startActivity(i);
                        }
                        hideDialog();
                    }
                }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        });

    }

    private void sendVerificationEmail() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        Toast.makeText(getApplicationContext(), R.string.sent_verification, Toast.LENGTH_LONG).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(getApplicationContext(), R.string.could_not_sent_verification + "\n"
                            + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setUserDetails(String name){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
//                    .setPhotoUri()
            user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.i(TAG, "onComplete");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "OnFailure");
                }
            });

        }
    }

    private void showDialog(){
        progressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void goToLogin(View view) {
        startActivity(new Intent(SingUpActivity.this, SingInActivity.class));
        finish();
    }
}
