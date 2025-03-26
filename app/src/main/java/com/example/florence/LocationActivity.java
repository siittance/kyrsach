package com.example.florence;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.GeoObject;
import com.yandex.mapkit.GeoObjectCollection;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraPosition;
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

    private MapView mapView;
    private UserLocationLayer userLocationLayer;
    private PlacemarkMapObject userMark;
    private TextView deliveryAddressText;
    private Button selectAddressButton;
    private String currentAddress = "";
    private SearchManager searchManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Инициализация MapKit перед setContentView
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);

        setContentView(R.layout.activity_map);

        // Инициализация элементов UI
        mapView = findViewById(R.id.mapview);
        deliveryAddressText = findViewById(R.id.delivery_address_text);
        selectAddressButton = findViewById(R.id.select_address_button);

        // Инициализация SearchManager
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);

        // Настройка карты и маркера
        MapObjectCollection mapObjects = mapView.getMap().getMapObjects();
        userMark = mapObjects.addPlacemark(new Point(0, 0));
        userMark.setIcon(ImageProvider.fromResource(this, R.drawable.ic_pin));

        // Настройка параметров карты
        mapView.getMap().setRotateGesturesEnabled(false);

        // Настройка определения местоположения
        MapKit mapKit = MapKitFactory.getInstance();
        userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(true);
        userLocationLayer.setObjectListener(this);

        // Обработчик нажатия на карту
        mapView.getMap().addInputListener(new com.yandex.mapkit.map.InputListener() {
            @Override
            public void onMapTap(@NonNull com.yandex.mapkit.map.Map map, @NonNull Point point) {
                moveMarkerToPoint(point);
                reverseGeocode(point);
            }

            @Override
            public void onMapLongTap(@NonNull com.yandex.mapkit.map.Map map, @NonNull Point point) {
            }
        });

        // Обработчик кнопки выбора адреса
        selectAddressButton.setOnClickListener(v -> {
            if (!currentAddress.isEmpty()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_address", currentAddress);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Выберите точку на карте", Toast.LENGTH_SHORT).show();
            }
        });

        // Настройка нижней навигации
        setupBottomNavigation();
    }

    private void reverseGeocode(Point point) {
        searchManager.submit(point, 1, new SearchOptions(), this);
    }

    @Override
    public void onSearchResponse(@NonNull Response response) {
        if (response.getCollection().getChildren().size() > 0) {
            GeoObjectCollection.Item item = response.getCollection().getChildren().get(0);
            GeoObject geoObject = ((GeoObjectCollection.Item) item).getObj();
            if (geoObject != null) {
                ToponymObjectMetadata metadata = geoObject.getMetadataContainer().getItem(ToponymObjectMetadata.class);
                if (metadata != null) {
                    currentAddress = metadata.getAddress().getFormattedAddress();
                    runOnUiThread(() -> deliveryAddressText.setText(currentAddress));
                } else {
                    runOnUiThread(() -> deliveryAddressText.setText("Адрес не найден"));
                }
            } else {
                runOnUiThread(() -> deliveryAddressText.setText("Адрес не найден"));
            }
        } else {
            runOnUiThread(() -> deliveryAddressText.setText("Адрес не найден"));
        }
    }

    @Override
    public void onSearchError(@NonNull Error error) {
        currentAddress = "Не удалось определить адрес";
        runOnUiThread(() -> deliveryAddressText.setText(currentAddress));
    }

    private void moveMarkerToPoint(Point point) {
        userMark.setGeometry(point);
        mapView.getMap().move(
                new CameraPosition(point, 15, 0, 0),
                new Animation(Animation.Type.SMOOTH, 0.5f),
                null
        );
    }

    @Override
    public void onObjectAdded(@NonNull UserLocationView userLocationView) {
        Point userLocation = userLocationView.getArrow().getGeometry();
        moveMarkerToPoint(userLocation);
        reverseGeocode(userLocation);
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