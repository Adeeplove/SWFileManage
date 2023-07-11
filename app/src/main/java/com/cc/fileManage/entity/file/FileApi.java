package com.cc.fileManage.entity.file;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.cc.fileManage.entity.MethodValue;
import com.cc.fileManage.utils.CharUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FileApi {

    public enum DIR{
        DATA("data"),
        OBB("obb"),
        MEDIA("media");

        public final String value;
        DIR(String value) {
            this.value = value;
        }
    }

    /**
     * 创建文件夹
     * @param context   上下文
     * @param path      文件夹路径
     */
    public static Uri mkdirs(Context context, String path) {
        return mkdirs(context, path, false);
    }

    /**
     * 创建文件夹
     * @param context    上下文
     * @param path       文件夹路径
     * @param createFile 首文件是否是文件
     */
    public static Uri mkdirs(Context context, String path, boolean createFile) {
       Uri childUri = null;
       try {
           List<MethodValue<String, Uri>> documentFiles = new ArrayList<>();
           // 迭代
           createDocumentFileDirs(context, path, documentFiles);
           //
           if(documentFiles.size() > 0) {
               childUri = documentFiles.get(0).getValueTwo();
               for (int i = documentFiles.size() - 1; i >= 0; i--){
                   MethodValue<String, Uri> uriMethodValue = documentFiles.get(i);
                   String key = uriMethodValue.getValueOne();          //文件夹名
                   Uri value = uriMethodValue.getValueTwo();           //父文件夹uri
                   //创建文件夹
                   if(childUri != null) {
                       if(createFile && i == 0) {
                           childUri = createFile(context, value, key);
                       } else {
                           childUri = createDir(context, value, key);
                       }
                   } else {
                       break;
                   }
               }
           }
       } catch (Exception e){
           e.printStackTrace();
       }
       return childUri;
    }

    /**
     *   递归获取需要创建的父路径
     */
    private static void createDocumentFileDirs(Context context, String path, List<MethodValue<String, Uri>> documentFiles) {
        //当前文件
        File file = new File(path);
        // 父路径是否为空
        String parent = file.getParent();
        if(parent == null) return;
        //获取父目录的uri
        Uri uri = getDocumentUri(parent);
        //如果父目录不存在或不是文件夹 继续往上层执行
        if(!exists(context, uri)) {
            //put 要创建的子文件名  父文件uri
            documentFiles.add(new MethodValue<>(file.getName(), uri));
            //迭代
            createDocumentFileDirs(context, parent, documentFiles);
        } else if(!exists(context, getDocumentUri(path))){
            //put 要创建的子文件名  父文件uri
            documentFiles.add(new MethodValue<>(file.getName(), uri));
        }
    }

    /**
     * 创建文件
     * @param path 文件路径
     */
    public static Uri createFile(Context context, String path, String fileName) throws Exception{
        //查看此路径是否存在
        Uri file = getDocumentUri(path);
        //
        if(DocumentFile.isDocumentUri(context, file)) {
            return createFile(context, file, fileName);
        }
        return null;
    }

    /**
     * 创建文件
     * @param context       上下文
     * @param uri           文件uri
     * @param fileName      文件名称
     * @return              创建的文件uri
     */
    public static Uri createFile(Context context, Uri uri, String fileName) throws Exception{
        //文件是否存在
        if(!TextUtils.isEmpty(fileName)){
            Uri fileUri = Uri.parse(uri.toString() + "%2F" + fileName);
            if(exists(context, fileUri)){
                throw new Exception("文件夹已存在!");
            }
        }
        /// 文件名是否合法
        if(!CharUtil.isValidFileName(fileName)) {
            throw new Exception("文件名不合法!");
        }
        //
        return DocumentsContract.createDocument(context.getContentResolver(),
                uri, "application/octet-stream", fileName);
    }

    /**
     * 创建单个文件夹
     * @param path      路径
     * @param dirName   创建的文件名
     * @param context   上下文
     */
    public static Uri createDir(Context context, String path, String dirName) throws Exception{
        //
        Uri file = getDocumentUri(path);
        //是否可以访问
        if(DocumentFile.isDocumentUri(context, file)){
            //
            String fileName = dirName.endsWith(File.separator) ? dirName.substring(0, dirName.length() - 1) : dirName;
            //
            return createDir(context, file, fileName);
        }
        return null;
    }

    /**
     * 创建文件夹
     * @param context   上下文
     * @param uri       文件uri
     * @param dirName   子文件夹名称
     * @return          创建的文件夹的uri
     */
    public static Uri createDir(Context context, Uri uri, String dirName) throws Exception{
        //
        if(!TextUtils.isEmpty(dirName)){
            Uri fileUri = Uri.parse(uri.toString() + "%2F" + dirName);
            if(exists(context, fileUri)){
                throw new Exception("文件已存在!");
            }
        }
        /// 文件名是否合法
        if(!CharUtil.isValidFileName(dirName)) {
            throw new Exception("文件名不合法!");
        }
        return DocumentsContract.createDocument(context.getContentResolver(),
                uri, DocumentsContract.Document.MIME_TYPE_DIR, dirName);
    }

    /**
     * 文件是否存在
     * @param context   上下文
     * @param filePath  文件路径
     * @return          文件是否存在
     */
    public static boolean exists(Context context, String filePath) {
        return exists(context, getDocumentUri(filePath));
    }

    /**
     * 文件是否存在
     * @param context   上下文
     * @param self      uri
     * @return          文件是否存在
     */
    public static boolean exists(Context context, Uri self) {
        final ContentResolver resolver = context.getContentResolver();
        Cursor c = null;
        try {
            c = resolver.query(self, new String[] {
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID }, null, null, null);
            return c.getCount() > 0;
        } catch (Exception e) {
            return false;
        } finally {
            closeQuietly(c);
        }
    }

    /**
     * 删除文件
     * @param context   上下文
     * @param path      文件luj
     * @return          结果
     */
    public static boolean delete(Context context, String path) {
        return delete(context, getDocumentUri(path));
    }

    /**
     * 删除文件
     * @param context   上下文
     * @param uri       文件uri
     * @return          结果
     */
    public static boolean delete(Context context, Uri uri) {
        try {
            return DocumentsContract.deleteDocument(context.getContentResolver(), uri);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 查询uri文件名
     * @param context   上下文
     * @param self      uri
     * @return          文件名
     */
    @Nullable
    public static String getName(Context context, Uri self) {
        return queryForString(context, self, DocumentsContract.Document.COLUMN_DISPLAY_NAME);
    }

    /**
     * 查询uri文件名类型
     * @param context   上下文
     * @param self      uri
     * @return          文件类型
     */
    @Nullable
    private static String getRawType(Context context, Uri self) {
        return queryForString(context, self, DocumentsContract.Document.COLUMN_MIME_TYPE);
    }

    /**
     * 查询uri文件类型
     * @param context   上下文
     * @param self      uri
     * @return          文件类型
     */
    @Nullable
    public static String getType(Context context, Uri self) {
        final String rawType = getRawType(context, self);
        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(rawType)) {
            return null;
        } else {
            return rawType;
        }
    }

    /**
     * uri文件是否是文件夹
     * @param context   上下文
     * @param self      uri
     * @return          是否是文件夹
     */
    public static boolean isDirectory(Context context, Uri self) {
        return DocumentsContract.Document.MIME_TYPE_DIR.equals(getRawType(context, self));
    }

    /**
     * uri文件是否是文件
     * @param context   上下文
     * @param self      uri
     * @return          是否是文件
     */
    public static boolean isFile(Context context, Uri self) {
        final String type = getRawType(context, self);
        return !DocumentsContract.Document.MIME_TYPE_DIR.equals(type) && !TextUtils.isEmpty(type);
    }

    /**
     * uri文件最后修改时间
     * @param context   上下文
     * @param self      uri
     * @return          最后修改时间
     */
    public static long lastModified(Context context, Uri self) {
        return queryForLong(context, self, DocumentsContract.Document.COLUMN_LAST_MODIFIED);
    }

    /**
     * uri文件长度
     * @param context   上下文
     * @param self      uri
     * @return          文件长度
     */
    public static long length(Context context, Uri self) {
        return queryForLong(context, self, DocumentsContract.Document.COLUMN_SIZE);
    }

    /**
     * uri文件是否可读
     * @param context   上下文
     * @param path      文件路径
     * @return          是否可读
     */
    public static boolean canRead(Context context, String path) {
        return canRead(context, getDocumentUri(path));
    }

    /**
     * uri文件是否可读
     * @param context   上下文
     * @param self      uri
     * @return          是否可读
     */
    public static boolean canRead(Context context, Uri self) {
        // Ignore if grant doesn't allow read
        if (context.checkCallingOrSelfUriPermission(self, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        // Ignore documents without MIME
        return !TextUtils.isEmpty(queryForString(context, self, DocumentsContract.Document.COLUMN_MIME_TYPE));
    }

    /**
     * uri文件是否可写
     * @param context   上下文
     * @param path      文件路径
     * @return          是否可写
     */
    public static boolean canWrite(Context context, String path) {
        return canWrite(context, getDocumentUri(path));
    }

    /**
     * uri文件是否可写
     * @param context   上下文
     * @param self      uri
     * @return          是否可写
     */
    public static boolean canWrite(Context context, Uri self) {
        // Ignore if grant doesn't allow write
        if (context.checkCallingOrSelfUriPermission(self, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        final String type = getRawType(context, self);
        final int flags = queryForInt(context, self);

        // Ignore documents without MIME
        if (TextUtils.isEmpty(type)) {
            return false;
        }

        // Deletable documents considered writable
        if ((flags & DocumentsContract.Document.FLAG_SUPPORTS_DELETE) != 0) {
            return true;
        }

        // Writable normal files considered writable
        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(type)
                && (flags & DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE) != 0) {
            // Directories that allow create considered writable
            return true;
        } else {
            return !TextUtils.isEmpty(type)
                    && (flags & DocumentsContract.Document.FLAG_SUPPORTS_WRITE) != 0;
        }
    }

    @Nullable
    private static String queryForString(Context context, Uri self, String column) {
        final ContentResolver resolver = context.getContentResolver();
        Cursor c = null;
        try {
            c = resolver.query(self, new String[] { column }, null, null, null);
            if (c.moveToFirst() && !c.isNull(0)) {
                return c.getString(0);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            closeQuietly(c);
        }
    }

    private static int queryForInt(Context context, Uri self) {
        return (int) queryForLong(context, self, DocumentsContract.Document.COLUMN_FLAGS);
    }

    private static long queryForLong(Context context, Uri self, String column) {
        final ContentResolver resolver = context.getContentResolver();
        Cursor c = null;
        try {
            c = resolver.query(self, new String[] { column }, null, null, null);
            if (c.moveToFirst() && !c.isNull(0)) {
                return c.getLong(0);
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        } finally {
            closeQuietly(c);
        }
    }

    public static void closeQuietly(@Nullable AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    //====================================其他方法=====================================
    /**
     * 查询data或obb下的子目录
     * @param context       上下文
     * @param fullPath      父路径
     * @param listener      回调接口
     */
    private static void queryDataFile(Context context, String fullPath, OnFileDataListener listener) {
        ///
        PackageManager packageManager = context.getPackageManager();
        // flag 0查询速度比 PackageManager.GET_ACTIVITIES 快
        List<PackageInfo> packageInfo = packageManager.getInstalledPackages(0);
        ////
        if(packageInfo != null && packageInfo.size() > 0) {
            for (PackageInfo info : packageInfo) {
                File file = new File(fullPath, info.packageName);
                if(file.exists()) {
                    /// 构建对象  type  directory 文件夹
                    MFile manageFile = file.canRead() ? new JFile(file) : new DFile(context, file,
                            DocumentsContract.Document.MIME_TYPE_DIR, file.lastModified(), 0L);
                    /// 回调
                    listener.onData(manageFile, fullPath);
                }
            }
        }
    }

    /**
     * Android13访问data或obb目录
     * @param file      文件
     * @param listener  回调接口
     */
    public static void document(Context context, String file, OnFileDataListener listener) {
        // 如果是data或者obb目录
        if(FileApi.isDataDir(file)) {
            // 查询
            queryDataFile(context, file, listener);
        } else {
            // 调用
            documentFileLists(context, file, listener);
        }
    }

    /**
     * 获取document文件夹下所有子文件
     * @param context   上下文
     * @param filePath  文件路径
     */
    public static void documentFileLists(Context context, String filePath, OnFileDataListener listener) {
        documentFileLists(context, getDocumentUri(filePath), listener);
    }

    /**
     * 获取document文件夹下所有子文件
     * @param context   上下文
     * @param uri       文件uri
     */
    public static void documentFileLists(Context context, Uri uri, OnFileDataListener listener) {
        if(exists(context, uri) && canRead(context, uri)) {
            //
            Uri childrenUri = DocumentsContract
                    .buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri));
            //
            try (Cursor cursor = context.getContentResolver().query(childrenUri, new String[]{
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED, DocumentsContract.Document.COLUMN_SIZE
            }, null, null, null) ){
                /// 文件路径
                String filePath = getDocumentFilePath(uri.getLastPathSegment());
                // 迭代
                while (cursor.moveToNext()) {
                    // 构建对象  type  directory 文件夹
                    MFile manageFile = new DFile(context, filePath,
                            cursor.getString(0),
                            cursor.getString(1),
                            cursor.getLong(2),
                            cursor.getLong(3));
                    // 回调数据
                    listener.onData(manageFile, filePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // 读取失败
            listener.onNoPermission();
        }
    }

    /**
     * 获取document文件夹下所有子文件
     * @param context   上下文
     * @param rootFile  DocumentFile
     * @return          子文件集合
     */
    public static List<DocumentFile> documentFileLists(Context context, DocumentFile rootFile) {
        Cursor cursor = null;
        List<DocumentFile> data = new ArrayList<>();
        try {
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootFile.getUri(),
                    DocumentsContract.getDocumentId(rootFile.getUri()));
            cursor = context.getContentResolver().
                    query(childrenUri, new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID},
                            null, null, null);
            while (cursor.moveToNext()) {
                String documentId = cursor.getString(0);
                Uri cui = DocumentsContract.buildDocumentUriUsingTree(rootFile.getUri(), documentId);
                ///
                DocumentFile documentFile = DocumentFile.fromSingleUri(context, cui);
                data.add(documentFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(cursor);
        }
        return data;
    }

    /**
     *  是否是android11的数据目录或子目录
     * @param       file 文件路径
     * @return     是否是android 数据目录
     */
    public static boolean isAndroidDataDir(File file){
        return isAndroid11() && isDataDirChild(file);
    }

    /**
     * 是否是Android11及以上
     * @return      是否是Android11
     */
    public static boolean isAndroid11(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

    /**
     * 是否是Android13及以上
     * @return      是否是Android13
     */
    public static boolean isAndroid13(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2;
    }

    /**
     *  是否是android13的dara或obb数据目录
     * @param       file 文件路径
     * @return     是否是android 数据目录
     */
    public static boolean isDataDir(File file) {
        return isDataDir(file.getPath());
    }

    /**
     *  是否是android13的dara或obb数据目录
     * @param       path 文件路径
     * @return     是否是android 数据目录
     */
    public static boolean isDataDir(String path) {
        String filePath = parseExternalStorage(path);
        return filePath.equals(getAndroidChild(DIR.DATA))
                || filePath.equals(getAndroidChild(DIR.OBB));
    }

    /**
     * 是否啊data或obb目录或其子目录
     * @param file  文件路径
     * @return      是否是android数据目录或其子目录
     */
    public static boolean isDataDirChild(File file) {
        return isDataDirChild(file.getPath());
    }

    /**
     * 是否啊data或obb目录
     * @param path  文件路径
     * @return      是否是android 数据目录
     */
    public static boolean isDataDirChild(String path) {
        String filePath = parseExternalStorage(path);
        return filePath.startsWith(getAndroidChild(DIR.DATA))
                || filePath.startsWith(getAndroidChild(DIR.OBB));
    }

    /**
     * 获取Android下的data或obb目录路径
     * @param dir       文件夹名
     * @return          data或obb目录路径
     */
    public static String getAndroidChild(DIR dir) {
        return getExternalStorageDirectory() + File.separator
                + "Android" + File.separator + dir.value;
    }

    /**
     * /sdcard替换为内部储存路径
     * @param file  路径
     * @return      内部储存路径
     */
    public static String parseExternalStorage(String file) {
        String start = File.separator + "sdcard";
        if(file.startsWith(start)) {
            return getExternalStorageDirectory() + file.substring(start.length());
        }
        return file;
    }

    /**
     * 根据路径创建DocumentFile
     * @param context       上下文
     * @param filePath      文件路径
     * @return              DocumentFile
     */
    public static DocumentFile getDocumentFile(Context context, String filePath){
        //根据路径获取uri路径
        Uri uri = getDocumentUri(filePath);
        if(uri != null && DocumentFile.isDocumentUri(context, uri)){
            //获取documentFile实例
            return DocumentFile.fromSingleUri(context, uri);
        }
        return null;
    }

    /**
     * 文件路径转Uri
     * @param file  Android/data or obb 下的文件路径
     * @return      uri
     */
    public static Uri getDocumentUri(String file) {
        if(file == null) return Uri.parse("NULL");
        ////
        int index = file.indexOf("Android");
        ////
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(ContentResolver.SCHEME_CONTENT);
        builder.authority("com.android.externalstorage.documents");
        // 包含Android
        if(index > 0) {
            String child = file.substring(index);
            String tree  = parseTree(child);
            builder.appendPath("tree");
            builder.appendPath("primary:" + tree);
            ///////
            builder.appendPath("document");
            builder.appendPath("primary:" + child);
        }
        return builder.build();
    }

    /**
     * uri 头
     * @param path  路径
     * @return      tree 头
     */
    public static String parseTree(String path) {
        int index = findString(path, File.separator, isAndroid13() ? 3 : 2);
        if(index > 0)
            return path.substring(0, index);
        return path;
    }

    /**
     * 获取内部储存目录
     * @return  内部储存目录
     */
    public static String getExternalStorageDirectory(){
        //内部储存正常挂载返回对应路径
        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) return "";
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * 获取android下文件全路径
     * @param lastPath  uri路径
     * @return          android下文件全路径
     */
    public static String getDocumentFilePath(String lastPath){
        String content = "primary:";
        if(lastPath.startsWith(content)){
            String storagePath = getExternalStorageDirectory() + File.separator;
            return lastPath.replace(content, storagePath);
        }
        return lastPath;
    }

    /**
     * 复制文件
     * @param sourceFile    源文件
     * @param targetFile    新路径文件
     */
    public static boolean copyFile(MFile sourceFile, MFile targetFile) throws Exception{
        // 验证
        if(sourceFile == null || targetFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
            return false;
        }
        ////
        if(sourceFile.getESPath().equals(targetFile.getESPath())) {
            return false;
        }
        // 如果目标文件存在
        if(targetFile.exists()) {
            boolean fileDirectory = !targetFile.isDirectory() || targetFile.delete();
            /// 写出文件
            if(fileDirectory) {
                writeFile(sourceFile.openInputStream(), targetFile.openOutStream());
                return true;
            }
        } else if(createOrExistsDir(targetFile.getParentFile())) {
            writeFile(sourceFile.openInputStream(), targetFile.openOutStream());
            return true;
        }
        return false;
    }

    /**
     * 复制文件夹
     * @param sourceFile    源文件夹
     * @param targetFile    目标路径
     */
    public static void copyDir(Context context, MFile sourceFile, MFile targetFile) throws Exception{
        // 验证
        if(sourceFile == null || targetFile == null || !sourceFile.exists() || !sourceFile.isDirectory()) {
            return;
        }
        // 创建目录
        if (createOrExistsDir(targetFile)) {
            // 目录路径
            String destPath = targetFile.getPath() + File.separator;
            List<MFile> files = sourceFile.listFiles(true);
            if (files != null && files.size() > 0) {
                for (MFile file : files) {
                    // 目标文件
                    MFile destFile = MFile.create(context, destPath + file.getName());
                    if (file.isFile()) {
                        // 复制文件
                        copyFile(file, destFile);
                    } else if (file.isDirectory()) {
                        // 复制文件夹
                        copyDir(context, file, destFile);
                    }
                }
            }
        }
    }

    /**
     * 如果目标文件夹不存在则创建
     * @param file  文件夹
     * @return      是否创建成功
     */
    @SuppressWarnings("all")
    public static boolean createOrExistsDir(MFile file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 如果目标文件夹不存在则创建
     * @param file  文件夹
     * @return      是否创建成功
     */
    public static boolean createOrExistsDir(File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 写出文件
     * @param inputStream   输入流
     * @param outputStream  文件输出流
     */
    public static void writeFile(InputStream inputStream, OutputStream outputStream) throws Exception{
        byte [] buf = new byte[1024];
        try (inputStream;
             BufferedOutputStream bos = new BufferedOutputStream(outputStream, buf.length)) {
            //文件写出
            int len;
            while((len = inputStream.read(buf,0, buf.length)) != -1){
                bos.write(buf,0, len);
            }
            bos.flush();
        }
    }

    /**
     * 文件大小转字符
     * @param fileLength    文件产长度
     * @return              文件长度
     */
    public static String sizeToString(long fileLength){
        if(fileLength > 0){
            DecimalFormat df = new DecimalFormat("#.00");
            if (fileLength < 1024) {
                return Double.valueOf(fileLength).intValue() + "B";
            } else if (fileLength < 1048576) {
                return df.format((double) fileLength / 1024) + "KB";
            } else if (fileLength < 1073741824) {
                return df.format((double) fileLength / 1048576) + "MB";
            } else {
                return df.format((double) fileLength / 1073741824) +"GB";
            }
        }
        return "0B";
    }

    /**
     * 时间转日期格式
     * @param time  时间
     * @return      时间
     */
    public static String timeToString(long time){
        DateFormat dateFormat = SimpleDateFormat.getInstance();
        return dateFormat.format(time);
    }

    /**
     * 查找字符位置
     * @param str   字符串
     * @param s     要查找的字符
     * @param size  第几个字符
     * @return      查找的字符位置
     */
    public static int findString(String str, String s, int size){
        if(TextUtils.isEmpty(str) || TextUtils.isEmpty(s)) return -1;
        //字符数组
        char[] ch = str.toCharArray();
        for(int i = 0; i < ch.length; i++) {
            if(i + s.length() > ch.length) break;
            for(int x = 0; x < s.length(); x++) {
                if(ch[i + x] != s.charAt(x)){
                    if(ch[i + x] != s.charAt(0)) {
                        i += x;
                    }
                    break;
                }
                //
                if(s.length() - x == 1) {
                    i += x;
                    size--;
                    if(size <= 0) return i;
                }
            }
        }
        return -1;
    }

    /**
     * 读取文件内容
     * @param inputStream   输入流
     * @return              文件内容
     */
    public static StringBuilder readFile(InputStream inputStream) throws Exception {
        return readFile(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * 读取文件内容
     * @param inputStream   输入流
     * @param charset       编码
     * @return              文件内容
     */
    public static StringBuilder readFile(InputStream inputStream, Charset charset) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStreamReader streamReader = new InputStreamReader(inputStream, charset);
             BufferedReader reader = new BufferedReader(streamReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder;
    }

    /**
     * 是否是二进制文件
     * @param input     输入流
     * @return          是否是二进制文件
     */
    public static boolean isBinary(InputStream input, long length)
    {
        boolean isBinary = false;
        try {
            for (int j = 0; j < (int) length; j++) {
                int t = input.read();
                if (t < 32 && t != 9 && t != 10 && t != 13) {
                    isBinary = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isBinary;
    }
}
