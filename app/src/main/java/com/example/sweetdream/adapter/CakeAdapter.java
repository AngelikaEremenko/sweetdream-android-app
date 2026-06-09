package com.example.sweetdream.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sweetdream.DetailedActivity;
import com.example.sweetdream.R;
import com.example.sweetdream.model.CakeModel;

import java.util.List;

public class CakeAdapter extends RecyclerView.Adapter<CakeAdapter.ViewHolder> {

    private Context context;
    private List<CakeModel> list;

    public CakeAdapter(Context context, List<CakeModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cake_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Glide.with(context).load(list.get(position).getImg_url()).into(holder.cakeImg);
        holder.name.setText(list.get(position).getName());
        holder.price.setText(list.get(position).getPrice());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailedActivity.class);
                intent.putExtra("detail", list.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cakeImg;
        TextView name, price;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cakeImg = itemView.findViewById(R.id.cake_img);
            name = itemView.findViewById(R.id.cake_name);
            price = itemView.findViewById(R.id.cake_price);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(List<CakeModel> filteredList) {
        this.list = filteredList;
        notifyDataSetChanged();
    }
}
