package com.cc.fileManage.task;

import android.app.AlertDialog;
import android.os.AsyncTask;
import java.util.List;
import android.content.Context;
import androidx.documentfile.provider.DocumentFile;

import com.blankj.utilcode.util.FileUtils;
import com.cc.fileManage.file.DFileMethod;
import com.cc.fileManage.file.JFile;
import com.cc.fileManage.file.ManageFile;

public class DeleteFilesTask extends AsyncTask<String, String, String>
{
    private Context context;

    private AlertDialog log;

    private List<ManageFile> data;

    private OnDeleteListener onDeleteListener;

    public DeleteFilesTask(Context context, List<ManageFile> data){
        this.context = context;
        this.data = data;
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    //执行后台任务前做一些UI操作
    @Override
    protected void onPreExecute() {
        log = new AlertDialog.Builder(context).create();
        log.setCancelable(false);
        log.setMessage("正在删除...");
        log.show();
    }

    //执行后台任务（耗时操作）,不可在此方法内修改UI
    @Override
    protected String doInBackground(String... params)
    {
        try{
            for(ManageFile file : data){

                publishProgress("up", "正在删除 " + file.getFileName());
                if(file instanceof JFile){
                    //删除文件
                    FileUtils.delete(((JFile) file).getFile());
                }else{
                    DocumentFile du = DFileMethod.getDocumentFile(context, file.getFilePath());
                    if(du == null){
                        continue;
                    }
                    if(file.isFile()){
                        du.delete();
                    }else{
                        deleteDir(context, du);
                    }
                }
            }
        }catch(Exception e){
            publishProgress("error", "删除失败!", e.getMessage());
        }
        return null;
    }

    //更新进度信息
    @Override
    protected void onProgressUpdate(String... progresses)
    {
        if(progresses[0].equals("up"))
            log.setMessage(progresses[1]);
        else
            showDialog(progresses[1], progresses[2]);
    }

    //执行完后台任务后更新UI
    @Override
    protected void onPostExecute(String result) {
        try{
            //刷新
            if(onDeleteListener != null)
                onDeleteListener.onDeleteFiles();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(log != null){
                log.dismiss();
            }
        }
    }

    /**
     *  提示信息
     * @param title
     * @param message
     */
    private void showDialog(String title, String message){
        AlertDialog.Builder ab = new AlertDialog.Builder(context);
        ab.setTitle(title);
        ab.setMessage(message);
        ab.show();
    }

    /**
     * delete dir 删除文件夹
     * @param dir
     * @return
     */
    private boolean deleteDir(Context context, DocumentFile dir) {
        if (dir.isDirectory()) {
            List<DocumentFile> docu = DFileMethod.documentFileLists(context, dir);
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

    public interface OnDeleteListener{
        void onDeleteFiles();
    }
}