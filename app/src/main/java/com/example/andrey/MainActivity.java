package com.example.andrey;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.example.andrey.R;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private TextView tvTime, tvSeconds, tvDate, tvDay, tvEpoch, tvAmPm;
    private Button btnFormat, btnTheme;
    private boolean is24HourFormat = true;
    private boolean isDarkTheme = true;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "ClockPrefs";
    private static final String KEY_THEME = "isDarkTheme";
    private Spinner spinnerTimeZone;
    private String selectedTimeZone = "GMT+3"; // По умолчанию Москва
    private SimpleDateFormat timeFormat, secondsFormat, dateFormat, dayFormat;

    private boolean isAlarmFragmentVisible = false;
    private FrameLayout fragmentContainer;
    private LinearLayout mainContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isDarkTheme = prefs.getBoolean(KEY_THEME, true);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        fragmentContainer = findViewById(R.id.fragmentContainer);
        mainContent = findViewById(R.id.mainContent);

        // Изначально скрываем контейнер фрагментов
        fragmentContainer.setVisibility(View.GONE);

        // Скрываем ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }



        View rootView = findViewById(R.id.mainLayout);

        // Инициализация элементов
        initViews();

        // Запуск обновления времени
        updateTime();

        // Обработчики кнопок
        setupButtonListeners();
    }


    private void initViews() {
        tvTime = findViewById(R.id.tvTime);
        tvSeconds = findViewById(R.id.tvSeconds);
        tvDate = findViewById(R.id.tvDate);
        tvDay = findViewById(R.id.tvDay);
        tvEpoch = findViewById(R.id.tvEpoch);
        tvAmPm = findViewById(R.id.tvAmPm);
        btnFormat = findViewById(R.id.btnFormat);
        btnTheme = findViewById(R.id.btnTheme);
    }

    private void setupButtonListeners() {
        btnFormat.setOnClickListener(v -> {
            is24HourFormat = !is24HourFormat;
            btnFormat.setText(is24HourFormat ? "24H" : "12H");
            animateButton(v);
            updateTime();

        });

        btnTheme.setOnClickListener(v -> {
            isDarkTheme = !isDarkTheme;
            applyTheme();
            animateButton(v);
        });
        Button btnAlarm = findViewById(R.id.btnAlarm);
        btnAlarm.setOnClickListener(v -> {
            if (isAlarmFragmentVisible) {
                setupTimeZoneSpinner();
                hideAlarmFragment();
            } else {
                showAlarmFragment();
            }
        });
    }
    private void setupTimeZoneSpinner() {
        // Массив часовых поясов
        String[] timeZones = {
                "Москва (GMT+3)",
                "Лондон (GMT+0)",
                "Нью-Йорк (GMT-5)",
                "Токио (GMT+9)",
                "Сидней (GMT+11)",
                "Пекин (GMT+8)"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                timeZones
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeZone.setAdapter(adapter);

        // Обработчик выбора
        spinnerTimeZone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                updateTimeZone(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    // Метод обновления часового пояса
    private void updateTimeZone(String timeZoneStr) {
        // Парсим выбранный часовой пояс
        if (timeZoneStr.contains("Москва")) selectedTimeZone = "GMT+3";
        else if (timeZoneStr.contains("Лондон")) selectedTimeZone = "GMT+0";
        else if (timeZoneStr.contains("Нью-Йорк")) selectedTimeZone = "GMT-5";
        else if (timeZoneStr.contains("Токио")) selectedTimeZone = "GMT+9";
        else if (timeZoneStr.contains("Сидней")) selectedTimeZone = "GMT+11";
        else if (timeZoneStr.contains("Пекин")) selectedTimeZone = "GMT+8";

        // Принудительно обновляем время
        updateClock();
    }
    // Обновленный метод updateClock() с поддержкой часовых поясов:
    private void updateClock() {
        Date now = new Date();

        // Создаем форматеры с учетом часового пояса
        TimeZone timeZone = TimeZone.getTimeZone(selectedTimeZone);

        timeFormat = new SimpleDateFormat(is24HourFormat ? "HH:mm" : "hh:mm", Locale.getDefault());
        timeFormat.setTimeZone(timeZone);

        secondsFormat = new SimpleDateFormat(":ss", Locale.getDefault());
        secondsFormat.setTimeZone(timeZone);

        dateFormat = new SimpleDateFormat("d MMMM, EEEE", new Locale("ru"));
        dateFormat.setTimeZone(timeZone);

        dayFormat = new SimpleDateFormat("d", Locale.getDefault());
        dayFormat.setTimeZone(timeZone);

        // Время Unix (всегда по UTC)
        long epochSeconds = now.getTime() / 1000;

        // AM/PM
        if (!is24HourFormat) {
            SimpleDateFormat amPmFormat = new SimpleDateFormat("a", Locale.getDefault());
            amPmFormat.setTimeZone(timeZone);
            tvAmPm.setText(amPmFormat.format(now));
            tvAmPm.setVisibility(View.VISIBLE);
        } else {
            tvAmPm.setVisibility(View.GONE);
        }

        // Установка значений
        tvTime.setText(timeFormat.format(now));
        tvSeconds.setText(secondsFormat.format(now));
        tvDate.setText(dateFormat.format(now));
        tvDay.setText(dayFormat.format(now));
        tvEpoch.setText(String.valueOf(epochSeconds));
    }
    private void showAlarmFragment() {
        isAlarmFragmentVisible = true;

        // Скрываем основной контент часов
        mainContent.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);

        // Добавляем фрагмент
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        AlaramFragment alarmFragment = new AlaramFragment();
        transaction.replace(R.id.fragmentContainer, alarmFragment, "AlarmFragment");
        transaction.addToBackStack(null);
        transaction.commit();

        // Меняем текст кнопки (способ для любой кнопки)
        MaterialButton btnAlarm = findViewById(R.id.btnAlarm);
        btnAlarm.setText("<Часы");



    }
    private void hideAlarmFragment() {
        isAlarmFragmentVisible = false;

        // Показываем основной контент
        mainContent.setVisibility(View.VISIBLE);
        fragmentContainer.setVisibility(View.GONE);

        // Убираем фрагмент из back stack
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }

        // Восстанавливаем кнопку
        MaterialButton btnAlarm = findViewById(R.id.btnAlarm);
        btnAlarm.setText("Будильник");


    }
    @Override
    public void onBackPressed() {
        if (isAlarmFragmentVisible) {
            hideAlarmFragment();
        } else {
            super.onBackPressed();
        }
    }
    // Добавьте кнопку для показа/скрытия Spinner
    private void setupTimeZoneButton() {
        // В activity_main.xml добавьте кнопку рядом с другими кнопками:
    /*
    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnTimeZone"
        android:layout_width="wrap_content"
        android:layout_height="55dp"
        android:text="🌍"
        android:textSize="18sp"
        app:backgroundTint="#2C5364"/>
    */

        MaterialButton btnTimeZone = findViewById(R.id.btnTimeZone);
        btnTimeZone.setOnClickListener(v -> {
            if (spinnerTimeZone.getVisibility() == View.VISIBLE) {
                spinnerTimeZone.setVisibility(View.GONE);
                btnTimeZone.setText("🌍");
            } else {
                spinnerTimeZone.setVisibility(View.VISIBLE);
                btnTimeZone.setText("✕");
            }
        });
    }

    private void applyTheme() {
        View rootView = findViewById(R.id.mainLayout);

        if (isDarkTheme) {
            // Тёмная тема
            rootView.setBackgroundResource(R.drawable.background_gradient);

            // Цвета текста для тёмной темы
            tvTime.setTextColor(Color.WHITE);
            tvSeconds.setTextColor(Color.parseColor("#B3FFFFFF")); // Полупрозрачный белый
            tvDate.setTextColor(Color.WHITE);
            tvDay.setTextColor(Color.WHITE);
            tvEpoch.setTextColor(Color.parseColor("#80FFFFFF"));
            tvAmPm.setTextColor(Color.parseColor("#B3FFFFFF"));

            btnTheme.setText("Светлая");

            // Цвет кнопок для тёмной темы
            btnFormat.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2C5364")));
            btnTheme.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2C5364")));

        } else {
            // Светлая тема
            rootView.setBackgroundColor(Color.WHITE);

            // Цвета текста для светлой темы
            tvTime.setTextColor(Color.BLACK);
            tvSeconds.setTextColor(Color.parseColor("#80000000")); // Полупрозрачный чёрный
            tvDate.setTextColor(Color.BLACK);
            tvDay.setTextColor(Color.BLACK);
            tvEpoch.setTextColor(Color.parseColor("#60000000"));
            tvAmPm.setTextColor(Color.parseColor("#80000000"));

            btnTheme.setText("Тёмная");

            // Цвет кнопок для светлой темы
            btnFormat.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            btnTheme.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_THEME, isDarkTheme);
            editor.apply();
        }
    }

    private void animateButton(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1.1f, 1f);
        animator.setDuration(200);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    private void updateTime() {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000); // Обновляем каждую секунду

                    runOnUiThread(() -> {
                        Date now = new Date();

                        // Форматирование времени
                        SimpleDateFormat timeFormat;
                        if (is24HourFormat) {
                            timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            tvAmPm.setVisibility(View.GONE);
                        } else {
                            timeFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
                            tvAmPm.setVisibility(View.VISIBLE);
                            SimpleDateFormat amPmFormat = new SimpleDateFormat("a", Locale.getDefault());
                            tvAmPm.setText(amPmFormat.format(now));
                        }

                        // Секунды
                        SimpleDateFormat secondsFormat = new SimpleDateFormat(":ss", Locale.getDefault());

                        // Дата
                        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM, EEEE", new Locale("ru"));

                        // День месяца
                        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());

                        // Эпоха
                        long epochSeconds = now.getTime() / 1000;

                        // Применение анимации
                        animateTimeChange();

                        // Установка значений
                        tvTime.setText(timeFormat.format(now));
                        tvSeconds.setText(secondsFormat.format(now));
                        tvDate.setText(dateFormat.format(now));
                        tvDay.setText(dayFormat.format(now));
                        tvEpoch.setText(String.valueOf(epochSeconds));
                    });

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void animateTimeChange() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(tvTime, "alpha", 0.7f, 1f);
        animator.setDuration(300);
        animator.start();
    }
}