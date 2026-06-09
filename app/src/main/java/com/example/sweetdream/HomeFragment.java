package com.example.sweetdream;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.sweetdream.adapter.CakeAdapter;
import com.example.sweetdream.databinding.FragmentHomeBinding;
import com.example.sweetdream.model.CakeModel;
import com.example.sweetdream.utils.AndroidUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    private FragmentHomeBinding fragmentHomeBinding;

    FirebaseFirestore db;
    List<CakeModel> list;
    CakeAdapter adapter;
    RecyclerView cakes;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = fragmentHomeBinding.getRoot();

        db = FirebaseFirestore.getInstance();

        fragmentHomeBinding.progressBar.setVisibility(View.VISIBLE);
        fragmentHomeBinding.scrollView.setVisibility(View.GONE);

        cakes = fragmentHomeBinding.cakes;
        cakes.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        list = new ArrayList<>();
        adapter = new CakeAdapter(getActivity(), list);
        cakes.setAdapter(adapter);

        // Инициализация поиска
        EditText searchBox = view.findViewById(R.id.search_box);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        db.collection("cakes")
                .get()
                .addOnCompleteListener(task -> {
                    if (getActivity() == null) return; // Проверка на null

                    if (task.isSuccessful()) {
                        list.clear(); // Очистка списка перед добавлением новых данных
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            CakeModel cakeModel = document.toObject(CakeModel.class);
                            list.add(cakeModel);
                        }
                        adapter.notifyDataSetChanged();
                        fragmentHomeBinding.progressBar.setVisibility(View.GONE);
                        fragmentHomeBinding.scrollView.setVisibility(View.VISIBLE);
                    } else {
                        Log.e("FirestoreError", "Error loading data", task.getException());
                        AndroidUtil.showToast(getActivity(), "Ошибка: " + task.getException().getMessage());
                    }
                });

        return view;
    }

    private void filter(String text) {
        List<CakeModel> filteredList = new ArrayList<>();
        for (CakeModel cake : list) {
            // Проверяем, содержит ли название или описание торта введенный текст
            if (cake.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(cake);
            }
        }
        adapter.filterList(filteredList);
    }
}