package com.cc.fileManage.ui.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.boycy815.pinchimageview.PinchImageView;
import com.cc.fileManage.R;
import com.cc.fileManage.databinding.ActicityTexBinding;
import com.cc.fileManage.entity.TEXFile;
import com.cc.fileManage.entity.file.FileApi;
import com.cc.fileManage.module.file.MergePng;
import com.cc.fileManage.task.tex.LoadTexFileTask;
import com.cc.fileManage.task.tex.SaveTexFileTask;
import com.cc.fileManage.utils.CommonUtil;

import java.io.File;

public class LoadTexActivity extends BaseActivity
{
    //bind
    private ActicityTexBinding binding;

    // handler
    private final Handler handler = new Handler();

    //tex文件路径
    private String   texPath;

    //动画
    private Animator mAnimator;
    private boolean  show = true;

    //图片
    private Bitmap  texImage;
    //tex对象
    private TEXFile texFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //viewBind
        binding = ActicityTexBinding.inflate(getLayoutInflater());
        //
        setContentView(binding.getRoot());

        //初始化页面
        initViews();
        //初始化数据
        initData();
    }

    /**
     * 初始化页面
     */
    public void initViews() {
        setSupportActionBar(binding.openTexToolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        //设置点击事件
        initListener();
    }

    /**
     * 初始化数据
     */
    public void initData() {
        Intent intent = getIntent();
        texPath = intent.getStringExtra("path");
        //文件
        if(texPath != null){
            File file = new File(texPath);
            //设置名称
            binding.openTexToolbar.setTitle(file.getName());
            //载入图片
            openTexFile(file);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE,1000, 0,"保存为PNG");
        menu.add(Menu.NONE,1100, 0,"保存为DXT1");
        menu.add(Menu.NONE,1200, 0,"保存为DXT3");
        menu.add(Menu.NONE,1300, 0,"保存为DXT5");
        menu.add(Menu.NONE,1400, 0,"保存为ARGB");
        ////
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case 1000:        //图片保存
                saveBitmap();
                return true;
            case 1100:
                saveDXTFile(TEXFile.PixelFormat.DXT1);
                return true;
            case 1200:
                saveDXTFile(TEXFile.PixelFormat.DXT3);
                return true;
            case 1300:
                saveDXTFile(TEXFile.PixelFormat.DXT5);
                return true;
            case 1400:
                saveDXTFile(TEXFile.PixelFormat.ARGB);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 打开tex文件
     * @param file tex文件
     */
    @SuppressLint("SetTextI18n")
    private void openTexFile(File file){
        try {
            LoadTexFileTask load = new LoadTexFileTask(this, file);
            load.setLoadImageListener((bitmap, texFile) -> {
                //set
                LoadTexActivity.this.texImage = bitmap;
                LoadTexActivity.this.texFile = texFile;

                if(texImage != null){
                    binding.openTexImage.setImageBitmap(texImage);
                    TEXFile.PixelFormat format = TEXFile.PixelFormat.getType((int)texFile.getHeader().pixelFormat);
                    String texFormat = format == null ? "未知" : formatString(format);

                    binding.openTexToolbar.setSubtitle("宽高: "+texImage.getWidth()
                            +" * "+texImage.getHeight()+"  格式: " + texFormat);
                } else{
                    binding.openTexImage.setImageResource(R.mipmap.oil);
                    binding.openTexToolbar.setSubtitle(new File(texPath).getName()+"图片损坏");
                }
                //
                binding.openTexImage.setScaleType(PinchImageView.ScaleType.FIT_CENTER);
            });
            load.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 监听事件
     */
    private void initListener(){
        //点击事件
        binding.openTexImage.setOnClickListener(p1 -> {
            if(show){
                toolbarAnimal(1);//隐藏toolBar
                show = false;
            } else{
                toolbarAnimal(0);//显示toolBar
                show = true;
            }
        });
        //
        binding.openTexImage.setOnLongClickListener(p1 -> {
            showBitmapInfo();
            return true;
        });
    }

    //显示图片信息
    public void showBitmapInfo(){
        try{
            if(texFile == null){
                Toast.makeText(this, "打开失败!", Toast.LENGTH_SHORT).show();
                return;
            }

            //tex 图片格式
            TEXFile.PixelFormat format = TEXFile.PixelFormat.getType((int)texFile.getHeader().pixelFormat);
            String texFormat = format == null ? "未知" : formatString(format);

            //tex 纹理
            TEXFile.Mipmap mipmap = texFile.getMainMipmap();
            //tex type
            TEXFile.TextureType type = TEXFile.TextureType.getType((int)texFile.getHeader().textureType);
            String texType = type == null ? "未知" : typeString(type);

            File file = new File(texPath);
            AlertDialog.Builder ab = new AlertDialog.Builder(this);

            if(file.exists()){
                ab.setTitle(file.getName());
                ab.setMessage("格式:\n"+texFormat+
                        "\n\n纹理贴图:\n"+texFile.getHeader().numMips+
                        "\n\n宽高:\n"+mipmap.width+" * "+mipmap.height+
                        "\n\nTex类型:\n"+texType+
                        "\n\n文件大小:\n"+ FileApi.sizeToString(file.length())+
                        "\n\n储存路径:\n"+file.getPath());
            }
            ab.show();
        } catch(Exception e){e.printStackTrace();}
    }

    /**
     * tex 格式
     * @param format    格式
     * @return          格式
     */
    public String formatString(TEXFile.PixelFormat format){
        switch(format){
            case DXT1:
                return TEXFile.PixelFormat.DXT1.getName();
            case DXT3:
                return TEXFile.PixelFormat.DXT3.getName();
            case DXT5:
                return TEXFile.PixelFormat.DXT5.getName();
            case ARGB:
                return TEXFile.PixelFormat.ARGB.getName();
            case RGB:
                return TEXFile.PixelFormat.RGB.getName();
            case Un18:
                return TEXFile.PixelFormat.Un18.getName();
            default:
                return "未知";
        }
    }

    /**
     * tex type
     * @param type  图片类型
     * @return      图片类型
     */
    public String typeString(TEXFile.TextureType type){
        switch(type){
            case OneD:
                return TEXFile.TextureType.OneD.getDes();
            case TwoD:
                return TEXFile.TextureType.TwoD.getDes();
            case ThreeD:
                return TEXFile.TextureType.ThreeD.getDes();
            case CubeMap:
                return TEXFile.TextureType.CubeMap.getDes();
            default:
                return "未知";
        }
    }

    /**
     * 隐藏toolbar动画
     * @param flag  标记
     */
    private void toolbarAnimal(int flag) {
        if(mAnimator != null && mAnimator.isRunning()){
            mAnimator.cancel();
        }
        ///
        handler.postDelayed(() -> {
            if(flag == 0){
                mAnimator = ObjectAnimator.ofFloat(binding.openTexToolbar,
                        "translationY", binding.openTexToolbar.getTranslationY(), 0);
            } else{
                mAnimator = ObjectAnimator.ofFloat(binding.openTexToolbar, "translationY",
                        binding.openTexToolbar.getTranslationY(), -binding.openTexToolbar.getHeight());
            }
            mAnimator.start();
        }, 60);
    }

    /**
     * 保存为图片
     */
    public void saveBitmap(){
        File file = new File(texPath);
        if(texImage != null){
            String saveFile = file.getParent() + File.separator + MergePng.replaceSuffix(file.getName(), "png");
            CommonUtil.saveBitmap2file(texImage, this, saveFile, true);
            Toast.makeText(this, "已保存至图库", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 保存
     * @param pixelFormat 格式
     */
    private void saveDXTFile(TEXFile.PixelFormat pixelFormat){
        //
        SaveTexFileTask save = new SaveTexFileTask(this, pixelFormat, TEXFile.TextureType.OneD);
        save.setGenerateMipmaps(true);
        save.setPreMultiplyAlpha(true);
        save.setTexImage(texImage);
        ///====
        save.execute(texPath);
    }

    @Override
    protected void onDestroy() {
        //t
        texFile = null;
        //
        if(texImage != null){
            texImage.recycle();
            texImage = null;
        }
        super.onDestroy();
    }
}