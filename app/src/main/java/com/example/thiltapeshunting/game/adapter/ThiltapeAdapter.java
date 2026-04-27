package com.example.thiltapeshunting.game.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thiltapeshunting.R;
import com.example.thiltapeshunting.model.Thiltape;

import java.util.ArrayList;
import java.util.List;

public class ThiltapeAdapter extends RecyclerView.Adapter<ThiltapeAdapter.ThiltapeViewHolder> {

    private final List<Thiltape> itens = new ArrayList<>();

    public void submitList(List<Thiltape> novaLista) {
        itens.clear();
        itens.addAll(novaLista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ThiltapeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thiltape, parent, false);
        return new ThiltapeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThiltapeViewHolder holder, int position) {
        Thiltape item = itens.get(position);
        holder.tvNome.setText(item.getNome());
        holder.tvRaridade.setText(holder.itemView.getContext().getString(R.string.raridade_label, item.getRaridade()));
        holder.tvFotoUrl.setText(item.getFoto());
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class ThiltapeViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNome;
        final TextView tvRaridade;
        final TextView tvFotoUrl;

        ThiltapeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvRaridade = itemView.findViewById(R.id.tvRaridade);
            tvFotoUrl = itemView.findViewById(R.id.tvFotoUrl);
        }
    }
}
