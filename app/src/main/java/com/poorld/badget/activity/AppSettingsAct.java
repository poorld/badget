package com.poorld.badget.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
//import androidx.preference.SwitchPreferenceCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.material.appbar.MaterialToolbar;
import com.poorld.badget.R;
import com.poorld.badget.entity.ConfigEntity;
import com.poorld.badget.entity.InteractionType;
import com.poorld.badget.entity.ItemAppEntity;
import com.poorld.badget.utils.CommonUtils;
import com.poorld.badget.utils.ConfigUtils;
import com.poorld.badget.utils.PkgManager;

import java.io.File;

public class AppSettingsAct extends AppCompatActivity {

    public static final String TAG = "AppSettingsAct";
    private static ItemAppEntity mApp;

    private MaterialToolbar toolbar;

    public static final String EXTRA_PKG = "package";

    private static final int REQUEST_CODE_OPEN_ASSIGN = 9527;

    private static String mPackageName;

    public static void startAct(Context context, String pkg) {
        mPackageName = pkg;
        Intent intent = new Intent(context, AppSettingsAct.class);
        intent.putExtra(EXTRA_PKG, pkg);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("badget");

        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(getDrawable(R.drawable.baseline_arrow_back_24));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_OPEN_ASSIGN) {
                File file = CommonUtils.saveFileFromUri(data.getData(), this, ConfigUtils.getBadgetJSPath(mPackageName).getPath());
                if (file.exists()) {
                    Log.d(TAG, "saveFileFromUri success: ");
                    ConfigEntity.PkgConfig pkgConfig = ConfigUtils.getPkgConfig(mPackageName);
                    pkgConfig.setJsPath(file.getPath());
                    pkgConfig.setSoName(ConfigUtils.getRandomName());
                    ConfigUtils.updatePkgConfig();
                }
            }
        }
    }

    public static class AppSettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        private static final String KEY_PREF_APP = "pref_app";
        private static final String KEY_PREF_SWITCH_ENABLE = "pref_switch_enable";
        private static final String KEY_PREF_JS_PATH = "pref_js_path";

        Preference prefApp;
        Preference prefJs;
        SwitchPreference prefEnable;
        private ConfigEntity.PkgConfig pkgConfig;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "AppSettingsFragment#onCreate: ");
            addPreferencesFromResource(R.xml.root_preferences);

            prefApp = findPreference(KEY_PREF_APP);
            prefEnable = findPreference(KEY_PREF_SWITCH_ENABLE);
            prefJs = findPreference(KEY_PREF_JS_PATH);

            Intent intent = getActivity().getIntent();
            if (intent != null) {
                String pkg = intent.getStringExtra(EXTRA_PKG);
                mApp = PkgManager.getItemAppEntity(getActivity(), pkg);
                if (mApp != null) {
                    pkgConfig = ConfigUtils.getPkgConfig(mApp.getPackageName());
                    prefApp.setTitle(mApp.getAppName());
                    prefApp.setSummary(mApp.getPackageName());
                    Drawable drawable = CommonUtils.resizeDrawable(getContext(), mApp.getDrawable(), 30, 30);
                    prefApp.setIcon(drawable);
                    prefEnable.setChecked(pkgConfig.isEnabled());
                    prefEnable.setOnPreferenceChangeListener(this);
                    prefJs.setOnPreferenceClickListener(this);
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mApp != null) {
                String jsPath = ConfigUtils.getPkgConfig(mApp.getPackageName()).getJsPath();
                if (!TextUtils.isEmpty(jsPath)) {
                    prefJs.setSummary(jsPath);
                } else {
                    prefJs.setSummary("无");
                }
            }
        }

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {

        }


        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            Log.d(TAG, "onPreferenceChange: ");

            if (KEY_PREF_SWITCH_ENABLE.equals(preference.getKey())) {
                if (pkgConfig != null) {
                    Log.d(TAG, pkgConfig.getPkgName());
                    pkgConfig.setEnabled((Boolean) newValue);
                    pkgConfig.setAppName(mApp.getAppName());

                    // 暂时只支持Script模式
                    pkgConfig.setType(InteractionType.Script);
                    ConfigUtils.updatePkgConfig();
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceClick(@NonNull Preference preference) {
            if (KEY_PREF_JS_PATH.equals(preference.getKey())) {
                if (pkgConfig != null) {
                    CommonUtils.openAssignFolder(getActivity(), REQUEST_CODE_OPEN_ASSIGN);
                }
            }
            return false;
        }
    }
}