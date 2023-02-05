package com.suzuki.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.suzuki.R;

public class test extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test);

        findViewById(R.id.BasButton).setOnClickListener(view -> {
            Toast.makeText(this, "Bas Button clicked", Toast.LENGTH_SHORT).show();

        });

        findViewById(R.id.JeroenButton).setOnClickListener(view -> {
            Toast.makeText(this, "Jeroen Button clicked", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
