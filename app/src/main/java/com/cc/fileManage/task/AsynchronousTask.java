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
    private static final ExecutorService executor;

    static {
        executor = new ThreadPoolExecutor(1, 3,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10));
    }

    private static final int MESSAGE = 1;      //消息
    private static final int RESULT = 2;       //任务结果
    private final Handler handler = new Handler(Looper.getMainLooper()){
        @SuppressWarnings({"unchecked"})
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == MESSAGE) {
                onProgressUpdate((News[])msg.obj);
            }
            else if(msg.what == RESULT) {
                this.removeCallbacksAndMessages(null);
                if(isCancelled()) {
                    onCancelled();
                } else {
                    onPostExecute((Result)msg.obj);
                }
            }
        }
    };

    //线程状态
    private boolean cancel, runThread;
    //参数
    private Parameter[] parameter;

    public boolean isCancelled() {
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
        try {
            sendMessage(RESULT, doInBackground(parameter));
        } finally {
            this.runThread = false;
        }
    }

    /**
     * 执行前的准备任务
     */
    protected void onPreExecute() {}

    /**
     * 任务线程(子线程处理)
     */
    @SuppressWarnings({"unchecked", "varargs"})
    protected abstract Result doInBackground(Parameter... parameters);

    /**
     * 消息处理
     * @param msg   接收的消息
     */
    @SafeVarargs
    protected final void publishProgress(News... msg) {
        sendMessage(MESSAGE, msg);
    }

    /**
     * 更新信息
     */
    @SuppressWarnings({"unchecked", "varargs"})
    protected void onProgressUpdate(News... msg){}

    /**
     * 任务执行结束 返回结果
     */
    protected void onPostExecute(Result result){}

    /**
     * 任务取消
     */
    protected void onCancelled(){}

    /**
     * 提交消息
     */
    private void sendMessage(int what, Object msg) {
        Message message = Message.obtain();
        message.what = what;
        message.obj = msg;
        handler.sendMessage(message);
    }

    /**
     * 执行线程
     */
    @SafeVarargs
    public final void execute(Parameter... parameters) {
        ///=======================
        cancel(false);
        onPreExecute();
        this.parameter = parameters;
        executor.submit(this);
    }
}
