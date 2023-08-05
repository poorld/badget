package com.poorld.badget;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.poorld.badget.activity.SelectAppAct;
import com.poorld.badget.app.MyApp;
import com.poorld.badget.utils.ConfigUtils;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity#badget";

    private MaterialToolbar toolbar;
    private MaterialCardView statusCard;
    private ImageView moduleStatusIcon;
    private MaterialTextView moduleStatus;
    private MaterialTextView filterCount;

    private MaterialCardView initCard;

    private MaterialButton initButton;

    private FloatingActionButton fabSelectApp;

    private static final int MSG_INIT_SUCCESS = 101;
    private static final int MSG_INIT_FAILED = 102;

    private Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_INIT_SUCCESS:
                    Toast.makeText(MainActivity.this, "初始化成功", Toast.LENGTH_SHORT).show();
                    checkInited();
                    break;
                case MSG_INIT_FAILED:
                    Toast.makeText(MainActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (MaterialToolbar) findViewById(R.id.toolbar);
        statusCard = (MaterialCardView) findViewById(R.id.status_card);
        moduleStatusIcon = (ImageView) findViewById(R.id.module_status_icon);
        moduleStatus = (MaterialTextView) findViewById(R.id.module_status);
        filterCount = (MaterialTextView) findViewById(R.id.filter_count);
        initCard = (MaterialCardView) findViewById(R.id.init_card);
        initButton = (MaterialButton) findViewById(R.id.btn_init);
        initButton.setOnClickListener( v -> {
            initBadget();
        });
        fabSelectApp = (FloatingActionButton) findViewById(R.id.fab_select_app);
        fabSelectApp.setOnClickListener(v -> startActivity(new Intent(this, SelectAppAct.class)));




        //requestPermission();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MyApp.isModuleActive) {
            statusCard.setCardBackgroundColor(getColor(R.color.md_theme_light_primary));
            statusCard.setOutlineAmbientShadowColor(getColor(R.color.md_theme_light_primary));
            statusCard.setOutlineSpotShadowColor(getColor(R.color.md_theme_light_primary));

            moduleStatusIcon.setImageDrawable(getDrawable(R.drawable.outline_done_all_24));
            String activatedText = getText(R.string.home_xposed_activated).toString();
            moduleStatus.setText(String.format(activatedText, getVersionName()));
            moduleStatus.setTextColor(getColor(R.color.md_theme_light_onPrimary));
            filterCount.setVisibility(View.VISIBLE);
            //filterCount.setText();
        } else {
            statusCard.setCardBackgroundColor(getColor(R.color.md_theme_light_errorContainer));
            statusCard.setOutlineAmbientShadowColor(getColor(R.color.md_theme_light_errorContainer));
            statusCard.setOutlineSpotShadowColor(getColor(R.color.md_theme_light_errorContainer));

            moduleStatusIcon.setImageDrawable(getDrawable(R.drawable.baseline_info_24));
            moduleStatus.setText(getText(R.string.home_xposed_not_activated));
            moduleStatus.setTextColor(getColor(R.color.md_theme_light_onSecondaryContainer));
            filterCount.setVisibility(View.GONE);
        }

        checkInited();
    }

    private void initBadget() {

        new Thread(() -> {
            // /data/user/0/com.poorld.badget/app_cache
            File cache = getDir("cache", Context.MODE_PRIVATE);
            Log.d(TAG, "copy: " + cache.getPath());

            int result = ConfigUtils.firstInit(getApplicationContext(), ConfigUtils.ASSETS_BADGET_PATH, cache.getPath());

            if (result == 0) {
                mainHandler.sendEmptyMessage(MSG_INIT_SUCCESS);
            } else {
                mainHandler.sendEmptyMessage(MSG_INIT_FAILED);
            }
        }).start();


    }

    private void checkInited() {
        boolean badgetSoExists = ConfigUtils.checkBadgetSoExists();
        if (badgetSoExists) {
            initCard.setVisibility(View.GONE);
        }
        boolean badgetConfigExists = ConfigUtils.checkBadgetConfigExists();
        if (badgetConfigExists) {
            ConfigUtils.initConfig();
        }
    }

    public String getVersionName() {
        PackageManager manager = getPackageManager();
        String code = "1.0.0";
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            code = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }

    private boolean isModuleActivated() {
        return false;
    }



    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (Environment.isExternalStorageManager()) {
                //FileUtils.init();
            } else {
                Intent intent = new Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION");
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 9527);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 先判断有没有权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //FileUtils.init();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 9527);
            }
        } else {
            //FileUtils.init();
        }
    }
}