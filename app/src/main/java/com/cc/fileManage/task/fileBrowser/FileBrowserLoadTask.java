package com.cc.fileManage.task.fileBrowser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.UriPermission;
import android.widget.Toast;

import com.cc.fileManage.entity.file.FileApi;
import com.cc.fileManage.entity.file.MFile;
import com.cc.fileManage.entity.file.MFileSort;
import com.cc.fileManage.entity.file.OnFileDataListener;
import com.cc.fileManage.task.AsynchronousTask;
import com.cc.fileManage.utils.ProgressDialogUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBrowserLoadTask extends AsynchronousTask<String, String, List<MFile>> {

    private final WeakReference<Context> weakReference;
    /// 文件数量
    private int dirSize = 0, fileSize = 0;
    ///
    private AlertDialog dialog;         //加载弹窗
    //访问的路径
    private String readPath;            //访问路径
    //要高亮的item
    private String showItem;            //高亮的item名称
    private boolean findItem;           //是否查找要高亮的文件

    private boolean canReadSystemPath;  //是否能访问系统目录
    private boolean showHideFile;       //是否显示隐藏文件

    private boolean loading = true;     //加载弹框

    private OnFileChangeListener onLoadFilesListener;

    public void setOnLoadFilesListener(OnFileChangeListener onLoadFilesListener) {
        this.onLoadFilesListener = onLoadFilesListener;
    }

    public void setPath(String path) {
        this.readPath = path;
    }

    public String getPath() {
        return readPath;
    }

    public void setShowItem(String showItem) {
        this.showItem = showItem;
        this.findItem = showItem != null;
    }

    public void setCanReadSystemPath(boolean canReadSystemPath) {
        this.canReadSystemPath = canReadSystemPath;
    }

    public void setShowHideFile(boolean showHideFile) {
        this.showHideFile = showHideFile;
    }

    public int getDirSize() {
        return dirSize;
    }

    public int getFileSize() {
        return fileSize;
    }

    public FileBrowserLoadTask(Context context){
        this.weakReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        getHandler().postDelayed(() -> {
            if(loading){      //弹窗
                Context context = weakReference.get();
                if(context == null) return;
                dialog = ProgressDialogUtil.showProgressDialog(context);
            }
        }, 400);
    }

    @Override
    protected List<MFile> doInBackground(String... strings) {
        //数据区
        List<MFile> data = new ArrayList<>();
        try {
            Context context = weakReference.get();
            if(context == null) return data;
            /// 构建ManageFile
            MFile readFile = MFile.create(context, readPath);
            /// 不是根目录 则添加返回上一级item
            if (!readPath.equals(File.separator)) {
                /// 父目录是否可读
                if (canReadSystemPath || FileApi.isDataDir(readFile.getParent()) || readFile.parentCanRead()) {
                    data.add(MFile.createTag());
                }
            }
            // 是否加载图标
            boolean loadIcon = FileApi.isDataDir(readFile.getPath())
                    || readFile.getESPath().equals(FileApi.getAndroidChild(FileApi.DIR.MEDIA));
            /// 列出子文件
            readFile.listFiles(context, new OnFileDataListener() {
                @Override
                public void onData(MFile file, String readPath) {
                    if(!showHideFile && file.isHidden()) return;
                    /////////
                    if(file.isFile()) {
                        fileSize++;
                    } else {
                        file.setLoadIcon(loadIcon);
                        dirSize++;
                    }
                    /// 查找需要高亮的文件
                    if(findItem) {
                        if(file.getName().equals(showItem)) {
                            file.setHighlight(true);
                            findItem = false;
                        }
                    }
                    data.add(file);
                }
                @Override
                public void onNoPermission() {
                    // 申请权限
                    publishProgress("APPLY");
                }
                @Override
                public void onNoExist() {
                    publishProgress("访问路径失败");
                }
            });
            ///========直接退出==============
            if(isCancelled()) return null;
            ///排序
            Collections.sort(data, new MFileSort());
        } catch (Exception e) {
            //更新数据
            onLoadFilesListener.onFailure(e);
        }
        return data;
    }

    @Override
    protected void onProgressUpdate(String... msg) {
        if(msg[0].equals("APPLY")) {
            //检查路径访问权限
            validation();
        } else {
            dismiss();
            ///
            Context context = weakReference.get();
            if(context != null) {
                Toast.makeText(context, msg[0], Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPostExecute(List<MFile> data) {
        dismiss();
        if(data != null) {
            //更新数据
            onLoadFilesListener.onFilesData(data, showItem);
        }
    }

    /**
     * 验证读写权限
     */
    private void validation() {
        ///
        Context context = weakReference.get();
        if(context != null) {
            //读取持久保存的权限
            Map<String, String> maps = new HashMap<>();
            List<UriPermission> uris = context.getContentResolver().getPersistedUriPermissions();
            if(uris != null && uris.size() > 0){
                for(UriPermission up : uris){
                    maps.put(up.getUri().getLastPathSegment(), "keys");
                }
            }
            //申请权限
            File file = new File(readPath);
            String tree = getTreePath(file.getPath());
            if(maps.get("primary:" + tree) == null)
                onLoadFilesListener.askPathPermission(tree);
            else
                onLoadFilesListener.onPathNoExist(readPath);
        }
    }

    /**
     * 获取文件路径树
     * @param file   路径
     * @return      文件树头
     */
    private String getTreePath(String file) {
        int index = file.indexOf("Android");
        // 包含Android
        if(index > 0) {
            /// Android/........
            String child = file.substring(index);
            return FileApi.parseTree(child);
        }
        return file;
    }

    //关闭弹窗
    private void dismiss(){
        this.loading = false;
        if(this.dialog != null){
            this.dialog.dismiss();
        }
    }

    //回调接口
    public interface OnFileChangeListener{
        void onFilesData(List<MFile> data, String showItem);
        void askPathPermission(String dir);
        void onFailure(Exception e);
        void onPathNoExist(String path);
    }
}
