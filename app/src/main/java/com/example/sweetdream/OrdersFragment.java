package com.example.sweetdream;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sweetdream.adapter.OrdersAdapter;
import com.example.sweetdream.databinding.FragmentOrdersBinding;
import com.example.sweetdream.model.OrderModel;
import com.example.sweetdream.model.UserModel;
import com.example.sweetdream.utils.FirebaseUtil;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {
    FirebaseFirestore db;
    private FragmentOrdersBinding binding;
    private OrdersAdapter adapter;
    private List<OrderModel> ordersList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Настройка RecyclerView
        binding.ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrdersAdapter(ordersList);
        binding.ordersRecyclerView.setAdapter(adapter);

        // Загрузка заказов пользователя
        loadUserOrders();
    }

    private void loadUserOrders() {

        FirebaseUtil.currentUserDetails().get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        UserModel user = task.getResult().toObject(UserModel.class);
                        if (user != null && user.getOrders() != null && !user.getOrders().isEmpty()) {
                            ordersList.clear();
                            ordersList.addAll(user.getOrders());
                            adapter.notifyDataSetChanged();
                            binding.emptyOrdersText.setVisibility(View.GONE);
                        } else {
                            binding.emptyOrdersText.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e("OrdersFragment", "Error loading user orders", task.getException());
                        binding.emptyOrdersText.setVisibility(View.VISIBLE);
                    }
                });
    }
}