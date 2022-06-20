package com.cc.fileManage.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

/**
 * 轻量级的异步线程任务类 替代AsyncTask使用
 * 只能在主线程使用
 *
 * 2022年6月21日15:47:34
 * @author sowhat
 * @param <T>
 */
public abstract class AsynchronousTask<T,M> implements Runnable{

    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                M m = (M)msg.obj;
                onProgressUpdate(m);
            }catch (Exception e){
                onError(e);
            }
        }
    };

    private boolean cancel, runThread;

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public boolean isRunThread() {
        return runThread;
    }

    protected Handler getHandler() {
        return handler;
    }

    @Override
    public void run() {
        this.runThread = true;
        try {
            handler.post(this::onPreExecute);
            T value = doInBackground();
            handler.post(() -> {
                if(isCancel()) {
                    onCancelled();
                }else {
                    onPostExecute(value);
                }
            });
        } catch (Exception e){
            handler.post(() -> onError(e));
        } finally {
            this.runThread = false;
        }
    }

    /**
     * 执行在主线程
     */
    protected void onPreExecute() {}

    /**
     * 执行在子线程
     */
    protected abstract T doInBackground();

    @SafeVarargs
    protected final void publishProgress(M... msg) {
        Message message = Message.obtain();
        message.obj = msg;
        handler.sendMessage(message);
    }

    /**
     * 执行在主线程
     */
    protected void onProgressUpdate(M... msg){}

    /**
     * 执行在主线程
     */
    protected void onPostExecute(T value){}

    /**
     * 执行在主线程
     */
    protected void onCancelled(){}

    /**
     * 执行在主线程
     */
    protected void onError(Exception e){}
}
