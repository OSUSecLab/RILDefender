package com.seclab.rildefender.UI;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.seclab.rildefender.R;
import com.seclab.rildefender.Util.FileUtil;

import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;

public class SettingsActivity extends AppCompatActivity {

    private static Context mContext;
    private BroadcastReceiver rilReceiver;
    private static SettingsFragment settingsFragment;
    private static FragmentManager fragmentManager;
    private static Preference smsHistory;
    private static Preference yamlUpdate;
    // TODO setting common Android process by default
    private static String default_sms_whitelist = "com.android.messaging;";

    public static Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            System.out.println("key preference: " + key + " " + newValue.toString());

            if (key.equals("sms_whitelist")) {
                String value = newValue.toString();
                // send broadcast to notify RIL
                Intent intent = new Intent();
                intent.setAction("android.telephony.action.UPDATE_SOURCE");
                intent.putExtra("key", key);
                intent.putExtra("value", value);
                mContext.sendBroadcast(intent);
            }
            else {
                int value = option2Int(newValue.toString());
                // send broadcast to notify RIL
                Intent intent = new Intent();
                intent.setAction("android.telephony.action.UPDATE_SP");
                intent.putExtra("key", key);
                intent.putExtra("value", value);
                mContext.sendBroadcast(intent);
            }
            return true;
        }
    };

    public static int option2Int(String option) {
        if (option.equals("Allow"))
            return 0;
        else if (option.equals("Notify Me"))
            return 1;
        else if (option.equals("Block Automatically"))
            return 2;
        else if (option.equals("Block and Notify Me"))
            return 3;
        else
            return -1;
    }

    public static String int2Option(int op) {
        if (op == 0)
            return "Allow";
        else if (op == 1)
            return "Notify Me";
        else if (op == 2)
            return "Block Automatically";
        else if (op == 3)
            return "Block and Notify Me";
        else
            return "";
    }

    public static String type2Str(String type) {
        if (type.equals("silent_sms"))
            return "Silent SMS";
        else if (type.equals("binary_sms"))
            return "Binary SMS";
        else if (type.equals("malware_sms"))
            return "Malware SMS";
        else if (type.equals("flash_sms"))
            return "Flash SMS";
        else if (type.equals("fbs_sms"))
            return "Fake base station SMS";
        else if (type.equals("proactive_sim_sms"))
            return "Proactive SIM SMS";
        else
            return type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getBaseContext();
        setContentView(R.layout.settings_activity);
        settingsFragment = new SettingsFragment();
        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, settingsFragment)
                    .commit();
        }

        // configure default SMS whitelist
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();

        verifyPermissions(this, 0);

        Intent serviceIntent = new Intent(this, ListenService.class);
        startService(serviceIntent);

    }

    public static boolean readAndSendYaml(String yamlStr) {

        if (yamlStr.length() == 0)
            return false;

        try {
            Yaml yaml = new Yaml();
            LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) yaml.load(yamlStr);
            // Broadcast yaml
            Intent intent = new Intent();
            intent.setAction("android.telephony.action.UPDATE_YAML");
            intent.putExtra("value", map);
            mContext.sendBroadcast(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void verifyPermissions(Activity activity, int code) {
        if (Build.VERSION.SDK_INT >= 33) {
            // Check if we have permission
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // In Android 13, we need notification permission
                String[] allPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.POST_NOTIFICATIONS};
                ActivityCompat.requestPermissions(activity, allPermissions, code);
            }
        }
        else {
            // Check if we have permission
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                String[] allPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(activity, allPermissions, code);
            }
        }
    }
    
    public static String getSpValue(String key) {
        try {
            return ((ListPreference) settingsFragment.findPreference(key)).getValue();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat{
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            ListPreference silent_sms = findPreference("silent_sms");
            silent_sms.setOnPreferenceChangeListener(listener);

            ListPreference binary_sms = findPreference("binary_sms");
            binary_sms.setOnPreferenceChangeListener(listener);

            ListPreference flash_sms = findPreference("flash_sms");
            flash_sms.setOnPreferenceChangeListener(listener);

            ListPreference malware_sms = findPreference("malware_sms");
            malware_sms.setOnPreferenceChangeListener(listener);

            ListPreference fbs_sms = findPreference("fbs_sms");
            fbs_sms.setOnPreferenceChangeListener(listener);

            ListPreference proactive_sim_sms = findPreference("proactive_sim_sms");
            proactive_sim_sms.setOnPreferenceChangeListener(listener);

            EditTextPreference whitelist = findPreference("sms_whitelist");
            if (whitelist.getText() == null) {
                // configure default whitelist
                whitelist.setText(default_sms_whitelist);
                Intent intent = new Intent();
                intent.setAction("android.telephony.action.UPDATE_SOURCE");
                intent.putExtra("key", "sms_whitelist");
                intent.putExtra("value", default_sms_whitelist);
                mContext.sendBroadcast(intent);
            }
            whitelist.setOnPreferenceChangeListener(listener);

            smsHistory = (Preference) findPreference("sms_history");
            smsHistory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // jump to view SMS history
                    Intent myIntent = new Intent(mContext, SMSHistoryActivity.class);
                    myIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(myIntent);
                    return true;
                }
            });

            yamlUpdate = (Preference) findPreference("update_yaml");
            yamlUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String yamlStr;
                    String diaLogStr;
                    try {
                        if (FileUtil.createFileIfNotExist(mContext, FileUtil.yamlFile)) {
                            FileUtil.writeToFile(mContext, FileUtil.yamlFile, "CustomSMS1:\n" +
                                    "  {lvalue: [ 'sms.pid' ], condition: '==', rvalue: 0x20, securityLevel: 1}\n", true);

                            diaLogStr = "A template YAML attack signature configuration file has been created" +
                                    " at " + FileUtil.yamlFile + ". Please use the default file editor to edit and click on the Update" +
                                    " button to apply the update.";
                        }
                        else {
                            diaLogStr = "YAML attack signature configuration file found" +
                                    " at " + FileUtil.yamlFile + ". Please use the default file editor to edit and click on the Update" +
                                    " button to apply the update.";
                        }
                        yamlStr = FileUtil.getStringFromFile(mContext, FileUtil.yamlFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(diaLogStr)
                            .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    boolean result = readAndSendYaml(yamlStr);
                                    String text;
                                    if (result)
                                        text = "Signature update succeeded";
                                    else
                                        text = "Signature update failed";
                                    Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // do nothing
                                }
                            });

                    builder.create();
                    builder.show();
                    return true;
                }
            });
        }

    }

}