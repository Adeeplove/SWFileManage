package com.cc.fileManage.module.file;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.cc.fileManage.entity.TEXFile;
import com.cc.fileManage.utils.CommonUtil;
import com.cc.fileManage.utils.LSUtil;
import com.github.memo33.jsquish.Squish;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片转Tex文件类
 */
public class TEXCreator 
{
	// 格式
	private final TEXFile.PixelFormat pixelFormat;
	// 类型
	private final TEXFile.TextureType textureType;
	// 生成mipmap
	private boolean generateMipmaps;
 	// 预乘透明度
	private boolean preMultiplyAlpha;

	public void setGenerateMipmaps(boolean generateMipmaps)
	{
		this.generateMipmaps = generateMipmaps;
	}

	public void setPreMultiplyAlpha(boolean preMultiplyAlpha)
	{
		this.preMultiplyAlpha = preMultiplyAlpha;
	}

	public TEXCreator(TEXFile.PixelFormat format, TEXFile.TextureType type){
		this.pixelFormat = format;
		this.textureType = type;
	}

	/**
	 * PNG To Tex
	 * @param inputImage	图片
	 * @param outputStream	文件输出流
	 * @throws Exception	Exception
	 */
	public void convertPNGToTex(Bitmap inputImage, OutputStream outputStream) throws Exception
	{
		List<TEXFile.Mipmap> Mipmaps = new ArrayList<>();
		//翻转图片
		inputImage = CommonUtil.scaleImageView(inputImage);
		//add
		Mipmaps.add(generateMipmap(inputImage, this.pixelFormat, this.preMultiplyAlpha));
		//
		if (this.generateMipmaps) {
			int width = inputImage.getWidth();
			int height = inputImage.getHeight();

			while (Math.max(width, height) > 1)
			{
				width = Math.max(1, width >> 1);
				height = Math.max(1, height >> 1);
				//
				Mipmaps.add(generateMipmap(inputImage, this.pixelFormat, width, height, this.preMultiplyAlpha));
			}
		}
		inputImage.recycle();

		// 创建TEXFile文件
		TEXFile outputTEXFile = new TEXFile();
		//
		outputTEXFile.getHeader().platform = 0;
		outputTEXFile.getHeader().pixelFormat = this.pixelFormat.getValue();
		outputTEXFile.getHeader().textureType = this.textureType.getValue();
		outputTEXFile.getHeader().numMips = Mipmaps.size();
		outputTEXFile.getHeader().flags = 0;
		// 字节流
		try (ByteArrayOutputStream ms = new ByteArrayOutputStream()){
			for(TEXFile.Mipmap mip : Mipmaps) {
				//宽
				byte[] width = LSUtil.toLH((short)mip.width);
				ms.write(width);

				//高
				byte[] height = LSUtil.toLH((short)mip.height);
				ms.write(height);
				//点
				byte[] pitch = LSUtil.toLH((short)mip.pitch);
				ms.write(pitch);

				//data.length
				byte[] ARGBLength = LSUtil.toLH(mip.data.length);
				ms.write(ARGBLength);
			}
			// 写出字节数据
			try (DataOutputStream out = outputTEXFile.saveFileHeader(outputStream)){
				out.write(ms.toByteArray());
				out.flush();
				////
				for(TEXFile.Mipmap mip : Mipmaps){
					out.write(mip.data);
					out.flush();
				}
				Mipmaps.clear();
			}
		}
	}

	/**
	 * PNG To Tex
	 * @param inputImage	图片
	 * @return				byte[] tex文件字节数组
	 * @throws Exception	Exception
	 */
	public byte[] convertPNGToTex(Bitmap inputImage) throws Exception
    {
        List<TEXFile.Mipmap> Mipmaps = new ArrayList<>();
		// 上下反转图片
		inputImage = CommonUtil.scaleImageView(inputImage);
		//
		Mipmaps.add(generateMipmap(inputImage, this.pixelFormat, this.preMultiplyAlpha));
        //抗锯齿
		if (this.generateMipmaps) {
            int width = inputImage.getWidth();
            int height = inputImage.getHeight();

            while (Math.max(width, height) > 1)
            {
                width = Math.max(1, width >> 1);
                height = Math.max(1, height >> 1);
				///
                Mipmaps.add(generateMipmap(inputImage, this.pixelFormat, width, height, this.preMultiplyAlpha));
            }
		}
		inputImage.recycle();

		////
        TEXFile outputTEXFile = new TEXFile();
        outputTEXFile.getHeader().platform = 0;
        outputTEXFile.getHeader().pixelFormat = this.pixelFormat.getValue();
        outputTEXFile.getHeader().textureType = this.textureType.getValue();
        outputTEXFile.getHeader().numMips = Mipmaps.size();
        outputTEXFile.getHeader().flags = 0;
		//
		try (ByteArrayOutputStream ms = new ByteArrayOutputStream()){
			for(TEXFile.Mipmap mip : Mipmaps)
			{
				//宽
				byte[] width = LSUtil.toLH((short)mip.width);
				ms.write(width);
				//高
				byte[] height = LSUtil.toLH((short)mip.height);
				ms.write(height);
				//点
				byte[] pitch = LSUtil.toLH((short)mip.pitch);
				ms.write(pitch);
				//data.length
				byte[] data = LSUtil.toLH(mip.data.length);
				ms.write(data);
			}
			////
			for(TEXFile.Mipmap mip : Mipmaps){
				ms.write(mip.data);
			}
			Mipmaps.clear();
			///
			outputTEXFile.setRaw(ms.toByteArray());
			///
			return outputTEXFile.fileData();
		}
	}

