package com.team.deltahacks2020;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class UserActivity extends AppCompatActivity {

    TextView mtextView;
    ImageView redImgView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

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
    private void updateUIMotionStatus(boolean isMotion) {
        redImgView = findViewById(R.id.redCircle);
        mtextView = findViewById(R.id.motionTV);

        if (isMotion == true) {
            redImgView.setVisibility(View.INVISIBLE);
            mtextView.setText("Motion Detected");
        } else {
            redImgView.setVisibility(View.VISIBLE);
            mtextView.setText("Motion Not Detected");

        }
    }
}
