package com.example.thiltapeshunting.admin;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thiltapeshunting.R;
import com.example.thiltapeshunting.admin.adapter.PlayerAdminAdapter;
import com.example.thiltapeshunting.model.Player;
import com.example.thiltapeshunting.network.ApiClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class GerenciarJogadoresActivity extends AppCompatActivity implements PlayerAdminAdapter.OnPlayerActionListener {

    private RecyclerView rvPlayers;
    private PlayerAdminAdapter adapter;
    private ApiClient apiClient;
    private FloatingActionButton fabAddPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_jogadores);

        apiClient = new ApiClient();
        rvPlayers = findViewById(R.id.rvPlayers);
        rvPlayers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PlayerAdminAdapter(this);
        rvPlayers.setAdapter(adapter);

        fabAddPlayer = findViewById(R.id.fabAddPlayer);
        fabAddPlayer.setOnClickListener(v -> mostrarDialogoAdicionar());

        carregarJogadores();
    }

    private void carregarJogadores() {
        apiClient.buscarRanking(players -> {
            if (players != null) {
                adapter.submitList(players);
            } else {
                Toast.makeText(this, "Erro ao carregar jogadores", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoAdicionar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Novo Jogador");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Nome do jogador");
        builder.setView(input);

        builder.setPositiveButton("Criar", (dialog, which) -> {
            String nome = input.getText().toString().trim();
            if (!nome.isEmpty()) {
                apiClient.criarPlayer(nome, player -> {
                    if (player != null) {
                        Toast.makeText(this, "Jogador criado com sucesso", Toast.LENGTH_SHORT).show();
                        carregarJogadores();
                    } else {
                        Toast.makeText(this, "Erro ao criar jogador", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onEdit(Player player) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Jogador");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(player.nome);
        builder.setView(input);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String novoNome = input.getText().toString().trim();
            if (!novoNome.isEmpty()) {
                apiClient.atualizarPlayer(player.id, novoNome, ok -> {
                    if (ok) {
                        Toast.makeText(this, "Jogador atualizado", Toast.LENGTH_SHORT).show();
                        carregarJogadores();
                    } else {
                        Toast.makeText(this, "Erro ao atualizar", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onDelete(Player player) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Jogador")
                .setMessage("Deseja realmente excluir " + player.nome + "?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    apiClient.deletarPlayer(player.id, ok -> {
                        if (ok) {
                            Toast.makeText(this, "Jogador excluído", Toast.LENGTH_SHORT).show();
                            carregarJogadores();
                        } else {
                            Toast.makeText(this, "Erro ao excluir", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Não", null)
                .show();
    }
}