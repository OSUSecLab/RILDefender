package com.seclab.rildefender.UI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import com.seclab.rildefender.R;
import com.seclab.rildefender.Util.FileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SMSHistoryActivity extends AppCompatActivity {

    private RecyclerViewAdapter adapter;
    public List<JSONObject> smsEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smshistory);

        loadSmsEvents();

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.sms_event);
        int numberOfColumns = 6; // Time, SMS type, src, dst, content, PDU

        String[] data = new String[(smsEvents.size()+1) * numberOfColumns];
        int counter = 0;
        // headers
        data[counter++] = "Time";
        data[counter++] = "Type";
        data[counter++] = "Source";
        data[counter++] = "Destination";
        data[counter++] = "Content";
        data[counter++] = "PDU";
        for (JSONObject event: smsEvents) {
            try {
                data[counter++] = tsToDate(event.getLong("Time"));
                data[counter++] = event.getString("Type");
                if (event.has("Source"))
                    data[counter++] = event.getString("Source");
                else
                    data[counter++] = "";
                if (event.has("Dest"))
                    data[counter++] = event.getString("Dest");
                else
                    data[counter++] = "";
                if (event.has("Content"))
                    data[counter++] = event.getString("Content");
                else
                    data[counter++] = "";
                if (event.has("Pdu"))
                    data[counter++] = event.getString("Pdu");
                else
                    data[counter++] = "";
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

//        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns, LinearLayoutManager.VERTICAL, false));
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, numberOfColumns, LinearLayoutManager.VERTICAL, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, numberOfColumns);
//        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(gridLayoutManager);

        adapter = new RecyclerViewAdapter(this, data);
        recyclerView.setAdapter(adapter);
    }

    public void loadSmsEvents() {
        JSONArray jsonArray = FileUtil.readSMSEventsFromFile(this.getApplicationContext());
        if (jsonArray == null)
            return;
        for (int i=0; i<jsonArray.length(); ++i) {
            try {
                smsEvents.add(jsonArray.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String tsToDate(long ts) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(ts);
        return sdf.format(resultdate);
    }

}