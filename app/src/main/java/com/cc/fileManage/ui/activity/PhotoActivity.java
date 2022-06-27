package com.cc.fileManage.ui.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.cc.fileManage.R;
import com.cc.fileManage.databinding.ActicityPhotoBinding;
import com.cc.fileManage.entity.file.DFileMethod;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PhotoActivity extends AppCompatActivity {

    private ActicityPhotoBinding binding;
    private Handler handler;

    private List<String> images;        //图片路径集合
    private int index;                  //图片下标
    //================================================
    private Animator mAnimator;
    private boolean show = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        binding = ActicityPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //
        initData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 初始化数据
     */
    private void initData(){
        this.handler = new Handler();
        this.images = new ArrayList<>();

        //
        setSupportActionBar(binding.imageOpenToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        binding.imageOpenToolbar.getBackground().setAlpha(50);
        binding.imageOpenToolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        binding.imageOpenToolbar.setSubtitleTextColor(Color.parseColor("#ffffff"));
        //
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //
        //图片数据
        Intent intent = getIntent();
        images = intent.getStringArrayListExtra("image");
        index = intent.getIntExtra("index", 0);
        //
        setToolbarTitle("图片查看器("+(index+1)+"/"+images.size()+")");
        File f = new File(images.get(index));
        try{
            BitmapFactory.Options bitmapWH = getBitmapWH(f);
            if(bitmapWH.outHeight < 1 || bitmapWH.outWidth < 1) {
                setToolbarSubTitle(f.getName()+" 图片损坏!");
            }else {
                setToolbarSubTitle(f.getName()+"("+bitmapWH.outWidth +" * "+bitmapWH.outHeight+")");
            }
        }catch(Exception e){
            e.printStackTrace();
            setToolbarSubTitle(f.getName()+" 图片损坏!");
        }
        //
        binding.imageOpenViewpage.setAdapter(new CPageAdapter());
        binding.imageOpenViewpage.setOffscreenPageLimit(2);
        binding.imageOpenViewpage.setCurrentItem(index);
        binding.imageOpenViewpage.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setToolbarTitle("图片查看器("+(position+1)+"/"+images.size()+")");
                File file = new File(images.get(position));
                try{
                    BitmapFactory.Options bitmapWH = getBitmapWH(f);
                    if(bitmapWH.outHeight < 1 || bitmapWH.outWidth < 1) {
                        setToolbarSubTitle(f.getName()+" 图片损坏!");
                    }else {
                        setToolbarSubTitle(f.getName()+"("+bitmapWH.outWidth +" * "+bitmapWH.outHeight+")");
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    setToolbarSubTitle(file.getName()+" 图片损坏!");
                }
            }
        });
        //全屏
        full(true);
    }

    //======adapter
    private class CPageAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return images.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View arg0, @NonNull Object arg1) {
            // TODO Auto-generated method stub
            return arg0 == arg1;
        }

        //设置ViewPager指定位置要显示的view
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position){
//            PinchImageView im = new PinchImageView(PhotoActivity.this);
            SubsamplingScaleImageView im = new SubsamplingScaleImageView(PhotoActivity.this);
            try{
                File file = new File(images.get(position));
                ///
                Bitmap bm = getRealBitmap(file);
                if(bm != null){
                    im.setImage(ImageSource.bitmap(bm));
                }else{
                    im.setImage(ImageSource.resource(R.drawable.ic_oil));
                }
            }catch(Exception e){
                e.printStackTrace();
                im.setImage(ImageSource.resource(R.drawable.ic_oil));
            }
            ///点击隐藏显示
            im.setOnClickListener(p1 -> {
                if(show){
                    toolbarAnimal(1);//隐藏toolBar
                    show = false;
                }else{
                    toolbarAnimal(0);//显示toolBar
                    show = true;
                }
            });
            container.addView(im);
            return im;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            // 销毁对应位置上的Object
            container.removeView((View) object);
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
//        full(true);
        //==========================
        if(flag == 0){
//            full(false);
            try{
                Thread.sleep(90);
            }catch (InterruptedException ignored){}
            mAnimator = ObjectAnimator.ofFloat(binding.imageOpenToolbar, "translationY",
                    binding.imageOpenToolbar.getTranslationY(), 0);
        }else{
//            full(true);
            try{
                Thread.sleep(60);
            }catch (InterruptedException ignored){}
            mAnimator = ObjectAnimator.ofFloat(binding.imageOpenToolbar,"translationY",
                    binding.imageOpenToolbar.getTranslationY(),-binding.imageOpenToolbar.getHeight());
        }

        mAnimator.start();
    }

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

    private void setToolbarTitle(final String mes){
        handler.post(() -> binding.imageOpenToolbar.setTitle(mes));
    }

    private void setToolbarSubTitle(final String mes){
        handler.post(() -> binding.imageOpenToolbar.setSubtitle(mes));
    }

    /**
     * 获取图片信息 不加载至内存
     * @param f 图片文件
     * @return  图片信息
     */
    private BitmapFactory.Options getBitmapWH(File f) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        try {
            //设置为true,代表加载器不加载图片,而是把图片的宽高读出来
            opts.inJustDecodeBounds=true;
            if(f.canRead()) {
                BitmapFactory.decodeFile(images.get(index), opts);
            }else if(DFileMethod.isAndroidDataDir(f)) {
                Uri uri = DFileMethod.getDocumentUri(f.getPath());
                if(uri != null) {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    if(inputStream != null)
                        BitmapFactory.decodeStream(inputStream, null, opts);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return opts;
    }

    /**
     * 加载图片
     * @param f 图片文件
     * @return  图片
     */
    private Bitmap getRealBitmap(File f) {
        Bitmap bm = null;
        try {
            if(f.canRead()) {
                bm = BitmapFactory.decodeFile(images.get(index));
            }else if(DFileMethod.isAndroidDataDir(f)) {
                Uri uri = DFileMethod.getDocumentUri(f.getPath());
                if(uri != null) {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    if(inputStream != null)
                        bm = BitmapFactory.decodeStream(inputStream);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return bm;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
