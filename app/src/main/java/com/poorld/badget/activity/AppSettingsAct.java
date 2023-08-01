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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.appbar.MaterialToolbar;
import com.poorld.badget.R;
import com.poorld.badget.entity.ItemAppEntity;
import com.poorld.badget.utils.CommonUtils;
import com.poorld.badget.utils.PkgManager;

public class AppSettingsAct extends AppCompatActivity {

    public static final String TAG = "AppSettingsAct";
    private static ItemAppEntity mApp;

    private MaterialToolbar toolbar;

    public static final String EXTRA_PKG = "package";

    public static void startAct(Context context, String pkg) {
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

    public static class AppSettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

        private static final String KEY_PREF_APP = "pref_app";
        private static final String KEY_PREF_SWITCH_ENABLE = "pref_switch_enable";
        Preference prefApp;
        SwitchPreference prefEnable;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "AppSettingsFragment#onCreate: ");
            addPreferencesFromResource(R.xml.root_preferences);

            prefApp = findPreference(KEY_PREF_APP);
            prefEnable = findPreference(KEY_PREF_SWITCH_ENABLE);

            Intent intent = getActivity().getIntent();
            if (intent != null) {
                String pkg = intent.getStringExtra(EXTRA_PKG);
                mApp = PkgManager.getItemAppEntity(getActivity(), pkg);
                if (mApp != null) {
                    prefApp.setTitle(mApp.getAppName());
                    prefApp.setSummary(mApp.getPackageName());
                    Drawable drawable = CommonUtils.resizeDrawable(getContext(), mApp.getDrawable(), 30, 30);
                    prefApp.setIcon(drawable);
                    prefEnable.setChecked(mApp.isHookEnabled());

                    prefEnable.setOnPreferenceChangeListener(this);
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
                if (mApp != null) {
                    Log.d(TAG, mApp.getPackageName());
                }
                return true;
            }
            return false;
        }
    }
}