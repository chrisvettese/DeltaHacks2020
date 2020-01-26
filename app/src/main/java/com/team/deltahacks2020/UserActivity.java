package com.team.deltahacks2020;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class UserActivity extends AppCompatActivity {
    //for the log out method
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;

    private TextView humanTimeText;
    private TextView motionTimeText;
    private TextView humanSensedText;
    private TextView motionSensedText;
    private ImageView redImgViewMotion;
    private ImageView greenImgViewMotion;
    private ImageView redImgViewHuman;
    private ImageView greenImgViewHuman;
    private boolean countHuman;
    private boolean countMotion;
    private int timeHuman;
    private int timeMotion;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        countHuman = true;
        countMotion = true;

        timer = new CountDownTimer(9000000000000000000l, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                if (countHuman) {
                    timeHuman++;
                    humanTimeText.setText("Last human seen: " + timeHuman + " seconds ago");
                }
                if (countMotion) {
                    timeMotion++;
                    motionTimeText.setText("Last movement detected: " + timeMotion + " seconds ago");
                }
            }

            @Override
            public void onFinish() {

            }
        }.start();
        timeHuman = 0;
        timeMotion = 0;


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        redImgViewMotion = findViewById(R.id.redCircleMotion);
        greenImgViewMotion = findViewById(R.id.greenCircleMotion);
        redImgViewMotion = findViewById(R.id.redCircleHuman);
        greenImgViewMotion = findViewById(R.id.greenCircleHuman);

        humanTimeText = findViewById(R.id.humanTV);
        motionTimeText = findViewById(R.id.motionTime);

        humanSensedText = findViewById(R.id.humansSensed);
        motionSensedText = findViewById(R.id.motionSensed);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        final DocumentReference docRef = db.collection("controller").document(email);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                System.out.println("Listen failed");
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Object motionStatus = snapshot.get("motionAlert");
                if (motionStatus != null) {
                    boolean status = (Boolean) motionStatus;
                    updateUIMotionStatus(status);
                }
                Object humanStatus = snapshot.get("motionAlert");
                if (humanStatus != null) {
                    boolean status = (Boolean) humanStatus;
                    updateUIHumanStatus(status);
                }
            } else {
                System.out.println("TEST : null");
            }
        });
    }


    public void logOutClick(View view) {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent switchIntent = new Intent(this, MainActivity.class);
            startActivity(switchIntent);
            finish();
        });
    }
    @Override
    public void onStop() {

    }

    private void updateUIMotionStatus(boolean isMotion) {

        if (isMotion == true) {
            countMotion = false;
            redImgViewMotion.setVisibility(View.VISIBLE);
            greenImgViewMotion.setVisibility(View.INVISIBLE);
            motionSensedText.setText("Motion Detected");
            //reset timer
            motionTimeText.setText("Last movement detected: 0 seconds ago");
            timeMotion = 0;


        } else {
            motionSensedText.setText("No Motion Detected");
            countMotion = true;
            greenImgViewMotion.setVisibility(View.VISIBLE);
            redImgViewMotion.setVisibility(View.INVISIBLE);
            //start timer


        }
    }

    private void updateUIHumanStatus(boolean isHuman) {
        if (isHuman == true) {
            redImgViewHuman.setVisibility(View.VISIBLE);
            greenImgViewHuman.setVisibility(View.INVISIBLE);
            countHuman = false;
            timeHuman = 0;
            humanSensedText.setText("Humans Detected");
            humanTimeText.setText("Last human seen: 0 seconds ago");
        } else {
            humanSensedText.setText("No Humans Detected");
            greenImgViewHuman.setVisibility(View.VISIBLE);
            redImgViewHuman.setVisibility(View.INVISIBLE);
            countHuman = true;

        }
    }

}
