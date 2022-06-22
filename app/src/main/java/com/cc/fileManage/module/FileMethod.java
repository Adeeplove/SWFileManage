package com.cc.fileManage.module;

import android.content.Context;
import android.content.Intent;

import com.cc.fileManage.entity.file.FileComparator;
import com.cc.fileManage.ui.activity.EditActivity;
import com.cc.fileManage.ui.activity.LoadTexActivity;
import com.cc.fileManage.ui.activity.PhotoActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class FileMethod {

    private Context context;
    private File file;

    public FileMethod(Context context, String filePath){
        this(context, new File(filePath));
    }

    public FileMethod(Context context, File file){
        this.context = context;
        this.file = file;
    }

    /**
     *  打开文件
     */
    public boolean openFile(){
        //后缀名
        String fileSuffix = file.getName().toLowerCase();
        if(fileSuffix.endsWith(".txt") || fileSuffix.endsWith(".lua")
            || fileSuffix.endsWith(".log") || fileSuffix.endsWith(".java")
        ){
            //
            Intent intent = new Intent(context, EditActivity.class);
            intent.putExtra("path", file.getPath());
            context.startActivity(intent);
            return true;
        }
        else if(fileSuffix.endsWith(".tex")){
            Intent intent = new Intent(context, LoadTexActivity.class);
            intent.putExtra("path", file.getPath());
            context.startActivity(intent);
            return true;
        }
        else if(fileSuffix.endsWith(".jpg") || fileSuffix.endsWith(".jpeg") || fileSuffix.endsWith(".png")){
            loadImage();
        }
        return false;
    }

    //图片浏览器
    private void loadImage(){
        //
        ArrayList<String> list = new ArrayList<>();
        //获取文件夹所有子文件
        File[] files = file.getParentFile().listFiles();

        int index = 0;
        int i = 0;

        //排序
        Arrays.sort(files, new FileComparator<>());
        //遍历子文件
        for(File child : files){
            //子文件名
            String endName = child.getName().toLowerCase();
            if(endName.endsWith(".jpeg")
                    ||endName.endsWith(".jpg")
                    ||endName.endsWith(".png")){

                if(file.getPath().equals(child.getPath())){
                    index = i;
                }
                list.add(child.getPath());
                i++;
            }
        }
        Intent intent = new Intent(context, PhotoActivity.class);
        intent.putStringArrayListExtra("image", list);
        intent.putExtra("index",index);
        context.startActivity(intent);
    }
}
