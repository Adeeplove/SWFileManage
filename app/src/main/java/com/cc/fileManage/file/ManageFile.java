package com.cc.fileManage.file;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * 文件
 * @author sowhat
 */
public abstract class ManageFile {

    // 文件名
    protected String fileName;
    //文件类型
    protected String fileType;
    //文件最后修改时间
    protected long   fileLastModified;
    //文件大小
    protected long   fileSize;
    //文件路径
    protected String filePath;

    //是否是标签
    protected boolean   isTag;
    //是否被选中
    protected boolean   isCheck;
    //是否高亮
    protected boolean   isHighlight;

    public boolean isDirectory() {
        return !fileType.equals("stream");
    }

    public boolean isFile() {
        return fileType.equals("stream");
    }

    public boolean isHidden() {
        return fileName.startsWith(".");
    }

    public String getTimeToString(){
        return getFileTime(fileLastModified);
    }

    public String getSizeToString(){
        return getFileSize(fileSize);
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileLastModified() {
        return fileLastModified;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isTag() {
        return isTag;
    }

    public void setTag(boolean tag) {
        isTag = tag;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public boolean isHighlight() {
        return isHighlight;
    }

    public void setHighlight(boolean highlight) {
        isHighlight = highlight;
    }


    @NonNull
    @Override
    public String toString() {
        return "ManageFile{" +
                "fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileLastModified=" + fileLastModified +
                ", fileSize=" + fileSize +
                ", filePath='" + filePath + '\'' +
                ", isTag=" + isTag +
                ", isCheck=" + isCheck +
                ", isHighlight=" + isHighlight +
                '}';
    }

    //获取文件大小 自动转换
    public static String getFileSize(long fileLength){
        String size;
        if(fileLength > 0){
            DecimalFormat df = new DecimalFormat("#.00");
            if (fileLength < 1024) {
                size = Double.valueOf(fileLength).intValue() + "B";
            } else if (fileLength < 1048576) {
                size = df.format((double) fileLength / 1024) + "KB";
            } else if (fileLength < 1073741824) {
                size = df.format((double) fileLength / 1048576) + "MB";
            } else {
                size = df.format((double) fileLength / 1073741824) +"GB";
            }
        }else{
            size = "0B";
        }
        return size;
    }

    //时间转日期格式
    public static String getFileTime(long time){
        DateFormat dateFormat = SimpleDateFormat.getInstance();
        return dateFormat.format(time);
    }
}
