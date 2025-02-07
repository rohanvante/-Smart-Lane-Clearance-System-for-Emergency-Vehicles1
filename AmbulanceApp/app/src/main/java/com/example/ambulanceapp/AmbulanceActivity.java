package com.example.ambulanceapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AmbulanceActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private Button btnBroadcast;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance);

        databaseReference = FirebaseDatabase.getInstance().getReference("vehicles");
        btnBroadcast = findViewById(R.id.btnBroadcast);
        txtStatus = findViewById(R.id.txtStatus);

        btnBroadcast.setOnClickListener(v -> broadcastEmergencyAlert());
    }

    private void broadcastEmergencyAlert() {
        databaseReference.child("ambulance1").child("alert").setValue(true);
        txtStatus.setText("Status: Broadcasting Alert...");
        Toast.makeText(getApplicationContext(), "Emergency alert sent!", Toast.LENGTH_LONG).show();
    }
}