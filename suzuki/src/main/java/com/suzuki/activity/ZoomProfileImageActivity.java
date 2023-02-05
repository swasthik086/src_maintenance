package com.suzuki.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.suzuki.R;

import static com.suzuki.activity.RiderProfileActivity.decodeBase64;
import static com.suzuki.fragment.DashboardFragment.MyPREFERENCES;
import static com.suzuki.fragment.DashboardFragment.key;

public class ZoomProfileImageActivity extends AppCompatActivity {
    SharedPreferences myPrefrence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zoom_profile_image_activity);
        ImageView ivUserImage = findViewById(R.id.ivUserImage);
        ImageView ivClose = findViewById(R.id.ivClose);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZoomProfileImageActivity.this.finish();
            }
        });
        myPrefrence = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
//            Toast.makeText(this, "----- " + myPrefrence.contains(key), Toast.LENGTH_SHORT).show();
        String photo = myPrefrence.getString(key, "photo");

        if (!photo.equals("photo")) {
            Bitmap bitmap = decodeBase64(photo);
            ivUserImage.setImageBitmap(bitmap);
        }
    }
}
