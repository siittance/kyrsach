package com.example.florence;


import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    //case R.id.nav_dashboard:
                        // Открыть MainActivity
                        //startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                        //return true;
                    //case R.id.nav_favorite:
                        // Открыть FavoriteActivity
                        //startActivity(new Intent(MainActivity.this, FavoriteActivity.class));
                        //return true;
                    //case R.id.nav_home:
                        // Открыть HomeActivity
                        //startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        //return true;
                    //case R.id.nav_location:
                        // Открыть LocationActivity
                        //startActivity(new Intent(MainActivity.this, LocationActivity.class));
                        //return true;
                    //case R.id.nav_profile:
                        // Открыть ProfileActivity
                        //startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                        //return true;
                }
                return false;
            }
        });
    }
}
