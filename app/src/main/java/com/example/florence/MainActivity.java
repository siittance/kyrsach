package com.example.florence;

import android.content.Intent;
import android.os.Bundle;
<<<<<<< HEAD
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
=======
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_LOCATION = 101;

    private TextView addressLink;
    private boolean isActivityResumed = false;
>>>>>>> 295aa9b (noyt)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
<<<<<<< HEAD

        addressLink = findViewById(R.id.address_link);
        productsList = findViewById(R.id.products_list);

        // Обработчик нажатия на адрес для открытия LocationActivity
=======
        Log.d(TAG, "onCreate");

        initViews();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityResumed = true;
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityResumed = false;
        Log.d(TAG, "onPause");
    }

    private void initViews() {
        addressLink = findViewById(R.id.address_link);
>>>>>>> 295aa9b (noyt)
        addressLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LocationActivity.class);
            startActivityForResult(intent, REQUEST_CODE_LOCATION);
        });
<<<<<<< HEAD

        // Настройка нижней навигации
        setupBottomNavigation();
=======
>>>>>>> 295aa9b (noyt)
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
<<<<<<< HEAD
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
=======
        Log.d(TAG, String.format(
                "onActivityResult: req=%d, res=%d, data=%s",
                requestCode, resultCode, data != null ? "exists" : "null"
        ));

        if (requestCode == REQUEST_CODE_LOCATION && resultCode == RESULT_OK && data != null) {
            handleLocationResult(data);
        }
    }

    private void handleLocationResult(Intent resultData) {
        if (!isActivityResumed) {
            Log.w(TAG, "Activity not resumed, delaying address update");
            new Handler().postDelayed(() -> handleLocationResult(resultData), 300);
            return;
        }

        try {
            String address = resultData.getStringExtra("selected_address");
            if (address != null && !address.isEmpty()) {
                updateAddressUI(address);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling location result", e);
        }
    }

    private void updateAddressUI(String address) {
        runOnUiThread(() -> {
            try {
                String displayText = address.length() > 30 ?
                        address.substring(0, 27) + "..." : address;
                addressLink.setText(displayText);
                Log.d(TAG, "Address updated: " + displayText);
            } catch (Exception e) {
                Log.e(TAG, "Error updating address UI", e);
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_dashboard);

        bottomNav.setOnNavigationItemSelectedListener(item -> {
>>>>>>> 295aa9b (noyt)
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                return true;
            } else if (itemId == R.id.nav_favorite) {
<<<<<<< HEAD
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
=======
                startActivity(new Intent(this, FavoriteActivity.class));
                return true;
            } else if (itemId == R.id.nav_basket) {
                startActivity(new Intent(this, BasketActivity.class));
                return true;
            } else if (itemId == R.id.nav_location) {
                startActivity(new Intent(this, LocationActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
>>>>>>> 295aa9b (noyt)
                return true;
            }
            return false;
        });
    }
}