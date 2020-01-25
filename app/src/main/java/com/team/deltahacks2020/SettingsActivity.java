package com.team.deltahacks2020;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    String userID;
    Button userButt;
    Button camButt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userButt = findViewById(R.id.userButton);
        camButt = findViewById(R.id.cameraButton);

        //reads from a file the userID
        //userID = readFromFile();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        db.collection("controller").document(auth.getCurrentUser().getEmail()).get()
                .addOnCompleteListener((@Nonnull Task<DocumentSnapshot> task)-> {
                    if(!task.isSuccessful()){
                        //ask if user or camera

                        camButt.setVisibility(View.VISIBLE);
                        userButt.setVisibility(View.VISIBLE);
                    }
                    /*
                    else{
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()){
                            String phoneID = task.getResult().get("userID").toString();
                            //this means the phone is user
                            if(userID == phoneID){
                            }
                            //this means the phone is a camera
                            else{
                            }
                        }
                    }*/
                });
    }


    private String readFromFile() {
        String ret = "";
        try {
            InputStream inputStream = openFileInput("settings.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("login activity File not found: " + e.toString());
        } catch (IOException e) {
            System.out.println("login activity Can not read file: " + e.toString());
        }

        return ret;
    }

    public void cameraClick(View view){


    }
    public void userClick(View view){

        //save id to firebase
        Map<String,Object> idMap = new HashMap<>();
        long time = System.currentTimeMillis();
        idMap.put(userID, time );
        db.collection("controller").document(auth.getCurrentUser().getEmail()).set(idMap);

        //save id to internal Storage
        //File file = new File("controller.txt");

        try {
            FileOutputStream fileout=openFileOutput("mytextfile.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            //outputWriter.write(textmsg.getText().toString());
            outputWriter.write(Long.toString(time));
            outputWriter.close();

            //display file saved message
            Toast.makeText(getBaseContext(), "File saved successfully!",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }



    }


}
