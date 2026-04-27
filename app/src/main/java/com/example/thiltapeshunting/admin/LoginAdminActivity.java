package com.example.thiltapeshunting.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thiltapeshunting.R;
import com.google.android.material.textfield.TextInputEditText;

public class LoginAdminActivity extends AppCompatActivity {

    private TextInputEditText etUser, etPass;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login_admin);

        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);

        findViewById(R.id.btnLogin).setOnClickListener(v -> login());
    }

    private void login() {
        String user = etUser.getText().toString();
        String pass = etPass.getText().toString();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Preencha os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        new com.example.thiltapeshunting.network.ApiClient().loginAdmin(user, pass, ok -> {
            if (ok) {
                startActivity(new Intent(this, AdminMainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Login inválido no servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}