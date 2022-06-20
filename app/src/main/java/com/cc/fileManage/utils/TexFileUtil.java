package com.cc.fileManage.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.cc.fileManage.entity.TEXFile;
import com.github.memo33.jsquish.Squish;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class TexFileUtil {

    public static TEXFile openTexFile(File file){
        try {
            FileInputStream fi = new FileInputStream(file);
            return openTexFile(fi);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static TEXFile openTexFile(InputStream inputStream){
        try {
            //实例化
            return new TEXFile(inputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 加载tex图片
     * @param currentFile tex文件
     * @return
     */
    public static Bitmap loadTexBitmap(TEXFile currentFile){
        try {
            TEXFile.Mipmap mipmap = currentFile.GetMainMipmap();

            TEXFile.PixelFormat format = TEXFile.PixelFormat.getType((int)currentFile.File.Header.PixelFormat);
            byte[] argbData = new byte[0];

            switch (format)
            {
                case DXT1:
                    argbData = Squish.decompressImage(argbData, mipmap.Width, mipmap.Height, mipmap.Data, Squish.CompressionType.DXT1);
                    break;
                case DXT3:
                    argbData = Squish.decompressImage(argbData, mipmap.Width, mipmap.Height, mipmap.Data, Squish.CompressionType.DXT3);
                    break;
                case Un18:
                case DXT5:
                    argbData = Squish.decompressImage(argbData, mipmap.Width, mipmap.Height, mipmap.Data, Squish.CompressionType.DXT5);
                    break;
                case ARGB:
                case RGB:
                    argbData = mipmap.Data;
                    break;
                default:
                    throw new Exception("未知的像素格式?");
            }

            //流
            DataInputStream imgReader = new DataInputStream(new ByteArrayInputStream(argbData));
            //图片
            Bitmap bit = null;
            //如果是RGB图片
            if(format.getValue() == TEXFile.PixelFormat.RGB.getValue()){
                bit = Bitmap.createBitmap(mipmap.Width, mipmap.Height, Bitmap.Config.RGB_565);
                for (int y = 0; y < mipmap.Height; y++)
                {
                    for (int x = 0; x < mipmap.Width; x++)
                    {
                        int r = FormatToUtil.getUnsignedByte(imgReader.readByte());
                        int g = FormatToUtil.getUnsignedByte(imgReader.readByte());
                        int b = FormatToUtil.getUnsignedByte(imgReader.readByte());

                        bit.setPixel(x, y, Color.rgb(r, g, b));
                    }
                }
            }else{
                if(argbData.length > (64 * 1048576)){
                    bit = Bitmap.createBitmap(mipmap.Width, mipmap.Height, Bitmap.Config.RGB_565);
                }else{
                    bit = Bitmap.createBitmap(mipmap.Width, mipmap.Height, Bitmap.Config.ARGB_8888);
                }
                //===============================
                for (int y = 0; y < mipmap.Height; y++)
                {
                    for (int x = 0; x < mipmap.Width; x++)
                    {
                        int r = FormatToUtil.getUnsignedByte(imgReader.readByte());
                        int g = FormatToUtil.getUnsignedByte(imgReader.readByte());
                        int b = FormatToUtil.getUnsignedByte(imgReader.readByte());
                        int a = FormatToUtil.getUnsignedByte(imgReader.readByte());
                        //
                        bit.setPixel(x, y, Color.argb(a, r, g, b));
                    }
                }
            }
            imgReader.close();
            return bit;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
