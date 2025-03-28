package com.example.florence;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button loginButton;
<<<<<<< HEAD
    private TextView registerLink;
    private FirebaseAuth auth;

=======
    private TextView registerLink, changePassLink;
    private FirebaseAuth auth;


>>>>>>> 295aa9b (noyt)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        auth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.et_login);
        passwordField = findViewById(R.id.et_password);
        loginButton = findViewById(R.id.btn_login);
        registerLink = findViewById(R.id.tv_register);
<<<<<<< HEAD

        loginButton.setOnClickListener(v -> loginUser());
        registerLink.setOnClickListener(v -> goToRegisterActivity());
=======
        changePassLink = findViewById(R.id.tv_forgot);

        loginButton.setOnClickListener(v -> loginUser());
        registerLink.setOnClickListener(v -> goToRegisterActivity());
        changePassLink.setOnClickListener(v -> goToChangeActivity());
>>>>>>> 295aa9b (noyt)
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailField.setError("Введите почту");
            emailField.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Введите корректную почту");
            emailField.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Введите пароль");
            passwordField.requestFocus();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        goToMainActivity();
                    } else {
                        Toast.makeText(AuthActivity.this,
                                "Ошибка авторизации: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToRegisterActivity() {
        Intent intent = new Intent(AuthActivity.this, RegistrActivity.class);
        startActivity(intent);
    }

<<<<<<< HEAD
    //@Override
    //protected void onStart() {
    //super.onStart();
    //FirebaseUser currentUser = auth.getCurrentUser();
    //if (currentUser != null) {
    //goToMainActivity();
    //}
=======
    private void goToChangeActivity(){
        Intent intent = new Intent(AuthActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    //@Override
    //protected void onStart() {
        //super.onStart();
        //FirebaseUser currentUser = auth.getCurrentUser();
        //if (currentUser != null) {
            //goToMainActivity();
        //}
>>>>>>> 295aa9b (noyt)
    //}
}
