package com.example.thiltapeshunting.game;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thiltapeshunting.R;
import com.example.thiltapeshunting.network.ApiClient;
import com.example.thiltapeshunting.session.SessionManager;
import com.example.thiltapeshunting.game.adapter.ThiltapeAdapter;

public class PokedexActivity extends AppCompatActivity {

    public static final String EXTRA_PLAYER_ID = "extra_player_id";

    private final ApiClient apiClient = new ApiClient();
    private final ThiltapeAdapter adapter = new ThiltapeAdapter();
    private int playerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokedex);

        RecyclerView rv = findViewById(R.id.rvPokedex);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        playerId = getIntent().getIntExtra(EXTRA_PLAYER_ID, -1);
        if (playerId <= 0) {
            playerId = new SessionManager(this).getPlayerId();
        }

        carregarPokedex();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarPokedex();
    }

    private void carregarPokedex() {
        if (playerId > 0) {
            apiClient.listarPokedex(playerId, adapter::submitList);
        }
    }
}
