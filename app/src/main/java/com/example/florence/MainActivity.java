package com.example.florence;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION = 1001;

    private TextView addressLink;
    private RecyclerView productsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addressLink = findViewById(R.id.address_link);
        productsList = findViewById(R.id.products_list);

        // Обработчик нажатия на адрес для открытия LocationActivity
        addressLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LocationActivity.class);
            startActivityForResult(intent, REQUEST_CODE_LOCATION);
        });

        // Настройка нижней навигации
        setupBottomNavigation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOCATION && resultCode == RESULT_OK && data != null) {
            String selectedAddress = data.getStringExtra("selected_address");
            if (selectedAddress != null) {
                addressLink.setText(selectedAddress);
            }
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                return true;
            } else if (itemId == R.id.nav_favorite) {
                startActivity(new Intent(MainActivity.this, FavoriteActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_basket) {
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_location) {
                startActivity(new Intent(MainActivity.this, LocationActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}