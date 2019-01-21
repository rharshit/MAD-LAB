package com.rharshit.winddown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rharshit.winddown.Camera.Camera;
import com.rharshit.winddown.Contacts.Contacts;
import com.rharshit.winddown.Gallery.Gallery;
import com.rharshit.winddown.Messages.Messages;
import com.rharshit.winddown.Music.Music;
import com.rharshit.winddown.Phone.Phone;
import com.rharshit.winddown.UI.AppIcon;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";
    private Context mContext;

    private TextView tvNotification;
    private NotificationReceiver nReceiver;

    private static int theme = R.style.AppThemeLight;
    private TextView tvWindDown;

    private HorizontalScrollView hsView;
    private LinearLayout llScroll;
    private LinearLayout llMainScroll;

    private int vHeight;
    private int vWidth;

    private int nChild;

    private void init(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        vHeight = displayMetrics.heightPixels;
        vWidth = displayMetrics.widthPixels;

        hsView = findViewById(R.id.hsMainScrollView);
        llScroll = findViewById(R.id.llHorizintalScroll);
        llMainScroll = findViewById(R.id.llVerticalScroll);
        tvWindDown = findViewById(R.id.tvWindDown);

        tvNotification = new TextView(mContext);
        tvNotification.setText("Notifications");
        llMainScroll.addView(tvNotification);

        tvWindDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Switching");
                getNotifications();
                theme = theme == R.style.AppThemeLight ?
                        R.style.AppThemeDark : R.style.AppThemeLight;
                recreate();
            }
        });
    }

    private void notificationListener() {
        nReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rharshit.winddown.NOTIFICATION_LISTENER");
        registerReceiver(nReceiver, filter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        init();
        populate();
        notificationListener();

        debug();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nReceiver);
    }

    private void getNotifications() {
        Intent i = new Intent("com.rharshit.winddown.NOTIFICATION_LISTENER_SERVICE");
        i.putExtra("EXTRA_ACTION", "getNotificaitons");
        sendBroadcast(i);
    }

    private void debug() {
        nChild = llScroll.getChildCount();
        Log.d(TAG, "width: " + vWidth);
        hsView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                for (int i = 0; i < nChild; i++) {
                    AppIcon tmp = (AppIcon) llScroll.getChildAt(i);
                    tmp.updatePos();
                    tmp.updateView();
                }
                Log.d(TAG, "onScrollChange: " + String.valueOf(scrollX) + " " + String.valueOf(oldScrollX));
                int d = oldScrollX - scrollX;
                d = d < 0 ? -d : d;
                if (d < 5) {
                    hsView.smoothScrollTo(((scrollX + (vWidth / 2)) / vWidth) * vWidth, scrollY);
                }
            }
        });
    }

    private void populate(){
        llScroll.addView(
                new AppIcon(this, vWidth, vHeight,
                    getResources().getDrawable(R.drawable.ic_phone), "Phone", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, Phone.class);
                    startActivity(i);
                }
            })
        );
        llScroll.addView(
                new AppIcon(this, vWidth, vHeight,
                        getResources().getDrawable(R.drawable.ic_contacts), "Contacts", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, Contacts.class);
                        startActivity(i);
                    }
                })
        );
        llScroll.addView(
                new AppIcon(this, vWidth, vHeight,
                        getResources().getDrawable(R.drawable.ic_message), "Messages", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, Messages.class);
                        startActivity(i);
                    }
                })
        );
        llScroll.addView(
                new AppIcon(this, vWidth, vHeight,
                        getResources().getDrawable(R.drawable.ic_camera), "Camera", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, Camera.class);
                        startActivity(i);
                    }
                })
        );
        llScroll.addView(
                new AppIcon(this, vWidth, vHeight,
                        getResources().getDrawable(R.drawable.ic_gallery), "Gallery", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, Gallery.class);
                        startActivity(i);
                    }
                })
        );
        llScroll.addView(
                new AppIcon(this, vWidth, vHeight,
                        getResources().getDrawable(R.drawable.ic_music), "Music", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, Music.class);
                        startActivity(i);
                    }
                })
        );
    }

    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + intent.getStringExtra("NOTIFICATION_EVENT"));
            TextView tvNotif = new TextView(mContext);
            tvNotif.setText(intent.getStringExtra("NOTIFICATION_EVENT"));
            llMainScroll.addView(tvNotif);
        }
    }
}
