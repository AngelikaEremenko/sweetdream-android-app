package com.example.sweetdream;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sweetdream.databinding.FragmentChangeProfileBinding;
import com.example.sweetdream.model.UserModel;
import com.example.sweetdream.utils.AndroidUtil;
import com.example.sweetdream.utils.FirebaseUtil;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.concurrent.atomic.AtomicInteger;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ChangeProfileFragment extends Fragment {
    private FragmentChangeProfileBinding fragmentChangeProfileBinding;

    UserModel currentUser;
    ActivityResultLauncher<Intent> imagePickLauncher;
    Uri selectedImageUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data =  result.getData();
                        if(data!=null && data.getData()!=null){
                            selectedImageUri = data.getData();
                            AndroidUtil.setProfilePic(getContext(), selectedImageUri, fragmentChangeProfileBinding.profilePhoto);
                        }
                    }
                }
                );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment,
        fragmentChangeProfileBinding = FragmentChangeProfileBinding.inflate(getLayoutInflater(),
                container, false);
        View view = fragmentChangeProfileBinding.getRoot();

        getUserData();

        fragmentChangeProfileBinding.changeProfileBtn.setOnClickListener(v -> {
            updateBtnClick();



            updateToFirestore();
        });

        fragmentChangeProfileBinding.profilePhoto.setOnClickListener((v)->{
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512)
                    .createIntent(new Function1<Intent, Unit>() {
                        @Override
                        public Unit invoke(Intent intent) {
                            imagePickLauncher.launch(intent);
                            return null;
                        }
                    });

        });

        return view;
    }

    void updateBtnClick(){
        String newUsername = fragmentChangeProfileBinding.editUsername.getText().toString();
        if(newUsername.isEmpty() || newUsername.length()<3){
            fragmentChangeProfileBinding.editUsername.setError(("Имя должно состоять минимум из 3 символов"));
            return;
        }
        currentUser.setUsername(newUsername);

        if(selectedImageUri!=null) {
            FirebaseUtil.getCurrentProfilePicStorageRef().putFile(selectedImageUri)
                    .addOnCompleteListener(task -> {

                    });
        }else{
            updateToFirestore();
        }

        String newEmail = fragmentChangeProfileBinding.editEmail.getText().toString();
        if(newUsername.isEmpty()){
            fragmentChangeProfileBinding.editEmail.setError(("Email должно состоять минимум из 3 символов"));
            return;
        }
        currentUser.setEmail(newEmail);
    }

    void updateToFirestore(){
        FirebaseUtil.currentUserDetails().set(currentUser)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        AndroidUtil.showToast(getContext(), "Данные обновлены");
                    }else{
                        AndroidUtil.showToast(getContext(), "Ошибка!");
                    }
                });
    }

    void getUserData(){
        fragmentChangeProfileBinding.layout.setVisibility(View.GONE);
        final int totalTasks = 2; // У нас 2 асинхронные задачи
        final AtomicInteger completedTasks = new AtomicInteger(0);

        Runnable checkCompletion = () -> {
            if (completedTasks.get() == totalTasks) {
                fragmentChangeProfileBinding.layout.setVisibility(View.VISIBLE);
            }
        };
        FirebaseUtil.getCurrentProfilePicStorageRef().getDownloadUrl()
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                Uri uri = task.getResult();
                                AndroidUtil.setProfilePic(getContext(), uri, fragmentChangeProfileBinding.profilePhoto);
                            }
                            completedTasks.incrementAndGet();
                            checkCompletion.run();
                        });
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                currentUser = task.getResult().toObject(UserModel.class);
                fragmentChangeProfileBinding.editUsername.setText(currentUser.getUsername());
                fragmentChangeProfileBinding.editEmail.setText(currentUser.getEmail());

                completedTasks.incrementAndGet();
                checkCompletion.run();
            }

        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentChangeProfileBinding = null;
    }

}

