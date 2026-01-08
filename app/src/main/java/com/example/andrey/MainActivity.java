package com.example.andrey;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.andrey.R;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isDarkTheme = prefs.getBoolean(KEY_THEME, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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