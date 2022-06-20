package com.cc.fileManage.file;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;

public class FileComparator<T> implements Comparator<T>
{
    @Override
    public int compare(T file1, T file2) {
        // TODO Auto-generated method stub
        if(file1 instanceof File) {
            File f1 = (File) file1, f2 = (File)file2;
            if (f1.isDirectory() && f2.isDirectory()) {// 都是目录
                return f1.getName().compareToIgnoreCase(f2.getName());//都是目录时按照名字排序
            } else if (f1.isDirectory() && f2.isFile()) {//目录与文件.目录在前
                return -1;
            } else if (f2.isDirectory() && f1.isFile()) {//文件与目录
                return 1;
            } else {
                return f1.getName().compareToIgnoreCase(f2.getName());//都是文件
            }
        }else if(file1 instanceof ManageFile){
            ManageFile f1 = (ManageFile) file1, f2 = (ManageFile)file2;
            ////================
            if(f1.isTag() || f2.isTag()) {
                return 1;
            }
            if (f1.isDirectory() && f2.isDirectory()) {// 都是目录
                return f1.getFileName().compareToIgnoreCase(f2.getFileName());//都是目录时按照名字排序
            } else if (f1.isDirectory() && f2.isFile()) {//目录与文件.目录在前
                return -1;
            } else if (f2.isDirectory() && f1.isFile()) {//文件与目录
                return 1;
            } else {
                return f1.getFileName().compareToIgnoreCase(f2.getFileName());//都是文件
            }
        }
        return 0;
    }

//    public static List<File> sort(List<File> files) {
//        try{
//            Collections.sort(files, new DFileComparator<>());//利用集合工具类排序
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return files;
//    }
}