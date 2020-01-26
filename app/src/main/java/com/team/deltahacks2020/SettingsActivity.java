package com.team.deltahacks2020;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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


    GoogleSignInClient mGoogleSignInClient;
    Button signOut;

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

        signOut = findViewById(R.id.btnSignOut);
        signOut.setOnClickListener(v -> {
            switch (v.getId()) {
                case R.id.btnSignOut:
                    signOut();
                    break;
            }
        });
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
        }




        db.collection("controller").document(auth.getCurrentUser().getEmail()).get()
                .addOnCompleteListener((@Nonnull Task<DocumentSnapshot> task)-> {
                    if(task.isSuccessful() && !task.getResult().exists()){
                        //ask if uer or camera
                        camButt.setVisibility(View.VISIBLE);
                        userButt.setVisibility(View.VISIBLE);
                    } else{
                        String fileName = auth.getCurrentUser().getEmail() + ".txt";


                        try {
                            FileInputStream fileIn=openFileInput(fileName);
                            InputStreamReader InputRead= new InputStreamReader(fileIn);

                            char[] inputBuffer= new char[100];
                            String s="";
                            int charRead;

                            while ((charRead=InputRead.read(inputBuffer))>0) {
                                // char to string conversion
                                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                                s +=readstring;
                            }
                            InputRead.close();
                            //if s matches firebase than go to user intent
                            //else go to camera
                            if (s.equals( task.getResult().get("userID").toString())){
                                Intent intent = new Intent(this,UserActivity.class);
                                startActivity(intent);
                            }
                            else{
                                Intent intent = new Intent(this, CameraActivity.class);
                                startActivity(intent);
                            }




                            Toast.makeText(getBaseContext(), s + " umm yay?",
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Arghhhhh",
                                    Toast.LENGTH_SHORT).show();
                        }




                    }
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
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }
    public void userClick(View view){

        //save id to firebase
        Map<String,Long> idMap = new HashMap<>();
        Long time = System.currentTimeMillis();
        idMap.put("userID", time );
        db.collection("controller").document(auth.getCurrentUser().getEmail()).set(idMap)
                .addOnCompleteListener((@NonNull Task<Void> task)->{
            if (task.isSuccessful()) {

                String fileName = auth.getCurrentUser().getEmail() + ".txt";

                try {
                    FileOutputStream fileout=openFileOutput(fileName, MODE_PRIVATE);
                    OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
                    outputWriter.write(Long.toString(time));
                    outputWriter.close();

                    //display file saved message
                    Toast.makeText(getBaseContext(), "File saved successfully!",
                            Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "Ooga :(",
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getBaseContext(), "Blehhhh",Toast.LENGTH_SHORT).show();
            }
        });






    }







    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(SettingsActivity.this, "Signed out", Toast.LENGTH_SHORT);
            }
        });
    }
}

