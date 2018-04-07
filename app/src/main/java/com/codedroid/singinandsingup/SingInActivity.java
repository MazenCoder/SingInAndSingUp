package com.codedroid.singinandsingup;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codedroid.singinandsingup.fragment.PasswordResetDialog;
import com.codedroid.singinandsingup.fragment.ResendVerificationDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SingInActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener stateListener;
    private ProgressBar progressBar;
    private TextInputLayout til_log_email, til_log_pass;
    private EditText etLogEmail, etLogPass;
    private final String TAG = SingInActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_in);


        setUpView();
//        setUpFirebase();
        setupFirebaseAuth();

        Intent i = getIntent();
        String email = i.getStringExtra(Check.KEY_EMAIL);
        String pass = i.getStringExtra(Check.KEY_PASS);
        etLogEmail.setText(email);
        etLogPass.setText(pass);
    }

    private void setUpView() {

        progressBar     = (ProgressBar)findViewById(R.id.progress);

        // widget
        til_log_email   = (TextInputLayout) findViewById(R.id.til_log_email);
        til_log_pass    = (TextInputLayout) findViewById(R.id.til_log_pas);

        etLogEmail      = (EditText) findViewById(R.id.et_lg_email);
        etLogPass       = (EditText) findViewById(R.id.et_lg_pass);
    }

    private void showDialog(){
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideDialog(){
        if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void login(View view) {

        if (Check.isNotEmpty(etLogEmail)) {

            if (Check.isValidEmail(etLogEmail.getText().toString().toLowerCase().trim())) {
                til_log_email.setErrorEnabled(false);

                if (Check.isNotEmpty(etLogPass)) {

                    if (Check.isValidPassword(etLogPass.getText().toString().trim())) {
                        til_log_pass.setErrorEnabled(false);

                        // Login User
                        signInUser(etLogEmail.getText().toString().toLowerCase().trim(),
                                etLogPass.getText().toString().trim());
                    }else {
                        til_log_pass.setError(getString(R.string.please_check_your_pass));
                    }
                }else {
                    til_log_pass.setError(getString(R.string.please_enter_your_pass));
                }
            }else {
                til_log_email.setError(getString(R.string.please_check_your_email));
            }
        }else {
            til_log_email.setError(getString(R.string.please_enter_your_email));
        }
    }

    private void signInUser(String email, String pass) {

        showDialog();
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideDialog();

//                        startActivity(new Intent(getApplicationContext(), Home.class));
//                        Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_LONG).show();
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

    /*
        ----------------------------- Firebase setup ---------------------------------
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: started.");

        stateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    //check if email is verified
                    if(user.isEmailVerified()){
                        Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                        Toast.makeText(getApplicationContext(), getString(R.string.authentication_with) + user.getEmail(), Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    }else{
                        Toast.makeText(getApplicationContext(), getString(R.string.inbox_verification), Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        Log.d(TAG, "onAuthStateChanged: user is not emailVerified");
                    }

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: user is null");
                }
                // ...
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(stateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (stateListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(stateListener);
        }
    }

    public void goToSignUp(View view) {
        startActivity(new Intent(SingInActivity.this, SingUpActivity.class));
    }

    public void forgetPass(View view) {
        PasswordResetDialog dialog = new PasswordResetDialog();
        dialog.show(getSupportFragmentManager(), "dialog_password_reset");
    }

    public void resendVerEmail(View view) {
        ResendVerificationDialog dialog = new ResendVerificationDialog();
        dialog.show(getSupportFragmentManager(), "dialog_resend_email_verification");
    }
}
