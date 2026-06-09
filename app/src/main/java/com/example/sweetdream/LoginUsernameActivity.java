package com.example.sweetdream;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sweetdream.databinding.ActivityLoginUsernameBinding;
import com.example.sweetdream.model.UserModel;
import com.example.sweetdream.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;


public class LoginUsernameActivity extends AppCompatActivity {

    private ActivityLoginUsernameBinding loginUsernameBinding;
    String phoneNumber;
    UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginUsernameBinding = ActivityLoginUsernameBinding.inflate(getLayoutInflater());
        View view = loginUsernameBinding.getRoot();
        setContentView(view);

        phoneNumber = getIntent().getExtras().getString("phone");
        getUsername();

        loginUsernameBinding.loginLetMeInBtn.setOnClickListener(view1 -> {
            setUsername();
        });
    }

    void setInProgress(boolean inProgress) {
        if (inProgress) {
            loginUsernameBinding.loginLetMeInBtn.setVisibility(View.GONE);
        } else {
            loginUsernameBinding.loginLetMeInBtn.setVisibility(View.VISIBLE);
        }
    }

    void setUsername() {
        String username = loginUsernameBinding.loginUsername.getText().toString();
        if(username.isEmpty() || username.length()<3){
            loginUsernameBinding.loginUsername.setError(("Имя должно состоять минимум из 3 символов"));
            return;
        }
        setInProgress(true);
        if(userModel != null){
            userModel.setUsername((username));
        }else{
            userModel = new UserModel(phoneNumber, username, Timestamp.now(), false, false, "Белгород", false);
        }

        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setInProgress(false);
                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginUsernameActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    void getUsername() {
        setInProgress(true);
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                setInProgress(false);
                if(task.isSuccessful()){
                    userModel = task.getResult().toObject(UserModel.class);
                    if(userModel != null){
                        Intent intent = new Intent(LoginUsernameActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });
    }
}