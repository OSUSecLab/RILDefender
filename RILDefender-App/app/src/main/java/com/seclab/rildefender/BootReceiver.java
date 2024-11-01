package com.seclab.rildefender;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;

import com.seclab.rildefender.UI.ListenService;
import com.seclab.rildefender.UI.SettingsActivity;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent i = new Intent(context, ListenService.class);
            context.startService(i);
        }
    }
}
