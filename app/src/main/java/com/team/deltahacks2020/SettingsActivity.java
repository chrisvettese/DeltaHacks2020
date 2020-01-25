package com.team.deltahacks2020;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private static AsyncTask<String, Integer, String> task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        new MyAsyncTask().execute();
    }

    private static class MyAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            //Angus put your code here
            return null;
        }
    }
}
