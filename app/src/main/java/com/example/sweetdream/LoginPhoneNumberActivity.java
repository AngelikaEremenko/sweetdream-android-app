package com.example.sweetdream;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import com.example.sweetdream.databinding.ActivityLoginPhoneNumberBinding;

public class LoginPhoneNumberActivity extends AppCompatActivity {

    private ActivityLoginPhoneNumberBinding loginPhoneNumberBinding;

    EditText phoneInput;
    Button sendOtpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginPhoneNumberBinding = ActivityLoginPhoneNumberBinding.inflate(getLayoutInflater());
        View view = loginPhoneNumberBinding.getRoot();
        setContentView(view);


        loginPhoneNumberBinding.sendOtpNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loginPhoneNumberBinding.loginMobileNumber.getText() != null) {
                    Intent intent = new Intent(LoginPhoneNumberActivity.this, LoginOtpActivity.class);
                    intent.putExtra("phone", loginPhoneNumberBinding.loginMobileNumber.getText().toString());
                    startActivity(intent);
                }
            }
        });
    }
}