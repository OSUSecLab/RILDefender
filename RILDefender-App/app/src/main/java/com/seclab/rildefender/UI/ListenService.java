package com.seclab.rildefender.UI;

import static com.seclab.rildefender.UI.SettingsActivity.int2Option;
import static com.seclab.rildefender.UI.SettingsActivity.type2Str;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.seclab.rildefender.R;
import com.seclab.rildefender.Util.BatteryUtil;
import com.seclab.rildefender.Util.FileUtil;
import com.seclab.rildefender.Util.NumUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class ListenService extends Service {

    private RILReceiver receiver;
    private NotificationManager notificationManager;

    private int RIL_NOTIFICATION_ID = 1;
    private int FOREGROUND_NOTIFICATION_ID = 2;

    public static final String NOTIFICATION_CHANNEL_ID = "foreground-notification-channel";

    public static final boolean measureOnly = false;


    @Override
    public void onCreate() {
        System.out.println("On create");
        super.onCreate();

        if (!measureOnly) {
            // register broadcast receiver for events from the RIL layer
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.seclab.rilmediator.NOTIFY");
            receiver = new RILReceiver();
            this.registerReceiver(receiver, filter);

            // create notification manager
            notificationManager =
                    (NotificationManager) this.getSystemService(Service.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel notificationChannel = new NotificationChannel("ril_notify", "Notify", NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notificationManager.createNotificationChannel(notificationChannel);
            } else {

            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!measureOnly) {
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID, "RILDefender", NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setDescription("RILDefender foreground notification");
                notificationChannel.enableLights(true);
                notificationChannel.enableVibration(true); //reflect default preferences value here (vibrate+ring)
                notificationManager.createNotificationChannel(notificationChannel);
            } else {

            }

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= 31)
                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            else
                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentTitle("RILDefender")
                    .setContentText("Monitoring SMS attacks.")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setOngoing(true)
                    .setContentIntent(pendingIntent);

            startForeground(FOREGROUND_NOTIFICATION_ID, notificationBuilder.build());
        }

        // Evaluation: record battery and power consumption
//        BatteryUtil.startRecordingBatteryPercentage(this);

        return START_STICKY; //this defines this service to stay alive
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(receiver);
    }


    // receive events from the RIL layer
    public class RILReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("OnReceive");
            if (intent.getAction().equals("com.seclab.rilmediator.NOTIFY")) {

                // send notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "ril_notify")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("RILDefender Warning!")
                        .setContentText("Suspicious SMS received!")
                        .setPriority(NotificationCompat.PRIORITY_MIN);


                String content = intent.getStringExtra("content");
                String source = intent.getStringExtra("source");
                String type = intent.getStringExtra("type");
                String dest = intent.getStringExtra("dest");
                byte[] pdu = intent.getByteArrayExtra("pdu");
                String securityLevel = SettingsActivity.getSpValue(type);

                if (securityLevel == null) {
                    // comes from a custom SMS event
                    securityLevel = int2Option(intent.getIntExtra("securityLevel", 0));
                }

                if (securityLevel.equals("Notify Me")) {
                    String notifyStr = type2Str(type) + " received from " + source + "\nSMS Content: " + content;
                    builder.setStyle(new NotificationCompat.BigTextStyle().bigText(notifyStr));
                    if (Build.VERSION.SDK_INT < 26) {
                        builder.setContentText(notifyStr); // Nexus 6
                    }
                }
                else if (securityLevel.equals("Block and Notify Me")) {
                    String notifyStr = type2Str(type) + " has been blocked from " + source + "\nSMS Content: " + content;
                    builder.setStyle(new NotificationCompat.BigTextStyle().bigText(notifyStr));
                    if (Build.VERSION.SDK_INT < 26) {
                        builder.setContentText(notifyStr); // Nexus 6
                    }
                }

                // log SMS event locally
                JSONObject smsJson = new JSONObject();
                try {
                    smsJson.put("Type", type);
                    smsJson.put("Source", source);
                    smsJson.put("Content", content);
                    smsJson.put("Dest", dest);
                    smsJson.put("Pdu", NumUtil.bytesToHex(pdu));
                    smsJson.put("SecurityLevel", securityLevel);
                    smsJson.put("Time", System.currentTimeMillis());
                    FileUtil.recordSMSToFile(context, smsJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // notify
                notificationManager.notify(1, builder.build());
            }
        }
    }
}
