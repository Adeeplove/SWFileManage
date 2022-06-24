package com.cc.fileManage.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 默认就在数据库里创建
 * @author sowhat
 */
public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String name = "file.db";    //数据库名称
    private static final int version = 1;            //数据库版本

    public DBOpenHelper(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //书签
        db.execSQL("CREATE TABLE IF NOT EXISTS bookmark (" +
                "id INTEGER primary key autoincrement," +
                "name TEXT,"+
                "path TEXT,"+
                "describe TEXT,"+
                "type INTEGER default 0"+
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //
    }
}