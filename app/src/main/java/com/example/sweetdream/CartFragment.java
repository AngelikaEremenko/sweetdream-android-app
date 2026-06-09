package com.example.sweetdream;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sweetdream.adapter.CartAdapter;
import com.example.sweetdream.databinding.FragmentCartBinding;
import com.example.sweetdream.model.CartModel;
import com.example.sweetdream.model.OrderModel;
import com.example.sweetdream.model.UserModel;
import com.example.sweetdream.utils.AndroidUtil;
import com.example.sweetdream.utils.OrderSuccessFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CartFragment extends Fragment {
    private FragmentCartBinding fragmentCartBinding;

    FirebaseFirestore db;
    FirebaseAuth auth;

    CartAdapter adapter;
    List<CartModel> list;
    int total = 0;
    private TextView deleteAll;
    private ConstraintLayout emptyCartView;
    private ConstraintLayout cartContentView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentCartBinding = FragmentCartBinding.inflate(getLayoutInflater(),
                container, false);
        View view = fragmentCartBinding.getRoot();

        db = FirebaseFirestore.getInstance();

        fragmentCartBinding.constraint2.setVisibility(View.GONE);
        fragmentCartBinding.progressBar.setVisibility(View.VISIBLE);

        auth = FirebaseAuth.getInstance();
        fragmentCartBinding.cartRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));

        list = new ArrayList<>();
        adapter = new CartAdapter(getActivity(), list);
        fragmentCartBinding.cartRecycleView.setAdapter(adapter);

        emptyCartView = view.findViewById(R.id.constraint1);
        cartContentView = view.findViewById(R.id.constraint2);

        deleteAll = view.findViewById(R.id.deleteAll);
        // Обработчик клика на "Удалить все"
        deleteAll.setOnClickListener(v -> clearCart());

        db.collection("AddToCart").document(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                        .collection("CurrentUser").get().addOnCompleteListener(
                        new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    for(DocumentSnapshot document : task.getResult().getDocuments()){
                                        CartModel cartModel = document.toObject(CartModel.class);
                                        total += Integer.parseInt(cartModel.getPrice());
                                        list.add(cartModel);
                                        adapter.notifyDataSetChanged();
                                    }
                                    fragmentCartBinding.total.setText(String.valueOf(total));
                                    fragmentCartBinding.progressBar.setVisibility(View.GONE);
                                    fragmentCartBinding.constraint2.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                );

        fragmentCartBinding.goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment homeFragment = new HomeFragment();
                FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
                fm.replace(R.id.frame_layout, homeFragment).commit();
            }
        });

        fragmentCartBinding.order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (list.isEmpty()) {
                    AndroidUtil.showToast(getContext(), "Корзина пуста");
                    return;
                }

                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser == null) {
                    AndroidUtil.showToast(getContext(), "Пользователь не авторизован");
                    return;
                }

                // Создаем новый заказ
                OrderModel newOrder = new OrderModel();
                newOrder.setOrderId(db.collection("Orders").document().getId()); // Генерируем ID
                newOrder.setItems(new ArrayList<>(list)); // Копируем список товаров
                newOrder.setTotal(total);
                newOrder.setTimestamp(String.valueOf(System.currentTimeMillis()));
                newOrder.setStatus("в процессе обработки");

                // 1. Добавляем заказ в коллекцию Orders
                db.collection("Orders").document(newOrder.getOrderId())
                        .set(newOrder)
                        .addOnSuccessListener(aVoid -> {
                            // 2. Обновляем пользователя, добавляя ссылку на заказ
                            db.collection("users").document(currentUser.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        UserModel user = documentSnapshot.toObject(UserModel.class);
                                        if (user != null) {
                                            user.addOrder(newOrder);

                                            // 3. Сохраняем обновленного пользователя
                                            db.collection("users").document(currentUser.getUid())
                                                    .update("orders", user.getOrders())
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        // Очищаем корзину БЕЗ диалогового окна
                                                        silentClearCart();
                                                        AndroidUtil.showToast(getContext(), "Заказ оформлен!");

                                                        // Переход на экран успешного заказа
                                                        Fragment successFragment = new OrderSuccessFragment();
                                                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                                                        transaction.replace(R.id.frame_layout, successFragment);
                                                        transaction.addToBackStack(null);
                                                        transaction.commit();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        AndroidUtil.showToast(getContext(), "Ошибка обновления пользователя");
                                                        Log.e("CartFragment", "Error updating user", e);
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        AndroidUtil.showToast(getContext(), "Ошибка получения данных пользователя");
                                        Log.e("CartFragment", "Error getting user", e);
                                    });
                        })
                        .addOnFailureListener(e -> {
                            AndroidUtil.showToast(getContext(), "Ошибка создания заказа");
                            Log.e("CartFragment", "Error creating order", e);
                        });
            }
        });

        return view;
    }

    // Добавляем новый метод для "тихой" очистки корзины без диалога
    private void silentClearCart() {
        // Очищаем данные
        list.clear();
        adapter.notifyDataSetChanged();

        // Обновляем UI
        updateCartVisibility();

        // Очищаем корзину в Firebase
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            db.collection("AddToCart")
                    .document(currentUser.getUid())
                    .collection("CurrentUser")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                document.getReference().delete();
                            }
                            total = 0;
                            fragmentCartBinding.total.setText("0");
                        }
                    });
        }
    }

    private void clearCart() {
        // 1. Показать диалог подтверждения
        new AlertDialog.Builder(getContext())
                .setTitle("Очистить корзину")
                .setMessage("Вы уверены, что хотите удалить все товары из корзины?")
                .setPositiveButton("Да", (dialog, which) -> {
                    // 2. Очищаем данные
                    list.clear();
                    adapter.notifyDataSetChanged();

                    // 3. Обновляем UI
                    updateCartVisibility();

                    // 4. Здесь можно добавить очистку в Firebase/BASE если нужно
                    clearCartFromDatabase();
                })
                .setNegativeButton("Нет", null)
                .show();
    }
    private void updateCartVisibility() {
        if (list.isEmpty()) {
            emptyCartView.setVisibility(View.VISIBLE);
            cartContentView.setVisibility(View.GONE);
        } else {
            emptyCartView.setVisibility(View.GONE);
            cartContentView.setVisibility(View.VISIBLE);
        }
    }
    private void clearCartFromDatabase() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Получаем ссылку на коллекцию товаров в корзине пользователя
            db.collection("AddToCart")
                    .document(currentUser.getUid())
                    .collection("CurrentUser")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Удаляем каждый документ в коллекции
                            for (DocumentSnapshot document : task.getResult()) {
                                document.getReference().delete();
                            }

                            // Обнуляем общую сумму
                            total = 0;
                            fragmentCartBinding.total.setText("0");

                            // Показываем уведомление об успешном удалении
                            if (getContext() != null) {
                                AndroidUtil.showToast(getContext(), "Корзина очищена");
                            }
                        } else {
                            if (getContext() != null) {
                                AndroidUtil.showToast(getContext(), "Ошибка при очистке корзины: " +
                                        task.getException().getMessage());
                            }
                            Log.e("CartFragment", "Error clearing cart", task.getException());
                        }
                    });
        }
    }
}