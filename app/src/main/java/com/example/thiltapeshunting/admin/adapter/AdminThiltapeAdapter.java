package com.example.thiltapeshunting.admin.adapter;

import android.view.*;
import android.widget.*;

import androidx.recyclerview.widget.RecyclerView;

import com.example.thiltapeshunting.R;
import com.example.thiltapeshunting.model.Thiltape;
import com.example.thiltapeshunting.network.ApiClient;

import java.util.*;

public class AdminThiltapeAdapter extends RecyclerView.Adapter<AdminThiltapeAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Thiltape thiltape);
    }

    private final List<Thiltape> lista = new ArrayList<>();
    private final ApiClient api = new ApiClient();
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Thiltape> nova) {
        lista.clear();
        lista.addAll(nova);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_thiltape_admin, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {
        Thiltape t = lista.get(position);

        h.nome.setText(t.getNome());
        h.raridade.setText(t.getRaridade());

        h.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(t);
            }
        });

        h.btnDelete.setOnClickListener(v -> {
            api.deletarThiltape(t.getId(), ok -> {
                if (ok) {
                    lista.remove(position);
                    notifyDataSetChanged();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nome, raridade;
        Button btnDelete;

        ViewHolder(View v) {
            super(v);
            nome = v.findViewById(R.id.tvNome);
            raridade = v.findViewById(R.id.tvRaridade);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}