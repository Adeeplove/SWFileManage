package com.cc.fileManage.task.apktool;

import android.app.ProgressDialog;
import android.content.Context;

import com.cc.fileManage.task.AsynchronousTask;

import java.lang.ref.WeakReference;

import brut.androlib.AndrolibException;
import brut.common.BrutException;

public class DecodeDexTask extends AsynchronousTask<String, String, String> {

    private final WeakReference<Context> weakReference;

    //弹框
    private ProgressDialog dialog;
    private long startTime;

    public DecodeDexTask(Context context, OnCompleteListener onCompleteListener) {
        this.weakReference = new WeakReference<>(context);
        this.onCompleteListener = onCompleteListener;
    }

    @Override
    protected void onPreExecute() {
        Context context = weakReference.get();
        if(context == null) return;
        ///============
        dialog = new ProgressDialog(context);
        dialog.setMessage("处理中 请稍后...");
        dialog.setCancelable(false);
        dialog.show();
        //
        startTime = System.currentTimeMillis();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            onCompleteListener.doMethod();
        }catch (Exception e){
            return e.getMessage();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(dialog != null)
            dialog.dismiss();
        onCompleteListener.success(s, ((float) System.currentTimeMillis() - startTime) / 1000);
    }

    private final OnCompleteListener onCompleteListener;

    public interface OnCompleteListener {
        void doMethod() throws BrutException;
        void success(String msg, float time);
    }
}
