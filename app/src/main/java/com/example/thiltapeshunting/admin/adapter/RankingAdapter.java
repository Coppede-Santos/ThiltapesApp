package com.example.thiltapeshunting.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.thiltapeshunting.R;
import com.example.thiltapeshunting.model.Player;

import java.util.ArrayList;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {

    private List<Player> lista = new ArrayList<>();

    public void submitList(List<Player> novaLista) {
        lista = novaLista;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranking, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Player p = lista.get(position);
        holder.nome.setText(p.nome);
        holder.capturas.setText("Capturas: " + p.capturas);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nome, capturas;

        ViewHolder(View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.tvNome);
            capturas = itemView.findViewById(R.id.tvCapturas);
        }
    }
}