package com.cc.fileManage;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;

import com.cc.fileManage._static.AppManager;
import com.cc.fileManage._static.CSetting;
import com.cc.fileManage.db.DBService;
import com.cc.fileManage.ui.activity.ErrorActivity;
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

public class App extends Application {

    //浏览器内核名称
    public static String browserType = "WebKit";

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化数据库
        DBService.getInstance(getApplicationContext());
        //初始化配置文件
        CSetting.init(getApplicationContext());
        //腾讯tbs浏览内核
        initTbs();
        //写错误log
        Thread.setDefaultUncaughtExceptionHandler(new CCUnCaughtExceptionHandler());
    }

    /**
     * 是否是暗黑模式
     * @param context   上下文
     * @return          是否是暗黑模式
     */
    public static boolean isUiMode(Context context) {
        return  (context.getApplicationContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_YES) != 0;
    }

    /**
     * 获取attrs资源
     * @param attrsId   attrsId
     * @return          resourceId
     */
    public static int getAttrsId(Context context, int attrsId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrsId, typedValue, true);
        return typedValue.resourceId;
    }

    /**
     * 获取指定style的attrs资源的颜色值
     * @param context   上下文
     * @param styleId   style id
     * @param attrId    attr  id
     * @param defaultColor    默认颜色
     * @return                颜色值
     */
    public static int getColorByStyle(Context context, int styleId, int attrId, int defaultColor) {
        int color = defaultColor;
        try {
            int[] attr = new int[]{android.R.attr.textColor};
            @SuppressLint("Recycle")
            TypedArray typedArray = context.obtainStyledAttributes(R.style.AppTextColor, attr);
            color = typedArray.getColor(0, defaultColor);
            typedArray.recycle();
        } catch (Exception ignored) {}
        return color;
    }

    //腾讯tbs浏览内核
    private void initTbs(){
        // 在调用TBS初始化、创建WebView之前进行如下配置
        HashMap<String, Object> map = new HashMap<>();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);
        //
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                if(arg0){
                    browserType = "X5";
                }else{
                    browserType = "WebKit";
                }
            }
            @Override
            public void onCoreInitFinished() {}
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(),  cb);
    }

    /**
     * 错误处理
     */
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
        Writer writer = new StringWriter();
        try (PrintWriter pw = new PrintWriter(writer)){
            throwable.printStackTrace(pw);
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 写出log文件
     * @param errorMessage 错误信息
     */
    private void saveThrowableMessage(String errorMessage) {
        if (TextUtils.isEmpty(errorMessage)) {
            return;
        }
        File file = getExternalFilesDir("crashLog");
        if (!file.exists()) {
            if (file.mkdirs()) {
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
                int len;
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
    private void startTo(String stackTraceInfo) {
        // 退出之前的所有activity
        AppManager.getAppManager().finishAllActivity();
        /////
        Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
        intent.putExtra("error", stackTraceInfo);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
        //
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
