package com.cc.fileManage;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.PathUtils;
import com.cc.fileManage.ui.activity.ErrorActivity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class App extends Application {

    //写出路径
    private String logFilePath = null;

    @Override
    public void onCreate() {
        super.onCreate();

        //log存放路径
        logFilePath = PathUtils.getExternalAppDataPath() + File.separator + "files/crashLog";

        //写错误log
        Thread.setDefaultUncaughtExceptionHandler(new CCUnCaughtExceptionHandler());
    }

    class CCUnCaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            Log.e("程序出现异常", "Thread = " + t.getName() + "\nThrowable = " + e.getMessage());
            String stackTraceInfo = getStackTraceInfo(e);
            Log.e("子线程报错", stackTraceInfo);

            //写出日志
            saveThrowableMessage(stackTraceInfo);
            //启动activity
            startTo(stackTraceInfo);
        }
    }

    /**
     * 获取错误的信息
     *
     * @param throwable 异常
     * @return          错误信息
     */
    private String getStackTraceInfo(final Throwable throwable) {
        PrintWriter pw = null;
        Writer writer = new StringWriter();
        try {
            pw = new PrintWriter(writer);
            throwable.printStackTrace(pw);
        } catch (Exception e) {
            return "";
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
        return writer.toString();
    }

    /**
     * 写出log文件
     * @param errorMessage 错误信息
     */
    private void saveThrowableMessage(String errorMessage) {
        if (TextUtils.isEmpty(errorMessage)) {
            return;
        }
        File file = new File(logFilePath);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            if (mkdirs) {
                writeStringToFile(errorMessage, file);
            }
        } else {
            writeStringToFile(errorMessage, file);
        }
    }

    /**
     * 写log文件
     * @param errorMessage  log
     * @param file          文件
     */
    private void writeStringToFile(final String errorMessage, final File file) {
        new Thread(() -> {
            FileOutputStream outputStream = null;
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(errorMessage.getBytes());
                outputStream = new FileOutputStream(new File(file, "error.txt"));
                int len = 0;
                byte[] bytes = new byte[1024];
                while ((len = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, len);
                }
                outputStream.flush();
                Log.e("程序异常", "写入本地文件成功：" + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 启动activity
     * @param stackTraceInfo 错误信息
     */
    private void startTo(String stackTraceInfo){
        Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
        intent.putExtra("error", stackTraceInfo);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
        //
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
