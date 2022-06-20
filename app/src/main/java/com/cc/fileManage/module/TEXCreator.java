package com.cc.fileManage.module;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.cc.fileManage.entity.TEXFile;
import com.cc.fileManage.utils.CommonUtil;
import com.cc.fileManage.utils.FormatToUtil;
import com.github.memo33.jsquish.Squish;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TEXCreator 
{
	//格式
	private TEXFile.PixelFormat PixelFormat;
	//类型
	private TEXFile.TextureType TextureType;
	
	private boolean GenerateMipmaps;

	private boolean preMultiplyAlpha;

	public void setGenerateMipmaps(boolean generateMipmaps)
	{
		GenerateMipmaps = generateMipmaps;
	}

	public void setPreMultiplyAlpha(boolean preMultiplyAlpha)
	{
		this.preMultiplyAlpha = preMultiplyAlpha;
	}
	
	//mipmap
	public class Mipmap {
		public int Width;
		public int Height;
		public int Pitch;
		public byte[] ARGBData;

		public Mipmap(int w, int h, int p, byte[] d) { 
			Width = w; Height = h; Pitch = p; ARGBData = d; 
		}
	}
	
	public TEXCreator(TEXFile.PixelFormat format, TEXFile.TextureType type){
		this.PixelFormat = format;
		this.TextureType = type;
	}
	
	//png to tex
	public void ConvertPNGToTex(Bitmap inputImage, FileOutputStream outputStream) throws Exception
	{
		List<Mipmap> Mipmaps = new ArrayList<Mipmap>();
		//翻转图片
		inputImage = CommonUtil.scaleImageView(inputImage);
		
		Mipmaps.add(GenerateMipmap(inputImage, PixelFormat, preMultiplyAlpha));
		
		//
		if (GenerateMipmaps) {
			int width = inputImage.getWidth();
			int height = inputImage.getHeight();

			while (Math.max(width, height) > 1)
			{
				width = Math.max(1, width >> 1);
				height = Math.max(1, height >> 1);

				Mipmaps.add(GenerateMipmap(inputImage, PixelFormat, width, height, preMultiplyAlpha));
			}
		}
		inputImage.recycle();

		TEXFile outputTEXFile = new TEXFile();

		outputTEXFile.File.Header.Platform = 0;
		outputTEXFile.File.Header.PixelFormat = PixelFormat.getValue();
		outputTEXFile.File.Header.TextureType = TextureType.getValue();
		outputTEXFile.File.Header.NumMips = Mipmaps.size();
		outputTEXFile.File.Header.Flags = 0;
		
		ByteArrayOutputStream ms = new ByteArrayOutputStream();
		
		for(Mipmap mip : Mipmaps)
		{
			//宽
			byte[] width = FormatToUtil.toLH((short)mip.Width);
			ms.write(width);

			//高
			byte[] height = FormatToUtil.toLH((short)mip.Height);
            ms.write(height);
			//点
			byte[] pitch = FormatToUtil.toLH((short)mip.Pitch);
			ms.write(pitch);

			//data.length
			byte[] ARGBlength = FormatToUtil.toLH(mip.ARGBData.length);
			ms.write(ARGBlength);
		}
		
		DataOutputStream out = outputTEXFile.SaveFileHeader(outputStream);
		out.write(ms.toByteArray());
		out.flush();
		
		for(Mipmap mip : Mipmaps){
			out.write(mip.ARGBData);
			out.flush();
		}
		Mipmaps.clear();

		out.close();
		//关闭
        ms.close();
	}
    
    //png to tex
    //默认zip包
    public byte[] ConvertPNGToTex(Bitmap inputImage) throws IOException
    {
        List<Mipmap> Mipmaps = new ArrayList<Mipmap>();
        //翻转图片
        inputImage = CommonUtil.scaleImageView(inputImage);

        Mipmaps.add(GenerateMipmap(inputImage, PixelFormat, preMultiplyAlpha));

        //抗锯齿
        if (GenerateMipmaps) {
            int width = inputImage.getWidth();
            int height = inputImage.getHeight();

            while (Math.max(width, height) > 1)
            {
                width = Math.max(1, width >> 1);
                height = Math.max(1, height >> 1);

                Mipmaps.add(GenerateMipmap(inputImage, PixelFormat, width, height, preMultiplyAlpha));
            }
        }
		inputImage.recycle();

        TEXFile outputTEXFile = new TEXFile();

        outputTEXFile.File.Header.Platform = 0;
        outputTEXFile.File.Header.PixelFormat = PixelFormat.getValue();
        outputTEXFile.File.Header.TextureType = TextureType.getValue();
        outputTEXFile.File.Header.NumMips = Mipmaps.size();
        outputTEXFile.File.Header.Flags = 0;
        
		ByteArrayOutputStream ms = new ByteArrayOutputStream();

		for(Mipmap mip : Mipmaps)
		{
			//宽
			byte[] width = FormatToUtil.toLH((short)mip.Width);
			ms.write(width);

			//高
			byte[] height = FormatToUtil.toLH((short)mip.Height);
            ms.write(height);
			//点
			byte[] pitch = FormatToUtil.toLH((short)mip.Pitch);
			ms.write(pitch);

			//data.length
			byte[] ARGBlength = FormatToUtil.toLH(mip.ARGBData.length);
			ms.write(ARGBlength);
		}
		
		for(Mipmap mip : Mipmaps){
			ms.write(mip.ARGBData);
		}
		Mipmaps.clear();

		outputTEXFile.File.Raw = ms.toByteArray();
        //回收
        ms.close();
        return outputTEXFile.FileData();
	}
	
	private Mipmap GenerateMipmap(Bitmap inputImage, TEXFile.PixelFormat pixelFormat, int width, int height, boolean preMultiplyAlpha)
	{
		Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	
		//画布
		Canvas can = new Canvas(b);
		//画笔
		Paint paint = new Paint();
		//抗锯齿
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		//防抖动
		paint.setDither(true);
        //双线性插值法绘图
        //paint.setFilterBitmap(true);

		//位置 要绘制的bitmap区域 
		Rect srcRect = new Rect(0, 0, inputImage.getWidth(), inputImage.getHeight());
        //要绘制的位置
		Rect destRect = new Rect(0, 0, width, height);
        
		//画
		can.drawBitmap(inputImage, srcRect, destRect, paint);

		return GenerateMipmap(b, pixelFormat, preMultiplyAlpha);
	}
	
	private Mipmap GenerateMipmap(Bitmap inputImage, TEXFile.PixelFormat pixelFormat, boolean preMultiplyAlpha)
	{
		byte[] rgba = new byte[inputImage.getWidth() * inputImage.getHeight() * 4];

		for (int y = 0; y < inputImage.getHeight(); y++)
			for (int x = 0; x < inputImage.getWidth(); x++)
			{
                //获取像素
				int color = inputImage.getPixel(x, y);
				// a r g b
				int r = Color.red(color);
				int g = Color.green(color);
				int b = Color.blue(color);
				int a = Color.alpha(color);

				if (preMultiplyAlpha) {
					float alphamod = (float)a / 255.0f; // Normalize.

					byte newR = (byte)(r * alphamod);
					byte newG = (byte)(g * alphamod);
					byte newB = (byte)(b * alphamod);

                    color = Color.argb(a, FormatToUtil.getUnsignedByte(newR), FormatToUtil.getUnsignedByte(newG), FormatToUtil.getUnsignedByte(newB));
                    
                    // a r g b
                    r = Color.red(color);
                    g = Color.green(color);
                    b = Color.blue(color);
                    a = Color.alpha(color);
                }
                
				rgba[y * inputImage.getWidth() * 4 + x * 4 + 0] = (byte)r;
				rgba[y * inputImage.getWidth() * 4 + x * 4 + 1] = (byte)g;
				rgba[y * inputImage.getWidth() * 4 + x * 4 + 2] = (byte)b;
				rgba[y * inputImage.getWidth() * 4 + x * 4 + 3] = (byte)a;
			}

		byte[] finalImageData = null;

		switch (pixelFormat)
		{
			case DXT1:
				finalImageData = Squish.compressImage(rgba, inputImage.getWidth(), inputImage.getHeight(), finalImageData, Squish.CompressionType.DXT1);
				break;
			case DXT3:
				finalImageData = Squish.compressImage(rgba, inputImage.getWidth(), inputImage.getHeight(), finalImageData, Squish.CompressionType.DXT3);
				break;
			case DXT5:
				finalImageData = Squish.compressImage(rgba, inputImage.getWidth(), inputImage.getHeight(), finalImageData, Squish.CompressionType.DXT5);
				break;
			case ARGB:
				finalImageData = rgba;
				break;
		}

		int width = FormatToUtil.getUnsignedByte((short)inputImage.getWidth());
		int height = FormatToUtil.getUnsignedByte((short)inputImage.getHeight());
		int pitch = 0;
		byte[] ARGBData = finalImageData;
			
		Mipmap mipmap = new Mipmap(width, height , pitch, ARGBData);
		
		return mipmap;
	}
}
