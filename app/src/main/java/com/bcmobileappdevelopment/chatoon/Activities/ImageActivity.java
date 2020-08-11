package com.bcmobileappdevelopment.chatoon.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bcmobileappdevelopment.chatoon.HelperClass.ImagePager;
import com.bcmobileappdevelopment.chatoon.R;
import com.bumptech.glide.Glide;
import com.ortiz.touchview.TouchImageView;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;

public class ImageActivity extends AppCompatActivity {

    TouchImageView imageView;
    String imageURL;
    ImageView btBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Initialize();
        InitializeListeners();
    }

    private void Initialize() {
        imageURL = getIntent().getStringExtra("imageURL");
        imageView = findViewById(R.id.imageView);
        Glide.with(getBaseContext()).load(imageURL).into(imageView);
        btBack = findViewById(R.id.btBack);
    }

    private void InitializeListeners() {
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageActivity.this.finish();
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        ImageActivity.this.finish();
        super.onBackPressed();
    }
}
