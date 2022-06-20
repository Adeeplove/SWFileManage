package com.cc.fileManage.ui.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import com.boycy815.pinchimageview.PinchImageView;
import com.cc.fileManage.R;
import com.cc.fileManage.callback.LoadTexFileCallback;
import com.cc.fileManage.callback.SaveTexFileCallback;
import com.cc.fileManage.databinding.ActicityTexBinding;
import com.cc.fileManage.entity.TEXFile;
import com.cc.fileManage.file.ManageFile;
import com.cc.fileManage.utils.CommonUtil;

import java.io.File;

public class LoadTexActivity extends BaseActivity
{
    //bind
    private ActicityTexBinding binding;

    //tex文件路径
    private String  texPath;

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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
            binding.openTexTitle.setText(file.getName());
            //载入图片
            openTexFile(file);
        }
        //
        full(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE,1000, 0,"作为图片保存");
        menu.add(Menu.NONE,1100, 0,"保存为DXT1");
        menu.add(Menu.NONE,1200, 0,"保存为DXT3");
        menu.add(Menu.NONE,1300, 0,"保存为DXT5");
        menu.add(Menu.NONE,1400, 0,"保存为ARGB");

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
                saveToDXT1();
                return true;
            case 1200:
                saveToDXT3();
                return true;
            case 1300:
                saveToDXT5();
                return true;
            case 1400:
                saveToARGB();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 打开tex文件
     * @param file tex文件
     */
    private void openTexFile(File file){
        LoadTexFileCallback load = new LoadTexFileCallback(this, file);
        load.setLoadImageListener(new LoadTexFileCallback.LoadImageListener() {
            @Override
            public void loadImage(Bitmap bitmap, TEXFile texFile) {
                //set
                LoadTexActivity.this.texImage = bitmap;
                LoadTexActivity.this.texFile = texFile;

                if(texImage != null){
                    binding.openTexImage.setImageBitmap(texImage);
                    TEXFile.PixelFormat format = TEXFile.PixelFormat.getType((int)texFile.File.Header.PixelFormat);
                    String texFormat = formatString(format);

                    binding.openTexSubtitle.setText("宽高: "+texImage.getWidth() +" * "+texImage.getHeight()+"  格式: " + texFormat);
                }else{
                    binding.openTexImage.setImageResource(R.mipmap.oil);
                    binding.openTexSubtitle.setText(new File(texPath).getName()+"图片损坏");
                }
                //
                binding.openTexImage.setScaleType(PinchImageView.ScaleType.FIT_CENTER);
            }
        });
        load.execute();
    }

    /**
     * 监听事件
     */
    private void initListener(){
        //点击事件
        binding.openTexImage.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View p1){
                if(show){
                    toolbarAnimal(1);//隐藏toolBar
                    show = false;
                }else{
                    toolbarAnimal(0);//显示toolBar
                    show = true;
                }
            }
        });
        //
        binding.openTexImage.setOnLongClickListener(new OnLongClickListener(){
            @Override
            public boolean onLongClick(View p1) {
                showBitmapInfo();
                return true;
            }
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
            TEXFile.PixelFormat format = TEXFile.PixelFormat.getType((int)texFile.File.Header.PixelFormat);
            String texFormat = formatString(format);

            //tex 纹理
            TEXFile.Mipmap mipmap = texFile.GetMainMipmap();
            //tex type
            TEXFile.TextureType type = TEXFile.TextureType.getType((int)texFile.File.Header.TextureType);
            String texType = typeString(type);

            File file = new File(texPath);
            AlertDialog.Builder ab = new AlertDialog.Builder(this);

            if(file.exists()){
                ab.setTitle(file.getName());
                ab.setMessage("格式:\n"+texFormat+
                        "\n\n纹理贴图:\n"+texFile.File.Header.NumMips+
                        "\n\n宽高:\n"+mipmap.Width+" * "+mipmap.Height+
                        "\n\nTex类型:\n"+texType+
                        "\n\n文件大小:\n"+ ManageFile.getFileSize(file.length())+
                        "\n\n储存路径:\n"+file.getPath());
            }
            ab.show();
        }catch(Exception e){e.printStackTrace();}
    }

    /**
     * tex 格式
     * @param format
     * @return
     */
    public String formatString(TEXFile.PixelFormat format){
        switch(format){
            case DXT1:
                return format.DXT1.getName();
            case DXT3:
                return format.DXT3.getName();
            case DXT5:
                return format.DXT5.getName();
            case ARGB:
                return format.ARGB.getName();
            case RGB:
                return format.RGB.getName();
            case Un18:
                return format.Un18.getName();
            default:
                return "未知";
        }
    }

    /**
     * tex type
     * @param type
     * @return
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
     * @param flag
     */
    private void toolbarAnimal(int flag) {
        if(mAnimator != null && mAnimator.isRunning()){
            mAnimator.cancel();
        }
        //=====================
        if(flag == 0){
//            full(false);
            try{
                Thread.sleep(90);
            }catch (InterruptedException e){}
            mAnimator = ObjectAnimator.ofFloat(binding.openTexToolbar, "translationY", binding.openTexToolbar.getTranslationY(), 0);
            //mAnimator.setDuration(500);
        }else{
//            full(true);
            try{
                Thread.sleep(60);
            }catch (InterruptedException e){}
            mAnimator = ObjectAnimator.ofFloat(binding.openTexToolbar,"translationY",binding.openTexToolbar.getTranslationY(),-binding.openTexToolbar.getHeight());
            //mAnimator.setDuration(500);
        }
        mAnimator.start();
    }

    /**
     * 填充
     * @param enable
     */
    private void full(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    /**
     * 保存为图片
     */
    public void saveBitmap(){
        File file = new File(texPath);
        if(texImage != null){
            String name = file.getName();
            if(name.lastIndexOf(".") != -1){
                name = name.substring(0, name.lastIndexOf("."));
            }
            String saveFile = file.getParent() +"/"+ name + ".png";
            CommonUtil.saveBitmap2file(texImage, this, saveFile);
            Toast.makeText(this, "已保存至图库", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 保存为dxt1
     */
    public void saveToDXT1(){
        //保存
        saveDXTFile(TEXFile.PixelFormat.DXT1);
    }

    /**
     * 保存为dxt3
     */
    public void saveToDXT3(){
        //保存
        saveDXTFile(TEXFile.PixelFormat.DXT3);
    }

    /**
     * 保存为dxt5
     */
    public void saveToDXT5(){
        //保存
        saveDXTFile(TEXFile.PixelFormat.DXT5);
    }

    /**
     * 保存为argb
     */
    public void saveToARGB(){
        //保存
        saveDXTFile(TEXFile.PixelFormat.ARGB);
    }

    /**
     * 保存
     * @param pixelFormat 格式
     */
    private void saveDXTFile(TEXFile.PixelFormat pixelFormat){
        //
        SaveTexFileCallback save = new SaveTexFileCallback(this, pixelFormat, TEXFile.TextureType.OneD);
        save.setGenerateMipmaps(true);
        save.setPreMultiplyAlpha(true);
        save.setTexImage(texImage);

        save.execute(texPath, pixelFormat.getName());
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