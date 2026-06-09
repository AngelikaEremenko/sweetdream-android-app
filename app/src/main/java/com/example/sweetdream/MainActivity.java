package com.example.sweetdream;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.sweetdream.databinding.ActivityMainBinding;
import com.example.sweetdream.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding activityMainBinding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Скрываем обе навигации до проверки роли
        activityMainBinding.bottomNavigationViewUser.setVisibility(View.GONE);
        activityMainBinding.bottomNavigationViewManager.setVisibility(View.GONE);

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginPhoneNumberActivity.class));
            finish();
        } else {
            checkUserRole();
        }
    }

    private void checkUserRole() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            UserModel user = document.toObject(UserModel.class);
                            if (user != null && Boolean.TRUE.equals(user.getManager())) {
                                // Настройка для менеджера
                                setupManagerInterface();
                            } else {
                                // Настройка для обычного пользователя
                                setupUserInterface();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Ошибка загрузки данных пользователя", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupManagerInterface() {
        // Показываем навигацию менеджера
        activityMainBinding.bottomNavigationViewManager.setVisibility(View.VISIBLE);

        // Настройка обработчиков для менеджера
        activityMainBinding.bottomNavigationViewManager.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.cakes) {
                selectedFragment = new HomeManagerFragment();
            } else if (item.getItemId() == R.id.info) {
                selectedFragment = new AuthorFragment();
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
            }
            return true;
        });

        // Загружаем стартовый фрагмент для менеджера
        replaceFragment(new HomeManagerFragment());
    }

    private void setupUserInterface() {
        // Показываем навигацию пользователя
        activityMainBinding.bottomNavigationViewUser.setVisibility(View.VISIBLE);

        // Настройка обработчиков для пользователя
        activityMainBinding.bottomNavigationViewUser.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.cart) {
                replaceFragment(new CartFragment());
            } else {
                replaceFragment(new ProfileFragment());
            }
            return true;
        });

        // Загружаем стартовый фрагмент для пользователя
        replaceFragment(new HomeFragment());
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}