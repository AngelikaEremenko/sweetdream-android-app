package com.example.sweetdream;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


import com.example.sweetdream.databinding.FragmentProfileBinding;
import com.example.sweetdream.model.UserModel;
import com.example.sweetdream.utils.AndroidUtil;
import com.example.sweetdream.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding fragmentProfileBinding;
    UserModel currentUser;
    SwitchCompat email_news;
    SwitchCompat phone_news;
    Spinner addressSpinner;
    String[] addresses = {"Белгород", "Дубовое"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentProfileBinding = FragmentProfileBinding.inflate(getLayoutInflater(),
                container, false);
        View view = fragmentProfileBinding.getRoot();

        // Инициализация свичей
        email_news = view.findViewById(R.id.switch_email_notify);
        phone_news = view.findViewById(R.id.switch_phone_notify);

        // Загрузка данных пользователя
        getUserData();

        // Обработчики изменения состояния свичей
        email_news.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentUser != null) {
                updateUserNewsPrefs("news_email", isChecked);
            }
        });

        phone_news.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentUser != null) {
                updateUserNewsPrefs("news_phone", isChecked);
            }
        });

        fragmentProfileBinding.settingsBtn.setOnClickListener(v -> {
            Fragment changeProfileFragment = new ChangeProfileFragment();
            FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
            fm.replace(R.id.frame_layout, changeProfileFragment).commit();
        });

        AppCompatButton callUsButton = view.findViewById(R.id.call_us_btn);
        callUsButton.setOnClickListener(v -> {
            String phoneNumber = "tel:+79637705343";
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(phoneNumber));
            startActivity(intent);
        });

        // Настройка Spinner для выбора адреса
        addressSpinner = view.findViewById(R.id.address_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                addresses
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addressSpinner.setAdapter(adapter);

        // Обработчик выбора в Spinner
        addressSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedAddress = addresses[position];
                if (currentUser != null && !selectedAddress.equals(currentUser.getAddress())) {
                    currentUser.setAddress(selectedAddress);
                    updateUserAddress(selectedAddress);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Обработчик для кнопки "Мои заказы"
        fragmentProfileBinding.ordersBtn.setOnClickListener(v -> {
            Fragment ordersFragment = new OrdersFragment(); // Создаем фрагмент заказов
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, ordersFragment);
            transaction.addToBackStack(null); // Добавляем в back stack для возврата
            transaction.commit();
        });

        // Обработчик для кнопки "Выйти"
        fragmentProfileBinding.logoutBtn.setOnClickListener(v -> {
            // Выход из аккаунта
            FirebaseAuth.getInstance().signOut();

            // Переход на экран входа
            Intent intent = new Intent(getContext(), LoginPhoneNumberActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Завершение текущей активности (если нужно)
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        return view;
    }

    void getUserData() {
        fragmentProfileBinding.constraint.setVisibility(View.GONE);

        final int totalTasks = 2; // У нас 2 асинхронные задачи
        final AtomicInteger completedTasks = new AtomicInteger(0);

        Runnable checkCompletion = () -> {
            if (completedTasks.get() == totalTasks) {
                fragmentProfileBinding.constraint.setVisibility(View.VISIBLE);
            }
        };
        // Загрузка фото профиля
        FirebaseUtil.getCurrentProfilePicStorageRef().getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Uri uri = task.getResult();
                        AndroidUtil.setProfilePic(getContext(), uri, fragmentProfileBinding.profilePhoto);
                    }
                    completedTasks.incrementAndGet();
                    checkCompletion.run();
                });

        // Загрузка данных пользователя
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUser = task.getResult().toObject(UserModel.class);
                if (currentUser != null) {
                    fragmentProfileBinding.profileName.setText(currentUser.getUsername());
                    fragmentProfileBinding.profilePhone.setText(currentUser.getPhone());
                    fragmentProfileBinding.profileEmail.setText(currentUser.getEmail());

                    // Установка состояния свичей без срабатывания слушателей
                    email_news.setOnCheckedChangeListener(null); // Временно отключаем слушатель
                    email_news.setChecked(Boolean.TRUE.equals(currentUser.getNews_email()));
                    email_news.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        updateUserNewsPrefs("news_email", isChecked);
                    });

                    phone_news.setOnCheckedChangeListener(null);
                    phone_news.setChecked(Boolean.TRUE.equals(currentUser.getNews_phone()));
                    phone_news.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        updateUserNewsPrefs("news_phone", isChecked);
                    });

                    // Установка сохраненного адреса в Spinner
                    if (currentUser.getAddress() != null) {
                        for (int i = 0; i < addresses.length; i++) {
                            if (addresses[i].equals(currentUser.getAddress())) {
                                addressSpinner.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }
            completedTasks.incrementAndGet();
            checkCompletion.run();
        });
    }

    // Обновление настроек уведомлений в Firestore
    private void updateUserNewsPrefs(String field, boolean value) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(field, value);

        FirebaseUtil.currentUserDetails().update(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AndroidUtil.showToast(getContext(), "Настройки обновлены");
                        // Обновляем локальную модель
                        if (field.equals("news_email")) {
                            currentUser.setNews_email(value);
                        } else if (field.equals("news_phone")) {
                            currentUser.setNews_phone(value);
                        }
                    } else {
                        AndroidUtil.showToast(getContext(), "Ошибка обновления");
                        // Возвращаем предыдущее состояние
                        if (field.equals("news_email")) {
                            email_news.setChecked(!value);
                        } else {
                            phone_news.setChecked(!value);
                        }
                    }
                });
    }

    // Обновление адреса пользователя в Firestore
    private void updateUserAddress(String address) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("address", address);

        FirebaseUtil.currentUserDetails().update(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AndroidUtil.showToast(getContext(), "Адрес обновлен");
                        currentUser.setAddress(address);
                    } else {
                        AndroidUtil.showToast(getContext(), "Ошибка обновления адреса");
                    }
                });
    }
}