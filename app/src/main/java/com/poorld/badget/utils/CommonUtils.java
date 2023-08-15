package com.poorld.badget.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class CommonUtils {
    private static final String TAG = "Badget#CommonUtils";

    private static final String letter = "abcdefghijklmnopqrstuvwxyz";

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f); // 四舍五入取整
    }

    public static int sp2px(Context context, float spValue) {
        //fontScale （DisplayMetrics类中属性scaledDensity）
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static Drawable resizeDrawable(Context context, Drawable image, int width, int height) {
        Bitmap b = drawableToBitmap(image);
        int dstWidth = CommonUtils.dip2px(context, width);
        int dstHight = CommonUtils.dip2px(context, height);
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, dstWidth, dstHight, false);
        return new BitmapDrawable(context.getResources(), bitmapResized);
    }

    public static String randomString(int number) {
        if (number >= 1) {
            Random random = new Random();

            if (number == 1) {
                return String.valueOf(letter.charAt(random.nextInt(27)));
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < number; i++) {
                sb.append(letter.charAt(random.nextInt(27)));
            }
            return sb.toString();
        }
        return null;
    }

    /**
     * 将Drawable转成Bitmap
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {

        Bitmap bitmap = Bitmap.createBitmap(

                drawable.getIntrinsicWidth(),

                drawable.getIntrinsicHeight(),

                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888

                        : Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);

        // canvas.setBitmap(bitmap);

        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());

        drawable.draw(canvas);

        return bitmap;
    }


    public static void openAssignFolder(Context context, int requestCode){
        //调用系统文件管理器打开指定路径目录
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/javascript");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        ((Activity)context).startActivityForResult(intent, requestCode);
    }


    public static File saveFileFromUri(Uri uri, Context context, String fileSavePath) {
        Log.d(TAG, "saveFileFromUri: ");
        Log.d(TAG, "uri: " + uri);
        Log.d(TAG, "fileSavePath: " + fileSavePath);
        if (uri == null) {
            return null;
        }
        switch (uri.getScheme()) {
            case "content":
                return getFileFromContentUri(uri, context, fileSavePath);
            case "file":
                return new File(uri.getPath());
            default:
                return null;
        }
    }

    private static File getFileFromContentUri(Uri contentUri, Context context,String fileSavePath) {
        if (contentUri == null) {
            return null;
        }
        File file = null;
        String filePath = null;
        String fileName;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(contentUri, filePathColumn, null,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            try{
                filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
            }catch(Exception e){
            }
            fileName = cursor.getString(cursor.getColumnIndex(filePathColumn[1]));
            cursor.close();
            if (!TextUtils.isEmpty(filePath)) {
                file = new File(filePath);
            }
            if (TextUtils.isEmpty(filePath)) {//!file.exists() || file.length() <= 0 ||
                filePath = getPathFromInputStreamUri(context, contentUri, fileSavePath, fileName);
            }
            if (!TextUtils.isEmpty(filePath)) {
                file = new File(filePath);
            }
        }
        return file;
    }

    private static String getPathFromInputStreamUri(Context context, Uri uri,String fileSavePath, String fileName) {
        InputStream inputStream = null;
        String filePath = null;

        if (uri.getAuthority() != null) {
            try {
                inputStream = context.getContentResolver().openInputStream(uri);
                File file = createTemporalFileFrom(inputStream, fileSavePath, fileName);
                filePath = file.getPath();

            } catch (Exception e) {
                Log.e("teenyda", e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    // L.e(e);
                }
            }
        }

        return filePath;
    }

    private static File createTemporalFileFrom(InputStream inputStream, String fileSavePath, String fileName)
            throws IOException {
        File targetFile = null;

        if (inputStream != null) {
            int read;
            byte[] buffer = new byte[8 * 1024];
            //自己定义拷贝文件路径
            targetFile = new File(fileSavePath, fileName);

            if (!targetFile.getParentFile().exists()) {
                // create dir: /data/local/tmp/badget/packageName/
                // touch /data/local/tmp/badget/packageName/target.js
                ShellUtils.execCommand(new String[]{
                        "mkdir " + targetFile.getParentFile().getPath(),
                        "touch " + targetFile.getPath(),
                        "chmod 777 " + targetFile.getPath()
                }, true, false);
            } else {
                // touch /data/local/tmp/badget/packageName/target.js
                ShellUtils.execCommand(new String[]{
                        "touch " + targetFile.getPath(),
                        "chmod 777 " + targetFile.getPath()
                }, true, false);
            }

            // 写入内容到/data/local/tmp/badget/packageName/targetFile.js
            try (OutputStream outputStream = Files.newOutputStream(targetFile.toPath())){
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            }
        }

        return targetFile;
    }

    public static boolean saveFile(String config, String toFile) {
        Log.d(TAG, "saveConfig " + config);
        Log.d(TAG, "saveConfig to " + toFile);
        try (FileOutputStream fileOutput = new FileOutputStream(toFile);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();){
            byteOut.write(config.getBytes());
            // 从内存到写入到具体文件
            fileOutput.write(byteOut.toByteArray());
            fileOutput.flush();
            return true;
        } catch (Exception ex) {
            Log.d(TAG, "saveFile Exception " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public static String readFile(File file) {
        Log.d(TAG, "readFile: ");
        try (FileInputStream fis = new FileInputStream(file);
             FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr)){
            String str = null;

            StringBuffer sb = new StringBuffer();
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }

            return sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * @param fromDir 指定的下载目录
     * @param toDir    应用的包路径
     * @param gadgetLibName 给gadget库重命名
     */
    public static void copyFile(String fromDir, String toDir,String gadgetLibName) {
        Log.d(TAG, "copyFile: ");
        Log.d(TAG, "fromDir: " + fromDir);
        Log.d(TAG, "toDir: " + toDir);
        //要复制的文件目录
        File[] currentFiles;
        File root = new File(fromDir);
        if (!root.exists()) {
            Log.d(TAG, root.getPath() + "目录不存在");
            return;
        }
        //如果存在则获取当前目录下的全部文件 填充数组
        currentFiles = root.listFiles();
        if (currentFiles == null) {
            Log.d("soFile---", "未获取到文件");
            return;
        }
        //目标目录
        File targetDir = new File(toDir);
        //创建目录
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        //遍历要复制该目录下的全部文件
        for (int i = 0; i < currentFiles.length; i++) {
            if (currentFiles[i].isDirectory()) {
                //如果当前项为子目录 进行递归
                copyFile(currentFiles[i].getPath() + File.separator, toDir + currentFiles[i].getName() + File.separator, gadgetLibName);
            } else {
                //如果当前项为文件则进行文件拷贝
                int id = doCopy(currentFiles[i].getPath(), toDir + File.separator + /*currentFiles[i].getName()*/ gadgetLibName);

            }
        }
    }

    /**
     * 要复制的目录下的所有非子目录(文件夹)文件拷贝
     *
     * @param fromFiles 指定的下载目录
     * @param toFile    应用的包路径
     * @return int
     */
    private static int doCopy(String fromFiles, String toFile) {
        Log.d(TAG, "复制文件到" + toFile);
        try {
            FileInputStream fileInput = new FileInputStream(fromFiles);
            FileOutputStream fileOutput = new FileOutputStream(toFile);
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 1];
            int len = -1;
            while ((len = fileInput.read(buffer)) != -1) {
                byteOut.write(buffer, 0, len);
            }
            // 从内存到写入到具体文件
            fileOutput.write(byteOut.toByteArray());
            // 关闭文件流
            byteOut.close();
            fileOutput.close();
            fileInput.close();
            return 0;
        } catch (Exception ex) {
            return -1;
        }
    }


    public static void copyAssetsFile(Context context, String src, String dst) {
        try {
            String[] badgetsDirs = context.getAssets().list(src);
            Log.d(TAG, "badgetsDirs: " + Arrays.toString(badgetsDirs));
            if (badgetsDirs.length > 0) {
                File file = new File(dst);
                if (!file.exists()) file.mkdirs();
                for (String fileName : badgetsDirs) {
                    if (!src.equals("")) { // assets 文件夹下的目录
                        copyAssetsFile( context,src + File.separator + fileName, dst + File.separator + fileName);
                    } else { // assets 文件夹
                        copyAssetsFile( context, fileName, dst + File.separator + fileName);
                    }
                }
            } else {
                File outFile = new File(dst);
                if (!outFile.exists()) {
                    outFile.createNewFile();
                }
                InputStream is = context.getAssets().open(src);
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

    public static void unzip(String zipFileString, boolean deleteZip) {
        File zipFile = new File(zipFileString);
        if (!zipFile.exists()) {
            return;
        }
        String outPathString = zipFile.getParent();

        Log.d(TAG, "unzip: " + zipFileString);
        Log.d(TAG, "unzip path: " + outPathString);

        FileInputStream fis = null; //文件输入流
        ZipInputStream inZip = null; // android提供的zip包输入流
        try {
            fis = new FileInputStream(zipFileString); //先读取文件,
            inZip = new ZipInputStream(fis);//将文件流变成zip输入流
            ZipEntry zipEntry; //zip实体
            String szName = "";
            while ((zipEntry = inZip.getNextEntry()) != null) { //while(true)循环解析
                szName = zipEntry.getName();
                if (zipEntry.isDirectory()) {// 如果是文件夹
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(outPathString + File.separator + szName);
                    folder.mkdirs();
                } else {//如果是文件
                    File file = new File(outPathString + File.separator + szName);
                    file.createNewFile();
                    FileOutputStream out = new FileOutputStream(file);
                    int length;
                    byte[] buffer = new byte[1024];
                    while ((length = inZip.read(buffer)) != -1) {
                        out.write(buffer, 0, length);
                        out.flush();
                    }
                    if (out != null) {
                        out.close();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inZip != null) {
                try {
                    inZip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (deleteZip) {
                zipFile.delete();
            }
        }

    }

}