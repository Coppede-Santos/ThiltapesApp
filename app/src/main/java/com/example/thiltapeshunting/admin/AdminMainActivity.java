package com.example.thiltapeshunting.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thiltapeshunting.R;

public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        findViewById(R.id.btnGerenciarJogadores).setOnClickListener(v ->
                startActivity(new Intent(this, GerenciarJogadoresActivity.class)));

        findViewById(R.id.btnCadastrar).setOnClickListener(v ->
                startActivity(new Intent(this, TelaCadastro.class)));

        findViewById(R.id.btnListar).setOnClickListener(v ->
                startActivity(new Intent(this, ListaThiltapesActivity.class)));

        findViewById(R.id.btnRanking).setOnClickListener(v ->
                startActivity(new Intent(this, RankingActivity.class)));
    }
}