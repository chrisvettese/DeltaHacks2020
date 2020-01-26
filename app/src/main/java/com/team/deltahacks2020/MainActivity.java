package com.team.deltahacks2020;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO Temporary
        Intent switchActivity = new Intent(this, CameraActivity.class);
        startActivity(switchActivity);
    }
}
