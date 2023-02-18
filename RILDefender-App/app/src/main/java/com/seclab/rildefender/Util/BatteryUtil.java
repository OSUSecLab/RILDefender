package com.seclab.rildefender.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BatteryUtil {

    private static BroadcastReceiver batteryStateReceiver;

    // TODO switch to app's private folder
    private static String outPath = FileUtil.baseDir + "/Download/batteryLog.txt";

    static float prev_batteryPct = -1;

    public static double getBatteryPercentage(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        double batteryPct = level * 100 / (float)scale;
        return batteryPct;
    }

    public static void clearBatteryLog(Context context) {
        FileUtil.writeToFile(context, outPath, "", false);
        Toast.makeText(context, "Clear battery log",
                Toast.LENGTH_LONG).show();
    }

    public static void startRecordingBatteryPercentage(Context context) {
        System.out.println(outPath);
        long startTime = System.currentTimeMillis();
        double initialBatteryPercentage = getBatteryPercentage(context);
        prev_batteryPct = -1;
        FileUtil.writeToFile(context, outPath, "\n Start recording battery", true);

        batteryStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level * 100 / (float)scale;

                if (batteryPct == prev_batteryPct)
                    return; // log only when battery pct change
                else
                    prev_batteryPct = batteryPct;

                // calculate power
                double elapsedTime =
                        (double) (System.currentTimeMillis() - startTime) / 1000 / 3600; // in hours
                double percentageDiff = initialBatteryPercentage - batteryPct;
                double averagePower = getAveragePower(context, percentageDiff, elapsedTime);

                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                Log.i("BatteryUtil", currentTime + "\t battery : " + batteryPct);
                // record battery status
                FileUtil.writeToFile(context, outPath,
                        currentTime + "\t" + batteryPct + "\t " +
                                "average power: " + averagePower + "W",
                        true);
            }
        };

        context.registerReceiver(batteryStateReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public static double getAveragePower(Context context, double percentage, double time) {
        if (time == 0 || percentage == 0)
            return 0;
        double capacity = getBatteryCapacity(context);
        double voltage = getVoltage(context);

        double averageCurrent = capacity * percentage / 100 / 1000 / time; // in A
        return averageCurrent * voltage; // W = A*V
    }

    public static double getBatteryCapacity(Context context) {
        // return in mAh
        Object mPowerProfile;
        double batteryCapacity = 0;
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class)
                    .newInstance(context);

            batteryCapacity = (double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getBatteryCapacity")
                    .invoke(mPowerProfile);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return batteryCapacity;
    }


    public static double getVoltage(Context context) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent b = context.registerReceiver(null, filter);
        return b.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) * 0.001;
    }
}
