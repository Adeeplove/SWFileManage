package com.cc.fileManage.task.fileBrowser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.cc.fileManage.entity.file.DFile;
import com.cc.fileManage.entity.file.DFileMethod;
import com.cc.fileManage.entity.file.FileComparator;
import com.cc.fileManage.entity.file.JFile;
import com.cc.fileManage.entity.file.ManageFile;
import com.cc.fileManage.task.AsynchronousTask;
import com.cc.fileManage.utils.ProgressDialogUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBrowserLoadTask extends AsynchronousTask<List<ManageFile>,String> {

    private final WeakReference<Context> weakReference;
    ///
    private AlertDialog dialog;         //加载弹窗

    //访问的路径
    private String path;                //访问路径
    //要高亮的item
    private String showItem;            //高亮的item名称

    private boolean canReadSystemPath;  //是否能访问系统目录
    private boolean isShowHideFile;     //是否显示隐藏文件

    private int i = 0, fileIndex = 0;   //高亮的文件下标

    private boolean isLoading = true;   //加载弹框

    private OnFileChangeListener onLoadFilesListener;

    public void setOnLoadFilesListener(OnFileChangeListener onLoadFilesListener) {
        this.onLoadFilesListener = onLoadFilesListener;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setShowItem(String showItem) {
        this.showItem = showItem;
    }

    public String getShowItem() {
        return showItem;
    }

    public void setCanReadSystemPath(boolean canReadSystemPath) {
        this.canReadSystemPath = canReadSystemPath;
    }

    public boolean isCanReadSystemPath() {
        return canReadSystemPath;
    }

    public void setIsShowHideFile(boolean isShowHideFile) {
        this.isShowHideFile = isShowHideFile;
    }

    public boolean isShowHideFile() {
        return isShowHideFile;
    }

    public FileBrowserLoadTask(Context context){
        this.weakReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        getHandler().postDelayed(() -> {
            if(isLoading){      //弹窗
                Context context = weakReference.get();
                if(context == null) return;
                dialog = ProgressDialogUtil.showProgressDialog(context);
            }
        }, 400);
    }

    @Override
    protected List<ManageFile> doInBackground() {
        //数据区
        List<ManageFile> data = new ArrayList<>();
        //不是根目录 则添加返回上一级item
        if (!path.equals("/")) {
            if (DFileMethod.isParentCanRead(path) || canReadSystemPath) {
                ManageFile manageFile = new JFile();
                manageFile.setTag(true);
                data.add(manageFile);
            }
        }
        //实例化文件对象
        File file = new File(path);
        //获取不到说明没有权限访问
        if(file.exists()) {
            if (file.canRead()) {
                //
                fileList(file, data);
            } else if(DFileMethod.isAndroidDataDir(file)){
                //document file
                documentList(file, data);
            } else {
                publishProgress("2", "访问受限");
            }
        }else {
            publishProgress("2", "不存在的路径");
        }
        ///========直接退出==============
        if(isCancel()) return null;
        //排序
        Collections.sort(data, new FileComparator<>());
        //高亮
        highlight(data);
        return data;
    }

    @Override
    protected void onError(Exception e) {
        //更新数据
        onLoadFilesListener.onFailure(e);
    }

    @Override
    protected void onProgressUpdate(String... msg) {
        if(msg[0].equals("1")) {
            //检查路径访问权限
            validation();
        }else {
            Context context = weakReference.get();
            if(context == null) return;
            //
            Toast.makeText(context, msg[1], Toast.LENGTH_SHORT).show();
            //
            dismiss();
        }
    }

    @Override
    protected void onPostExecute(List<ManageFile> data) {
        dismiss();
        //更新数据
        onLoadFilesListener.onFilesData(data, showItem, fileIndex);
    }

    /**
     * 高亮item
     * @param data  数据
     */
    private void highlight(List<ManageFile> data){
        //要高亮的item
        if (showItem != null) {
            for (ManageFile f : data) {
                if (f.getFileName().equals(showItem)) {
                    //高亮
                    f.setHighlight(true);
                    fileIndex = i;
                    break;
                }
                i++;
            }
        }
    }

    /**
     * 获取File 子文件
     * @param file      文件
     * @param data      数据
     */
    private void fileList(File file, List<ManageFile> data) {
        //获取文件夹下的内容
        File[] lists = file.listFiles();
        if(lists != null) {
            for(File f : lists){
                if(isCancel())return;     //退出
                ///
                if (!isShowHideFile && f.isHidden()) continue;
                //添加
                ManageFile manageFile = new JFile(f);
                data.add(manageFile);
            }
        }
    }

    /**
     * 获取documentFile 子文件
     * @param file      文件
     * @param data      数据
     */
    private void documentList(File file, List<ManageFile> data) {
        Context context = weakReference.get();
        if(context == null) return;
        //
        //根据路径获取文件uri
        Uri uri = DFileMethod.getDocumentUri(path);
        //通过uri构造document file 对象
        DocumentFile dfile = DocumentFile.fromSingleUri(context, uri);
        if(dfile != null && dfile.canRead()) {
            //
            Cursor cursor = null;
            try {
                if(isCancel())return;     //退出
                //拿到子路径
                Uri childrenUri = DocumentsContract
                        .buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri));
                //查询数据库
                cursor = context.getContentResolver().query(childrenUri, new String[]{
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE,
                        DocumentsContract.Document.COLUMN_LAST_MODIFIED, DocumentsContract.Document.COLUMN_SIZE
                }, null, null, null);

                //迭代
                while (cursor.moveToNext()) {
                    if(isCancel())return;     //退出
                    //是否是隐藏文件
                    if (!isShowHideFile) {
                        if (cursor.getString(0).startsWith("."))
                            continue;
                    }
                    //构建对象  type  directory 文件夹
                    ManageFile manageFile = new DFile(
                            cursor.getString(0),
                            cursor.getString(1),
                            cursor.getLong(2),
                            cursor.getLong(3));
                    //完整的文件路径
                    manageFile.setFilePath(file.getPath() + File.separatorChar + manageFile.getFileName());
                    data.add(manageFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }else {
            /////
            publishProgress("1");
        }
    }

    /**
     * 验证读写权限
     */
    private void validation() {
        ///
        Context context = weakReference.get();
        if(context == null) return;
        //读取持久保存的权限
        Map<String, String> maps = new HashMap<>();
        List<UriPermission> uris = context.getContentResolver().getPersistedUriPermissions();
        if(uris != null && uris.size() > 0){
            for(UriPermission up : uris){
                maps.put(up.getUri().getLastPathSegment(), "keys");
            }
        }
        //申请权限
        //  primary:Android/data
        //  primary:Android/obb
        File file = new File(path);
        if (file.getAbsolutePath().startsWith(DFileMethod.getAndroidPath() + "/data")) {
            if(maps.get("primary:Android/data") == null)
                onLoadFilesListener.askPathPermission("data");
        } else {
            if(maps.get("primary:Android/obb") == null)
                onLoadFilesListener.askPathPermission("obb");
        }
    }

    //关闭弹窗
    private void dismiss(){
        isLoading = false;
        if(dialog != null){
            dialog.dismiss();
        }
    }

    //回调接口
    public interface OnFileChangeListener{
        void onFilesData(List<ManageFile> data, String showItem, int fileIndex);
        void askPathPermission(String dir);
        void onFailure(Exception e);
    }
}
