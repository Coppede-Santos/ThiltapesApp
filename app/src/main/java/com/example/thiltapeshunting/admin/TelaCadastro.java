package com.example.thiltapeshunting.admin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.thiltapeshunting.R;
import com.example.thiltapeshunting.network.ApiClient;
import com.example.thiltapeshunting.network.ApiConfig;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class TelaCadastro extends AppCompatActivity {

    private EditText txtNome, txtLat, txtLng;
    private RadioGroup radioRaridade;
    private ImageView imgThiltape;

    private Uri imageUri;
    private String currentPhotoPath;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int PERMISSION_LOCATION = 100;
    private static final int PERMISSION_CAMERA = 101;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    imgThiltape.setImageURI(imageUri);
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    imgThiltape.setImageURI(imageUri);
                }
            });

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
        imgThiltape.setOnClickListener(v -> selecionarImagem());
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

                double randomLat = currentLat + (Math.random() - 0.5) * 2 * 0.009;
                double randomLng = currentLng + (Math.random() - 0.5) * 2 * (0.009 / Math.cos(Math.toRadians(currentLat)));

                txtLat.setText(String.format(Locale.US, "%.6f", randomLat));
                txtLng.setText(String.format(Locale.US, "%.6f", randomLng));
            } else {
                toast("Não foi possível obter a localização atual");
            }
        });
    }

    private void selecionarImagem() {
        String[] options = {"Câmera", "Galeria"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Escolher imagem");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                verificarPermissaoCamera();
            } else {
                abrirGaleria();
            }
        });
        builder.show();
    }

    private void verificarPermissaoCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
        } else {
            abrirCamera();
        }
    }

    private void abrirCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            toast("Erro ao criar arquivo: " + ex.getMessage());
            return;
        }

        if (photoFile != null) {
            String authority = getPackageName() + ".fileprovider";
            imageUri = FileProvider.getUriForFile(this, authority, photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            try {
                cameraLauncher.launch(takePictureIntent);
            } catch (Exception e) {
                toast("Erro ao abrir câmera: " + e.getMessage());
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamera();
            } else {
                toast("Permissão de câmera negada");
            }
        }
    }

    private void cadastrar() {
        String nome = txtNome.getText().toString().trim();
        String latStr = txtLat.getText().toString().trim();
        String lngStr = txtLng.getText().toString().trim();

        int selectedId = radioRaridade.getCheckedRadioButtonId();
        RadioButton rb = findViewById(selectedId);
        String raridade = rb != null ? rb.getText().toString() : "";

        if (nome.isEmpty() || latStr.isEmpty() || lngStr.isEmpty() || raridade.isEmpty() || imageUri == null) {
            toast("Preencha todos os campos e selecione uma imagem");
            return;
        }

        double lat = Double.parseDouble(latStr);
        double lng = Double.parseDouble(lngStr);

        enviar(nome, raridade, lat, lng);
    }

    private void enviar(String nome, String raridade, double lat, double lng) {
        try {
            Bitmap bitmap;
            if (currentPhotoPath != null && new File(currentPhotoPath).exists()) {
                bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);

            RequestBody fileBody = RequestBody.create(baos.toByteArray(), MediaType.parse("image/jpeg"));

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

            ApiClient.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> toast("Erro conexão: " + e.getMessage()));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            toast("Cadastrado com sucesso!");
                            finish();
                        });
                    } else {
                        try (ResponseBody responseBody = response.body()) {
                            String errorBody = responseBody != null ? responseBody.string() : "Sem detalhes";
                            runOnUiThread(() -> toast("Erro do servidor (" + response.code() + "): " + errorBody));
                        }
                    }
                }
            });

        } catch (Exception e) {
            toast("Erro ao processar imagem: " + e.getMessage());
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
