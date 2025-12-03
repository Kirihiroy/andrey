package com.example.andrey;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AlaramFragment extends Fragment {

    private Button btnAddAlarm;
    private ListView alarmListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> alarms = new ArrayList<>();
    private AlarmManager alarmManager;
    private void checkAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                // Запрашиваем разрешение
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alaram, container, false);

        btnAddAlarm = view.findViewById(R.id.btnAddAlarm);
        alarmListView = view.findViewById(R.id.alarmListView);

        adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, alarms);
        alarmListView.setAdapter(adapter);

        alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        btnAddAlarm.setOnClickListener(v -> showAddAlarmDialog());

        alarmListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            removeAlarm(position);
            return true;
        });

        return view;
    }

    private void showAddAlarmDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_alarm, null);

        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        new AlertDialog.Builder(requireContext())
                .setTitle("Установить будильник")
                .setView(dialogView)
                .setPositiveButton("Установить", (dialog, which) -> {
                    int hour = timePicker.getCurrentHour();
                    int minute = timePicker.getCurrentMinute();

                    setAlarm(hour, minute);
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                    alarms.add(time);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Если время уже прошло сегодня, установить на завтра
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent pendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(
                    getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getBroadcast(
                    getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        // Для Android 6.0+ используем setExactAndAllowWhileIdle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), pendingIntent);
        }
        // Для Android 4.4+ используем setExact
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), pendingIntent);
        }
        // Для старых версий
        else {
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), pendingIntent);
        }

        Toast.makeText(getContext(),
                "Будильник установлен на " + String.format(Locale.getDefault(), "%02d:%02d", hour, minute),
                Toast.LENGTH_SHORT).show();
    }

    private void removeAlarm(int position) {
        // Отменяем будильник
        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

        alarms.remove(position);
        adapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Будильник удален", Toast.LENGTH_SHORT).show();
    }
}