package com.example.thiltapeshunting.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thiltapeshunting.R;
import com.example.thiltapeshunting.model.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerAdminAdapter extends RecyclerView.Adapter<PlayerAdminAdapter.ViewHolder> {

    private List<Player> lista = new ArrayList<>();
    private final OnPlayerActionListener listener;

    public interface OnPlayerActionListener {
        void onEdit(Player player);
        void onDelete(Player player);
    }

    public PlayerAdminAdapter(OnPlayerActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Player> novaLista) {
        this.lista = novaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player_admin, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player p = lista.get(position);
        holder.nome.setText(p.nome);
        holder.capturas.setText("Capturas: " + p.capturas);
        
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(p));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(p));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nome, capturas;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.tvNome);
            capturas = itemView.findViewById(R.id.tvCapturas);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}