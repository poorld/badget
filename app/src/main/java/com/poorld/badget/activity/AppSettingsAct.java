package com.poorld.badget.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.DialogPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.SwitchPreference;
//import androidx.preference.SwitchPreferenceCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.poorld.badget.R;
import com.poorld.badget.entity.ConfigEntity;
import com.poorld.badget.entity.InteractionType;
import com.poorld.badget.entity.ItemAppEntity;
import com.poorld.badget.utils.CommonUtils;
import com.poorld.badget.utils.ConfigUtils;
import com.poorld.badget.utils.PkgManager;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AppSettingsAct extends AppCompatActivity {

    public static final String TAG = "AppSettingsAct";
    private static ItemAppEntity mApp;

    private MaterialToolbar toolbar;

    public static final String EXTRA_PKG = "package";

    private static final int REQUEST_CODE_TYPE_SCRIPT = 9527;
    private static final int REQUEST_CODE_TYPE_SCRIPT_DIR = 9528;

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

    private void copyJs(Intent data) {
        File file = CommonUtils.saveFileFromUri(data.getData(), this, ConfigUtils.getBadgetPackagePath(mPackageName).getPath());
        if (file != null && file.exists()) {
            Log.d(TAG, "saveFileFromUri success: " + file.getPath());
            ConfigEntity.PkgConfig pkgConfig = ConfigUtils.getPkgConfig(mPackageName);
            if (pkgConfig == null) {
                return;
            }
            pkgConfig.setJsPath(file.getPath());
            pkgConfig.setSoName(ConfigUtils.getRandomName());
            ConfigUtils.updatePkgConfig();

            runOnUiThread(() -> Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show());
        } else {
            runOnUiThread(() -> Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");
        if (data == null) {
            return;
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_TYPE_SCRIPT
                || requestCode == REQUEST_CODE_TYPE_SCRIPT_DIR) {
                new Thread(() -> copyJs(data)).start();
            }
        }
    }

    public static class AppSettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        private static final String KEY_PREF_APP = "pref_app";
        private static final String KEY_PREF_SWITCH_ENABLE = "pref_switch_enable";
        private static final String KEY_MANAGER_INTERACTION_TYPE = "manager_interaction_type";
        private static final String KEY_PREF_JS_PATH = "pref_js_path";
        private static final String KEY_PREF_SCRIPT_DIRECTORY = "pref_script_directory";

        private static final String KEY_INTERACTION_TYPES = "interaction_types";

        Preference prefApp;
        Preference prefJs;
        Preference prefScriptDirectory;

        ListPreference prefType;
        SwitchPreference prefEnable;

        PreferenceGroup managerGroup;
        private ConfigEntity.PkgConfig pkgConfig;

        private Map<String, Preference> mDirScriptPreferences = new ArrayMap<>();


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "AppSettingsFragment#onCreate: ");
            addPreferencesFromResource(R.xml.app_settings_preferences);

            prefApp = findPreference(KEY_PREF_APP);
            prefEnable = findPreference(KEY_PREF_SWITCH_ENABLE);
            prefJs = findPreference(KEY_PREF_JS_PATH);
            prefScriptDirectory = findPreference(KEY_PREF_SCRIPT_DIRECTORY);
            prefType = findPreference(KEY_INTERACTION_TYPES);
            managerGroup = findPreference(KEY_MANAGER_INTERACTION_TYPE);

            Intent intent = getActivity().getIntent();
            if (intent != null) {
                String pkg = intent.getStringExtra(EXTRA_PKG);
                mApp = PkgManager.getItemAppEntity(getActivity(), pkg);
                if (mApp != null) {
                    prefEnable.setOnPreferenceChangeListener(this);
                    prefJs.setOnPreferenceClickListener(this);
                    prefScriptDirectory.setOnPreferenceClickListener(this);
                    prefType.setOnPreferenceChangeListener(this);

                    pkgConfig = ConfigUtils.getPkgConfig(mApp.getPackageName());
                    if (pkgConfig != null) {
                        prefEnable.setChecked(pkgConfig.isEnabled());
                    }
                    prefApp.setTitle(mApp.getAppName());
                    prefApp.setSummary(mApp.getPackageName());
                    Drawable drawable = CommonUtils.resizeDrawable(getContext(), mApp.getDrawable(), 30, 30);
                    prefApp.setIcon(drawable);

                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mApp != null) {
                ConfigEntity.PkgConfig pkgConfig = ConfigUtils.getPkgConfig(mApp.getPackageName());
                if (pkgConfig != null) {
                    String jsPath = pkgConfig.getJsPath();
                    if (!TextUtils.isEmpty(jsPath)) {
                        prefJs.setSummary(jsPath);
                    } else {
                        prefJs.setSummary("无");
                    }

                    InteractionType type = pkgConfig.getType();
                    if (type != null) {
                        updateInteractionTypePreferences(type);
                    }

                }

            }
        }

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {

        }

        private void updateInteractionTypePreferences(InteractionType interactionType) {
            clearDirScripts();

            int index = interactionType.getInteractionType();
            prefType.setValueIndex(index);
            String[] values = getResources().getStringArray(R.array.types_entries);
            prefType.setSummary(values[index]);

            if (interactionType == InteractionType.Listen) {
                prefType.setSummary(values[index] + " 127.0.0.1:27042");
                prefJs.setVisible(false);
                prefScriptDirectory.setVisible(false);
            } else if (interactionType == InteractionType.Connect) {
                prefType.setSummary(values[index] + " 127.0.0.1:27052");
                prefJs.setVisible(false);
                prefScriptDirectory.setVisible(false);
            } else if (interactionType == InteractionType.Script) {
                prefJs.setVisible(true);
                prefScriptDirectory.setVisible(false);
            } else if (interactionType == InteractionType.ScriptDirectory) {
                prefScriptDirectory.setVisible(true);
                prefJs.setVisible(false);
                refreshDirScripts();
            }
        }

        private void refreshDirScripts() {

            Log.d(TAG, "refreshDirScripts: ");
            if (mApp != null) {
                File[] Scripts = ConfigUtils.getDirScripts(mApp.getPackageName());
                if (Scripts == null) {
                    return;
                }

                for (File script : Scripts) {
                    String name = script.getName();

                    Preference item = new Preference(getContext());
                    item.setKey(name);
                    //item.setTitle(name);
                    item.setSummary(name);
                    item.setIcon(R.drawable.baseline_javascript_24);
                    mDirScriptPreferences.put(name, item);
                    managerGroup.addPreference(item);

                }
            }
        }

        private void clearDirScripts() {
            if (mDirScriptPreferences.size() > 0) {
                for (Preference preference : mDirScriptPreferences.values()) {
                    managerGroup.removePreference(preference);
                }
                mDirScriptPreferences.clear();
            }
        }


        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            Log.d(TAG, "onPreferenceChange: ");

            if (pkgConfig == null) {
                return false;
            }

            if (KEY_PREF_SWITCH_ENABLE.equals(preference.getKey())) {

                Log.d(TAG, pkgConfig.getPkgName());
                pkgConfig.setEnabled((Boolean) newValue);
                pkgConfig.setAppName(mApp.getAppName());

                ConfigUtils.updatePkgConfig();

            } else if (KEY_INTERACTION_TYPES.equals(preference.getKey())) {
                int index = Integer.parseInt((String) newValue);
                Log.d(TAG, "index:" + index);

                InteractionType interactionType = InteractionType.fromAttr(index);
                Log.d(TAG, "type:" + interactionType);

                updateInteractionTypePreferences(interactionType);
                pkgConfig.setType(interactionType);
                ConfigUtils.updatePkgConfig();

            }
            return true;
        }

        @Override
        public boolean onPreferenceClick(@NonNull Preference preference) {
            if (KEY_PREF_JS_PATH.equals(preference.getKey())) {
                if (pkgConfig != null) {
                    CommonUtils.openAssignFolder(getActivity(), REQUEST_CODE_TYPE_SCRIPT);
                }
            } else if (KEY_PREF_SCRIPT_DIRECTORY.equals(preference.getKey())) {
                CommonUtils.openAssignFolder(getActivity(), REQUEST_CODE_TYPE_SCRIPT_DIR);
            }
            return false;
        }
    }
}