package com.example.thiltapeshunting.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thiltapeshunting.admin.adapter.AdminThiltapeAdapter;
import com.example.thiltapeshunting.R;
import com.example.thiltapeshunting.network.ApiClient;

public class ListaThiltapesActivity extends AppCompatActivity {

    private RecyclerView rv;
    private AdminThiltapeAdapter adapter = new AdminThiltapeAdapter();
    private ApiClient api = new ApiClient();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_lista_thiltapes);

        rv = findViewById(R.id.rvLista);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        adapter.setOnItemClickListener(thiltape -> {
            Toast.makeText(this, "ID: " + thiltape.getId(), Toast.LENGTH_SHORT).show();
            // Aqui depois você pode abrir tela de edição
        });

        carregar();
    }

    private void carregar() {
        api.buscarTodosAdmin(adapter::submitList);
    }
}