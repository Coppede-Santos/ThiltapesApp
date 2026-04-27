package com.example.thiltapeshunting.game;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.thiltapeshunting.R;
import com.example.thiltapeshunting.model.Thiltape;
import com.example.thiltapeshunting.network.ApiClient;
import com.example.thiltapeshunting.session.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    public static final String EXTRA_PLAYER_ID = "extra_player_id";
    public static final String EXTRA_PLAYER_NAME = "extra_player_name";

    private GoogleMap map;
    private final ApiClient apiClient = new ApiClient();
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private int playerId;
    private boolean gameFinishedToastShown = false;
    private MaterialButton btnCapturar;
    private Thiltape selectedThiltape;
    private final Set<Integer> pendingCaptures = new HashSet<>();
    private final Set<Integer> capturedInSession = new HashSet<>();

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    confirmarCapturaNoServidor();
                } else {
                    Toast.makeText(this, "Captura cancelada: Foto não tirada", Toast.LENGTH_SHORT).show();
                    if (selectedThiltape != null) {
                        pendingCaptures.remove(selectedThiltape.getId());
                        btnCapturar.setEnabled(true);
                    }
                }
            });

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onLocationPermissionResult);

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult result) {
            if (result.getLastLocation() == null) {
                return;
            }
            currentLocation = result.getLastLocation();
            atualizarMapaEThiltapes();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        playerId = getIntent().getIntExtra(EXTRA_PLAYER_ID, -1);
        if (playerId <= 0) {
            playerId = new SessionManager(this).getPlayerId();
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnCapturar = findViewById(R.id.btnCapturar);
        btnCapturar.setOnClickListener(v -> capturarSelecionado());

        MaterialButton btnPokedex = findViewById(R.id.btnPokedex);
        btnPokedex.setOnClickListener(v -> {
            Intent intent = new Intent(this, PokedexActivity.class);
            intent.putExtra(PokedexActivity.EXTRA_PLAYER_ID, playerId);
            startActivity(intent);
        });

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.mapContainer, mapFragment).commit();
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        garantirPermissaoELocalizacao();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(this);
        garantirPermissaoELocalizacao();
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        if (!(marker.getTag() instanceof Thiltape) || currentLocation == null) {
            btnCapturar.setVisibility(View.GONE);
            selectedThiltape = null;
            return false;
        }

        Thiltape thiltape = (Thiltape) marker.getTag();
        if (capturedInSession.contains(thiltape.getId())) {
            btnCapturar.setVisibility(View.GONE);
            selectedThiltape = null;
            return false;
        }

        float distancia = distanciaAte(thiltape);
        if (distancia > 100f) {
            btnCapturar.setVisibility(View.GONE);
            selectedThiltape = null;
            Toast.makeText(this, R.string.captura_fora_do_raio, Toast.LENGTH_SHORT).show();
            return false;
        }

        selectedThiltape = thiltape;
        btnCapturar.setText(getString(R.string.btn_capturar) + " " + thiltape.getNome());
        btnCapturar.setVisibility(View.VISIBLE);
        marker.showInfoWindow();
        return true;
    }

    private void garantirPermissaoELocalizacao() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        iniciarAtualizacaoLocalizacao();
    }

    private void onLocationPermissionResult(boolean granted) {
        if (!granted) {
            Toast.makeText(this, R.string.erro_localizacao, Toast.LENGTH_LONG).show();
            return;
        }
        iniciarAtualizacaoLocalizacao();
    }

    private void iniciarAtualizacaoLocalizacao() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest request = new LocationRequest.Builder(5000)
                .setMinUpdateIntervalMillis(3000)
                .build();

        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
            if (map != null) {
                map.setMyLocationEnabled(true);
            }
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.erro_localizacao, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.erro_mapa, Toast.LENGTH_LONG).show();
        }
    }

    private void atualizarMapaEThiltapes() {
        if (map == null || currentLocation == null || playerId <= 0) {
            return;
        }

        LatLng playerLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(playerLatLng, 16f));

        apiClient.buscarThiltapes(playerId, currentLocation.getLatitude(), currentLocation.getLongitude(), this::renderizarThiltapes);
    }

    private void renderizarThiltapes(List<Thiltape> thiltapes) {
        if (map == null || currentLocation == null) {
            return;
        }

        map.clear();

        for (Thiltape thiltape : thiltapes) {
            LatLng pos = new LatLng(thiltape.getLat(), thiltape.getLng());
            Marker marker = map.addMarker(new MarkerOptions().position(pos).title(thiltape.getNome()).snippet(thiltape.getRaridade()));
            if (marker != null) {
                marker.setTag(thiltape);
            }
        }

        if (selectedThiltape == null) {
            btnCapturar.setVisibility(View.GONE);
        }
    }

    private void capturarSelecionado() {
        if (selectedThiltape == null || currentLocation == null) {
            Toast.makeText(this, R.string.captura_toque_no_marcador, Toast.LENGTH_SHORT).show();
            return;
        }

        final Thiltape thiltape = selectedThiltape;
        if (capturedInSession.contains(thiltape.getId()) || pendingCaptures.contains(thiltape.getId())) {
            return;
        }

        if (distanciaAte(thiltape) > 100f) {
            btnCapturar.setVisibility(View.GONE);
            selectedThiltape = null;
            Toast.makeText(this, R.string.captura_fora_do_raio, Toast.LENGTH_SHORT).show();
            return;
        }

        pendingCaptures.add(thiltape.getId());
        btnCapturar.setEnabled(false);

        // ABRE A CÂMERA ANTES DE CONFIRMAR NO SERVIDOR (REQUISITO DA COMANDA)
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            // Se não houver app de câmera (raro), confirma direto
            confirmarCapturaNoServidor();
        }
    }

    private void confirmarCapturaNoServidor() {
        if (selectedThiltape == null || currentLocation == null) return;

        apiClient.capturar(
                playerId,
                selectedThiltape.getId(),
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                ok -> {
                    if (selectedThiltape != null) {
                        pendingCaptures.remove(selectedThiltape.getId());
                        btnCapturar.setEnabled(true);
                    }
                    if (!ok) {
                        Toast.makeText(this, R.string.captura_falhou, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    capturedInSession.add(selectedThiltape.getId());
                    btnCapturar.setVisibility(View.GONE);
                    Toast.makeText(this, getString(R.string.captura_sucesso) + " " + selectedThiltape.getNome(), Toast.LENGTH_SHORT).show();
                    selectedThiltape = null;
                    atualizarMapaEThiltapes();
                    validarFimDeJogo();
                }
        );
    }

    private float distanciaAte(Thiltape thiltape) {
        float[] resultados = new float[1];
        Location.distanceBetween(
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                thiltape.getLat(),
                thiltape.getLng(),
                resultados
        );
        return resultados[0];
    }

    private void validarFimDeJogo() {
        apiClient.statusPartida(playerId, status -> {
            if (status != null && status.isFinalizada() && !gameFinishedToastShown) {
                gameFinishedToastShown = true;
                Toast.makeText(this, R.string.jogo_concluido, Toast.LENGTH_LONG).show();
            }
        });
    }
}
