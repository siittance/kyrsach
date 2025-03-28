package com.example.florence;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
<<<<<<< HEAD
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

=======
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
>>>>>>> 295aa9b (noyt)
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
<<<<<<< HEAD

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.GeoObject;
import com.yandex.mapkit.GeoObjectCollection;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
=======
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.geometry.Point;
>>>>>>> 295aa9b (noyt)
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
<<<<<<< HEAD
import com.yandex.mapkit.search.Response;
=======
>>>>>>> 295aa9b (noyt)
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.Session;
<<<<<<< HEAD
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
=======
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

public class LocationActivity extends AppCompatActivity {
    private static final String TAG = "LocationActivity";
    private static final int LOCATION_PERMISSION_CODE = 101;

    private MapView mapView;
    private TextView deliveryAddressText;
    private Button selectAddressButton;

    private PlacemarkMapObject userLocationMarker;
    private PlacemarkMapObject selectedLocationMarker;
    private MapObjectCollection mapObjects;

    private Point selectedPoint;
    private String selectedAddress;

    private FusedLocationProviderClient fusedLocationClient;
    private SearchManager searchManager;
    private Session searchSession;

    private boolean isManualSelection = false;
>>>>>>> 295aa9b (noyt)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
<<<<<<< HEAD

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
=======
        Log.d(TAG, "onCreate");

        try {
            MapKitFactory.setApiKey("1f905670-92fb-4bb5-ace9-a3107428aaa7");
            MapKitFactory.initialize(this);
            setContentView(R.layout.activity_map);

            initViews();
            setupMap();
            checkLocationPermissions();
            setupBottomNavigation();
        } catch (Exception e) {
            Log.e(TAG, "Initialization error", e);
            Toast.makeText(this, "Ошибка инициализации карты", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            mapView = findViewById(R.id.mapview);
            deliveryAddressText = findViewById(R.id.delivery_address_text);
            selectAddressButton = findViewById(R.id.select_address_button);

            selectAddressButton.setEnabled(false);
            selectAddressButton.setOnClickListener(v -> confirmSelection());
        } catch (Exception e) {
            Log.e(TAG, "View initialization error", e);
            finish();
        }
    }

