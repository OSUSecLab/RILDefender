package com.seclab.rildefender.UI;

import static com.seclab.rildefender.UI.SettingsActivity.type2Str;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class AlertActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String content = intent.getStringExtra("content");
        String source = intent.getStringExtra("source");
        String type = intent.getStringExtra("type");

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle("Warning! Suspicious SMS received!");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });


//                builder1.setNegativeButton(
//                        "No",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        });


        String securityLevel = SettingsActivity.getSpValue(type);


        if (securityLevel.equals("Notify Me")) {
            builder1.setMessage(type2Str(type) + " received from " + source + "\nSMS Content: " + content);
            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
        else if (securityLevel.equals("Block and Notify Me")) {
            builder1.setMessage(type2Str(type) + " has been blocked from " + source + "\nSMS Content: " + content);
            AlertDialog alert11 = builder1.create();
            alert11.show();
        }

    }

    @Override
    public void finish() {
        super.finish();
    }
}
