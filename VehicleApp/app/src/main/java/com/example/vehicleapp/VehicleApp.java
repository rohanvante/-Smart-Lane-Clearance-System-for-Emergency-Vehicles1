package com.example.vehicleapp;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VehicleApp extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private Button btnNotifyPolice;
    private TextView txtVehicleStatus;
    private boolean ambulanceAlert = false;
    private static final String CHANNEL_ID = "alert_channel";
    private static final String BROADCAST_ACTION = "com.example.vehicleapp.AMBULANCE_ALERT";
    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;

    private BroadcastReceiver alertReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showHeadsUpNotification("🚨 Emergency Alert", "Ambulance is nearby! Please give way.");
        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_app);

        databaseReference = FirebaseDatabase.getInstance().getReference("vehicles");
        btnNotifyPolice = findViewById(R.id.btnNotifyPolice);
        txtVehicleStatus = findViewById(R.id.txtVehicleStatus);

        txtVehicleStatus.setText("Status: Waiting for alert");
        listenForAmbulanceAlert();
        btnNotifyPolice.setOnClickListener(v -> notifyTrafficPolice());

        requestNotificationPermission();
        createNotificationChannel();

        // Register broadcast receiver
        registerReceiver(alertReceiver, new IntentFilter(BROADCAST_ACTION));
    }

    private void listenForAmbulanceAlert() {
        databaseReference.child("ambulance1").child("alert").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean alert = snapshot.getValue(Boolean.class);
                if (Boolean.TRUE.equals(alert)) {
                    ambulanceAlert = true;
                    btnNotifyPolice.setEnabled(true);
                    txtVehicleStatus.setText("Status: Alert received from ambulance");

                    // Send a broadcast to trigger notification
                    Intent intent = new Intent(BROADCAST_ACTION);
                    sendBroadcast(intent);
                } else {
                    ambulanceAlert = false;
                    btnNotifyPolice.setEnabled(false);
                    txtVehicleStatus.setText("Status: Waiting for alert");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Database Error: " + error.getMessage());
            }
        });
    }

    private void notifyTrafficPolice() {
        if (ambulanceAlert) {
            databaseReference.child("trafficPolice").child("alert").setValue(true);
            Toast.makeText(getApplicationContext(), "🚨 Alert sent to Traffic Police!", Toast.LENGTH_LONG).show();
            Log.d("V2P", "Emergency alert received. Notifying traffic police.");
        }
    }

    private void showHeadsUpNotification(String title, String message) {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        Intent intent = new Intent(this, VehicleApp.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX) // Ensures heads-up alert
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setAutoCancel(true)
                .setFullScreenIntent(pendingIntent, true); // Enables popup behavior

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Emergency Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for emergency alerts");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 500, 500});

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(alertReceiver);
    }
}
