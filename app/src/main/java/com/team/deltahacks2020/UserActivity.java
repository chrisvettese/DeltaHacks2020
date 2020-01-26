package com.team.deltahacks2020;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        final DocumentReference docRef = db.collection("controller").document(email);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                System.out.println("Listen failed");
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                System.out.println("TEST " + snapshot.getData());
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
}
