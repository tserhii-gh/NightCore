package home.tunote.nightcore;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlarmManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextClock;
import android.widget.TextView;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Nightcore MainActivity";
    //    private float curBrightnessValue = 0.0f;
//    private GestureDetectorCompat gestureDetectorCompat;
    TextClock textClock;
    private GestureDetector gestureDetector;
    private ContentResolver cResolver;
    private RelativeLayout swipeLayout;
    private SeekBar seekBarBrightness;
    private SeekBar seekBarAlpha;
    private int clockColorAlphaChannel;
    private boolean visible = false;
    private long delay = 0;
//    private boolean showClockSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settingPermission();
        cResolver = getContentResolver();
        final ImageView alarm_image = findViewById(R.id.alarm_image);
        final TextView next_alarm = findViewById(R.id.next_alarm_text);
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager!=null) {
            AlarmManager.AlarmClockInfo clockInfo = alarmManager.getNextAlarmClock();
            if (clockInfo!=null){
                long nextAlarmTime = alarmManager.getNextAlarmClock().getTriggerTime();
                Date nextAlarmDate = new Date(nextAlarmTime);
                next_alarm.setText(nextAlarmDate.toString());
                alarm_image.setVisibility(View.VISIBLE);
                next_alarm.setVisibility(View.VISIBLE);
            }
//
        }

//        Log.e(TAG, "Alart: " + nextAlarmDate + ", Time: " + nextAlarmTime);
//        Drawable alarm_img = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_access_alarm_black_24dp, null);

//        next_alarm.setCompoundDrawablesWithIntrinsicBounds(alarm_img, null, null,null);

        alarm_image.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorClock));
        initSettings();

        seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                android.provider.Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                showDelay();
                toggleSettingsSlider();
            }
        });

        seekBarAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (50 <= progress && progress <= 255) {
                    int color = ResourcesCompat.getColor(getResources(), R.color.colorClock, null);
//                Color.pack(color);
                    int a = Color.alpha(color);
                    int r = Color.red(color);
                    int g = Color.green(color);
                    int b = Color.blue(color);
                    color = (progress << 24) | (r << 16) | (g << 8) | b;
                    textClock.setTextColor(color);
                    next_alarm.setTextColor(color);
                    alarm_image.setColorFilter(color);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                toggleSettingsSlider();
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        textClock = findViewById(R.id.clock);
        Typeface fontface = Typeface.createFromAsset(getAssets(), "fonts/pix.ttf");
        textClock.setTypeface(fontface);
        fullsceenApp();

        findViewById(R.id.clock_layout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                Log.e(TAG, "Touch, Y=" + event.getY());
                delay = 0;
                if (200 <= event.getY() && event.getY() <= 400) {
                    toggleSettingsSlider();
                }
                v.performClick();
                return false;
            }
        });
    }



    private void fullsceenApp() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    private void initSettings() {
        seekBarBrightness = findViewById(R.id.seekBarBrightness);
        seekBarBrightness.setProgress(getCurrentBrightness());
        seekBarAlpha = findViewById(R.id.seekBarAlpha);
        seekBarAlpha.setProgress(getCurrentAlpha());
    }

    public int getCurrentAlpha() {
        int clockColor = ResourcesCompat.getColor(getResources(), R.color.colorClock, null);
        return Color.alpha(clockColor);
    }


    public void toggleSettingsSlider() {
        if (seekBarBrightness.getVisibility() == View.GONE && seekBarAlpha.getVisibility() == View.GONE) {

            seekBarBrightness.animate()
                    .alpha(1.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            seekBarBrightness.clearAnimation();
//                            seekBarAlpha.clearAnimation();
                            seekBarBrightness.setVisibility(View.VISIBLE);
//                            seekBarAlpha.setVisibility(View.VISIBLE);
                        }
                    });
            seekBarAlpha.animate()
                    .alpha(1.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
//                            seekBarBrightness.clearAnimation();
                            seekBarAlpha.clearAnimation();
//                            seekBarBrightness.setVisibility(View.VISIBLE);
                            seekBarAlpha.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            seekBarBrightness.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            seekBarBrightness.clearAnimation();
//                            seekBarAlpha.clearAnimation();
                            seekBarBrightness.setVisibility(View.GONE);
//                            seekBarAlpha.setVisibility(View.GONE);
                        }
                    });
            seekBarAlpha.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            seekBarAlpha.clearAnimation();
//                            seekBarAlpha.clearAnimation();
                            seekBarAlpha.setVisibility(View.GONE);
//                            seekBarAlpha.setVisibility(View.GONE);
                        }
                    });
        }
    }

    public int getCurrentBrightness() {
        int currentBrightness = 0;
        try {
            currentBrightness = android.provider.Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return currentBrightness;
    }

    @Override
    public void onResume(){
        super.onResume();
        fullsceenApp();
    }

    public void settingPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 200);

            }
        }
    }
}
