package com.example.thiltapeshunting.admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thiltapeshunting.admin.adapter.RankingAdapter;
import com.example.thiltapeshunting.R;
import com.example.thiltapeshunting.network.ApiClient;

public class RankingActivity extends AppCompatActivity {

    private RecyclerView rv;
    private RankingAdapter adapter = new RankingAdapter();
    private ApiClient api = new ApiClient();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_ranking);

        rv = findViewById(R.id.rvRanking);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        api.buscarRanking(adapter::submitList);
    }
}