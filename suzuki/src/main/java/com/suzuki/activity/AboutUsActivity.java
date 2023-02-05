package com.suzuki.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.suzuki.R;

public class AboutUsActivity extends AppCompatActivity {
    TextView aboutLink;
    ImageView ivTermsArr;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);

        aboutLink = (TextView) findViewById(R.id.tvAboutLink);
        ivTermsArr = (ImageView) findViewById(R.id.ivTerms);

        aboutLink.setOnClickListener(view -> {
            Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://www.suzukimotorcycle.co.in/about-us"));
            startActivity(viewIntent);
        });
//test
        ivTermsArr.setOnClickListener(v -> finish());
    }
}
