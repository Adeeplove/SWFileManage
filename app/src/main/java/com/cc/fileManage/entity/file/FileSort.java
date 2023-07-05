package com.cc.fileManage.entity.file;

import java.io.File;
import java.util.Comparator;

public class FileSort implements Comparator<File>
{
    @Override
    public int compare(File f1, File f2) {
        if (f1.isDirectory() && f2.isDirectory()) {                 // 都是目录
            return f1.getName().compareToIgnoreCase(f2.getName());  //都是目录时按照名字排序
        } else if (f1.isDirectory() && f2.isFile()) {               //目录与文件.目录在前
            return -1;
        } else if (f2.isDirectory() && f1.isFile()) {               //文件与目录
            return 1;
        } else {
            return f1.getName().compareToIgnoreCase(f2.getName());  //都是文件
        }
    }
}