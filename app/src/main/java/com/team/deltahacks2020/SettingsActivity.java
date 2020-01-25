package com.team.deltahacks2020;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
            File file = new File("settings.txt");
            if (file.exists()){
                try (FileReader reader = new FileReader(file)){




                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            else{
                //Phone is camera

            }





            return null;
        }
    }
}
