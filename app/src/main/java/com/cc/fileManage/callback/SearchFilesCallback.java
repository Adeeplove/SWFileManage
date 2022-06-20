package com.cc.fileManage.callback;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ToastUtils;
import com.cc.fileManage.R;
import com.cc.fileManage.file.JFile;
import com.cc.fileManage.file.ManageFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SearchFilesCallback extends AsyncTask<String, String, String>
{
    private Activity context;

    //搜索的文件数据
    private List<ManageFile> fileData;
    //搜索到的内容
    private List<File> searchData;

    ///================
    private boolean searchSubdirectory = true;   //搜索子目录
    private boolean caseSensitive = false;       //区分大小写
    private String  searchContent;               //搜索的文件名称
    private String  subContent;                  //搜索的文本内容

    //message
    private AlertDialog dialog;
    //搜索信息
    @SuppressLint("StaticFieldLeak")
    private TextView message;
    //数量
    @SuppressLint("StaticFieldLeak")
    private TextView size;
    //取消搜索按钮
    @SuppressLint("StaticFieldLeak")
    public Button cancel;

    //监听
    private OnSearchDataListener onSearchDataListener;
    public void setOnSearchDataListener(OnSearchDataListener onSearchDataListener) {
        this.onSearchDataListener = onSearchDataListener;
    }

    public SearchFilesCallback(Activity context, List<ManageFile> fileData){
        this.context = context;
        this.fileData = fileData;
        this.searchData = new ArrayList<>();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //
        showMessageView();
    }

    @Override
    protected String doInBackground(String... strings) {
        //搜索的文件夹
        for(ManageFile manageFile : fileData){
            try{
                System.out.println(searchContent+"<>搜索: "+manageFile.toString());
                //搜索
                if(manageFile instanceof JFile)
                    filePattern(((JFile) manageFile).getFile(), searchContent);
            }catch(Exception e){
                publishProgress("error",e.getMessage());
                continue;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(dialog != null){
            dialog.dismiss();
        }
        //
        if(searchData.size() >= 1 && onSearchDataListener != null){
            onSearchDataListener.onSearchData(searchData);
        }else{
            ToastUtils.showShort("未找到");
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if(values[0].equals("size")){
            size.setText(values[1]);
        }
        else if(values[0].equals("error")){
            showErrorView(values[1]);
        }
        else{
            message.setText(values[1]);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if(dialog != null){

            dialog.dismiss();
        }
        //
        if(searchData.size() >= 1 && onSearchDataListener != null){
            onSearchDataListener.onSearchData(searchData);
        }else{
            ToastUtils.showShort("未找到");
        }
    }

    /**
     *  搜索弹窗
     */
    public void showSearchView(String searchText){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = context.getLayoutInflater().inflate(R.layout.search_files,null);

        //搜索内容
        final EditText edit = view.findViewById(R.id.search_files_edit);
        edit.setText(searchText);

        //搜索子目录
        final CheckBox box = view.findViewById(R.id.search_files_check);
        box.setChecked(searchSubdirectory);
        //区分大小写
        final CheckBox big = view.findViewById(R.id.search_files_big);
        big.setChecked(caseSensitive);

        //文件包含内容
        final EditText subEdit = view.findViewById(R.id.search_files_content);

        builder.setView(view);
        builder.setNegativeButton("搜索", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface p1, int p2)
            {
                //搜索内容
                searchContent = edit.getText() == null ? "" : edit.getText().toString();
                if(TextUtils.isEmpty(searchContent)){
                    ToastUtils.showShort("搜索内容为空!");
                    return;
                }

                //文件内容
                subContent = subEdit.getText() == null ? "" : subEdit.getText().toString();
                if(TextUtils.isEmpty(subContent)){
                    subContent = null;
                }
                //搜索子目录
                searchSubdirectory = box.isChecked();
                //区分大小写
                caseSensitive = big.isChecked();

                //运行线程
                execute();
            }
        });
        builder.setNeutralButton("取消", null);
        builder.show();
    }

    /**
     * 搜索信息弹框
     */
    private void showMessageView(){
        //view
        View view = context.getLayoutInflater().inflate(R.layout.search_message,null);

        message = view.findViewById(R.id.search_message_text);
        size = view.findViewById(R.id.search_message_size);
        cancel = view.findViewById(R.id.search_message_button);

        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View p1)
            {
                //取消线程
                cancel(true);
                //不可点击
                cancel.setEnabled(false);
            }
        });

        dialog = new AlertDialog.Builder(context).create();
        dialog.setView(view);
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 错误弹框
     */
    private void showErrorView(String message){
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle("错误");
        adb.setMessage(message);
        adb.show();
    }

    /**
     * @param file File 起始文件夹
     * @return ArrayList 其文件夹下的文件夹
     */
    private void filePattern(File file, String search) {
        //更新信息
        publishProgress("text", file.getPath());
        publishProgress("size", "已找到: " + searchData.size());

        if (file == null) {
            return;
        }
        //是文件
        else if (file.isFile())
        {
            if(isCancelled()){
                return;
            }
            //判断
            fileIsMatch(search, file);

            if(isCancelled()){
                return;
            }
        }
        //文件夹
        else if (file.isDirectory())
        {
            if(isCancelled()){
                return;
            }
            //所有子文件
            File[] files = file.listFiles();
            //不为Null
            if (files != null && files.length > 0) {
                for (File child : files) {
                    if(child.isDirectory()){
                        if(isCancelled()){
                            return;
                        }
                        //是否搜索子目录
                        if(searchSubdirectory){
                            filePattern(child, search);
                        }
                    }
                    else if(child.isFile())
                    {
                        if(isCancelled()){
                            return;
                        }
                        //判断是否匹配
                        fileIsMatch(search, child);
                    }
                }
            }
        }
    }

    /**
     *  字符匹配
     * @param search 搜索内容
     * @param file   文件
     */
    private void fileIsMatch(String search, File file){
        //区分大小写
        if(caseSensitive){
            if(file.isHidden()){
            }
            //判断是否匹配
            else if (wildcardMatch(search, file.getName())) {
                if(subContent != null){
                    if(isHaveContent(file))
                        searchData.add(file);
                }else{
                    searchData.add(file);
                }
                publishProgress("size", "已找到: " + searchData.size());
            }
        }
        else{
            if(file.isHidden()){
            }
            //判断是否匹配
            else if (wildcardMatch(search.toLowerCase(), file.getName().toLowerCase())) {
                if(subContent != null){
                    if(isHaveContent(file))
                        searchData.add(file);
                }else{
                    searchData.add(file);
                }
                publishProgress("size", "已找到: " + searchData.size());
            }
        }
    }
    /**
     * 通配符匹配
     * @param pattern    通配符模式
     * @param str    待匹配的字符串
     * @return    匹配成功则返回true，否则返回false
     */
    private boolean wildcardMatch(String pattern, String str) {
        int patternLength = pattern.length();
        int strLength = str.length();

        int strIndex = 0;

        char ch;
        for (int patternIndex = 0; patternIndex < patternLength; patternIndex++) {
            ch = pattern.charAt(patternIndex);
            if (ch == '*') {
                //通配符星号*表示可以匹配任意多个字符
                while (strIndex < strLength) {
                    if (wildcardMatch(pattern.substring(patternIndex + 1),
                            str.substring(strIndex))) {
                        return true;
                    }
                    strIndex++;
                }
            } else if (ch == '?') {
                //通配符问号?表示匹配任意一个字符
                strIndex++;
                if (strIndex > strLength) {
                    //表示str中已经没有字符匹配?了。
                    return false;
                }
            } else {
                if ((strIndex >= strLength) || (ch != str.charAt(strIndex))) {
                    return false;
                }
                strIndex++;
            }
        }
        return (strIndex == strLength);
    }

    /**
     * 文件是否包含内容
     */
    private boolean isHaveContent(File file){
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        try{
            //获取编码
            String fileEncode = "UTF-8";
            // 创建输入流
            inputStreamReader = new InputStreamReader(new FileInputStream(file), fileEncode);
            bufferedReader = new BufferedReader(inputStreamReader);

            String lineStr = "";
            while ((lineStr = bufferedReader.readLine()) != null) {
                if(lineStr.contains(subContent)){
                    return true;
                }
            }
        }catch (Exception e) {
            e.getLocalizedMessage();
        }finally {
            try {
                if(inputStreamReader!=null)inputStreamReader.close();
                if(bufferedReader!=null)bufferedReader.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
        return false;
    }

    public interface OnSearchDataListener{
        void onSearchData(List<File> files);
    }
}
