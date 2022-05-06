package com.ratul.topactivity;

import android.annotation.*;
import android.app.*;
import android.app.ActivityManager.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.util.*;

import java.util.*;
import android.app.usage.*;
import android.widget.Toast;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
public class MonitoringService extends Service {
    public boolean serviceAlive = false;
    private boolean firstRun = true;
    public static MonitoringService INSTANCE;
    private UsageStatsManager usageStats;
    public Handler mHandler = new Handler();
    private String text;
    private String text1;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        serviceAlive = true;
        usageStats = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
    }

    @Override
    public void onDestroy() {
        serviceAlive = false;
        super.onDestroy();
    }

    public void getActivityInfo() {
        long currentTimeMillis = System.currentTimeMillis();
        UsageEvents queryEvents = usageStats.queryEvents(currentTimeMillis - (firstRun ? 600000 : 60000), currentTimeMillis);
        while (queryEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            queryEvents.getNextEvent(event);
            switch (event.getEventType()) {
                case UsageEvents.Event.MOVE_TO_FOREGROUND:
                    text = event.getPackageName();
                    text1 = event.getClassName();
                    break;
                case UsageEvents.Event.MOVE_TO_BACKGROUND:
                    if (event.getPackageName().equals(text)) {
                        text = null;
                        text1 = null;
                    }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        INSTANCE = this;
        Runnable runner = new Runnable() {
            @Override
            public void run() {
                if (!SharedPrefsUtil.isShowWindow(MonitoringService.INSTANCE)) {
                    MonitoringService.INSTANCE.mHandler.removeCallbacks(this);
                    MonitoringService.INSTANCE.stopSelf();
                }

                getActivityInfo();
                if (MonitoringService.INSTANCE.text == null)
                    return;

                MonitoringService.INSTANCE.firstRun = false;
                if (SharedPrefsUtil.isShowWindow(MonitoringService.INSTANCE)) {
                    WindowUtility.show(MonitoringService.INSTANCE, MonitoringService.INSTANCE.text, MonitoringService.INSTANCE.text1);
                } else {
                    MonitoringService.INSTANCE.stopSelf();
                }
                mHandler.postDelayed(this, 500);
            }
        };
        
        mHandler.postDelayed(runner, 500);
        return super.onStartCommand(intent, flags, startId);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(),
                                                 this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(
            getApplicationContext(), 1, restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext()
            .getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME,
                         SystemClock.elapsedRealtime() + 500,
                         restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }
}
