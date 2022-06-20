package com.cc.fileManage.ui.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.boycy815.pinchimageview.PinchImageView;
import com.cc.fileManage.databinding.ActicityPhotoBinding;

import java.io.File;
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
        switch(item.getItemId()){
            case android.R.id.home:
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
            Bitmap bm = BitmapFactory.decodeFile(images.get(index));
            if(bm != null){
                setToolbarSubTitle(f.getName()+"("+bm.getWidth() +" * "+bm.getHeight()+")");
            }else{
                setToolbarSubTitle(f.getName()+" 图片损坏!");
            }
        }catch(Exception e){
            e.printStackTrace();
            setToolbarSubTitle(f.getName()+" 图片损坏!");
        }
        //
        binding.imageOpenViewpage.setAdapter(new CPageAdapter());
        binding.imageOpenViewpage.setOffscreenPageLimit(2);
        binding.imageOpenViewpage.setCurrentItem(index);
        binding.imageOpenViewpage.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setToolbarTitle("图片查看器("+(position+1)+"/"+images.size()+")");
                File file = new File(images.get(position));
                try{
                    Bitmap bm = BitmapFactory.decodeFile(images.get(position));
                    if(bm != null){
                        setToolbarSubTitle(file.getName()+"("+bm.getWidth() +" * "+bm.getHeight()+")");
                    }else{
                        setToolbarSubTitle(file.getName()+" 图片损坏!");
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    setToolbarSubTitle(file.getName()+" 图片损坏!");
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
                // 页面正在滑动时间回调
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // 当pageView 状态发生改变的时候，回调
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
        public boolean isViewFromObject(View arg0, Object arg1) {
            // TODO Auto-generated method stub
            return arg0 == arg1;
        }

        //设置ViewPager指定位置要显示的view
        @Override
        public Object instantiateItem(ViewGroup container, int position){
            PinchImageView im=new PinchImageView(PhotoActivity.this);
            try{
                Bitmap bm = BitmapFactory.decodeFile(images.get(position));
                if(bm != null){
                    im.setImageBitmap(bm);
                }else{
//                    im.setImageResource(R.drawable.oil);
                }
            }catch(Exception e){
                e.printStackTrace();
//                im.setImageResource(R.drawable.oil);
            }
            ///点击隐藏显示
            im.setOnClickListener(new View.OnClickListener(){
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
            container.addView(im);
            return im;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // 销毁对应位置上的Object
            container.removeView((View) object);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
