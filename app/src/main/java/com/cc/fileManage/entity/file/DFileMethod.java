package com.cc.fileManage.entity.file;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.text.TextUtils;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DFileMethod {

    /**
     * 创建文件夹
     * @param context   上下文
     * @param path      文件夹路径
     */
    public static Uri mkdirs(Context context, String path){
       Uri childUri = null;
       try {
           Map<String, Uri> documentFiles = new HashMap<>();
           //
           createDocumentFileDirs(context, path, documentFiles);
           //
           if(documentFiles.size() > 0){
               Object[] array = documentFiles.keySet().toArray();
               //
               for (int i = documentFiles.size()-1 ; i >= 0; i--){
                   String key = (String) array[i];          //文件夹名
                   Uri value = documentFiles.get(key);      //父文件夹uri
                   //创建文件夹
                   childUri = createDir(context, value, key);
               }
           }
       }catch (Exception e){
           e.printStackTrace();
       }
       return childUri;
    }

    /**
     *   xx/cc
     */
    private static void createDocumentFileDirs(Context context, String path, Map<String,Uri> documentFiles){
        //当前文件
        File file = new File(path);
        //获取父目录的uri
        Uri uri = getDocumentUri(Objects.requireNonNull(file.getParent()));

        //put 要创建的子文件名  父文件uri
        documentFiles.put(file.getName(), uri);

        //如果父目录不存在或不是文件夹 继续往上层执行
        if(!exists(context, uri) || !isDirectory(context, uri)){
            //迭代
            createDocumentFileDirs(context, file.getParent(), documentFiles);
        }
    }

    /**
     * 创建文件
     * @param path 文件路径
     */
    public static Uri createFile(Context context, String path, String fileName) {
        //查看此路径是否存在
        Uri file = getDocumentUri(path);
        //
        if(DocumentFile.isDocumentUri(context, file)){
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
    public static Uri createFile(Context context, Uri uri, String fileName) {
        try{
            //文件是否存在
            if(!TextUtils.isEmpty(fileName)){
                Uri fileUri = Uri.parse(uri.toString() + "%2F" + fileName);
                if(exists(context, fileUri)){
                    //存在即返回
                    return null;
                }
            }
            //
            return DocumentsContract.createDocument(context.getContentResolver(),
                    uri, "application/octet-stream", fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建单个文件夹
     * @param path      路径
     * @param dirName   创建的文件名
     * @param context   上下文
     */
    public static Uri createDir(Context context, String path, String dirName) {
        //
        Uri file = getDocumentUri(path);
        //是否可以访问
        if(DocumentFile.isDocumentUri(context, file)){
            //
            String fileName = dirName.endsWith(File.separator) ? dirName.substring(0,dirName.length() - 1) : dirName;
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
    public static Uri createDir(Context context, Uri uri, String dirName) {
        try{
            //
            if(!TextUtils.isEmpty(dirName)){
                Uri fileUri = Uri.parse(uri.toString() + "%2F" + dirName);
                if(exists(context, fileUri)){
                    //存在即返回
                    return null;
                }
            }
            return DocumentsContract.createDocument(context.getContentResolver(),
                    uri, DocumentsContract.Document.MIME_TYPE_DIR, dirName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
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
            if (c != null) {
                try {
                    c.close();
                } catch (RuntimeException rethrown) {
                    throw rethrown;
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * 文件是否是文件夹
     * @param context   上下文
     * @param self      文件uri
     * @return          文件是否是文件夹
     */
    public static boolean isDirectory(Context context, Uri self){
        String type = queryForString(context, self, DocumentsContract.Document.COLUMN_MIME_TYPE);
        return DocumentsContract.Document.MIME_TYPE_DIR.equals(type);
    }

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
            if (c != null) {
                try {
                    c.close();
                } catch (RuntimeException rethrown) {
                    throw rethrown;
                } catch (Exception ignored) {
                }
            }
        }
    }


    //====================================其他方法=====================================
    public static List<ManageFile> documentFileLists(Context context, String filePath){
        Uri file = getDocumentUri(filePath);
        //是否可以访问
        if(DocumentFile.isDocumentUri(context, file) && exists(context, file)){
            return documentFileLists(context, file);
        }
        return new ArrayList<>();
    }

    /**
     * 获取document文件夹下所有子文件
     * @param context   上下文
     * @param uri       文件uri
     * @return          子文件集合
     */
    public static List<ManageFile> documentFileLists(Context context, Uri uri){
        Cursor cursor = null;
        List<ManageFile> data = new ArrayList<>();
        try {
            if(!exists(context, uri)) return data;
            //
            Uri childrenUri = DocumentsContract
                    .buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri));
            //查询数据库
            cursor = context.getContentResolver().query(childrenUri, new String[]{
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED, DocumentsContract.Document.COLUMN_SIZE
            }, null, null, null);

            //迭代
            while (cursor.moveToNext()) {
                //构建对象  type  directory 文件夹
                ManageFile manageFile = new DFile(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getLong(2),
                        cursor.getLong(3));
                //完整的文件路径
                manageFile.setFilePath(getDocumentFilePath(uri.getLastPathSegment())
                        + File.separatorChar + manageFile.getFileName());
                //添加
                data.add(manageFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return data;
    }

    public static List<DocumentFile> documentFileLists(Context context, DocumentFile docu){
        Cursor cursor = null;
        List<DocumentFile> data = null;
        try {
            data = new ArrayList<>();

            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(docu.getUri(),
                    DocumentsContract.getDocumentId(docu.getUri()));
            cursor = context.getContentResolver().
                    query(childrenUri, new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID},
                            null, null, null);
            while (cursor.moveToNext()) {
                String documentId = cursor.getString(0);
                Uri cui = DocumentsContract.buildDocumentUriUsingTree(docu.getUri(), documentId);

                DocumentFile documentFile = DocumentFile.fromSingleUri(context, cui);
                data.add(documentFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return data;
    }

    /**
     * delete dir 删除文件夹
     * @param dir 待删除的文件夹
     * @return    结果
     */
    public static boolean deleteDir(Context context, DocumentFile dir) {
        if (dir.isDirectory()) {
            List<DocumentFile> docu = documentFileLists(context, dir);
            for(DocumentFile df : docu){
                boolean success = deleteDir(context, df);
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    /**
     *  是否是android 数据目录
     * @param       file 文件路径
     * @return     是否是android 数据目录
     */
    public static boolean isAndroidDataDir(File file){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && (file.getAbsolutePath().startsWith(getAndroidPath() + File.separator + "data")
                || file.getAbsolutePath().startsWith(getAndroidPath() + File.separator + "obb"));
    }

    //获取document文件
    public static DocumentFile getDocumentFile(Context context, String filePath){
        DocumentFile documentFile = null;
        try{
            //根据路径获取uri路径
            Uri uri = getDocumentUri(filePath);
            if(uri != null && DocumentFile.isDocumentUri(context, uri)){
                //获取documentFile实例
                documentFile = DocumentFile.fromSingleUri(context, uri);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return documentFile;
    }

    //根据路径获取document file uri
    public static Uri getDocumentUri(String path){
        Uri uri;
        if (path.startsWith(getAndroidPath() + File.separator + "data")) {
            uri = getDataFileUri(path, "data");     //data
        } else {
            uri = getDataFileUri(path, "obb");      //obb
        }
        return uri;
    }

    //路径转android路径下的uri路径
    private static Uri getDataFileUri(String path, String dir){
        String[] paths = path.replaceAll(getAndroidPath()+ File.separator + dir, "").split(File.separator);
        StringBuilder stringBuilder = new
                StringBuilder("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2F"
                +dir+"/document/primary%3AAndroid%2F"+dir);
        //
        for (String files : paths) {
            if (files.length() == 0) continue;
            stringBuilder.append("%2F").append(files);
        }
        return Uri.parse(stringBuilder.toString());
    }

    //内存储存目录
    public static String getExternalStorageDirectory(){
        //内部储存正常挂载返回对应路径
        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))return "";
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    //获取android根路径
    public static String getAndroidPath(){
        return getExternalStorageDirectory() + File.separator + "Android";
    }

    //获取android下文件全路径
    public static String getDocumentFilePath(String lastPath){
        String content = "primary:";
        if(lastPath.startsWith(content)){
            String storagePath = getExternalStorageDirectory() + File.separator;
            return lastPath.replace(content, storagePath);
        }
        return lastPath;
    }

    //父目录是否可读
    public static boolean isParentCanRead(String path){
        //获取父目录
        File file = new File(path).getParentFile();
        if(file == null){
            return false;
        }
        //如果父目录存在且可读
        if(file.exists() && file.canRead()){
            return true;
        }
        else {
            return isAndroidDataDir(file);
        }
    }
}
