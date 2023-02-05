package com.suzuki.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.suzuki.R;

public class PrivacyActivity extends AppCompatActivity {

    ImageView ivPrivacyArr;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_activity);

        ivPrivacyArr = ( ImageView ) findViewById(R.id.ivPrivacy);
        ivPrivacyArr.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               onBackPressed();
            }
        });
    }
}
