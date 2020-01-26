package com.team.deltahacks2020;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class UserActivity extends AppCompatActivity {
    //for the log out method
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;

    private Bitmap greenCircle, redCircle;

    private long userID;

    private CountDownTimer timer;

    private Map<String, Object> mapFromFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        userID = Long.parseLong(getIntent().getStringExtra("userID"));

        mapFromFirebase = new HashMap<>();

        greenCircle = BitmapFactory.decodeResource(getResources(), R.drawable.greencircle);
        redCircle = BitmapFactory.decodeResource(getResources(), R.drawable.redcircle);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String email = auth.getCurrentUser().getEmail();

        Map<String, Object> firebaseDBUpdate = new HashMap<>();
        firebaseDBUpdate.put("userID", userID);
        db.collection("controller").document(auth.getCurrentUser().getEmail()).set(firebaseDBUpdate).addOnCompleteListener((@NonNull Task<Void> task)->{
            if (task.isSuccessful()) {

            } else {
                System.out.println("ERROR: " + "Failed to clean database.");
            }
        });

        timer = new CountDownTimer(9000000000000000000l, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                LinearLayout view = findViewById(R.id.mainUserLayout);
                for (int i = 0; i < view.getChildCount(); i++) {
                    LinearLayout verticalMainLayout = (LinearLayout) view.getChildAt(i);
                    LinearLayout humanLayout = (LinearLayout) verticalMainLayout.getChildAt(1);
                    LinearLayout motionLayout = (LinearLayout) verticalMainLayout.getChildAt(2);

                    TextView humanTime = (TextView) verticalMainLayout.getChildAt(3);
                    TextView motionTime = (TextView) verticalMainLayout.getChildAt(4);
                    TextView humanStatus = (TextView) humanLayout.getChildAt(1);
                    TextView motionStatus = (TextView) motionLayout.getChildAt(1);

                    boolean countHuman = humanStatus.getText().toString().equals("No Humans Detected");
                    boolean countMotion = motionStatus.getText().toString().equals("No Motion Detected");

                    if (countHuman) {
                        int currentTime = Integer.parseInt(humanTime.getText().toString().replaceAll("\\D+",""));
                        humanTime.setText("Last human seen: " + (currentTime + 1) + " seconds ago");
                    }
                    if (countMotion) {
                        int currentTime = Integer.parseInt(motionTime.getText().toString().replaceAll("\\D+",""));
                        motionTime.setText("Last movement detected: " + (currentTime + 1) + " seconds ago");
                    }
                }
            }

            @Override
            public void onFinish() {

            }
        }.start();


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        final DocumentReference docRef = db.collection("controller").document(email);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                System.out.println("Listen failed");
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                for (String key : snapshot.getData().keySet()) {
                    if (!key.equals("userID")) {
                        Object value = snapshot.get(key);
                        //If this is the key-value pair being updated on firebase
                        Object oldValueFromFirebase = mapFromFirebase.get(key);
                        if (!value.equals(oldValueFromFirebase)) {
                            mapFromFirebase.put(key, value);
                            String[] splitKey = key.split("-");
                            //If this is an update to human movement
                            if (key.contains("humanAlert")) {
                                if (oldValueFromFirebase == null && mapFromFirebase.get("motionAlert-" + splitKey[1]) == null) {
                                    addToScrollView(splitKey[1]);
                                }
                                updateUIHumanStatus((Boolean) value, "Camera-" + splitKey[1]);
                                //this is an update to motion sensor
                            } else {
                                if (oldValueFromFirebase == null && mapFromFirebase.get("humanAlert-" + splitKey[1]) == null) {
                                    addToScrollView(splitKey[1]);
                                }
                                updateUIMotionStatus((Boolean) value, "Camera-" + splitKey[1]);
                            }
                        }
                    }
                }
            } else {
                System.out.println("TEST : null");
            }
        });
    }
    private void addToScrollView(String cameraId) {
        LinearLayout cameraLayout = new LinearLayout(this);
        cameraLayout.setOrientation(LinearLayout.VERTICAL);
        cameraLayout.setPadding(0, 20, 0, 20);

        TextView text = new TextView(this);
        text.setText("Camera-" + cameraId);
        cameraLayout.addView(text);

        LinearLayout horizontalLayoutHuman = new LinearLayout(this);
        horizontalLayoutHuman.setOrientation(LinearLayout.HORIZONTAL);
        ImageView humanCircle = new ImageView(this);
        humanCircle.setImageBitmap(greenCircle);
        TextView humanStateText = new TextView(this);
        humanStateText.setText("No Humans Detected");
        horizontalLayoutHuman.addView(humanCircle);
        horizontalLayoutHuman.addView(humanStateText);
        cameraLayout.addView(horizontalLayoutHuman);

        LinearLayout horizontalLayoutMotion = new LinearLayout(this);
        horizontalLayoutHuman.setOrientation(LinearLayout.HORIZONTAL);
        ImageView motionCircle = new ImageView(this);
        motionCircle.setImageBitmap(greenCircle);
        TextView motionStateText = new TextView(this);
        motionStateText.setText("No Motion Detected");
        horizontalLayoutMotion.addView(motionCircle);
        horizontalLayoutMotion.addView(motionStateText);
        cameraLayout.addView(horizontalLayoutMotion);

        TextView humanUpdateText = new TextView(this);
        humanUpdateText.setText("Last human seen: 0 seconds ago");
        cameraLayout.addView(humanUpdateText);

        TextView motionUpdateText = new TextView(this);
        motionUpdateText.setText("Last movement seen: 0 seconds ago");
        cameraLayout.addView(motionUpdateText);

        LinearLayout mainUserLayout = findViewById(R.id.mainUserLayout);
        mainUserLayout.addView(cameraLayout);
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
        super.onStop();
        timer.cancel();
    }

    private void updateUIMotionStatus(boolean isMotion, String cameraId) {
        LinearLayout mainUserLayout = findViewById(R.id.mainUserLayout);
        LinearLayout verticalMainLayout = null;
        for (int i = 0; i < mainUserLayout.getChildCount(); i++) {
            LinearLayout layout = (LinearLayout) mainUserLayout.getChildAt(i);
            if (((TextView) layout.getChildAt(0)).getText().toString().equals(cameraId)) {
                verticalMainLayout = layout;
            }
        }
        LinearLayout motionLayout = (LinearLayout) verticalMainLayout.getChildAt(2);
        ImageView circle = (ImageView) motionLayout.getChildAt(0);

        TextView motionTime = (TextView) verticalMainLayout.getChildAt(4);
        TextView motionStatus = (TextView) motionLayout.getChildAt(1);

        if (isMotion == true) {
            circle.setImageBitmap(redCircle);
            motionStatus.setText("Motion Detected");
            //reset timer
            motionTime.setText("Last movement detected: 0 seconds ago");
        } else {
            motionStatus.setText("No Motion Detected");
            circle.setImageBitmap(greenCircle);
        }
    }

    private void updateUIHumanStatus(boolean isHuman, String cameraId) {
        LinearLayout linearLayout = findViewById(R.id.mainUserLayout);
        LinearLayout verticalMainLayout = null;
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            LinearLayout layout = (LinearLayout) linearLayout.getChildAt(i);
            if (((TextView) layout.getChildAt(0)).getText().toString().equals(cameraId)) {
                verticalMainLayout = layout;
            }
        }
        LinearLayout humanLayout = (LinearLayout) verticalMainLayout.getChildAt(1);
        ImageView circle = (ImageView) humanLayout.getChildAt(0);

        TextView humanTime = (TextView) verticalMainLayout.getChildAt(3);
        TextView humanStatus = (TextView) humanLayout.getChildAt(1);


        if (isHuman == true) {
            circle.setImageBitmap(redCircle);
            humanStatus.setText("Humans Detected");
            humanTime.setText("Last human seen: 0 seconds ago");
        } else {
            circle.setImageBitmap(greenCircle);
            humanStatus.setText("No Humans Detected");

        }
    }

}
