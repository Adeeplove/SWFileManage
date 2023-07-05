package com.cc.fileManage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cc.fileManage.entity.BookMark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class DBService {
    private static final String TAG = "DBService";

    private static volatile DBService databaseService;
    //
    private final DBOpenHelper dbOpenHelper;

    //互斥锁
    protected final ReentrantLock lock = new ReentrantLock();

    private DBService(Context context) {
        this.dbOpenHelper = new DBOpenHelper(context);
    }

    public static DBService getInstance(Context context){
        if(databaseService == null) {
            synchronized (DBService.class) {
                if(databaseService == null)
                    databaseService = new DBService(context);
            }
        }
        return databaseService;
    }

    //新增书签
    public boolean addBookMark(BookMark bookMark) {
        try {
            this.lock.lock();
            //
            try (SQLiteDatabase sqLiteDatabase = dbOpenHelper.getWritableDatabase()) {
                //实例化常量值
                ContentValues values = new ContentValues();
                values.put("name", bookMark.getName());
                values.put("path", bookMark.getPath());
                values.put("describe", bookMark.getDescribe());
                values.put("type", bookMark.getType() == BookMark.Type.Path ? 0 : 1);
                //
                //调用insert()方法插入数据
                long code = sqLiteDatabase.insert("bookmark", null, values);
                if (code == -1) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            this.lock.unlock();
        }
        return true;
    }

    //更新书签信息
    public boolean updateBookMark(BookMark bookMark) {
        //
        long code = 0;
        try {
            this.lock.lock();
            try (SQLiteDatabase sqLiteDatabase = dbOpenHelper.getWritableDatabase()){
                //实例化内容值
                ContentValues values = new ContentValues();
                values.put("name", bookMark.getName());
                values.put("path", bookMark.getPath());
                values.put("describe", bookMark.getDescribe());
                //调用insert()方法插入数据
                code = sqLiteDatabase.update("bookmark", values,
                        "id=?", new String[]{String.valueOf(bookMark.getId())} );
            }catch (Exception e){
                e.printStackTrace();
            }
        }finally {
            this.lock.unlock();
        }
        return code > 0;
    }

    //查询全部书签
    public List<BookMark> queryAllBookMark(BookMark.Type type) {
        try{
            this.lock.lock();
            //
            List<BookMark> list = new ArrayList<>();
            int typeNum = type == BookMark.Type.Path ? 0 : 1;
            //
            SQLiteDatabase writableDatabase = dbOpenHelper.getWritableDatabase();
            //查询全部数据
            try (Cursor cursor = writableDatabase
                    .rawQuery("select * from bookmark where type = ? order by id", new String[]{
                            String.valueOf(typeNum)
                    })) {
                if (cursor.getCount() > 0) {
                    //移动到首位
                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        //
                        list.add(bookMarkDataResult(cursor));
                        //移动到下一位
                        cursor.moveToNext();
                    }
                }
                return list;
            }
        }finally {
            this.lock.unlock();
        }
    }

    //删除书签
    public boolean deleteBookMark(BookMark bookMark) {
        try{
            this.lock.lock();
            try (SQLiteDatabase writableDatabase = dbOpenHelper.getWritableDatabase()){
                return writableDatabase.delete("bookmark",
                        "id=?", new String[]{String.valueOf(bookMark.getId())}) > 0;
            }catch (Exception e){
                e.printStackTrace();
            }
        }finally {
            this.lock.unlock();
        }
        return false;
    }

    private BookMark bookMarkDataResult(Cursor cursor){
        //
        BookMark bookMark = new BookMark();
        bookMark.setId(cursor.getInt(0));
        bookMark.setName(cursor.getString(1));
        bookMark.setPath(cursor.getString(2));
        bookMark.setDescribe(cursor.getString(3));
        bookMark.setType(cursor.getInt(4) == 0 ? BookMark.Type.Path : BookMark.Type.Web);
        return bookMark;
    }

    /**
     * 关闭
     */
    public void close() {
        dbOpenHelper.close();
        databaseService = null;
    }
}