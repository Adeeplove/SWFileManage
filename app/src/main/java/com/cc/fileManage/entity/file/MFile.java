package com.cc.fileManage.entity.file;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import org.apache.tools.zip.Zip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * 文件
 * @author sowhat
 */
public abstract class MFile {

    // 是否是标签
    protected boolean       tag;
    // 是否被选中
    protected boolean       check;
    // 是否高亮
    protected boolean       highlight;
    // 写出文件
    protected MFile         target;
    // 是否读取图标
    protected boolean       loadIcon;

    /**
     * 创建一个空白文件
     * @param context   上下文
     * @param fullPath  文件路径
     * @return          MFile
     */
    public static MFile create(Context context, String fullPath) {
        File file = new File(fullPath);
        if(!file.canRead() && FileApi.isAndroidDataDir(file)) {
            return new DFile(context, file,
                    "", 0L, 0L).setBlank(true);
        }
        return new JFile(file);
    }

    /**
     * 创建一个TAG
     * @return          ManageFile
     */
    public static MFile createTag() {
        return new JFile(true);
    }


    // 文件名称
    public abstract String getName();

    // 文件uri
    public abstract Uri getUri();

    // 文件路径
    public abstract String getPath();

    // 获取SD文件路径
    public abstract String getESPath();

    // 父文件路径
    public abstract String getParent();

    // 父文件
    public abstract MFile getParentFile();

    // 父文件路径是否可读
    public abstract boolean parentCanRead();

    // 是否是文件夹
    public abstract boolean isDirectory();

    // 是否是文件
    public abstract boolean isFile();

    // 是否是隐藏文件
    public abstract boolean isHidden();

    // 是否可读
    public abstract boolean canRead();

    // 是否可写
    public abstract boolean canWrite();

    // 最后修改时间
    public abstract long lastModified();

    // 长度
    public abstract long length();

    // 删除
    public abstract boolean delete();

    // 是否存在
    public abstract boolean exists();

    // listFiles
    public abstract List<MFile> listFiles(boolean showHidden);

    // 列出子文件
    public abstract void listFiles(Context context, OnFileDataListener listener);

    /**
     * 统计子文件夹文件跟文件夹数量
     * @return Integer[] [0]文件数 [1]文件夹数
     */
    public abstract int[] countChild();

    // 创建文件
    public abstract boolean createFile() throws Exception;

    // 创建文件(包括父目录)
    public abstract boolean createFiles() throws Exception;

    // 创建文件夹
    public abstract boolean mkdir() throws Exception;

    // 创建文件夹(包括父目录)
    public abstract boolean mkdirs();

    // 文件长度 格式字符串
    public abstract String lengthString();

    // 最后修改时间 格式字符串
    public abstract String lastModifiedString();

    // 重命名
    public abstract boolean rename(String displayName);

    // 移动文件至MFile
    public abstract boolean move(Context context, MFile manageFile) throws Exception;

    // 复制文件至MFile
    public abstract boolean copy(Context context, MFile manageFile) throws Exception;

    // 打开输入流
    public abstract InputStream openInputStream() throws Exception;

    // 打开输出流
    public abstract OutputStream openOutStream() throws Exception;

    // 打开文件描述符
    public abstract ParcelFileDescriptor openFileDescriptor(String mode) throws FileNotFoundException;

    // 打开zip压缩包
    public abstract Zip openZipFile() throws IOException;

    //============================================================//
    public MFile getTarget() {
        return target;
    }

    public void setTarget(MFile target) {
        this.target = target;
    }

    public boolean isTag() {
        return tag;
    }

    public void setTag(boolean tag) {
        this.tag = tag;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public boolean isHighlight() {
        return highlight;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public boolean isLoadIcon() {
        return loadIcon;
    }

    public void setLoadIcon(boolean loadIcon) {
        this.loadIcon = loadIcon;
    }

    @NonNull
    @Override
    public abstract String toString();
}
