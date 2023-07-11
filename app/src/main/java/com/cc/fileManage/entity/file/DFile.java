package com.cc.fileManage.entity.file;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.UriUtils;

import org.apache.tools.zip.Zip;
import org.apache.tools.zip.ZipInput;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *  android documentFile 特殊类
 */
public class DFile extends MFile {

    private final Context   context;
    private String    name;
    private String    path;
    private final boolean   isDirectory;
    private final long      lastModified;
    private final long      length;
    private boolean         blank;          /// 空白文件

    public DFile(Context context, String path, String name, String type, long lastModified, long length) {
        this(context, new File(path, name), type, lastModified, length);
    }

    public DFile(Context context, String fullPath, String type, long lastModified, long length) {
        this(context, new File(fullPath), type, lastModified, length);
    }

    public DFile(Context context, File file, String type, long lastModified, long length) {
        this.context = context;
        this.path = file.getPath();
        this.name = file.getName();
        this.isDirectory = type.equals(DocumentsContract.Document.MIME_TYPE_DIR);
        this.lastModified = lastModified;
        this.length = length;
    }

    protected DFile setBlank(boolean blank) {
        this.blank = blank;
        return this;
    }

    @Override
    public boolean isDirectory() {
        return blank ? FileApi.isDirectory(context, getUri()) : isDirectory;
    }

    @Override
    public boolean isFile() {
        return blank ? FileApi.isFile(context, getUri()) : !isDirectory;
    }

    @Override
    public boolean isHidden() {
        return getName() == null || getName().charAt(0) == '.';
    }

    @Override
    public Uri getUri() {
        return FileApi.getDocumentUri(getPath());
    }

    @Override
    public String getName() {
        return TextUtils.isEmpty(name) ? "" : name;
    }

    @Override
    public String getPath() {
        return TextUtils.isEmpty(path) ? "" : path;
    }

    @Override
    public String getESPath() {
        return FileApi.parseExternalStorage(getPath());
    }

    @Override
    public String getParent() {
        String parent = new File(getPath()).getParent();
        return TextUtils.isEmpty(parent) ? "" : parent;
    }

    @Override
    public MFile getParentFile() {
        return MFile.create(context, getParent());
    }

    @Override
    public boolean parentCanRead() {
        return getParentFile().canRead();
    }

    @Override
    public boolean canRead() {
        return FileApi.canRead(context, getPath());
    }

    @Override
    public boolean canWrite() {
        return FileApi.canWrite(context, getPath());
    }

    @Override
    public long lastModified() {
        return blank ? FileApi.lastModified(context, getUri()) : lastModified;
    }

    @Override
    public long length() {
        return blank ? FileApi.length(context, getUri()) : length;
    }

    @Override
    public boolean delete() {
        return FileApi.delete(context, getPath());
    }

    @Override
    public boolean exists() {
        return FileApi.exists(context, getPath()) || new File(path).exists();
    }

    @Override
    public List<MFile> listFiles(boolean showHidden) {
        List<MFile> manageFiles = new ArrayList<>();
        listFiles(context, new OnFileDataListener() {
            @Override
            public void onData(MFile file, String readPath) {
                // 不显示隐藏文件 直接跳过
                if(!showHidden) {
                    if(!file.isHidden()) {
                        manageFiles.add(file);
                    }
                } else {
                    manageFiles.add(file);
                }
            }
            @Override
            public void onNoPermission() {}
            @Override
            public void onNoExist() {}
        });
        return manageFiles;
    }

    @Override
    public void listFiles(Context mContext, OnFileDataListener listener) {
        /// Android 11 以上 且是data 或 obb目录
        if(FileApi.isAndroid13()) {
            /// Android 13
            FileApi.document(mContext, getPath(), listener);
        } else {
            /// Android 11
            FileApi.documentFileLists(mContext, getPath(), listener);
        }
    }

    @Override
    public int[] countChild() {
        int[] array = new int[]{0, 0};
        countDir(context, this, array);
        return array;
    }

    // 统计
    private static void countDir(Context context, MFile dir, int[] array) {
        if (dir.isDirectory() || FileApi.isDataDir(dir.getPath())) {
            ////
            FileApi.document(context, dir.getPath(), new OnFileDataListener() {
                @Override
                public void onData(MFile file, String readPath) {
                    if(file.isFile()) {
                        array[0]++;
                    } else {
                        array[1]++;
                        countDir(context, file, array);
                    }
                }
                @Override
                public void onNoPermission() {}
                @Override
                public void onNoExist() {}
            });
        }
    }

    @Override
    public boolean createFile() throws Exception{
        return FileApi.createFile(context, getParent(), getName()) != null;
    }

    @Override
    public boolean mkdir() throws Exception{
        return FileApi.createDir(context, getParent(), getName()) != null;
    }

    @Override
    public boolean mkdirs() {
        return FileApi.mkdirs(context, getPath()) != null;
    }

    @Override
    public boolean mkdirsF() {
        return FileApi.mkdirs(context, getPath(), true) != null;
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
    public boolean rename(String displayName) {
        try {
            final Uri result = DocumentsContract.renameDocument(
                    context.getContentResolver(), getUri(), displayName);
            if (result != null) {
                File file = UriUtils.uri2File(result);
                path = file.getPath();
                name = file.getName();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public InputStream openInputStream() throws Exception {
        if(exists() && canRead())
            return context.getContentResolver().openInputStream(getUri());
        return null;
    }

    @Override
    public OutputStream openOutStream() throws Exception {
        if(exists()) {
            return context.getContentResolver().openOutputStream(getUri());
        } else if(createFile()){
            return context.getContentResolver().openOutputStream(getUri());
        }
        return null;
    }

    @Override
    public ParcelFileDescriptor openFileDescriptor(String mode) throws FileNotFoundException {
        if(exists() && canRead())
            return context.getContentResolver().openFileDescriptor(getUri(), mode);
        return null;
    }

    @Override
    public Zip openZipFile() throws IOException {
        if(exists() && isFile() && canRead())
            return new ZipInput(openFileDescriptor("r"));
        return null;
    }

    @Override
    public boolean move(Context context, MFile manageFile) throws Exception {
        if(getESPath().equals(manageFile.getESPath())) {
            return false;
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
        //////
        if (isFile()) {
            return FileApi.copyFile(this, manageFile);
        } else {
            FileApi.copyDir(context, this, manageFile);
        }
        return manageFile.exists();
    }

    @NonNull
    @Override
    public String toString() {
        return "DFile{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", tag=" + tag +
                ", check=" + check +
                ", highlight=" + highlight +
                ", target='" + (target == null) + '\'' +
                ", loadIcon=" + loadIcon +
                '}';
    }
}
