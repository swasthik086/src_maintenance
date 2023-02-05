package com.suzuki.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.suzuki.R;

public class TermsActivity extends AppCompatActivity {

    ImageView ivTermsArr;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms_activity);

        ivTermsArr = (ImageView) findViewById(R.id.ivTerms);

        ivTermsArr.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
