package com.example.florence;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPassword";
    private EditText etEmail;
    private Button btnSendCode;
    private FirebaseAuth mAuth;
    private String verificationCode;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_email);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.et_email);
        btnSendCode = findViewById(R.id.btn_checkEmail);

        btnSendCode.setOnClickListener(v -> validateAndSendCode());
    }

    private void validateAndSendCode() {
        String email = etEmail.getText().toString().trim().toLowerCase();
        Log.d(TAG, "Processing email: " + email);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Введите почту");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Некорректный формат email");
            etEmail.requestFocus();
            return;
        }

        checkEmailAndSendResources(email);
    }

    private void checkEmailAndSendResources(String email) {
        // 1. Отправляем стандартную ссылку восстановления через Firebase
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 2. Генерируем и отправляем 6-значный код
                        userEmail = email;
                        verificationCode = generateVerificationCode();
                        sendVerificationCodeEmail(email, verificationCode);

                        // 3. Показываем диалог для ввода кода
                        showVerificationDialog();
                    } else {
                        handleEmailError(task.getException());
                    }
                });
    }

    private void sendVerificationCodeEmail(String email, String code) {
        new Thread(() -> {
            try {
                // Реальная отправка кода на email (реализуйте ваш EmailSender)
                EmailSender sender = new EmailSender();
                String subject = "Код подтверждения для сброса пароля";
                String body = "Ваш код подтверждения: " + code +
                        "\n\nИспользуйте его в приложении для завершения сброса пароля.";

                sender.sendEmail(email, subject, body);

                runOnUiThread(() -> {
                    Log.d(TAG, "Verification code sent: " + code);
                    Toast.makeText(this,
                            "Ссылка для сброса и код подтверждения отправлены на " + email,
                            Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Failed to send code", e);
                    Toast.makeText(this,
                            "Ошибка отправки кода, но ссылка для сброса отправлена",
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void showVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_verification, null);
        builder.setView(view).setCancelable(false);

        EditText etCode = view.findViewById(R.id.et_verification_code);
        Button btnVerify = view.findViewById(R.id.btn_verify);

        AlertDialog dialog = builder.create();

        btnVerify.setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            if (code.equals(verificationCode)) {
                dialog.dismiss();
                proceedToPasswordReset();
            } else {
                etCode.setError("Неверный код");
            }
        });

        dialog.show();
    }

    private void proceedToPasswordReset() {
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        intent.putExtra("email", userEmail);
        intent.putExtra("verification_passed", true);
        startActivity(intent);
        finish();
    }

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void handleEmailError(Exception ex) {
        Log.e(TAG, "Email error", ex);
        if (ex instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(this,
                    "Аккаунт с таким email не найден",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,
                    "Ошибка: " + ex.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}