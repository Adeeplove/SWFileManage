package com.cc.fileManage.entity.file;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.cc.fileManage.utils.CharUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *  java file 特殊类
 */
public class JFile extends ManageFile{

    private File file;

    protected JFile(boolean tag) {
        this.tag = tag;
    }

    public JFile(String filePath) {
        this(new File(filePath));
    }

    public JFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public boolean isFile() {
        return file.isFile();
    }

    @Override
    public boolean isHidden() {
        return file.isHidden();
    }

    @Override
    public Uri getUri() {
        return Uri.fromFile(file);
    }

    @Override
    public String getName() {
        return file == null ? "" : file.getName();
    }

    @Override
    public String getPath() {
        return file == null ? "" : file.getPath();
    }

    @Override
    public String getESPath() {
        return FileApi.parseExternalStorage(getPath());
    }

    @Override
    public String getParent() {
        String parent = file.getParent();
        return TextUtils.isEmpty(parent) ? "" : parent;
    }

    @Override
    public ManageFile getParentFile() {
        return new JFile(getParent());
    }

    @Override
    public boolean parentCanRead() {
        //获取父目录
        File parent = file.getParentFile();
        if(parent == null){
            return false;
        }
        return parent.exists() && parent.canRead();
    }

    @Override
    public boolean canRead() {
        return file.canRead();
    }

    @Override
    public boolean canWrite() {
        return file.canWrite();
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }

    @Override
    public long length() {
        return file.length();
    }

    @Override
    public boolean delete() {
        deleteContents(file);
        return file.delete();
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public List<ManageFile> listFiles(boolean showHidden) {
        List<ManageFile> manageFiles = new ArrayList<>();
        ////
        String[] list = file.list();
        if (list == null) return manageFiles;
        //
        for (String child : list) {
            File f = new File(getPath(), child);
            // 不显示隐藏文件 直接跳过
            if(!showHidden && f.isHidden()) {
                continue;
            }
            manageFiles.add(new JFile(f));
        }
        return manageFiles;
    }

    @Override
    public void listFiles(Context context, OnFileDataListener listener) {
        if(file.exists() && file.canRead()) {
            String[] list = file.list();
            if (list != null) {
                for (String child : list) {
                    File f = new File(getPath(), child);
                    listener.onData(new JFile(f), file.getPath());
                }
            }
        } else if(FileApi.isAndroidDataDir(file)) {
            /// Android 11 以上 且是data 或 obb目录
            if(FileApi.isAndroid13()) {
                /// Android 13
                FileApi.document(context, getPath(), listener);
            } else {
                /// Android 11
                FileApi.documentFileLists(context, getPath(), listener);
            }
        } else {
            // 不存在
            listener.onNoExist();
        }
    }

    @Override
    public int[] countChild() {
        int[] array = new int[]{0, 0};
        countDir(file, array);
        return array;
    }

    // 统计
    private static void countDir(File f, int[] array) {
        if (f.isDirectory()) {
            File[] manageFiles = f.listFiles();
            if(manageFiles != null) {
                for(File file : manageFiles){
                    if(file.isFile()) {
                        array[0]++;
                    } else {
                        array[1]++;
                        countDir(file, array);
                    }
                }
            }
        }
    }

    @Override
    public boolean createFile() throws Exception {
        if(file.exists()) {
            throw new Exception("文件已存在!");
        }
        if(!CharUtil.isValidFileName(getName())) {
            throw new Exception("文件名不合法!");
        }
        return file.createNewFile();
    }

    @Override
    public boolean mkdir() throws Exception{
        if(file.exists()) {
            throw new Exception("文件夹已存在!");
        }
        if(!CharUtil.isValidFileName(getName())) {
            throw new Exception("文件名不合法!");
        }
        return file.mkdir();
    }

    @Override
    public boolean mkdirs() {
        return file.mkdirs();
    }

    @Override
    public String lengthString() {
        return FileApi.sizeToString(length());
    }

    @Override
    public String lastModifiedString() {
        return FileApi.timeToString(lastModified());
    }

    @Override
    public InputStream openInputStream() throws Exception {
        return new FileInputStream(file);
    }

    @Override
    public OutputStream openOutStream() throws Exception {
        return new FileOutputStream(file);
    }

    @Override
    public boolean rename(String displayName) {
        File target = new File(file.getParentFile(), displayName);
        if(target.exists()) return false;
        //////////
        if (file.renameTo(target)) {
            file = target;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean move(Context context, ManageFile manageFile) throws Exception {
        if(getESPath().equals(manageFile.getESPath())) {
            return false;
        }
        /////
        if(manageFile instanceof JFile) {
            return file.renameTo(((JFile) manageFile).getFile());
        }
        if(copy(context, manageFile)) {
            return delete();
        }
        return false;
    }

    @Override
    public boolean copy(Context context, ManageFile manageFile) throws Exception {
        if(getESPath().equals(manageFile.getESPath())) {
            return false;
        }
        ////
        if (isFile()) {
            return FileApi.copyFile(this, manageFile);
        } else {
            FileApi.copyDir(context, this, manageFile);
        }
        return manageFile.exists();
    }

    /**
     * 迭代删除文件夹
     * @param dir   文件夹
     * @return      是否删除成功
     */
    private static boolean deleteContents(File dir) {
        File[] files = dir.listFiles();
        boolean success = true;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    success &= deleteContents(file);
                }
                if (!file.delete()) {
                    success = false;
                }
            }
        }
        return success;
    }

    @NonNull
    @Override
    public String toString() {
        return "JFile{" +
                "file=" + file.toString() +
                ", tag=" + tag +
                ", check=" + check +
                ", highlight=" + highlight +
                ", target='" + (target == null) + '\'' +
                ", loadIcon=" + loadIcon +
                '}';
    }
}
