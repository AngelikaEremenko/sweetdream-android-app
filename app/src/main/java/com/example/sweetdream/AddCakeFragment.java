package com.example.sweetdream;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sweetdream.databinding.FragmentAddCakeBinding;
import com.example.sweetdream.model.CakeModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AddCakeFragment extends Fragment {
    private FragmentAddCakeBinding binding;
    private FirebaseFirestore db;
    private Uri imageUri;
    private StorageReference storageRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddCakeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("cake_images");

        // Обработчик для загрузки изображения
        binding.profilePhoto.setOnClickListener(v -> openImageChooser());

        // Обработчик для кнопки сохранения
        binding.changeProfileBtn.setOnClickListener(v -> saveCakeToDatabase());

        return binding.getRoot();
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            binding.profilePhoto.setImageURI(imageUri);
        }
    }

    private void saveCakeToDatabase() {
        String id = binding.id.getText().toString().trim();
        String title = binding.title.getText().toString().trim();
        String price = binding.price.getText().toString().trim();

        if (id.isEmpty() || title.isEmpty() || price.isEmpty()) {
            Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            // Загружаем изображение в Storage
            StorageReference fileRef = storageRef.child(id + ".jpg");
            fileRef.putFile(imageUri)
                    .continueWithTask(task -> fileRef.getDownloadUrl())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String imageUrl = task.getResult().toString();
                            saveCakeData(id, title, price, imageUrl);
                        } else {
                            Toast.makeText(getContext(), "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            saveCakeData(id, title, price, "");
        }
    }

    private void saveCakeData(String id, String title, String price, String imageUrl) {
        CakeModel cake = new CakeModel(imageUrl, title, price, id);
        cake.setId(id);

        db.collection("cakes").document(id)
                .set(cake)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Торт успешно добавлен", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack(); // Возвращаемся назад
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}