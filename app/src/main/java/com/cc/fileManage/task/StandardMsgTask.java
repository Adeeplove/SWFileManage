package com.cc.fileManage.task;

import android.app.ProgressDialog;
import android.content.Context;

import java.lang.ref.WeakReference;

public class StandardMsgTask extends AsynchronousTask<String, String, String> {

    private final WeakReference<Context> weakReference;

    private boolean showTip = true;
    //弹框
    private ProgressDialog dialog;
    private long startTime;

    public StandardMsgTask(Context context, OnCompleteListener onCompleteListener) {
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
        dialog.getWindow().setDimAmount(0f);
        ///
        getHandler().postDelayed(() -> {
            if(showTip) dialog.show();
        }, 350);
        //
        startTime = System.currentTimeMillis();
    }

    @Override
    protected String doInBackground(String... strings) {
        return onCompleteListener.doMethod();
    }

    @Override
    protected void onCancelled() {
        showTip = false;
        if(dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        showTip = false;
        if(dialog != null) {
            dialog.dismiss();
        }
        onCompleteListener.success(s, (System.currentTimeMillis() - startTime) / 1000.0f);
    }

    private final OnCompleteListener onCompleteListener;

    public interface OnCompleteListener {
        String doMethod();
        void success(String msg, float time);
    }
}
