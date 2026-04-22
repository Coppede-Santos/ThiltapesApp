package com.example.thiltapeshunting.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thiltapeshunting.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.*;

public class TelaCadastro extends AppCompatActivity {

    private EditText txtNome, txtLat, txtLng;
    private RadioGroup radioRaridade;
    private ImageView imgThiltape;

    private Uri imageUri;

    private static final int PICK_IMAGE = 1;
    private static final String BASE_URL = "http://10.0.2.2:8080/thiltapes-api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_cadastro);

        txtNome = findViewById(R.id.txt_nome);
        txtLat = findViewById(R.id.txt_lat);
        txtLng = findViewById(R.id.txt_lng);
        radioRaridade = findViewById(R.id.radio_raridade);
        imgThiltape = findViewById(R.id.img_thiltape);

        findViewById(R.id.btn_enviar).setOnClickListener(v -> cadastrar());
        imgThiltape.setOnClickListener(v -> abrirGaleria());
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
                    .url(BASE_URL + "/admin/thiltape")
                    .post(requestBody)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> toast("Erro conexão"));
                }

                @Override
                public void onResponse(Call call, Response response) {
                    runOnUiThread(() -> {
                        toast("Cadastrado com sucesso!");
                        finish();
                    });
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