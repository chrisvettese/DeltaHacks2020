package com.team.deltahacks2020;

import android.media.Image;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class UserActivity extends AppCompatActivity {
    //for the log out method
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;

    private TextView mtextView;
    private ImageView redImgView;
    private ImageView greenImgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        redImgView = findViewById(R.id.redCircle);
        greenImgView = findViewById(R.id.greenCircle);
        mtextView = findViewById(R.id.motionTV);

        ImageView myImageView = findViewById(R.id.redCircle);
        myImageView.setImageResource(R.drawable.redcircle);

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
            } else {
                System.out.println("TEST : null");
            }
        });
    }


    public void logOutClick(View view){
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent switchIntent = new Intent(this, MainActivity.class);
            startActivity(switchIntent);
            finish();
        });
    }
    private void updateUIMotionStatus(boolean isMotion) {

        if (isMotion == true) {
            redImgView.setVisibility(View.VISIBLE);
            greenImgView.setVisibility(View.INVISIBLE);
            mtextView.setText("Motion Detected");
        } else {
            greenImgView.setVisibility(View.VISIBLE);
            redImgView.setVisibility(View.INVISIBLE);
            mtextView.setText("Motion Not Detected");

        }
    }
}
