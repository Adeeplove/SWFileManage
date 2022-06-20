package com.cc.fileManage.task;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.cc.fileManage.file.DFile;
import com.cc.fileManage.file.FileComparator;
import com.cc.fileManage.file.DFileMethod;
import com.cc.fileManage.file.JFile;
import com.cc.fileManage.file.ManageFile;
import com.cc.fileManage.utils.ProgressDialogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadFilesTask extends AsyncTask<String, Integer, List<ManageFile>>
{
    @SuppressLint("StaticFieldLeak")
    private final Context context;            //上下文

    private AlertDialog dialog;         //加载弹窗

    //访问的路径
    private String path;                //访问路径
    //要高亮的item
    private String showItem;            //高亮的item名称

    private boolean canReadSystemPath;  //是否能访问系统目录
    private boolean isShowHideFile;     //是否显示隐藏文件

    private int i = 0, fileIndex = 0;   //高亮的文件下标

    private boolean isLoading;          //加载弹框是

    private OnLoadFilesListener onLoadFilesListener;

    public LoadFilesTask(Context context){
        this.context = context;
        this.isLoading = true;
    }

    public void setOnLoadFilesListener(OnLoadFilesListener onLoadFilesListener) {
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

    @Override
    protected void onPreExecute()
    {
        new Handler().postDelayed(() -> {
            if(isLoading){
                dialog = ProgressDialogUtil.showProgressDialog(context);
            }
        }, 400);
    }

    @Override
    protected List<ManageFile> doInBackground(String[] p1)
    {
        List<ManageFile> data = new ArrayList<>();
        //
        try {
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
            if (file.canRead()) {
                //获取文件夹下的内容
                File[] lists = file.listFiles();
                if(lists != null) {
                    for(File f : lists){
                        if (!isShowHideFile) {
                            if (f.isHidden())
                                continue;
                        }
                        //添加
                        ManageFile manageFile = new JFile(f);
                        data.add(manageFile);
                    }
                }
            }
            else if(DFileMethod.isAndroidDataDir(file)){         //document file
                //根据路径获取文件uri
                Uri uri = DFileMethod.getDocumentUri(path);
                //通过uri构造document file 对象
                DocumentFile dfile = DocumentFile.fromSingleUri(context, uri);
                if(dfile != null && dfile.canRead()) {
                    //可读
                    if (dfile.canRead()) {
                        //
                        Cursor cursor = null;
                        try {
                            //拿到子路径
                            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri));
                            //查询数据库
                            cursor = context.getContentResolver().query(childrenUri, new String[]{
                                    DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE,
                                    DocumentsContract.Document.COLUMN_LAST_MODIFIED, DocumentsContract.Document.COLUMN_SIZE
                            }, null, null, null);

                            //迭代
                            while (cursor.moveToNext()) {
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
                    }
                }else {
                    publishProgress(1);
                }
            }
            else {
                publishProgress(2);
            }
            //排序
            Collections.sort(data, new FileComparator<>());

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
        } catch (Exception e) {
            e.printStackTrace();
            publishProgress(3);
        }
        return data;
    }

    @Override
    protected void onProgressUpdate(Integer[] values) {
        super.onProgressUpdate(values);
        if(values[0] == 1){
            //读取持久保存的权限
            Map<String, String> maps = new HashMap<>();
            List<UriPermission> uris = context.getContentResolver().getPersistedUriPermissions();
            if(uris != null && uris.size() > 0){
                for(UriPermission up : uris){
                    maps.put(up.getUri().getLastPathSegment(), "keys");
                }
            }
            //
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
        }else if(values[0] == 2){
            Toast.makeText(context, "访问受限", Toast.LENGTH_SHORT).show();
        }else if(values[0] == 3){
            Toast.makeText(context, "数据更新失败!", Toast.LENGTH_SHORT).show();
        }
        //关闭弹窗
        dismiss();
    }

    @Override
    protected void onPostExecute(List<ManageFile> result)
    {
        //关闭弹窗
        dismiss();
        //更新数据
        onLoadFilesListener.onFilesData(result, showItem, fileIndex);
    }

    @Override
    protected void onCancelled() {
        dismiss();
    }

    //关闭弹窗
    private void dismiss(){
        isLoading = false;
        if(dialog != null){
            dialog.dismiss();
        }
    }

    //回调接口
    public interface OnLoadFilesListener{
        void onFilesData(List<ManageFile> data, String showItem, int fileIndex);
        void askPathPermission(String dir);
    }
}
