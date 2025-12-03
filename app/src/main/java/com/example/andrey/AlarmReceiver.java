package com.example.andrey;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Воспроизводим звук
        playAlarmSound(context);

        // Включаем вибрацию
        vibrate(context);

        // Показываем уведомление (с проверкой разрешений для Android 13+)
        showNotification(context);

        Toast.makeText(context, "Будильник!", Toast.LENGTH_LONG).show();
    }

    private void playAlarmSound(Context context) {
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
            if (ringtone != null) {
                ringtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Если не удалось воспроизвести стандартный звук, используем системный beep
            try {
                Uri defaultRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone defaultRingtonePlayer = RingtoneManager.getRingtone(context, defaultRingtone);
                if (defaultRingtonePlayer != null) {
                    defaultRingtonePlayer.play();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void vibrate(Context context) {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(1000,
                            VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(1000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNotification(Context context) {
        try {
            // Создаем Intent для открытия приложения при нажатии на уведомление
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getActivity(context, 0, intent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getActivity(context, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }

            // Создаем уведомление
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarm_channel")
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle("Будильник")
                    .setContentText("Время проснуться!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setVibrate(new long[]{0, 1000, 500, 1000});

            // Показываем уведомление
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                // Для Android 13+ проверяем разрешение
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // В BroadcastReceiver мы не можем запросить разрешение,
                    // поэтому просто пытаемся показать уведомление
                    notificationManager.notify(1, builder.build());
                } else {
                    // Для версий ниже Android 13 показываем без проверки
                    notificationManager.notify(1, builder.build());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Ошибка при показе уведомления", Toast.LENGTH_SHORT).show();
        }
    }
}