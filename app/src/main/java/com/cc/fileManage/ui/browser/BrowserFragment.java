package com.cc.fileManage.ui.browser;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.cc.fileManage.App;
import com.cc.fileManage._static.CSetting;
import com.cc.fileManage.databinding.FragmentBrowserBinding;
import com.cc.fileManage.db.DBService;
import com.cc.fileManage.entity.BookMark;
import com.cc.fileManage.ui.BaseFragment;
import com.cc.fileManage.ui.adapter.BookMarkAdapter;
import com.cc.fileManage.ui.views.ListItemDialog;
import com.cc.fileManage.ui.views.RenameFileView;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简单的浏览器页面实现
 */
public class BrowserFragment extends BaseFragment implements View.OnClickListener{

    private FragmentBrowserBinding binding;

    //下载广播监听
    private DownloadCompleteReceiver receiver;
    //
    private final String loadUrl = "https://cn.bing.com/";

    // 下载文件记录
    private Map<Long,String> maps;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBrowserBinding.inflate(inflater, container, false);
        //初始化页面
        initView();
        //
        initListener();
        //
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        //=============
        getMainActivity().setTitleText(App.browserType);
        getMainActivity().setSubtitleText(loadUrl);
    }

    /**
     * 初始化
     */
    private void initView() {
        //监听下载
        monitorDownload();
        //初始化配置
        initWebViewSettings();
        //
        @SuppressLint("RtlHardcoded")
        ClipDrawable d = new ClipDrawable(new ColorDrawable(Color.BLUE), Gravity.LEFT, ClipDrawable.HORIZONTAL);
        binding.browserBar.setProgressDrawable(d);
        //加载URL
        binding.browserWeb.loadUrl(loadUrl);
        binding.browserWeb.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return urlDecode(url) || super.shouldOverrideUrlLoading(view, url);
            }
            // 页面加载失败
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
                handler.proceed(); // 兼容https
            }
        });

        /*
         * WebChromeClient类:用来辅助WebView处理JavaScript的对话框,网站图标,网站Title,加载进度等
         * 通过setWebChromeClient调协WebChromeClient类
         */
        binding.browserWeb.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if(newProgress >= 100){
                    binding.browserBar.setProgress(newProgress);
                    binding.browserBar.setVisibility(View.GONE);
                }else{
                    binding.browserBar.setVisibility(View.VISIBLE);
                    binding.browserBar.setProgress(newProgress);
                }
            }
            @Override
            public void onReceivedTitle(WebView view, String title) {
                getMainActivity().setTitleText(title);
                getMainActivity().setSubtitleText(view.getUrl());
            }
        });
        //下载
        binding.browserWeb.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            RenameFileView renameFileView = new RenameFileView(requireContext());
            renameFileView.setOnRenameFileListener((newName, dialog) -> {
                if(dialog != null) dialog.dismiss();
                downloadBySystem(url, newName);
            });
            renameFileView.rename("文件下载", decodeFileUri(contentDisposition), "下载");
        });
    }

    /**
     * 下载广播监听
     */
    private void monitorDownload() {
        this.maps = new HashMap<>();
        //下载文件广播监听
        receiver = new DownloadCompleteReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        getMainActivity().registerReceiver(receiver, intentFilter);
    }

    /**
     * 监听
     */
    private void initListener() {
        binding.browserUp.setOnClickListener(this);
        binding.browserTo.setOnClickListener(this);
        binding.browserRefresh.setOnClickListener(this);
        binding.browserRefresh.setOnLongClickListener(v -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
            dialog.setTitle("当前访问的链接");
            dialog.setMessage(binding.browserWeb.getUrl());
            dialog.setPositiveButton("复制", (dialog1, which) -> {
                ClipboardUtils.copyText(binding.browserWeb.getUrl());
                ToastUtils.showShort("已复制");
            });
            dialog.setNegativeButton("取消", null);
            dialog.show();
            return true;
        });
    }

    @Override
    public void onClick(View v) {
        if(v == binding.browserUp) {
            if(binding.browserWeb.canGoBack())
                binding.browserWeb.goBack();
        }
        else if(v == binding.browserTo) {
            if(binding.browserWeb.canGoForward())
                binding.browserWeb.goForward();
        }
        else if(v == binding.browserRefresh) {
            binding.browserWeb.reload();
        }
    }

    @Override
    public void onBack() {
        if(binding.browserWeb.canGoBack())
            binding.browserWeb.goBack();
        else
            getMainActivity().exit();
    }

    @Override
    public void onUpdate(String path) {
        if(!TextUtils.isEmpty(path)){
            binding.browserWeb.loadUrl(path);
        }else{
            ToastUtils.showShort("链接不能为空!");
        }
    }

    @Override
    public boolean onCreateMenu(Menu menu) {
        menu.add(Menu.NONE,2000, 0,"查看书签");
        menu.add(Menu.NONE,3000, 0,"添加书签");
        menu.add(Menu.NONE,4000, 0,"浏览器打开");
        menu.add(Menu.NONE,5000, 0,"设为应用首页")
                .setCheckable(true).setChecked(CSetting.webIsHome);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                getMainActivity().openDrawer();
                break;
            case 2000:
                showBookMark();     //显示书签
                break;
            case 3000:
                RenameFileView addBookMark = new RenameFileView(requireContext());
                addBookMark.setOnRenameFileListener((newName, dialog) -> {
                    if(dialog != null) dialog.dismiss();
                    BookMark bookMark = new BookMark(BookMark.Type.Web);
                    bookMark.setName(TextUtils.isEmpty(newName) ? binding.browserWeb.getUrl() : newName);
                    bookMark.setPath(binding.browserWeb.getUrl());
                    if(DBService.getInstance(requireContext()).addBookMark(bookMark)){
                        ToastUtils.showShort("添加成功");
                    }else {
                        ToastUtils.showShort("添加失败");
                    }
                });
                addBookMark.setShowPaste(true);
                addBookMark.rename("添加书签", binding.browserWeb.getUrl(), "添加");
                break;
            case 4000:              //浏览器内打开
                try{
                    Uri uri = Uri.parse(binding.browserWeb.getUrl());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            case 5000:
                CSetting.webIsHome = !item.isChecked();
                CSetting.writeSettingNow(requireContext());
                ToastUtils.showShort(CSetting.webIsHome ? "已设为首页" : "已取消首页");
                break;
        }
        return true;
    }

    /**
     * 显示书签
     */
    private ListItemDialog<BookMarkAdapter> itemDialog;
    private void showBookMark() {
        try {
            List<BookMark> data = DBService.getInstance(requireContext())
                    .queryAllBookMark(BookMark.Type.Web);
            if(data.size() < 1) {
                BookMark bookMark = new BookMark();
                bookMark.setName("暂无书签");
                bookMark.setPath("暂无书签");
                bookMark.setDescribe("暂无书签");
                data.add(bookMark);
            }
            BookMarkAdapter bookMarkAdapter = new BookMarkAdapter(data);
            bookMarkAdapter.setOnItemListener(new BookMarkAdapter.OnItemListener() {
                @Override
                public void onEdit(BookMark bookMark, int index) {
                    if(null == bookMark.getDescribe() || !bookMark.getDescribe().equals("暂无书签")) {
                        RenameFileView addBookMark = new RenameFileView(requireContext());
                        addBookMark.setOnRenameFileListener((newName, dialog) -> {
                            if(dialog != null) dialog.dismiss();
                            if(!TextUtils.isEmpty(newName)) {
                                bookMark.setName(newName);
                                if(DBService.getInstance(requireContext()).updateBookMark(bookMark)){
                                    ToastUtils.showShort("修改成功");
                                    bookMarkAdapter.notifyItemChanged(index);
                                }else {
                                    ToastUtils.showShort("修改失败");
                                }
                            }
                        });
                        addBookMark.setShowPaste(true);
                        addBookMark.rename("重命名书签", bookMark.getName(), "重命名");
                    }
                }
                @Override
                public void onClick(BookMark bookMark) {
                    if(null == bookMark.getDescribe() || !bookMark.getDescribe().equals("暂无书签")) {
                        binding.browserWeb.loadUrl(bookMark.getPath());
                        if(itemDialog != null) itemDialog.dismiss();
                    }
                }
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public boolean onLongClick(BookMark bookMark, int index) {
                    if(null == bookMark.getDescribe() || !bookMark.getDescribe().equals("暂无书签")) {
                        if(DBService.getInstance(requireContext()).deleteBookMark(bookMark)){
                            bookMarkAdapter.getData().remove(index);
                            bookMarkAdapter.notifyDataSetChanged();
                            ToastUtils.showShort("删除成功");
                        }else {
                            ToastUtils.showShort("删除失败");
                        }
                    }
                    return true;
                }
            });
            //
            itemDialog = new ListItemDialog<>(requireContext(), bookMarkAdapter);
            itemDialog.setTitle("书签列表");
            itemDialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebViewSettings() {
        //
        WebSettings webSettings = binding.browserWeb.getSettings();

        webSettings.setJavaScriptEnabled(true);

        webSettings.supportMultipleWindows();
        webSettings.setAllowContentAccess(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);

        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        //自适应屏幕
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        webSettings.setSavePassword(true);
        webSettings.setSaveFormData(true);

        //自动加载图片
        webSettings.setLoadsImagesAutomatically(true);

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);

        webSettings.setGeolocationEnabled(true);
        //支持插件
        webSettings.setPluginsEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON_DEMAND);

        webSettings.setDomStorageEnabled(true);
        //是否有网络
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webSettings.setDatabaseEnabled(false);
        //开启缓存
        webSettings.setAppCacheEnabled(false);
    }

    //下载广播监听
    private class DownloadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    //
                    if(maps.get(downloadId) != null){
                        ToastUtils.showShort( maps.get(downloadId) + " 文件下载完成");
                        maps.remove(downloadId);
                    }
                }
            }
        }
    }

    //
    private void downloadBySystem(String url, String fileName) {
        // 指定下载地址
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
        request.allowScanningByMediaScanner();
        // 设置通知的显示类型，下载进行时和完成后显示通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // 允许在计费流量下下载
        request.setAllowedOverMetered(true);
        // 允许该记录在下载管理界面可见
        request.setVisibleInDownloadsUi(true);
        // 允许漫游时下载
        request.setAllowedOverRoaming(true);
        // 允许下载的网路类型
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        //
        final DownloadManager downloadManager = (DownloadManager) getMainActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        // 添加一个下载任务
        long downloadId = downloadManager.enqueue(request);
        //put
        maps.put(downloadId, fileName);
    }

    /**
     * 解析文件下载链接
     * @param contentDisposition    内容
     * @return  文件命
     */
    private String decodeFileUri(String contentDisposition) {
        try {
            //======
            String decode = URLDecoder.decode(contentDisposition, "UTF-8");
            if(decode.contains("\"")){
                decode = decode.replace("\"","");
            }
            ///
            String subs = "filename=";
            if (decode.contains(subs)) {
                decode = decode.substring(decode.indexOf(subs) + subs.length());
                if(decode.contains(";")){
                    decode = decode.substring(0, decode.indexOf(";"));
                }
                decode = decode.trim();
            }
            //
            return TextUtils.isEmpty(decode) ? "文件" : decode;
        }catch (Exception e){
            return "文件";
        }
    }

    /**
     * 解析url
     * @param url   链接
     * @return      决定加载的方式
     */
    private boolean urlDecode(String url) {
        try {
            //处理intent协议
            if (url.startsWith("intent://")) {
                Intent intent;
                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    intent.addCategory("android.intent.category.BROWSABLE");
                    intent.setComponent(null);
                    intent.setSelector(null);
                    @SuppressLint("QueryPermissionsNeeded")
                    List<ResolveInfo> resolves = getMainActivity().
                            getPackageManager().queryIntentActivities(intent,0);
                    if(resolves.size() > 0){
                        startActivity(intent);
                    }
                    return true;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            // 处理自定义scheme协议
            if (!url.startsWith("http") || !url.startsWith("https")) {
                try {
                    // 以下固定写法
                    final Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                } catch (Exception ignored) {}
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(maps != null) {
            maps.clear();
            maps = null;
            getMainActivity().unregisterReceiver(receiver);
        }
        ///========================
        binding.browserWeb.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        binding.browserWeb.clearHistory();
        ((ViewGroup)binding.browserWeb.getParent()).removeView(binding.browserWeb);
        binding.browserWeb.destroy();
        binding = null;
    }
}
