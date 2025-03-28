package com.example.florence;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private EditText emailField;
    private TextView ordersCountField;
    private Button saveButton, changePasswordButton, exitAccountButton, deleteAccountButton;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        initFirebase();
        loadUserData();
        setupButtonListeners();
        setupBottomNavigation();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToAuth();
            return;
        }
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
    }

    private void initViews() {
        emailField = findViewById(R.id.emailField);
        ordersCountField = findViewById(R.id.phoneField);
        saveButton = findViewById(R.id.saveButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        exitAccountButton = findViewById(R.id.exitAccountButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);
    }

    private void redirectToAuth() {
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }

    private void loadUserData() {
        if (currentUser != null) {
            emailField.setText(currentUser.getEmail());
            loadOrdersCount();
        }
    }

    private void loadOrdersCount() {
        userRef.child("ordersCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long count = snapshot.exists() ? snapshot.getValue(Long.class) : 0L;
                ordersCountField.setText(String.valueOf(count != null ? count : 0));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load orders count", error.toException());
                ordersCountField.setText("0");
            }
        });
    }

    private void setupButtonListeners() {
        saveButton.setOnClickListener(v -> updateEmail());
        changePasswordButton.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
        exitAccountButton.setOnClickListener(v -> signOut());
        deleteAccountButton.setOnClickListener(v -> confirmAccountDeletion());
    }

    private void updateEmail() {
        String newEmail = emailField.getText().toString().trim();
        if (TextUtils.isEmpty(newEmail)) {
            emailField.setError("Введите email");
            return;
        }

        if (newEmail.equals(currentUser.getEmail())) {
            Toast.makeText(this, "Email не изменен", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressDialog("Обновление email...");
        currentUser.updateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    dismissProgressDialog();
                    if (task.isSuccessful()) {
                        updateUserEmailInDatabase(newEmail);
                        Toast.makeText(this, "Email обновлен", Toast.LENGTH_SHORT).show();
                    } else {
                        handleEmailUpdateError(task.getException());
                    }
                });
    }

    private void handleEmailUpdateError(Exception e) {
        if (e != null && e.getMessage() != null && e.getMessage().contains("requires recent authentication")) {
            showReauthDialog(false); // Запросить повторную аутентификацию
        } else {
            Toast.makeText(this, "Ошибка: " + (e != null ? e.getMessage() : "неизвестная ошибка"),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showReauthDialog(boolean forDeletion) {
        // Создаем вью для диалога с полем ввода пароля
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_reauthenticate, null);
        EditText passwordInput = view.findViewById(R.id.passwordField);

        // Строим диалог
        new AlertDialog.Builder(this)
                .setTitle("Подтверждение")
                .setMessage("Введите текущий пароль для подтверждения")
                .setView(view)
                .setPositiveButton("Подтвердить", (d, w) -> {
                    String password = passwordInput.getText().toString();
                    if (!TextUtils.isEmpty(password)) {
                        reauthenticate(password, forDeletion); // Переходим к процессу повторной аутентификации
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void reauthenticate(String password, boolean forDeletion) {
        // Показываем прогресс-диалог
        showProgressDialog("Проверка...");

        // Создаем учетные данные для повторной аутентификации с помощью email и пароля
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);

        // Выполняем повторную аутентификацию
        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    dismissProgressDialog(); // Закрываем прогресс-диалог

                    if (task.isSuccessful()) {
                        // Если аутентификация успешна, то выполняем нужное действие
                        if (forDeletion) {
                            deleteAccount(); // Удаление аккаунта
                        } else {
                            updateEmail(); // Обновление email
                        }
                    } else {
                        // Если ошибка, то показываем сообщение об ошибке
                        Toast.makeText(ProfileActivity.this, "Неверный пароль", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void updateUserEmailInDatabase(String newEmail) {
        userRef.child("email").setValue(newEmail)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update email in database", e));
    }

    private void signOut() {
        mAuth.signOut();
        clearSharedPreferences();
        redirectToAuth();
    }

    private void clearSharedPreferences() {
        getSharedPreferences("app_prefs", MODE_PRIVATE).edit().clear().apply();
    }

    private void confirmAccountDeletion() {
        new AlertDialog.Builder(this)
                .setTitle("Удаление аккаунта")
                .setMessage("Вы уверены? Это действие нельзя отменить!")
                .setPositiveButton("Удалить", (d, w) -> deleteAccount())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteAccount() {
        showProgressDialog("Удаление аккаунта...");

        userRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                deleteFirebaseUser();
            } else {
                dismissProgressDialog();
                Log.e(TAG, "Failed to delete user data", task.getException());
                Toast.makeText(this, "Ошибка удаления данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteFirebaseUser() {
        currentUser.delete()
                .addOnCompleteListener(task -> {
                    dismissProgressDialog();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Аккаунт удален", Toast.LENGTH_SHORT).show();
                        signOut();
                    } else {
                        Toast.makeText(this, "Ошибка удаления аккаунта", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showProgressDialog(String message) {
        progressDialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_profile) {
                return true;
            }
            Class<?> targetActivity = null;
            if (item.getItemId() == R.id.nav_favorite) {
                targetActivity = FavoriteActivity.class;
            } else if (item.getItemId() == R.id.nav_dashboard) {
                targetActivity = MainActivity.class;
            } else if (item.getItemId() == R.id.nav_location) {
                targetActivity = LocationActivity.class;
            } else if (item.getItemId() == R.id.nav_basket) {
                targetActivity = BasketActivity.class;
            }

            if (targetActivity != null) {
                startActivity(new Intent(this, targetActivity));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }
}