    private void setupMap() {
        if (mapView == null || mapView.getMap() == null) {
            Log.e(TAG, "MapView is not initialized");
            return;
        }

        try {
            Point initialPoint = new Point(55.751574, 37.573856);
            mapView.getMap().move(
                    new CameraPosition(initialPoint, 15f, 0f, 0f),
                    new Animation(Animation.Type.SMOOTH, 0.5f),
                    null
            );

            searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);
            mapObjects = mapView.getMap().getMapObjects();

            mapView.getMap().addInputListener(new InputListener() {
                @Override
                public void onMapTap(@NonNull Map map, @NonNull Point point) {
                    handleMapTap(point);
                }

                @Override
                public void onMapLongTap(@NonNull Map map, @NonNull Point point) {}
            });
        } catch (Exception e) {
            Log.e(TAG, "Map setup error", e);
        }
    }

    private void handleMapTap(Point point) {
        try {
            isManualSelection = true;
            selectedPoint = point;

            runOnUiThread(() -> {
                deliveryAddressText.setText("Определяем адрес...");
                selectAddressButton.setEnabled(false);
            });

            if (selectedLocationMarker != null) {
                mapObjects.remove(selectedLocationMarker);
            }

            selectedLocationMarker = mapObjects.addPlacemark(point);
            selectedLocationMarker.setIcon(
                    ImageProvider.fromBitmap(getBitmapFromVectorDrawable(R.drawable.ic_pin_selected))
            );

            mapView.getMap().move(
                    new CameraPosition(point, 15f, 0f, 0f),
                    new Animation(Animation.Type.SMOOTH, 0.3f),
                    null
            );

            searchAddress(point);
        } catch (Exception e) {
            Log.e(TAG, "Map tap handling error", e);
            runOnUiThread(() -> {
                deliveryAddressText.setText("Ошибка при выборе точки");
                selectAddressButton.setEnabled(false);
            });
        }
    }

    private void searchAddress(Point point) {
        try {
            if (searchSession != null) {
                searchSession.cancel();
            }

            SearchOptions searchOptions = new SearchOptions();
            searchOptions.setResultPageSize(1);

            searchSession = searchManager.submit(
                    point,
                    15,
                    searchOptions,
                    new Session.SearchListener() {
                        @Override
                        public void onSearchResponse(@NonNull com.yandex.mapkit.search.Response response) {
                            if (!response.getCollection().getChildren().isEmpty()) {
                                selectedAddress = response.getCollection().getChildren().get(0).getObj().getName();
                            } else {
                                selectedAddress = "Адрес не определён";
                            }

                            runOnUiThread(() -> {
                                deliveryAddressText.setText(selectedAddress);
                                selectAddressButton.setEnabled(true);
                            });
                        }

                        @Override
                        public void onSearchError(@NonNull Error error) {
                            runOnUiThread(() -> {
                                deliveryAddressText.setText("Ошибка определения адреса");
                                selectAddressButton.setEnabled(false);
                            });
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Address search error", e);
        }
    }

    private void confirmSelection() {
        if (selectedPoint == null || selectedAddress == null) {
            Toast.makeText(this, "Сначала выберите место на карте", Toast.LENGTH_SHORT).show();
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_address", selectedAddress);
            resultIntent.putExtra("latitude", selectedPoint.getLatitude());
            resultIntent.putExtra("longitude", selectedPoint.getLongitude());

            setResult(RESULT_OK, resultIntent);
            Log.d(TAG, "Returning address: " + selectedAddress);
            finish();
        });
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        }
    }

    private void startLocationUpdates() {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null && !isManualSelection) {
                    updateUserLocation(new Point(location.getLatitude(), location.getLongitude()));
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Не удалось получить местоположение", Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Location updates error", e);
        }
    }

    private void updateUserLocation(Point point) {
        try {
            if (userLocationMarker != null) {
                mapObjects.remove(userLocationMarker);
            }

            userLocationMarker = mapObjects.addPlacemark(point);
            userLocationMarker.setIcon(
                    ImageProvider.fromBitmap(getBitmapFromVectorDrawable(R.drawable.ic_pin))
            );

            mapView.getMap().move(
                    new CameraPosition(point, 15f, 0f, 0f),
                    new Animation(Animation.Type.SMOOTH, 1f),
                    null
            );

            selectedPoint = point;
            searchAddress(point);
        } catch (Exception e) {
            Log.e(TAG, "User location update error", e);
        }
    }

    private Bitmap getBitmapFromVectorDrawable(int drawableId) {
        try {
            Drawable drawable = ContextCompat.getDrawable(this, drawableId);
            if (drawable == null) return null;

            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Bitmap creation error", e);
            return null;
>>>>>>> 295aa9b (noyt)
        }
    }

    private void setupBottomNavigation() {
<<<<<<< HEAD
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_location);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
=======
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_location);

        bottomNav.setOnNavigationItemSelectedListener(item -> {
>>>>>>> 295aa9b (noyt)
            int itemId = item.getItemId();

            if (itemId == R.id.nav_location) {
                return true;
<<<<<<< HEAD
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
=======
            } else if (itemId == R.id.nav_dashboard) {
                navigateTo(MainActivity.class);
                return true;
            } else if (itemId == R.id.nav_favorite) {
                navigateTo(FavoriteActivity.class);
                return true;
            } else if (itemId == R.id.nav_basket) {
                navigateTo(BasketActivity.class);
                return true;
            } else if (itemId == R.id.nav_profile) {
                navigateTo(ProfileActivity.class);
>>>>>>> 295aa9b (noyt)
                return true;
            }
            return false;
        });
    }

<<<<<<< HEAD
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
=======
    private void navigateTo(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
>>>>>>> 295aa9b (noyt)
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
<<<<<<< HEAD
        mapView.onStart();
=======
        if (mapView != null) {
            mapView.onStart();
        }
>>>>>>> 295aa9b (noyt)
    }

    @Override
    protected void onStop() {
<<<<<<< HEAD
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }
=======
        if (mapView != null) {
            mapView.onStop();
        }
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (searchSession != null) {
            searchSession.cancel();
        }
        super.onDestroy();
    }
>>>>>>> 295aa9b (noyt)
}