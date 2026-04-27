package com.example.thiltapeshunting.game;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thiltapeshunting.R;
import com.example.thiltapeshunting.network.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.thiltapeshunting.session.SessionManager;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etNome;
    private MaterialButton btnEntrar, btnAdmin;
    private ApiClient apiClient;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiClient = new ApiClient();
        sessionManager = new SessionManager(this);

        etNome = findViewById(R.id.etNome);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnAdmin = findViewById(R.id.btnAdmin);

        // 👉 BOTÃO ADMIN (NOVO)
        btnAdmin.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.thiltapeshunting.admin.LoginAdminActivity.class));
        });

        // 👉 SE JÁ TEM SESSÃO
        if (sessionManager.hasActiveSession()) {
            String playerName = sessionManager.getPlayerName();
            etNome.setText(playerName);

            btnEntrar.setText(getString(R.string.btn_continuar_jogo));
            btnEntrar.setOnClickListener(v ->
                    abrirMapa(sessionManager.getPlayerId(), playerName)
            );
            return;
        }

        // 👉 CRIAR PLAYER
        btnEntrar.setOnClickListener(v -> criarPlayerEEntrar());
    }

    private void criarPlayerEEntrar() {
        String nome = etNome.getText() == null ? "" : etNome.getText().toString().trim();

        if (TextUtils.isEmpty(nome)) {
            Toast.makeText(this, R.string.erro_nome_obrigatorio, Toast.LENGTH_SHORT).show();
            return;
        }

        btnEntrar.setEnabled(false);

        apiClient.criarPlayer(nome, player -> {
            btnEntrar.setEnabled(true);

            if (player == null) {
                Toast.makeText(this, R.string.erro_criar_player, Toast.LENGTH_LONG).show();
                return;
            }

            sessionManager.savePlayer(player.getId(), player.getNome());
            abrirMapa(player.getId(), player.getNome());
        });
    }

    private void abrirMapa(int playerId, String playerName) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(MapActivity.EXTRA_PLAYER_ID, playerId);
        intent.putExtra(MapActivity.EXTRA_PLAYER_NAME, playerName);
        startActivity(intent);
        finish();
    }
}