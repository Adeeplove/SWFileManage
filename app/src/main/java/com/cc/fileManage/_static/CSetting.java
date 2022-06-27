package com.cc.fileManage._static;

import android.content.Context;
import android.content.SharedPreferences;

public class CSetting {

    private static final String[] serialPorts = new String[]{"home","hidden"};
    //配置文件
    private static final String serialPortSettingName = "setting";

    //浏览器是否为主页
    public static boolean webIsHome;
    //显示隐藏文件
    public static boolean showHiddenFile;

    public static void init(Context context){
        //尝试读取配置文件
        if(CSetting.readSetting(context).getAll().size() <= 0){
            //初始化
            CSetting.writeSetting(context);
        }
        //读取配置
        CSetting.loadSetting(context);
    }

    //载入配置文件数据
    public static void loadSetting(Context context)
    {
        SharedPreferences map = readSetting(context);
        //浏览器是否为主页
        webIsHome = map.getBoolean(serialPorts[0], false);
        //显示隐藏文件
        showHiddenFile = map.getBoolean(serialPorts[1], false);
    }

    //写配置文件
    public static void writeSetting(Context context)
    {
        //步骤1：创建一个SharedPreferences对象
        SharedPreferences sharedPreferences= context.getSharedPreferences(serialPortSettingName, Context.MODE_PRIVATE);
        //步骤2：实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //浏览器是否为主页
        editor.putBoolean(serialPorts[0], false);
        //显示隐藏文件
        editor.putBoolean(serialPorts[1], false);
        //步骤4：提交
        editor.apply();
    }

    //写配置文件
    public static void writeSettingNow(Context context)
    {
        //步骤1：创建一个SharedPreferences对象
        SharedPreferences sharedPreferences= context.getSharedPreferences(serialPortSettingName ,Context.MODE_PRIVATE);

        //步骤2：实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //浏览器是否为主页
        editor.putBoolean(serialPorts[0], webIsHome);
        //显示隐藏文件
        editor.putBoolean(serialPorts[1], showHiddenFile);
        //步骤4：提交
        editor.apply();
    }

    //读取配置文件
    public static SharedPreferences readSetting(Context context)
    {
        return context.getSharedPreferences(serialPortSettingName, Context.MODE_PRIVATE);
    }
}
