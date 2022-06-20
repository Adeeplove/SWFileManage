package com.cc.fileManage;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;

import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.blankj.utilcode.util.ToastUtils;
import com.cc.fileManage.databinding.ActivityMainBinding;
import com.cc.fileManage.ui.activity.BaseActivity;
import com.cc.fileManage.ui.views.RenameFileView;
import com.cc.fileManage.utils.RPermissionUtil;
import com.google.android.material.navigation.NavigationView;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

public class MainActivity extends BaseActivity {

    //=====================
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private DrawerLayout drawerLayout;

    //文件浏览器fragment
//    private FileBrowserFragment fileBrowserFragment;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //viewBind
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //设置布局
        setContentView(binding.getRoot());

        //设置
        //设置toolbar
        setSupportActionBar(binding.appBarMain.toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        binding.appBarMain.toolbarSubtitle.setMaxLines(1);
        binding.appBarMain.toolbarSubtitle.setEllipsizeNoun(TextUtils.TruncateAt.START);
        //
        //抽屉
        drawerLayout = binding.drawerLayout;
        //菜单
        NavigationView navigationView = binding.navView;
        // 将每个菜单 ID 作为一组 ID 传递
        // 菜单应被视为顶级目的地.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home).setOpenableLayout(drawerLayout).build();
        //
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //点击事件
        setSubtitleClick();
        //申请权限
        requestAccess();
    }

    /**
     * 跳转至文件浏览器fragment
     */
    private void startTo(){
        navController.navigate(R.id.nav_home);
    }

    /**
     * 设置点击事件
     */
    private void setSubtitleClick(){
        binding.appBarMain.toolbarSubtitle.setOnClickListener(v -> {
            String text = binding.appBarMain.toolbarSubtitle.getText().toString();
            if(TextUtils.isEmpty(text)){
                return;
            }
            RenameFileView renameFileView = new RenameFileView(MainActivity.this);
            renameFileView.setOnRenameFileListener((newName, dialog) -> {
                dialog.dismiss();
                if(onEventChangeListener != null)
                    onEventChangeListener.onUpdate(newName);
            });
            renameFileView.setShowPaste(true);
            renameFileView.rename("跳转至", text, "跳转");
        });
    }

    /**
     * 获取当前使用的fragment
     */
//    public void setFileBrowserFragment(Class<?> clazz) {
//        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().getFragments().get(0);
//        List<Fragment> fragmentList = navHostFragment.getChildFragmentManager().getFragments();
//        //
//        for (Fragment fragment : fragmentList) {
//            if(fragment.getClass().isAssignableFrom(clazz)){
//                fileBrowserFragment = (FileBrowserFragment) fragment;
//                break;
//            }
//        }
//    }

    /**
     * 设置subtitle文本内容
     * @param text  文本内容
     */
    public void setSubtitleText(String text){
        binding.appBarMain.toolbarSubtitle.setText(text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
        if(keyCode == KeyEvent.KEYCODE_BACK && drawerLayout.isDrawerOpen(Gravity.LEFT)){
            drawerLayout.closeDrawer(Gravity.LEFT);
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
        //
        if (requestCode == XXPermissions.REQUEST_CODE) {
            if (XXPermissions.isGranted(this, Permission.MANAGE_EXTERNAL_STORAGE)) {
                startTo();  //跳转
            }else {
                showDialog();
            }
        }
        else if(requestCode == RPermissionUtil.ANDROID_DATA){
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
            startTo();
        }else {
            XXPermissions.with(this)
                    .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                    .request(new OnPermissionCallback() {
                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            if(all) {
                                startTo();
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
        if (System.currentTimeMillis() - beginTime > 2000)
        {
            ToastUtils.showShort("再点击一次退出");
            beginTime = System.currentTimeMillis();
        }else {
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
    }
}