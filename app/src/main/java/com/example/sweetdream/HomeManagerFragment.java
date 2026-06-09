package com.example.sweetdream;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sweetdream.adapter.CakeManagerAdapter;
import com.example.sweetdream.databinding.FragmentHomeManagerBinding;
import com.example.sweetdream.model.CakeModel;
import com.example.sweetdream.utils.AndroidUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class HomeManagerFragment extends Fragment {
    private FragmentHomeManagerBinding fragmentHomeManagerBinding;

    FirebaseFirestore db;
    List<CakeModel> list;
    CakeManagerAdapter adapter;
    RecyclerView cakes;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentHomeManagerBinding = fragmentHomeManagerBinding.inflate(inflater, container, false);
        View view = fragmentHomeManagerBinding.getRoot();

        db = FirebaseFirestore.getInstance();

        fragmentHomeManagerBinding.progressBar.setVisibility(View.VISIBLE);
        fragmentHomeManagerBinding.cakes.setVisibility(View.GONE);

        cakes = fragmentHomeManagerBinding.cakes;
        cakes.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        list = new ArrayList<>();
        adapter = new CakeManagerAdapter(getActivity(), list);
        cakes.setAdapter(adapter);

        db.collection("cakes")
                .get()
                .addOnCompleteListener(task -> {
                    if (getActivity() == null) return;

                    if (task.isSuccessful()) {
                        list.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            CakeModel cakeModel = document.toObject(CakeModel.class);
                            cakeModel.setId(document.getId());  // Устанавливаем ID документа
                            list.add(cakeModel);
                        }
                        adapter.notifyDataSetChanged();
                        fragmentHomeManagerBinding.progressBar.setVisibility(View.GONE);
                        fragmentHomeManagerBinding.cakes.setVisibility(View.VISIBLE);
                    } else {
                        Log.e("FirestoreError", "Error loading data", task.getException());
                        AndroidUtil.showToast(getActivity(), "Ошибка: " + task.getException().getMessage());
                    }
                });

        fragmentHomeManagerBinding.addBtn.setOnClickListener(v -> {
            AddCakeFragment addCakeFragment = new AddCakeFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, addCakeFragment) // R.id.fragment_container - ваш контейнер для фрагментов
                    .addToBackStack(null) // Добавляем в стек возврата
                    .commit();
        });

        return view;
    }
}