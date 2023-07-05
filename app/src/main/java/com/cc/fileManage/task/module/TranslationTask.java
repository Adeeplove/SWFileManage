package com.cc.fileManage.task.module;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ClipboardUtils;
import com.cc.fileManage.task.AsynchronousTask;
import com.cc.fileManage.utils.CharUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TranslationTask extends AsynchronousTask<String, Integer, String>
{
    private final WeakReference<Context> weakReference;
    private ProgressDialog dialog;

    private final String text;

    public TranslationTask(String text, Context context){
        this.text = text;
        this.weakReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute()
    {
        Context context = weakReference.get();
        if(context == null) return;
        dialog = new ProgressDialog(context);
        dialog.setMessage("正在翻译...");
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected String doInBackground(String... strings)
    {
        return tranText(handlerString(text));
    }

    @Override
    protected void onPostExecute(final String result)
    {
        if(dialog != null)
            dialog.dismiss();
        ///======================
        Context context = weakReference.get();
        if(context == null) return;
        ///=========
        AlertDialog.Builder ab = new AlertDialog.Builder(context);
        String space = " &nbsp&nbsp&nbsp&nbsp ";

        String resultString = "<br/>"+ space +"<strong>原字符</strong><br/>"
                + space + text + "<br/>" +space+ "<strong>翻译字符</strong><br/>"
                + space + handlerString(text) +"<br/><br/>"+ space +
                "<strong>翻译结果</strong><br/>" + space + result;

        TextView link = new TextView(context);
        link.setText(android.text.Html.fromHtml(resultString));
        link.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());

        ab.setView(link);
        ab.setPositiveButton("复制结果", (p1, p2) -> {
            //copy
            ClipboardUtils.copyText(result);
        });
        ab.show();
    }

    /**
     *  翻译文本
     * @param content   文本内容
     * @return          翻译结果
     */
    private String tranText(String content){
        HttpURLConnection conn;
        String urlStr="https://m.youdao.com/translate";
        InputStream is;
        String resultData = "";
        try {
            URL url = new URL(urlStr); //URL对象
            conn = (HttpURLConnection)url.openConnection(); //使用URL打开一个链接,下面设置这个连接
            conn.setRequestMethod("POST"); //使用POST请求

            //参数字符串
            String param="inputtext="+ URLEncoder.encode(content,"UTF-8")//服务器不识别汉字
                    +"&type="+URLEncoder.encode("AUTO","UTF-8");

            //用输出流向服务器发出参数，要求字符，所以不能直接用getOutputStream
            DataOutputStream dos=new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(param);
            dos.flush();
            dos.close();

            if(conn.getResponseCode()==200) {//返回200表示相应成功
                is = conn.getInputStream();   //获取输入流
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader bufferReader = new BufferedReader(isr);
                String inputLine;
                while ((inputLine = bufferReader.readLine()) != null) {

                    if(inputLine.contains("<li>") && inputLine.contains("</li>") && !inputLine.contains("</a>")){
                        inputLine = inputLine.substring(inputLine.indexOf("<li>") + 4);
                        inputLine = inputLine.substring(0, inputLine.lastIndexOf("</li>"));

                        resultData = inputLine;
                        break;
                    }
                }
            }else{
                resultData = content;
            }
            if(resultData.equals("")){
                resultData = content;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultData;
    }

    /**
     *
     * @param code  文本
     * @return      处理后的文本
     */
    private String handlerString(String code){
        if(CharUtil.isChinese(code) || code.length() < 1)
            return code;

        StringBuilder strb = new StringBuilder();
        try{
            char[] chStr = code.toCharArray();
            String old = "";
            //遍历
            for(int i = 0; i < chStr.length; i++){
                char ch = chStr[i];
                //说明是第一个
                if(i == 0){
                    //是下划线
                    if(ch != '_')
                        strb.append(ch);
                    old += ch;
                    continue;
                }
                //是大写字母
                if(isStringUp(ch)){
                    //后面是否还有字符
                    if((i + 1) < chStr.length){
                        //判断前一个字符跟后一个是否是大写
                        if(!isStringUp(old) && !isStringUp(chStr[i + 1]))
                            //前面添加空格
                            strb.append(" ").append(ch);
                        else
                            strb.append(ch);
                    }else{
                        strb.append(ch);
                    }
                }else if(ch == '_'){
                    strb.append(" ");
                }else{
                    strb.append(ch);
                }
                //保存上一个字符
                old = "" + ch;
            }
        }catch(Exception e){
            return code;
        }
        return strb.toString().toLowerCase();
    }

    //是否是大写字母
    private boolean isStringUp(String str){
        if(str.length() >= 1)
            return isStringUp(str.charAt(0));
        return false;
    }

    //是否是大写字母
    private boolean isStringUp(char ch){
        return ch > 64 && ch < 91;
    }
}
