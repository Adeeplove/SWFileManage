package com.cc.fileManage.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 异步线程任务类 替代AsyncTask使用
 * 只能在主线程使用
 *
 * 2022年6月21日15:47:34
 * @author sowhat
 */
public abstract class AsynchronousTask<Parameter,News,Result> implements Runnable{

    ///线程池
    private static final ExecutorService  executor;

    static {
        executor = new ThreadPoolExecutor(1, 5,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10));
    }

    private final Handler handler = new Handler(Looper.getMainLooper()){
        @SuppressWarnings({"unchecked", "cast"})
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                News[] m = (News[])msg.obj;
                onProgressUpdate(m);
            }catch (Exception e){
                onError(e);
            }
        }
    };

    //线程状态
    private boolean cancel, runThread;
    //参数
    private Parameter[] parameter;

    public boolean isCancel() {
        return cancel;
    }

    public void cancel(boolean cancel) {
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
        Result value = null;
        try {
            value = doInBackground(parameter);
        } catch (Exception e){
            handler.post(() -> onError(e));
        } finally {
            Result finalValue = value;
            handler.post(() -> {
                try {
                    if(isCancel()) {
                        onCancelled();
                    }else {
                        onPostExecute(finalValue);
                    }
                }catch (Exception e){onError(e);}
            });
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
    @SuppressWarnings({"unchecked", "varargs"})
    protected abstract Result doInBackground(Parameter... parameters);

    @SafeVarargs
    protected final void publishProgress(News... msg) {
        Message message = Message.obtain();
        message.obj = msg;
        handler.sendMessage(message);
    }

    /**
     * 执行在主线程
     */
    @SuppressWarnings({"unchecked", "varargs"})
    protected void onProgressUpdate(News... msg){}

    /**
     * 执行在主线程
     */
    protected void onPostExecute(Result result){}

    /**
     * 执行在主线程
     */
    protected void onCancelled(){}

    /**
     * 执行在主线程
     */
    protected void onError(Exception e){}

    /**
     * 执行线程
     */
    @SafeVarargs
    public final void execute(Parameter... parameters) {
        ///=======================
        onPreExecute();
        this.parameter = parameters;
        executor.submit(this);
    }
}
