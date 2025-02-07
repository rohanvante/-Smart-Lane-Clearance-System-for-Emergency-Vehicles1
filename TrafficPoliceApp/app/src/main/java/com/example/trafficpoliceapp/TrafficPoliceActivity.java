package com.example.trafficpoliceapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TrafficPoliceActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private TextView txtPoliceStatus;
    private static final String CHANNEL_ID = "traffic_alert_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_police);

        databaseReference = FirebaseDatabase.getInstance().getReference("vehicles");
        txtPoliceStatus = findViewById(R.id.txtPoliceStatus);

        txtPoliceStatus.setText("Status: Waiting for alert");

        // Create notification channel for alerts
        createNotificationChannel();

        // Start listening for ambulance alerts
        listenForAmbulanceAlert();
    }

    private void listenForAmbulanceAlert() {
        databaseReference.child("ambulance1").child("alert").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean alert = snapshot.getValue(Boolean.class);
                if (Boolean.TRUE.equals(alert)) {
                    txtPoliceStatus.setText("🚨 Status: Emergency Alert Received from Ambulance!");
                    Log.d("P2V", "🚔 Emergency alert received from ambulance.");

                    // Show popup notification
                    showHeadsUpNotification("🚔 Ambulance Alert", "An ambulance is on the way! Please clear the path.");
                } else {
                    txtPoliceStatus.setText("Status: Waiting for alert");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("P2V", "Error listening for ambulance alert: " + error.getMessage());
            }
        });
    }

    private void showHeadsUpNotification(String title, String message) {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_notifications_24) // Ensure this drawable exists
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 500, 500, 500})
                .setAutoCancel(true)
                .setFullScreenIntent(null, true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.notify(2, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Traffic Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for emergency alerts from ambulances");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 500, 500});

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
