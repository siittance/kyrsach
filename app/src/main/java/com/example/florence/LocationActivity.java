package com.example.florence;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.GeoObject;
import com.yandex.mapkit.GeoObjectCollection;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.Response;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.Session;
import com.yandex.mapkit.search.ToponymObjectMetadata;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

public class LocationActivity extends AppCompatActivity implements UserLocationObjectListener, Session.SearchListener {

    private static final String MAPKIT_API_KEY = "1f905670-92fb-4bb5-ace9-a3107428aaa7";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;

    private MapView mapView;
    private UserLocationLayer userLocationLayer;
    private PlacemarkMapObject userMark;
    private TextView deliveryAddressText;
    private Button selectAddressButton;
    private String currentAddress = "";
    private SearchManager searchManager;
    private boolean isFirstLocationUpdate = true;
    private boolean locationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize MapKit before setContentView
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);

        setContentView(R.layout.activity_map);

        // Initialize UI elements
        mapView = findViewById(R.id.mapview);
        deliveryAddressText = findViewById(R.id.delivery_address_text);
        selectAddressButton = findViewById(R.id.select_address_button);

        // Check location permissions
        checkLocationPermissions();

        // Make address text more visible
        deliveryAddressText.setBackgroundResource(R.drawable.selected_address_background);
        deliveryAddressText.setPadding(24, 16, 24, 16);
        deliveryAddressText.setVisibility(View.VISIBLE);
        deliveryAddressText.setCompoundDrawablePadding(8);

        // Initialize SearchManager
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);

        // Configure map and marker
        MapObjectCollection mapObjects = mapView.getMap().getMapObjects();
        userMark = mapObjects.addPlacemark(new Point(0, 0));
        userMark.setIcon(ImageProvider.fromResource(this, R.drawable.ic_pin));
        userMark.setZIndex(100);

        // Configure map parameters
        mapView.getMap().setRotateGesturesEnabled(false);
        mapView.getMap().setTiltGesturesEnabled(false);

        // Configure location detection
        MapKit mapKit = MapKitFactory.getInstance();
        userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());

        // Enable location layer only if permission granted
        if (locationPermissionGranted) {
            enableLocationFeatures();
        }

        // Set initial camera position (will be updated when location is available)
        mapView.getMap().move(
                new CameraPosition(new Point(55.751244, 37.618423), 12, 0, 0),
                new Animation(Animation.Type.SMOOTH, 0),
                null
        );

        // Map tap listener
        mapView.getMap().addInputListener(new InputListener() {
            @Override
            public void onMapTap(@NonNull Map map, @NonNull Point point) {
                moveMarkerToPoint(point);
                reverseGeocode(point);
                // Show address is being loaded
                deliveryAddressText.setText("Определяем адрес...");
                deliveryAddressText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.anim_loading, 0);
            }

            @Override
            public void onMapLongTap(@NonNull Map map, @NonNull Point point) {
            }
        });

        // Address selection button
        selectAddressButton.setOnClickListener(v -> {
            if (!currentAddress.isEmpty()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_address", currentAddress);
                resultIntent.putExtra("latitude", userMark.getGeometry().getLatitude());
                resultIntent.putExtra("longitude", userMark.getGeometry().getLongitude());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Пожалуйста, выберите точку на карте", Toast.LENGTH_SHORT).show();
            }
        });

        setupBottomNavigation();
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    private void enableLocationFeatures() {
        if (userLocationLayer != null) {
            userLocationLayer.setVisible(true);
            userLocationLayer.setHeadingEnabled(true);
            userLocationLayer.setObjectListener(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                enableLocationFeatures();
            } else {
                Toast.makeText(this, "Для работы карты нужны разрешения на доступ к местоположению",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_location);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_location) {
                return true;
            } else if (itemId == R.id.nav_favorite) {
                startActivity(new Intent(LocationActivity.this, FavoriteActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_basket) {
                startActivity(new Intent(LocationActivity.this, BasketActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(LocationActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(LocationActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void reverseGeocode(Point point) {
        searchManager.submit(point, 1, new SearchOptions(), this);
    }

    @Override
    public void onSearchResponse(@NonNull Response response) {
        if (response.getCollection().getChildren().size() > 0) {
            GeoObjectCollection.Item item = response.getCollection().getChildren().get(0);
            GeoObject geoObject = item.getObj();
            if (geoObject != null) {
                ToponymObjectMetadata metadata = geoObject.getMetadataContainer().getItem(ToponymObjectMetadata.class);
                if (metadata != null) {
                    currentAddress = metadata.getAddress().getFormattedAddress();
                    runOnUiThread(() -> {
                        deliveryAddressText.setText(currentAddress);
                        deliveryAddressText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
                        selectAddressButton.setEnabled(true);
                        selectAddressButton.setBackgroundResource(R.drawable.rounded_button_background);
                    });
                    return;
                }
            }
        }

        // If we get here, address wasn't found
        runOnUiThread(() -> {
            currentAddress = "";
            deliveryAddressText.setText("Адрес не найден");
            deliveryAddressText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_warning, 0);
            selectAddressButton.setEnabled(false);
            selectAddressButton.setBackgroundResource(R.drawable.rounded_button_background_disabled);
        });
    }

    @Override
    public void onSearchError(@NonNull Error error) {
        runOnUiThread(() -> {
            currentAddress = "";
            deliveryAddressText.setText("Ошибка определения адреса");
            deliveryAddressText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_warning, 0);
            selectAddressButton.setEnabled(false);
            selectAddressButton.setBackgroundResource(R.drawable.rounded_button_background_disabled);
        });
    }

    private void moveMarkerToPoint(Point point) {
        userMark.setGeometry(point);
        mapView.getMap().move(
                new CameraPosition(point, 15, 0, 0),
                new Animation(Animation.Type.SMOOTH, 0.3f),
                null
        );
    }

    @Override
    public void onObjectAdded(@NonNull UserLocationView userLocationView) {
        Point userLocation = userLocationView.getArrow().getGeometry();
        moveMarkerToPoint(userLocation);
        reverseGeocode(userLocation);

        // Center map on user location on first update
        if (isFirstLocationUpdate) {
            isFirstLocationUpdate = false;
            mapView.getMap().move(
                    new CameraPosition(userLocation, 15, 0, 0),
                    new Animation(Animation.Type.SMOOTH, 0.5f),
                    null
            );
        }
    }

    @Override
    public void onObjectRemoved(@NonNull UserLocationView userLocationView) {
    }

    @Override
    public void onObjectUpdated(@NonNull UserLocationView userLocationView, @NonNull ObjectEvent objectEvent) {
        Point userLocation = userLocationView.getArrow().getGeometry();
        moveMarkerToPoint(userLocation);
        reverseGeocode(userLocation);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }
}