	/**
	 * 生成mipmap
	 * @param inputImage		图片
	 * @param pixelFormat		压缩格式
	 * @param width				宽
	 * @param height			高
	 * @param preMultiplyAlpha	预乘透明度
	 * @return					TEXFile.Mipmap
	 */
	private TEXFile.Mipmap generateMipmap(Bitmap inputImage, TEXFile.PixelFormat pixelFormat, int width, int height, boolean preMultiplyAlpha)
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

		//源资源位置
		Rect srcRect = new Rect(0, 0, inputImage.getWidth(), inputImage.getHeight());
        //要绘制的位置
		Rect destRect = new Rect(0, 0, width, height);
        
		//画
		can.drawBitmap(inputImage, srcRect, destRect, paint);

		return generateMipmap(b, pixelFormat, preMultiplyAlpha);
	}

	/**
	 * 生成mipmap
	 * @param inputImage		图片
	 * @param pixelFormat		压缩格式
	 * @param preMultiplyAlpha	预乘透明度
	 * @return					TEXFile.Mipmap
	 */
	private TEXFile.Mipmap generateMipmap(Bitmap inputImage, TEXFile.PixelFormat pixelFormat, boolean preMultiplyAlpha)
	{
		byte[] rgba = new byte[inputImage.getWidth() * inputImage.getHeight() * 4];

		for (int y = 0; y < inputImage.getHeight(); y++)
			for (int x = 0; x < inputImage.getWidth(); x++)
			{
                //获取像素
				int color = inputImage.getPixel(x, y);
				// r g b a
				int r = Color.red(color);
				int g = Color.green(color);
				int b = Color.blue(color);
				int a = Color.alpha(color);

				if (preMultiplyAlpha) {
					float alphaMod = (float)a / 255.0f; // Normalize.

					byte newR = (byte)(r * alphaMod);
					byte newG = (byte)(g * alphaMod);
					byte newB = (byte)(b * alphaMod);

                    color = Color.argb(a, LSUtil.getUnsignedByte(newR), LSUtil.getUnsignedByte(newG), LSUtil.getUnsignedByte(newB));
                    // r g b a
                    r = Color.red(color);
                    g = Color.green(color);
                    b = Color.blue(color);
                    a = Color.alpha(color);
                }
                
				rgba[y * inputImage.getWidth() * 4 + x * 4] = (byte)r;
				rgba[y * inputImage.getWidth() * 4 + x * 4 + 1] = (byte)g;
				rgba[y * inputImage.getWidth() * 4 + x * 4 + 2] = (byte)b;
				rgba[y * inputImage.getWidth() * 4 + x * 4 + 3] = (byte)a;
			}

		byte[] finalImageData = null;

		switch (pixelFormat)
		{
			case DXT1:
				finalImageData = Squish.compressImage(rgba, inputImage.getWidth(), inputImage.getHeight(), null, Squish.CompressionType.DXT1);
				break;
			case DXT3:
				finalImageData = Squish.compressImage(rgba, inputImage.getWidth(), inputImage.getHeight(), null, Squish.CompressionType.DXT3);
				break;
			case DXT5:
				finalImageData = Squish.compressImage(rgba, inputImage.getWidth(), inputImage.getHeight(), null, Squish.CompressionType.DXT5);
				break;
			case ARGB:
				finalImageData = rgba;
				break;
		}
		///
		int width = LSUtil.getUnsignedByte((short)inputImage.getWidth());
		int height = LSUtil.getUnsignedByte((short)inputImage.getHeight());
		int pitch = 0;
		byte[] ARGBData = finalImageData;

		return new TEXFile.Mipmap(width, height, pitch, ARGBData);
	}
}
