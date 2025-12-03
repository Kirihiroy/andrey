package com.example.andrey;

import android.provider.AlarmClock;
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
    private static final int ALARM_REQUEST_CODE = 1001;

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

        btnAddAlarm.setOnClickListener(v -> {
            // Проверяем разрешение перед установкой будильника
            if (checkAlarmPermission()) {
                showAddAlarmDialog();
            }
        });

        alarmListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            removeAlarm(position);
            return true;
        });

        return view;
    }

    private boolean checkAlarmPermission() {
        // Для Android 12+ (API 31+) проверяем разрешение на точные будильники
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Показываем диалог с объяснением
                showPermissionDialog();
                return false;
            }
        }
        return true;
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Разрешение требуется")
                .setMessage("Для установки точных будильников необходимо разрешение. " +
                        "Нажмите 'Разрешить', чтобы перейти в настройки.")
                .setPositiveButton("Разрешить", (dialog, which) -> {
                    // Открываем настройки для запроса разрешения
                    requestAlarmPermission();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void requestAlarmPermission() {
        try {
            // Для Android 12+ открываем системные настройки
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivityForResult(intent, ALARM_REQUEST_CODE);
            }
        } catch (Exception e) {
            // Если действие недоступно (на некоторых устройствах)
            Toast.makeText(getContext(),
                    "Не удалось открыть настройки разрешений", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ALARM_REQUEST_CODE) {
            // Пользователь вернулся из настроек
            Toast.makeText(getContext(),
                    "Проверьте разрешение в настройках", Toast.LENGTH_SHORT).show();
        }
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

                    // Устанавливаем будильник
                    boolean success = setAlarm(hour, minute);
                    if (success) {
                        String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                        alarms.add(time);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private boolean setAlarm(int hour, int minute) {
        try {
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
                        getContext(), generateRequestCode(hour, minute), intent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getBroadcast(
                        getContext(), generateRequestCode(hour, minute), intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }

            // Используем разные методы в зависимости от версии Android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Для Android 12+ сначала проверяем разрешение
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    // Если разрешения нет, используем неточный будильник
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    Toast.makeText(getContext(),
                            "Будильник установлен (неточный, требуется разрешение)",
                            Toast.LENGTH_LONG).show();
                    return true;
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Для Android 6.0-11
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // Для Android 4.4-5.1
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                // Для старых версий
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }

            Toast.makeText(getContext(),
                    "Будильник установлен на " + String.format(Locale.getDefault(), "%02d:%02d", hour, minute),
                    Toast.LENGTH_SHORT).show();
            return true;

        } catch (SecurityException e) {
            // Ловим SecurityException и предлагаем пользователю разрешение
            Toast.makeText(getContext(),
                    "Ошибка: требуется разрешение для точных будильников",
                    Toast.LENGTH_LONG).show();
            showPermissionDialog();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(),
                    "Ошибка при установке будильника: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void removeAlarm(int position) {
        try {
            // Удаляем из списка
            String alarmTime = alarms.get(position);

            // Создаем такой же Intent для отмены
            Intent intent = new Intent(getContext(), AlarmReceiver.class);

            // Извлекаем час и минуту из строки
            String[] parts = alarmTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getBroadcast(
                        getContext(), generateRequestCode(hour, minute), intent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
            } else {
                pendingIntent = PendingIntent.getBroadcast(
                        getContext(), generateRequestCode(hour, minute), intent,
                        PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            }

            // Отменяем будильник
            alarmManager.cancel(pendingIntent);

            // Удаляем из списка
            alarms.remove(position);
            adapter.notifyDataSetChanged();

            Toast.makeText(getContext(), "Будильник удален", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка при удалении будильника", Toast.LENGTH_SHORT).show();
        }
    }

    // Генерируем уникальный requestCode на основе времени
    private int generateRequestCode(int hour, int minute) {
        return hour * 100 + minute;
    }
}