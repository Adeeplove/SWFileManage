package com.cc.fileManage.entity.file;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.cc.fileManage.utils.CharUtil;

import org.apache.tools.zip.Zip;
import org.apache.tools.zip.ZipFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *  java file 特殊类
 */
public class JFile extends MFile {

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
    public MFile getParentFile() {
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
    public List<MFile> listFiles(boolean showHidden) {
        List<MFile> manageFiles = new ArrayList<>();
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
        if(!file.exists()) {
            if(!CharUtil.isValidFileName(getName())) {
                throw new Exception("文件名不合法!");
            }
            return file.createNewFile();
        }
        return true;
    }

    @Override
    public boolean createFiles() throws Exception {
        if(!file.exists()) {
            if(!CharUtil.isValidFileName(getName())) {
                throw new Exception("文件名不合法!");
            }
            MFile f = getParentFile();
            if(!f.exists() && f.mkdirs()) {
                return file.createNewFile();
            } else {
                return file.createNewFile();
            }
        }
        return true;
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
        if(file.exists() && file.isFile()) {
            if(file.canRead()) {
                return new FileInputStream(file);
            }
            throw new IOException("读文件失败");
        }
        throw new IOException("文件不存在");
    }

    @Override
    public OutputStream openOutStream() throws Exception {
        if(file.exists()) {
            return new FileOutputStream(file);
        } else if(createFiles()) {
            return new FileOutputStream(file);
        }
        throw new IOException("文件不存在");
    }

    @Override
    public ParcelFileDescriptor openFileDescriptor(String mode) throws FileNotFoundException {
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.parseMode(mode));
    }

    @Override
    public Zip openZipFile() throws IOException {
        if(exists() && isFile() && canRead())
            return new ZipFile(file);
        return null;
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
    public boolean move(Context context, MFile manageFile) throws Exception {
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
    public boolean copy(Context context, MFile manageFile) throws Exception {
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
