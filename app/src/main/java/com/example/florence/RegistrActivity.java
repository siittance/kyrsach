package com.example.florence;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RegistrActivity extends AppCompatActivity {

    private EditText etLogin, etPassword, etConfirmPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String verificationCode;
    private String tempEmail;
    private String tempPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registr);

        // Инициализация Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Находим View элементы
        etLogin = findViewById(R.id.et_login);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.again_password);
        btnRegister = findViewById(R.id.btn_login);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Валидация данных
        if (TextUtils.isEmpty(email)) {
            etLogin.setError("Введите почту");
            etLogin.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etLogin.setError("Введите корректную почту");
            etLogin.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Введите пароль");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Пароль должен содержать минимум 6 символов");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Пароли не совпадают");
            etConfirmPassword.requestFocus();
            return;
        }

        // Генерация кода подтверждения
        verificationCode = generateVerificationCode();
        tempEmail = email;
        tempPassword = password;

        // Отправка кода на почту с использованием EmailSender
        sendVerificationEmail(email, verificationCode);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    private void sendVerificationEmail(String email, String code) {
        new Thread(() -> {
            EmailSender sender = new EmailSender();
            String subject = "Код подтверждения регистрации";
            String message = "Ваш код подтверждения: " + code;

            sender.sendEmail(email, subject, message);

            runOnUiThread(() -> {
                // Показываем диалог для ввода кода только после успешной отправки
                showVerificationDialog();
                Toast.makeText(RegistrActivity.this,
                        "Код подтверждения отправлен на вашу почту", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void showVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_verification, null);
        builder.setView(dialogView);

        EditText etCode = dialogView.findViewById(R.id.et_verification_code);
        Button btnVerify = dialogView.findViewById(R.id.btn_verify);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        btnVerify.setOnClickListener(v -> {
            String enteredCode = etCode.getText().toString().trim();
            if (enteredCode.equals(verificationCode)) {
                dialog.dismiss();
                createUserAccount(tempEmail, tempPassword);
            } else {
                Toast.makeText(RegistrActivity.this,
                        "Неверный код подтверждения", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void createUserAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserData(user, email, password);
                    } else {
                        Toast.makeText(RegistrActivity.this,
                                "Ошибка регистрации: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserData(FirebaseUser user, String email, String password) {
        String encryptedPassword = encryptPassword(password);

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("password", encryptedPassword);
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("role", "user");

        db.collection("users")
                .document(user.getUid())
                .set(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegistrActivity.this,
                                "Регистрация завершена успешно!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(RegistrActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegistrActivity.this,
                                "Ошибка сохранения данных: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String encryptPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    public void goToLogin(View view) {
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }
}