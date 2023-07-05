package com.cc.fileManage.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.cc.fileManage.entity.TEXFile;
import com.github.memo33.jsquish.Squish;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TexFileUtil {

    /**
     * 打开tex文件
     * @param file  tex文件
     * @return      TEXFile 失败时返回null
     */
    public static TEXFile openTexFile(File file){
        try {
            FileInputStream fi = new FileInputStream(file);
            return openTexFile(fi);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 打开tex文件
     * @param inputStream   tex文件流
     * @return              TEXFile 失败时返回null
     */
    public static TEXFile openTexFile(InputStream inputStream){
        try {
            return new TEXFile(inputStream);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取tex图片信息
     * @param currentFile   TEXFile
     * @return              bitmap图片 失败时返回null
     */
    public static Bitmap loadTexBitmap(TEXFile currentFile){
        try {
            TEXFile.Mipmap mipmap = currentFile.getMainMipmap();
            TEXFile.PixelFormat format = TEXFile.PixelFormat.getType((int)currentFile.getHeader().pixelFormat);
            byte[] argbData = null;
            switch (format) {
                case DXT1:
                    argbData = Squish.decompressImage(argbData, mipmap.width, mipmap.height, mipmap.data, Squish.CompressionType.DXT1);
                    break;
                case DXT3:
                    argbData = Squish.decompressImage(argbData, mipmap.width, mipmap.height, mipmap.data, Squish.CompressionType.DXT3);
                    break;
                case Un18:              ////
                case DXT5:
                    argbData = Squish.decompressImage(argbData, mipmap.width, mipmap.height, mipmap.data, Squish.CompressionType.DXT5);
                    break;
                case ARGB:
                case RGB:
                    argbData = mipmap.data;
                    break;
                default:
                    throw new Exception("未知的像素格式?");
            }
            //格式
            Bitmap.Config config = format.getValue() == TEXFile.PixelFormat.RGB.getValue()
                    || argbData.length > (64 * 1048576) ? Bitmap.Config.RGB_565 : Bitmap.Config.ARGB_8888;
            ////
            try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(argbData))){
                //图片
                Bitmap bitmap = Bitmap.createBitmap(mipmap.width, mipmap.height, config);
                //设置像素
                setPixel(bitmap, mipmap.width, mipmap.height, stream, format.getValue() == TEXFile.PixelFormat.RGB.getValue());
                return bitmap;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    // 设置像素
    private static void setPixel(Bitmap bitmap, int width, int height, DataInputStream inputStream, boolean rgb) throws IOException {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = LSUtil.getUnsignedByte(inputStream.readByte());
                int g = LSUtil.getUnsignedByte(inputStream.readByte());
                int b = LSUtil.getUnsignedByte(inputStream.readByte());
                if(rgb) {
                    bitmap.setPixel(x, y, Color.rgb(r, g, b));
                } else {
                    int a = LSUtil.getUnsignedByte(inputStream.readByte());
                    bitmap.setPixel(x, y, Color.argb(a, r, g, b));
                }
            }
        }
    }

    // 解压bc7
    private static native byte[] unpackBC7(int width, int height, byte[] data);

    // 判断文件是否存在
    public static native int fileExists(String file);

    // 通过fd读取文件
    public static native void read(int fd);
}
