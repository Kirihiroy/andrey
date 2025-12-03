package com.example.andrey;


import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeZoneFragment extends Fragment {

    private ListView timeZoneListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> timeZoneList = new ArrayList<>();
    private Handler handler = new Handler();
    private Runnable updateRunnable;

    private String[] popularTimeZones = {
            "Europe/Moscow", "GMT", "Europe/London", "Europe/Paris",
            "America/New_York", "America/Los_Angeles", "Asia/Tokyo",
            "Asia/Shanghai", "Australia/Sydney", "Asia/Dubai"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_zone, container, false);

        timeZoneListView = view.findViewById(R.id.timeZoneListView);
        adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, timeZoneList);
        timeZoneListView.setAdapter(adapter);

        updateTimeZones();

        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimeZones();
                handler.postDelayed(this, 1000);
            }
        };

        return view;
    }

    private void updateTimeZones() {
        timeZoneList.clear();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        for (String tzId : popularTimeZones) {
            TimeZone timeZone = TimeZone.getTimeZone(tzId);
            sdf.setTimeZone(timeZone);
            String time = sdf.format(new Date());
            String displayText = timeZone.getDisplayName() + "\n" + time + " (" + tzId + ")";
            timeZoneList.add(displayText);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(updateRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(updateRunnable);
    }
}