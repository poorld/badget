package com.poorld.badget.helper;

import android.os.FileObserver;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

public class JsFileObserver extends FileObserver {

    public static final String TAG = "JsFileObserver";

    public JsFileObserver(@NonNull File file) {
        super(file);
    }

    public interface EventCallback{
        void onModify(String path);
        void onDelete(String path);
    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        Log.e(TAG, "pathaaaa:"+path);
        if (null!=path && !"".equals(path)){
            String substring = path.substring(path.lastIndexOf(".")+1);
            Log.e(TAG, "path:"+path);
            Log.e(TAG, "substring:"+substring);
            Log.e(TAG, "event:"+event);

        } else {
            return;
        }

        int e = event & FileObserver.ALL_EVENTS;
        Log.e(TAG, "event->e:"+e);
        switch (e) {
            case FileObserver.ACCESS:
                Log.e("wannoo", "文件操作___" + e + "__1打开文件后读取文件的操作");
                break;
            case FileObserver.MODIFY:
                Log.e("wannoo", "文件操作___" + e + "__2文件被修改");
                break;
            case FileObserver.ATTRIB:
                Log.e("wannoo", "文件操作___" + e + "__4属性变化");
                break;
            case FileObserver.CLOSE_WRITE:
                Log.e("wannoo", "文件操作___" + e + "__8文件写入或编辑后关闭");
                break;
            case FileObserver.CLOSE_NOWRITE:
                //录音时，最后一个有效回调是这个
                Log.e("wannoo", "文件操作___" + e + "__16只读文件被关闭");

                break;
            case FileObserver.OPEN:
                Log.e("wannoo", "文件操作___" + e + "__32文件被打开");
                break;
            case FileObserver.MOVED_FROM:
                Log.e("wannoo", "文件操作___" + e + "__64移出事件");//试了重命名先MOVED_FROM再MOVED_TO
                break;
            case FileObserver.MOVED_TO:
                Log.e("wannoo", "文件操作___" + e + "__128移入事件");
                break;
            case FileObserver.CREATE:
                Log.e("wannoo", "文件操作___" + e + "__256新建文件");//把文件移动给自己先CREATE在DELETE
                break;
            case FileObserver.DELETE:
                Log.e("wannoo", "文件操作___" + e + "__512有删除文件");//把文件移出去DELETE
                break;
            case FileObserver.DELETE_SELF:
                Log.e("wannoo", "文件操作___" + e + "__1024监听的这个文件夹被删除");
                break;
            case FileObserver.MOVE_SELF:
                Log.e("wannoo", "文件操作___" + e + "__2048监听的这个文件夹被移走");
                break;
            case FileObserver.ALL_EVENTS:
                Log.e("wannoo", "文件操作___" + e + "__4095全部操作");
                break;
        }
    }

}
