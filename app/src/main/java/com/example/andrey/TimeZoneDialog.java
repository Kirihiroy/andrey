package com.example.andrey;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class TimeZoneDialog {

    private Context context;
    private TimeZoneSelectListener listener;

    public interface TimeZoneSelectListener {
        void onTimeZoneSelected(String timeZoneId);
    }

    public TimeZoneDialog(Context context, TimeZoneSelectListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void show() {
        List<String> timeZones = new ArrayList<>();
        String[] allTimeZones = TimeZone.getAvailableIDs();

        // Выбираем популярные часовые пояса
        String[] popularTimeZones = {
                "Europe/Moscow",    // Москва
                "Europe/London",    // Лондон
                "Europe/Paris",     // Париж
                "America/New_York", // Нью-Йорк
                "America/Los_Angeles", // Лос-Анджелес
                "Asia/Tokyo",       // Токио
                "Asia/Shanghai",    // Шанхай
                "Australia/Sydney", // Сидней
                "Asia/Dubai",       // Дубай
                "Asia/Kolkata"      // Калькутта
        };

        for (String tz : popularTimeZones) {
            TimeZone timeZone = TimeZone.getTimeZone(tz);
            timeZones.add(timeZone.getID() + " (" + timeZone.getDisplayName() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_list_item_1, timeZones);

        ListView listView = new ListView(context);
        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Выберите часовой пояс")
                .setView(listView)
                .setNegativeButton("Отмена", null)
                .create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = popularTimeZones[position];
            if (listener != null) {
                listener.onTimeZoneSelected(selected);
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}