package com.example.florence;

import android.content.Intent;
import android.os.Bundle;
<<<<<<< HEAD
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

=======
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

    private static final String TAG = "ProfileActivity";
    private EditText emailField;
    private TextView ordersCountField;
    private Button saveButton, changePasswordButton, exitAccountButton, deleteAccountButton;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private AlertDialog progressDialog;

>>>>>>> 295aa9b (noyt)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

<<<<<<< HEAD
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile); // Устанавливаем текущий выбранный пункт

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_profile) {

                    return true;
                } else if (itemId == R.id.nav_favorite) {
                    startActivity(new Intent(ProfileActivity.this, FavoriteActivity.class));
                    overridePendingTransition(0, 0); // Убираем анимацию перехода
                    finish();
                    return true;
                } else if (itemId == R.id.nav_dashboard) {
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_location) {
                    startActivity(new Intent(ProfileActivity.this, LocationActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_basket) {
                    startActivity(new Intent(ProfileActivity.this, LocationActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }

                return false;
            }
        });
    }
}
=======
        initFirebase();
        initViews();
        setupBottomNavigation();
        setupButtonListeners();
        loadUserData();
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

    private void updateUserEmailInDatabase(String newEmail) {
        userRef.child("email").setValue(newEmail)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update email in database", e));
    }

    private void handleEmailUpdateError(Exception e) {
        if (e != null && e.getMessage() != null && e.getMessage().contains("requires recent authentication")) {
            showReauthDialog(false);
        } else {
            Toast.makeText(this, "Ошибка: " + (e != null ? e.getMessage() : "неизвестная ошибка"),
                    Toast.LENGTH_SHORT).show();
        }
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

        // 1. Удаляем данные пользователя
        userRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 2. Удаляем сам аккаунт
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
                        Log.d(TAG, "User account deleted");
                        Toast.makeText(this, "Аккаунт удален", Toast.LENGTH_SHORT).show();
                        signOut();
                    } else {
                        handleDeleteError(task.getException());
                    }
                });
    }

    private void handleDeleteError(Exception e) {
        if (e != null && e.getMessage() != null && e.getMessage().contains("requires recent authentication")) {
            showReauthDialog(true);
        } else {
            Toast.makeText(this, "Ошибка: " + (e != null ? e.getMessage() : "неизвестная ошибка"),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showReauthDialog(boolean forDeletion) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_reauthenticate, null);
        EditText passwordInput = view.findViewById(R.id.passwordField);

        new AlertDialog.Builder(this)
                .setTitle("Подтверждение")
                .setMessage("Введите текущий пароль для подтверждения")
                .setView(view)
                .setPositiveButton("Подтвердить", (d, w) -> {
                    String password = passwordInput.getText().toString();
                    if (!TextUtils.isEmpty(password)) {
                        reauthenticate(password, forDeletion);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void reauthenticate(String password, boolean forDeletion) {
        showProgressDialog("Проверка...");
        AuthCredential credential = EmailAuthProvider.getCredential(
                currentUser.getEmail(), password);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    dismissProgressDialog();
                    if (task.isSuccessful()) {
                        if (forDeletion) {
                            deleteAccount();
                        } else {
                            updateEmail();
                        }
                    } else {
                        Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show();
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
            int itemId = item.getItemId();

            if (itemId == R.id.nav_profile) {
                return true;
            } else if (itemId == R.id.nav_favorite) {
                startActivity(new Intent(ProfileActivity.this, FavoriteActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_location) {
                startActivity(new Intent(ProfileActivity.this, LocationActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_basket) {
                startActivity(new Intent(ProfileActivity.this, BasketActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }

    private void startNewActivity(Class<?> cls) {
        startActivity(new Intent(this, cls));
        overridePendingTransition(0, 0);
        finish();
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }
}
>>>>>>> 295aa9b (noyt)
