package com.cc.fileManage.entity.file;

import java.io.File;

/**
 *  java file 特殊类
 */
public class JFile extends ManageFile{

    private File file;

    public JFile(){}

    public JFile(String filePath){
        this(new File(filePath));
    }

    public JFile(File file){
        this.file = file;
        this.fileName = file.getName();
        this.fileType = file.isDirectory() ? "directory" : "stream";
        this.fileLastModified = file.lastModified();
        this.fileSize = file.length();
        this.filePath = file.getPath();
    }

    public File getFile() {
        return file;
    }
}
