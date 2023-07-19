package com.poorld.badget;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.poorld.badget.utils.ConfigUtils;
import com.poorld.badget.utils.ShellUtils;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity#badget";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //requestPermission();

        TextView hello_badget = (TextView) findViewById(R.id.hello_badget);

        // /data/user/0/poorld.xp.badget/app_cache
        File cache = getDir("cache", Context.MODE_PRIVATE);
        Log.d(TAG, "onCreate: " + cache.getPath());
        hello_badget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 复制assets/badget/下的库文件夹到/data/user/0/poorld.xp.badget/app_cache
                copyFile("badget", cache.getPath());
                // 从/data/user/0/poorld.xp.badget/app_cache 复制到 /data/local/tmp/badget/
                List<String> cmds = new ArrayList<>();
                cmds.add(String.format("cp -r %s/* %s", cache.getPath(), ConfigUtils.getBadgetDataPath()));
                cmds.add("chmod -R 777 " + ConfigUtils.getBadgetDataPath());
                ShellUtils.CommandResult result = ShellUtils.execCommand(cmds, true, true);
                if (result.result == 0) {
                    Toast.makeText(MainActivity.this, "初始化成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void copyFile(String src, String dst) {
        try {
            String[] badgetsDirs = getAssets().list(src);
            Log.d("TAG", "badgetsDirs: " + Arrays.toString(badgetsDirs));
            if (badgetsDirs.length > 0) {
                File file = new File(dst);
                if (!file.exists()) file.mkdirs();
                for (String fileName : badgetsDirs) {
                    if (!src.equals("")) { // assets 文件夹下的目录
                        copyFile( src + File.separator + fileName, dst + File.separator + fileName);
                    } else { // assets 文件夹
                        copyFile( fileName, dst + File.separator + fileName);
                    }
                }
            } else {
                File outFile = new File(dst);
                if (!outFile.exists()) {
                    outFile.createNewFile();
                }
                InputStream is = getAssets().open(src);
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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