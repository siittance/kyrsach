package com.example.florence;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FavoriteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_favorite); // Устанавливаем текущий выбранный пункт

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_favorite) {
                    // Уже на DashboardActivity, ничего не делаем
                    return true;
                } else if (itemId == R.id.nav_dashboard) {
                    startActivity(new Intent(FavoriteActivity.this, MainActivity.class));
                    overridePendingTransition(0, 0); // Убираем анимацию перехода
                    finish();
                    return true;
                } else if (itemId == R.id.nav_basket) {
                    startActivity(new Intent(FavoriteActivity.this, BasketActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_location) {
                    startActivity(new Intent(FavoriteActivity.this, LocationActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(FavoriteActivity.this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                return false;
            }
        });
    }
}