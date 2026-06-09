package com.example.sweetdream;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.sweetdream.databinding.ActivityDetailedBinding;
import com.example.sweetdream.databinding.ActivityMainBinding;
import com.example.sweetdream.model.CakeModel;
import com.example.sweetdream.utils.AndroidUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class DetailedActivity extends AppCompatActivity {
    ActivityDetailedBinding activityDetailedBinding;
    AppCompatButton close_btn;
    CakeModel cakeModel = null;
    FirebaseFirestore db;
    FirebaseAuth auth;
    String weight = "500 г";
    int price = 950;
    int amount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDetailedBinding = ActivityDetailedBinding.inflate(getLayoutInflater());
        View view = activityDetailedBinding.getRoot();
        setContentView(view);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        close_btn = view.findViewById(R.id.close_btn);
        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final Object object = getIntent().getSerializableExtra("detail");
        if(object instanceof CakeModel){
            cakeModel = (CakeModel) object;
        }

        if(cakeModel != null){
            Glide.with(getApplicationContext()).load(cakeModel.getImg_url()).into(activityDetailedBinding.detailedImg);
            activityDetailedBinding.detailedName.setText(cakeModel.getName());
            activityDetailedBinding.detailedPrice.setText(cakeModel.getPrice());
        }

        activityDetailedBinding.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(amount < 9){
                    amount++;
                    activityDetailedBinding.amount.setText(String.valueOf(amount));
                    price = Integer.parseInt(cakeModel.getPrice())*amount;
                    activityDetailedBinding.detailedPrice.setText(String.valueOf(price));
                }
            }
        });
        activityDetailedBinding.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(amount > 1){
                    amount--;
                    activityDetailedBinding.amount.setText(String.valueOf(amount));
                    price = Integer.parseInt(cakeModel.getPrice())*amount;
                    activityDetailedBinding.detailedPrice.setText(String.valueOf(price));
                }

            }
        });

        activityDetailedBinding.toggleButtonGroup.addOnButtonCheckedListener(
                new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    if (checkedId == R.id.btnSmall) {
                        weight = "500 г";
                        activityDetailedBinding.weight.setText(weight);
                        activityDetailedBinding.portions.setText("4");
                        price = Integer.parseInt(cakeModel.getPrice())*amount;
                        activityDetailedBinding.detailedPrice.setText(cakeModel.getPrice());
                    }
                    if (checkedId == R.id.btnStandart) {
                        weight = "1000 г";
                        activityDetailedBinding.weight.setText(weight);
                        activityDetailedBinding.portions.setText("8");
                        price = Integer.parseInt(cakeModel.getPrice())*2*amount;
                        activityDetailedBinding.detailedPrice.setText(Integer.toString(price));
                    }
                }
            }
        });

        activityDetailedBinding.addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addedToCart();
            }

            private void addedToCart() {

                final HashMap<String, Object> cartMap = new HashMap<>();

                cartMap.put("name", cakeModel.getName());
                cartMap.put("price", String.valueOf(price));
                cartMap.put("img_url", cakeModel.getImg_url());
                cartMap.put("weight", weight);
                cartMap.put("amount", String.valueOf(amount));

                db.collection("AddToCart").document(auth.getCurrentUser().getUid())
                        .collection("CurrentUser").add(cartMap).addOnCompleteListener(
                                new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                AndroidUtil.showToast(DetailedActivity.this, "Товар добавлен в корзину");
                                finish();

                            }
                        });
            }
        });
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}