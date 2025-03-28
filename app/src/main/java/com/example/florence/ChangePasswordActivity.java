package com.example.florence;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private FirebaseAuth mAuth;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        mAuth = FirebaseAuth.getInstance();
        email = getIntent().getStringExtra("email");

        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_again_new_password);
        btnResetPassword = findViewById(R.id.btn_reset_pass);

        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Валидация пароля
        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Введите пароль");
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Минимум 6 символов");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Пароли не совпадают");
            return;
        }

        // Способ 1: Для аутентифицированных пользователей
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            updatePasswordForAuthenticatedUser(user, newPassword);
        }
        // Способ 2: Через email (если пользователь не вошел)
        else if (email != null) {
            resetPasswordViaEmail(email, newPassword);
        } else {
            Toast.makeText(this, "Ошибка: данные пользователя не найдены", Toast.LENGTH_LONG).show();
        }
    }

    private void updatePasswordForAuthenticatedUser(FirebaseUser user, String newPassword) {
        user.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showSuccessAndRedirect();
                    } else {
                        // Требуется повторная аутентификация
                        if (task.getException() != null &&
                                task.getException().getMessage().contains("requires recent authentication")) {
                            Toast.makeText(this, "Требуется повторный вход", Toast.LENGTH_LONG).show();
                            redirectToLogin();
                        } else {
                            showError(task.getException());
                        }
                    }
                });
    }

    private void resetPasswordViaEmail(String email, String newPassword) {
        // Используем ссылку для сброса из письма Firebase
        Toast.makeText(this,
                "Используйте ссылку из письма для сброса пароля",
                Toast.LENGTH_LONG).show();

        // Альтернатива: вход по временному паролю + смена
        // Это сложнее и требует дополнительной логики
    }

    private void showSuccessAndRedirect() {
        Toast.makeText(this, "Пароль успешно изменён!", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }

    private void showError(Exception exception) {
        String errorMsg = "Ошибка";
        if (exception != null) {
            errorMsg += ": " + exception.getMessage();
            Log.e("PasswordChange", "Error", exception);
        }
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }
}