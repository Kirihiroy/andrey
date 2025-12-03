package com.example.andrey;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ClockFragment extends Fragment {

    private TextView currentTime, currentDate, currentTimeZone;
    private Button btnChangeTimeZone;
    private Handler handler;
    private Runnable updateTimeRunnable;
    private SimpleDateFormat timeFormat, dateFormat;
    private String selectedTimeZone = TimeZone.getDefault().getID();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clock, container, false);

        currentTime = view.findViewById(R.id.currentTime);
        currentDate = view.findViewById(R.id.currentDate);
        currentTimeZone = view.findViewById(R.id.currentTimeZone);
        btnChangeTimeZone = view.findViewById(R.id.btnChangeTimeZone);

        timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        dateFormat = new SimpleDateFormat("dd MMMM yyyy, EEEE", new Locale("ru"));

        btnChangeTimeZone.setOnClickListener(v -> showTimeZoneDialog());

        handler = new Handler();
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                handler.postDelayed(this, 1000);
            }
        };

        return view;
    }

    private void updateTime() {
        TimeZone timeZone = TimeZone.getTimeZone(selectedTimeZone);
        timeFormat.setTimeZone(timeZone);
        dateFormat.setTimeZone(timeZone);

        Date now = new Date();
        currentTime.setText(timeFormat.format(now));
        currentDate.setText(dateFormat.format(now));
        currentTimeZone.setText(timeZone.getDisplayName());
    }

    private void showTimeZoneDialog() {
        TimeZoneDialog dialog = new TimeZoneDialog(getContext(), timeZoneId -> {
            selectedTimeZone = timeZoneId;
            updateTime();
        });
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(updateTimeRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(updateTimeRunnable);
    }
}