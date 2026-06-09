package com.example.sweetdream.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sweetdream.R;
import com.example.sweetdream.model.CakeModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CakeManagerAdapter extends RecyclerView.Adapter<CakeManagerAdapter.ViewHolder> {

    private Context context;
    private List<CakeModel> list;
    private FirebaseFirestore db;

    public CakeManagerAdapter(Context context, List<CakeModel> list) {
        this.context = context;
        this.list = list;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cake_list_item_manager, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Glide.with(context).load(list.get(position).getImg_url()).into(holder.cakeImg);
        holder.name.setText(list.get(position).getName());
        holder.price.setText(list.get(position).getPrice());

        holder.deleteBtn.setOnClickListener(v -> {
            // Удаление из Firestore
            db.collection("cakes").document(list.get(position).getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Удаление из локального списка
                        list.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());
                        Toast.makeText(context, "Торт удален", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cakeImg;
        TextView name, price;
        AppCompatButton deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cakeImg = itemView.findViewById(R.id.cake_img);
            name = itemView.findViewById(R.id.cake_name);
            price = itemView.findViewById(R.id.cake_price);
            deleteBtn = itemView.findViewById(R.id.cake_deleteBtn);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(List<CakeModel> filteredList) {
        this.list = filteredList;
        notifyDataSetChanged();
    }
}

