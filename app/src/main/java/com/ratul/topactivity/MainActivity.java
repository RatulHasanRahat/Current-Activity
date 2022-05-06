package com.ratul.topactivity;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.provider.*;
import android.view.*;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.ratul.fancy.*;
import android.app.*;
import android.content.pm.*;
import android.graphics.drawable.*;
import android.graphics.*;
import android.text.*;
import android.view.accessibility.AccessibilityManager;
import android.accessibilityservice.AccessibilityService;
import java.util.List;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.hardware.display.DisplayManager;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
public class MainActivity extends Activity implements OnCheckedChangeListener {
    public static final String EXTRA_FROM_QS_TILE = "from_qs_tile";
    public static final String ACTION_STATE_CHANGED = "com.ratul.topactivity.ACTION_STATE_CHANGED";
    CompoundButton mWindowSwitch, mNotificationSwitch, mAccessibilitySwitch;
    private BroadcastReceiver mReceiver;
    private int theme;
    public static MainActivity INSTANCE;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        INSTANCE = this;
        if (AccessibilityWatcher.getInstance() == null && SharedPrefsUtil.hasAccess(this))
            startService(new Intent().setClass(this, AccessibilityWatcher.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        theme = FancyDialog.DARK_THEME;
        ColorSetup.setupColors(this, theme);

        SpannableString s = new SpannableString(getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "fonts/google_sans_bold.ttf"), 0, s.length(),
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(s);

        TextView showWindow = findViewById(R.id.showWindoww);
        TextView disaccess = findViewById(R.id.disableAccess);
        TextView disableNltification = findViewById(R.id.disableNotif);
        disaccess.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/google_sans_regular.ttf"));
        showWindow.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/google_sans_regular.ttf"));
        disableNltification.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/google_sans_regular.ttf"));
        disaccess.setTextColor(ColorSetup.messageColor);
        showWindow.setTextColor(ColorSetup.messageColor);
        disableNltification.setTextColor(ColorSetup.messageColor);

        mWindowSwitch = findViewById(R.id.sw_window);
        mWindowSwitch.setOnCheckedChangeListener(this);
        if (Build.VERSION.SDK_INT < 24) {
            findViewById(R.id.useNotificationPref).setVisibility(View.GONE);
            findViewById(R.id.divider_useNotificationPref).setVisibility(View.GONE);
        }
        mNotificationSwitch = findViewById(R.id.sw_notification);
        if (mNotificationSwitch != null) {
            mNotificationSwitch.setOnCheckedChangeListener(this);
        }
        mAccessibilitySwitch = findViewById(R.id.sw_accessibility);
        if (mAccessibilitySwitch != null) {
            mAccessibilitySwitch.setOnCheckedChangeListener(this);
        }
        if (getIntent().getBooleanExtra(EXTRA_FROM_QS_TILE, false)) {
            mWindowSwitch.setChecked(true);
        }
        mReceiver = new UpdateSwitchReceiver();
        registerReceiver(mReceiver, new IntentFilter(ACTION_STATE_CHANGED));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getIntent().getBooleanExtra(EXTRA_FROM_QS_TILE, false)) {
            mWindowSwitch.setChecked(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshWindowSwitch();
        refreshNotificationSwitch();
        refreshAccessibilitySwitch();
        NotificationMonitor.cancelNotification(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SharedPrefsUtil.isShowWindow(this)) {
            NotificationMonitor.showNotification(this, false);
        }
    }

    private void refreshWindowSwitch() {
        mWindowSwitch.setChecked(SharedPrefsUtil.isShowWindow(this));
        if (SharedPrefsUtil.hasAccess(this) && AccessibilityWatcher.getInstance() == null) {
            mWindowSwitch.setChecked(false);
        }
    }

    private void refreshAccessibilitySwitch() {
        if (mAccessibilitySwitch != null) {
            mAccessibilitySwitch.setChecked(SharedPrefsUtil.hasAccess(this));
        }
    }

    private void refreshNotificationSwitch() {
        if (mNotificationSwitch != null) {
            mNotificationSwitch.setChecked(!SharedPrefsUtil.isNotificationToggleEnabled(this));
        }
    }

    public void showToast(String str, int length) {
        Toast.makeText(this, str, length).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("GitHub Repo").setIcon(R.drawable.ic_github).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add("About App");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final FancyDialog fancy = new FancyDialog(this, theme);
        String title = item.getTitle().toString();
        if (title.equals("About App")) {
            fancy.setTitle("About App");
            fancy.setMessage("Current Activity 1.5.5\nAn useful tool for Android Developers & Reversers, which shows the package name and class name of current activity which you are in.\n\nWhat's new in 1.5.5:\n1. Added option to disable accessibility usage if needed (Not Recommended)\n2. Disabling notification is stable in this update\n3. Updated UI");
            fancy.setNegativeButton("Close", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fancy.dismiss();
                    }
                });
            fancy.setCancelable(false);
            fancy.show();
        } else if (title.equals("GitHub Repo")) {
            fancy.setTitle("GitHub Repo");
            fancy.setMessage("It is an open source project. Would you like to visit the official github repo of this app");
            fancy.setPositiveButton("Yes", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fancy.dismiss();
                        startActivity(new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/ratulhasanrahat/Current-Activity")));
                    }
                });
            fancy.setNegativeButton("No", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fancy.dismiss();
                    }
                });
            fancy.setCancelable(false);
            fancy.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mNotificationSwitch) {
            SharedPrefsUtil.setNotificationToggleEnabled(this, !isChecked);
            buttonView.setChecked(isChecked);
            return;
        }
        if (buttonView == mAccessibilitySwitch) {
            SharedPrefsUtil.setHasAccess(this, isChecked);
            buttonView.setChecked(isChecked);
            if(isChecked && AccessibilityWatcher.getInstance() == null)
                startService(new Intent().setClass(this, AccessibilityWatcher.class));
            return;
        }
        if (isChecked && buttonView == mWindowSwitch) {
            if (Build.VERSION.SDK_INT >= 29 && !((PowerManager) getSystemService("power")).isIgnoringBatteryOptimizations(getPackageName())) {
                setupBattery();
                SharedPrefsUtil.setHasBattery(this, true);
                return;
            }
            if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                final FancyDialog fancy = new FancyDialog(MainActivity.this, theme);
                fancy.setTitle("Overlay Permission");
                fancy.setMessage("Please enable overlay permission to show window over other apps");
                fancy.setCancelable(false);
                fancy.setPositiveButton("Allow", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                            fancy.dismiss();
                        }
                    });
                fancy.setNegativeButton("Close", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fancy.dismiss();
                        }
                    });
                fancy.show();
                onCheckedChanged(buttonView, false);
                return;
            }
            if (SharedPrefsUtil.hasAccess(this) && AccessibilityWatcher.getInstance() == null) {
                final FancyDialog fancy = new FancyDialog(MainActivity.this, theme);
                fancy.setTitle("Accessibility Permission");
                fancy.setMessage("Enable my Accessibility Service in order to get current activity info");
                fancy.setCancelable(false);
                fancy.setPositiveButton("Allow", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent();
                            intent.setAction("android.settings.ACCESSIBILITY_SETTINGS");
                            startActivity(intent);
                            fancy.dismiss();
                        }
                    });
                fancy.setNegativeButton("Close", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SharedPrefsUtil.setHasAccess(MainActivity.this, false);
                            fancy.dismiss();
                        }
                    });
                fancy.show();
                onCheckedChanged(buttonView, false);
                return;
            }
            if (!usageStats(MainActivity.this)) {
                final FancyDialog fancy = new FancyDialog(MainActivity.this, theme);
                fancy.setTitle("Usage Access");
                fancy.setMessage("Enable my Usage Access permission in order to get current activity info");
                fancy.setCancelable(false);
                fancy.setPositiveButton("Allow", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent();
                            intent.setAction("android.settings.USAGE_ACCESS_SETTINGS");
                            startActivity(intent);
                            fancy.dismiss();
                        }
                    });
                fancy.setNegativeButton("Close", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fancy.dismiss();
                        }
                    });
                fancy.show();
                onCheckedChanged(buttonView, false);
                return;
            }
        }
        if (buttonView == mWindowSwitch) {
            SharedPrefsUtil.setAppInitiated(this, true);
            SharedPrefsUtil.setIsShowWindow(this, isChecked);
            if (!isChecked) {
                WindowUtility.dismiss(this);
            } else {
                WindowUtility.show(this, getPackageName(), getClass().getName());
                startService(new Intent(this, MonitoringService.class));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    class UpdateSwitchReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshWindowSwitch();
            refreshNotificationSwitch();
            refreshAccessibilitySwitch();
        }
    }

    public static boolean usageStats(Context context) {
        boolean granted = false;
        AppOpsManager appOps = (AppOpsManager) context.
            getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, 
                                         android.os.Process.myUid(), context.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        return granted;
    }

    public void setupBattery() {
        final FancyDialog fancy = new FancyDialog(this, theme);
        fancy.setTitle("Battery Optimizations");
        fancy.setMessage("Google has blocked clipboard service from being accessed from background in Android 10 and higher devices. So, remove any battery optimization from this app to ensure it can access clipboard from background without restriction");
        fancy.setPositiveButton("Ok", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fancy.dismiss();
                    Intent intent = new Intent();
                    intent.setAction("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS");
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            });
        fancy.setNegativeButton("Close", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fancy.dismiss();
                }
            });
        fancy.setCancelable(false);
        fancy.show();

    }
}
