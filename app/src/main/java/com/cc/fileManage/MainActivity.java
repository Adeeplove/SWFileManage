package com.cc.fileManage;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.cc.fileManage._static.CSetting;
import com.cc.fileManage.databinding.ActivityMainBinding;
import com.cc.fileManage.module.ApplyPermission;
import com.cc.fileManage.ui.activity.BaseActivity;
import com.cc.fileManage.ui.browser.FileBrowserFragment;
import com.cc.fileManage.ui.views.RenameFileView;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

public class MainActivity extends BaseActivity {
    //=====================
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;

    ///============
    private ActivityMainBinding binding;

    // 加载tex解码库
    static {
        System.loadLibrary("fileManage");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //viewBind
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //设置布局
        setContentView(binding.getRoot());

        //设置toolbar
        setSupportActionBar(binding.appBarMain.toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        binding.appBarMain.toolbarTitle.setMaxLines(1);
        binding.appBarMain.toolbarTitle.setEllipsizeNoun(TextUtils.TruncateAt.START);
        //
        binding.appBarMain.toolbarSubtitle.setMaxLines(1);
        binding.appBarMain.toolbarSubtitle.setEllipsizeNoun(TextUtils.TruncateAt.START);
        // 将每个菜单 ID 作为一组 ID 传递
        // 菜单应被视为顶级目的地.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home)
                .setOpenableLayout(binding.drawerLayout).build();
        //
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        // 初始化一些数据
        initData();
    }

    // 初始化一些数据
    @SuppressLint("Recycle")
    private void initData() {
        // 点击事件
        setSubtitleClick();
        // 申请权限
        requestAccess();
        ///
        new Handler().postDelayed(() -> {
            new Thread(() -> {

            }).start();
        }, 500);
    }

    /**
     * 跳转至文件浏览器fragment
     */
    private void initStartTo(){
        navController.navigate(CSetting.webIsHome ? R.id.nav_browser : R.id.nav_home);
    }

    /**
     * 设置点击事件
     */
    private void setSubtitleClick(){
        binding.appBarMain.toolbarTitle.setOnClickListener(v -> startTo());
        binding.appBarMain.toolbarSubtitle.setOnClickListener(v -> startTo());
    }

    /**
     * 跳转事件
     */
    private void startTo() {
        String text = isFileBrowserFragment() ?
                binding.appBarMain.toolbarTitle.getTag().toString()
                : binding.appBarMain.toolbarSubtitle.getTag().toString();
        if(TextUtils.isEmpty(text)) {
            return;
        }
        RenameFileView renameFileView = new RenameFileView(this);
        renameFileView.setOnRenameFileListener((newName, dialog) -> {
            dialog.dismiss();
            if(onEventChangeListener != null)
                onEventChangeListener.onUpdate(newName);
        });
        renameFileView.setShowPaste(true);
        renameFileView.rename("跳转至", text, "跳转");
    }

    /**
     * 是否是文件浏览器
     * @return  true 或 false
     */
    private boolean isFileBrowserFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if(fragment != null) {
            Fragment frag = fragment.getChildFragmentManager().getPrimaryNavigationFragment();
            return frag instanceof FileBrowserFragment;
        }
        return true;
    }

    /**
     * 设置title文本内容
     * @param text  文本内容
     */
    public void setTitleText(String text){
        if(!TextUtils.isEmpty(text) && !text.equals(binding.appBarMain.toolbarTitle.getText().toString())) {
            binding.appBarMain.toolbarTitle.setText(text);
            binding.appBarMain.toolbarTitle.setTag(text);
        }
    }

    /**
     * 设置subtitle文本内容
     * @param text  文本内容
     */
    public void setSubtitleText(String text){
        if(!TextUtils.isEmpty(text)) {
            binding.appBarMain.toolbarSubtitle.setText(text);
            binding.appBarMain.toolbarSubtitle.setTag(text);
        }
    }

    @SuppressLint("RtlHardcoded")
    public void openDrawer() {
        if(binding.drawerLayout.isDrawerOpen(Gravity.LEFT))
            binding.drawerLayout.closeDrawer(Gravity.LEFT);
        else
            binding.drawerLayout.openDrawer(Gravity.LEFT);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        if(onEventChangeListener != null)
            return onEventChangeListener.onCreateMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(onEventChangeListener != null)
            return onEventChangeListener.onMenuItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    //按键监听
    @SuppressLint("RtlHardcoded")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && binding.drawerLayout.isDrawerOpen(Gravity.LEFT)){
            binding.drawerLayout.closeDrawer(Gravity.LEFT);
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(onEventChangeListener != null)
                onEventChangeListener.onBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //
        if (requestCode == XXPermissions.REQUEST_CODE) {
            if (XXPermissions.isGranted(this, Permission.MANAGE_EXTERNAL_STORAGE)) {
                initStartTo();  //跳转
            } else {
                showDialog();
            }
        }
        else if(requestCode == ApplyPermission.ANDROID_DATA){
            if(data == null) return;
            Uri uri = data.getData();
            if(uri == null) return;
            //
            int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            //持久化保存权限
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
        }
    }

    /**
     * 申请权限
     */
    private void requestAccess(){
        //android 11申请存储权限
        if(XXPermissions.isGranted(this, Permission.MANAGE_EXTERNAL_STORAGE)){
            initStartTo();
        } else {
            XXPermissions.with(this)
                    .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                    .request(new OnPermissionCallback() {
                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            if(all) {
                                initStartTo();
                            }else {
                                showDialog();
                            }
                        }
                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            if (never) {
                                // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                XXPermissions.startPermissionActivity(MainActivity.this, permissions);
                            } else {
                                //
                                showDialog();
                            }
                        }
                    });
        }
    }

    /**
     * 提示弹框
     */
    private void showDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("提示");
        dialog.setMessage(getString(R.string.app_name)+"需要读写内部储存的权限来管理文件");
        dialog.setCancelable(false);
        dialog.setPositiveButton("授予", (dialog12, which) -> requestAccess());
        dialog.setNegativeButton("退出", (dialog1, which) -> {
            finish();
            System.exit(0);
        });
        dialog.show();
    }

    /**
     * 退出提示
     */
    private long beginTime = 0L;
    public void exit() {
        //实现两次点击退出；
        if (System.currentTimeMillis() - beginTime > 2000) {
            Toast.makeText(this, "再点击一次退出", Toast.LENGTH_SHORT).show();
            beginTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }

    private OnEventChangeListener onEventChangeListener;

    public void setOnEventChangeListener(OnEventChangeListener onEventChangeListener) {
        this.onEventChangeListener = onEventChangeListener;
    }

    /**
     * 事件通知
     */
    public interface OnEventChangeListener{
        void onBack();
        void onUpdate(String path);
        boolean onCreateMenu(Menu menu);
        boolean onMenuItemSelected(MenuItem item);
    }
}