package com.cc.fileManage.ui.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.boycy815.pinchimageview.PinchImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.cc.fileManage.R;
import com.cc.fileManage.databinding.ActicityPhotoBinding;
import com.cc.fileManage.entity.file.MFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class PhotoActivity extends BaseActivity {

    private ActicityPhotoBinding binding;
    private Handler handler;

    //PinchImageView缓存
    private final LinkedList<PinchImageView> viewCache = new LinkedList<>();

    private List<String> images;        //图片路径集合
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

    /**
     * 初始化数据
     */
    private void initData(){
        this.handler = new Handler();
        this.images = new ArrayList<>();
        //
        setSupportActionBar(binding.imageOpenToolbar);
        //
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        //
        //图片数据
        Intent intent = getIntent();
        images = intent.getStringArrayListExtra("image");
        //图片下标
        int index = intent.getIntExtra("index", 0);
        // 设置图片信息
        initImageInfo(index);
        //
        binding.imageOpenViewpage.setAdapter(new CPageAdapter(this));
        binding.imageOpenViewpage.setOffscreenPageLimit(1);
        binding.imageOpenViewpage.setCurrentItem(index);
        binding.imageOpenViewpage.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                // 设置图片信息
                initImageInfo(position);
            }
        });
        // 默认背景颜色
        binding.imageOpenViewpage.setTag(android.R.color.white);
    }

    /**
     * 设置图片信息
     * @param index     下标
     */
    private void initImageInfo(int index) {
        setToolbarTitle("图片查看器("+(index +1)+"/"+images.size()+")");
        MFile f = MFile.create(this, images.get(index));
        try {
            BitmapFactory.Options bitmapWH = getBitmapWH(f);
            if(bitmapWH.outHeight < 1 || bitmapWH.outWidth < 1) {
                setToolbarSubTitle(f.getName()+" 打开失败!");
            } else {
                setToolbarSubTitle(f.getName()+" ("+bitmapWH.outWidth +" * "+bitmapWH.outHeight+")");
            }
        } catch(Exception e){
            e.printStackTrace();
            setToolbarSubTitle(f.getName()+" 打开失败!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE,1000, 0,"更换颜色").setIcon(R.drawable.ic_refresh_night)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        ////
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case 1000:
                setBgColor();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ///设置背景颜色
    private void setBgColor() {
        int color = (int) binding.imageOpenViewpage.getTag();
        if(color == android.R.color.white) {
            binding.imageOpenViewpage.setBackgroundResource(android.R.color.darker_gray);
            binding.imageOpenViewpage.setTag(android.R.color.darker_gray);
        }
        else if(color == android.R.color.darker_gray) {
            binding.imageOpenViewpage.setBackgroundResource(android.R.color.black);
            binding.imageOpenViewpage.setTag(android.R.color.black);
        }
        else {
            binding.imageOpenViewpage.setBackgroundResource(android.R.color.white);
            binding.imageOpenViewpage.setTag(android.R.color.white);
        }
    }

    //======adapter
    private class CPageAdapter extends PagerAdapter {

        private final Context context;

        public CPageAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View arg0, @NonNull Object arg1) {
            return arg0 == arg1;
        }

        //设置ViewPager指定位置要显示的view
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position){
            PinchImageView pinchImageView;
            if (viewCache.size() > 0) {
                pinchImageView = viewCache.remove();
                pinchImageView.reset();
            } else {
                pinchImageView = new PinchImageView(PhotoActivity.this, false);
            }
            container.addView(pinchImageView);
            // 设置
            try {
                // 加载图片
                MFile file = MFile.create(context, images.get(position));
                BitmapFactory.Options bitmapWH = getBitmapWH(file);
                //
                Glide.with(getApplicationContext())
                        .load(file.getUri())
                        .signature(new ObjectKey(file.lastModified()))
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .placeholder(R.drawable.ic_oil)
                        .override(bitmapWH.outWidth, bitmapWH.outHeight)
                        .into(pinchImageView);
            } catch(Exception e){
                pinchImageView.setImageResource(R.drawable.ic_oil);
            }
            ///点击隐藏显示
            pinchImageView.setOnClickListener(p1 -> {
                if(show){
                    toolbarAnimal(1);//隐藏toolBar
                    show = false;
                } else{
                    toolbarAnimal(0);//显示toolBar
                    show = true;
                }
            });
            return pinchImageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            // 销毁对应位置上的Object
            PinchImageView piv = (PinchImageView) object;
            container.removeView(piv);
            viewCache.add(piv);
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
        //==========================
        if(flag == 0){
            try{
                Thread.sleep(90);
            }catch (InterruptedException ignored){}
            mAnimator = ObjectAnimator.ofFloat(binding.imageOpenToolbar, "translationY",
                    binding.imageOpenToolbar.getTranslationY(), 0);
        } else{
            try{
                Thread.sleep(60);
            }catch (InterruptedException ignored){}
            mAnimator = ObjectAnimator.ofFloat(binding.imageOpenToolbar,"translationY",
                    binding.imageOpenToolbar.getTranslationY(),-binding.imageOpenToolbar.getHeight());
        }
        mAnimator.start();
    }

    private void setToolbarTitle(final String mes){
        handler.post(() -> Objects.requireNonNull(getSupportActionBar()).setTitle(mes));
    }

    private void setToolbarSubTitle(final String mes){
        handler.post(() -> Objects.requireNonNull(getSupportActionBar()).setSubtitle(mes));
    }

    /**
     * 获取图片信息 不加载至内存
     * @param f 图片文件
     * @return  图片信息
     */
    private BitmapFactory.Options getBitmapWH(MFile f) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        try {
            //设置为true,代表加载器不加载图片,而是把图片的宽高读出来
            opts.inJustDecodeBounds = true;
            InputStream inputStream = f.openInputStream();
            if(inputStream != null)
                BitmapFactory.decodeStream(inputStream, null, opts);
        } catch (Exception e){
            e.printStackTrace();
        }
        return opts;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewCache.clear();
    }
}
