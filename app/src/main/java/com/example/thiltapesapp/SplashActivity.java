package com.example.thiltapesapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thiltapeshunting.admin.LoginAdminActivity;
import com.example.thiltapeshunting.session.SessionManager;
import com.example.thiltapeshunting.game.MainActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);

        if (session.hasActiveSession()) {
            // Já tem jogador → vai pro jogo
            startActivity(new Intent(this, com.example.thiltapeshunting.game.MainActivity.class));
        } else {
            // Não tem → vai pra tela de login/jogador
            startActivity(new Intent(this, LoginAdminActivity.class));
        }

        finish();
    }
}