package com.cc.fileManage.entity.file;

import java.util.Comparator;

public class ManageFileSort implements Comparator<ManageFile>
{
    @Override
    public int compare(ManageFile f1, ManageFile f2) {
        if(f1.isTag() || f2.isTag()) {
            return 0;
        } else if (f1.isDirectory() && f2.isDirectory()) {          // 都是目录
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