package com.example.thiltapeshunting.admin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.thiltapeshunting.R;
import com.example.thiltapeshunting.network.ApiConfig;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Locale;

import okhttp3.*;

public class TelaCadastro extends AppCompatActivity {

    private EditText txtNome, txtLat, txtLng;
    private RadioGroup radioRaridade;
    private ImageView imgThiltape;

    private Uri imageUri;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int PICK_IMAGE = 1;
    private static final int PERMISSION_LOCATION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_cadastro);

        txtNome = findViewById(R.id.txt_nome);
        txtLat = findViewById(R.id.txt_lat);
        txtLng = findViewById(R.id.txt_lng);
        radioRaridade = findViewById(R.id.radio_raridade);
        imgThiltape = findViewById(R.id.img_thiltape);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        findViewById(R.id.btn_enviar).setOnClickListener(v -> cadastrar());
        findViewById(R.id.btn_sortear_local).setOnClickListener(v -> sortearLocalizacao());
        imgThiltape.setOnClickListener(v -> abrirGaleria());
    }

    private void sortearLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double currentLat = location.getLatitude();
                double currentLng = location.getLongitude();

                // Sorteia dentro de ~1km
                // 0.009 graus de latitude equivale a aprox 1km
                double randomLat = currentLat + (Math.random() - 0.5) * 2 * 0.009;
                double randomLng = currentLng + (Math.random() - 0.5) * 2 * (0.009 / Math.cos(Math.toRadians(currentLat)));

                txtLat.setText(String.format(Locale.US, "%.6f", randomLat));
                txtLng.setText(String.format(Locale.US, "%.6f", randomLng));
                toast("Localização sorteada em um raio de 1km!");
            } else {
                toast("Não foi possível obter sua localização. Tente ligar o GPS.");
            }
        });
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);

        if (req == PICK_IMAGE && res == RESULT_OK && data != null) {
            imageUri = data.getData();
            imgThiltape.setImageURI(imageUri);
        }
    }

    private void cadastrar() {

        String nome = txtNome.getText().toString().trim();
        String latStr = txtLat.getText().toString().trim();
        String lngStr = txtLng.getText().toString().trim();

        int selectedId = radioRaridade.getCheckedRadioButtonId();
        RadioButton rb = findViewById(selectedId);
        String raridade = rb != null ? rb.getText().toString() : "";

        if (nome.isEmpty() || latStr.isEmpty() || lngStr.isEmpty()
                || raridade.isEmpty() || imageUri == null) {
            toast("Preencha todos os campos");
            return;
        }

        double lat = Double.parseDouble(latStr);
        double lng = Double.parseDouble(lngStr);

        enviar(nome, raridade, lat, lng);
    }

    private void enviar(String nome, String raridade, double lat, double lng) {

        try {
            Bitmap bitmap = MediaStore.Images.Media
                    .getBitmap(getContentResolver(), imageUri);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);

            RequestBody fileBody = RequestBody.create(
                    baos.toByteArray(),
                    MediaType.parse("image/jpeg")
            );

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("nome", nome)
                    .addFormDataPart("raridade", raridade)
                    .addFormDataPart("lat", String.valueOf(lat))
                    .addFormDataPart("lng", String.valueOf(lng))
                    .addFormDataPart("foto", "img.jpg", fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url(ApiConfig.BASE_URL + "/admin/thiltape")
                    .post(requestBody)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> toast("Erro conexão: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            toast("Cadastrado com sucesso!");
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> toast("Erro do servidor: " + response.code()));
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            toast("Erro ao enviar");
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}