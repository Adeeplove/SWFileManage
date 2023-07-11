package com.cc.fileManage.module.file;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.blankj.utilcode.util.ToastUtils;
import com.cc.fileManage.entity.file.MFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MergePng {

    private static class Png {

        private final int width;
        private final int height;
        private final String path;

        public Png(int width, int height, String path) {
            this.width = width;
            this.height = height;
            this.path = path;
        }
    }

    // 图片间距
    private static int spacing = 0;

    /**
     * 合并 png
     */
    public static void mergePng(List<MFile> manageFiles, int spacingValue){
        spacing = Math.max(spacing, spacingValue);
        spacing = Math.min(50, spacing);
        try {
            int countWidth = 0, countHeight = 0;
            List<Png> pngList = new ArrayList<>(); // 图片信息合集
            /////
            for (MFile mf : manageFiles) {
                BitmapFactory.Options options = getBitmapWH(mf);
                if(options.outWidth > 0 && options.outHeight > 0) {
                    countWidth += options.outWidth;
                    countHeight += options.outHeight;
                    //// add
                    Png png = new Png(options.outWidth, options.outHeight, mf.getPath());
                    pngList.add(png);
                }
            }
            // 平均分布图片数据
            if(pngList.size() > 1) {
                //// 平均宽高
                int averageWidth = countWidth / pngList.size(), averageHeight = countHeight / pngList.size();
                // 平均分布图片数据
                pngList = equalization(pngList);
                // 计算大图大致宽度
                int width = Math.max(countWidth(pngList, averageWidth, averageHeight), pngList.get(0).width);
                // 宽度取4 或 8的整数
                width = rounding(width);
                // 根据宽度排列图片 获取最终需要的宽高
                int[] data = calculate(pngList, width);
                // 根据宽高开始绘图
                canvasPng(data[0], data[1], pngList);
            } else {
                // 根据宽高开始绘图
                canvasPng(rounding(pngList.get(0).width), rounding(pngList.get(0).height), pngList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算实际图片排列需要的宽高
     * @param pngList       图片集合
     * @param widthTmp      宽度
     * @return              图片宽高
     */
    private static int[] calculate(List<Png> pngList, int widthTmp) {
        // 图片宽度
        int width = widthTmp;
        // 每一行现在的宽度, 每一行最高的高度, 总高度, 间距
        int widthLine = 0, heightLine = 0, totalHeight = 0;
        // 遍历
        for (int i = 0; i < pngList.size();) {
            Png png = pngList.get(i);
            ///
            int length = widthLine + png.width;
            // 总宽度大于默认的宽度
            if(length > width) {
                // 如果是第一行且差值小于16 扩展一下默认宽度
                if(totalHeight == 0 && (length - width) <= 16) {
                    width = width + (length - width);
                    width = rounding(width);
                } else {    // 换行处理
                    // 总高度++
                    totalHeight = totalHeight + heightLine + spacing;
                    // 行宽度重置 行最高值重置
                    widthLine = 0; heightLine = 0;
                    // 跳过本次循环
                    continue;
                }
            }
            // 记录最高的图片高度
            heightLine = Math.max(heightLine, png.height);
            // 记录当前行的宽度
            widthLine = widthLine + spacing + png.width;
            // 当前行图片个数 下一张图片
            i++;
        }
        // 加上最后一行高度
        totalHeight += heightLine;
        return new int[]{width, rounding(totalHeight)};
    }

    /**
     * 合并图片
     * @param width     图片宽
     * @param height    图片高
     * @param pngList   图片集合
     */
    private static void canvasPng(int width, int height, List<Png> pngList) {
        // 创建目标图片
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //画布
        Canvas can = new Canvas(bitmap);
        //画笔
        Paint paint = new Paint();
        //抗锯齿
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        //防抖动
        paint.setDither(true);
        //第一张图片文件
        File file = new File(pngList.get(0).path);
        // 写配置文件头
        StringBuilder stringBuilder = writeXmlHead("SW_" + replaceSuffix(file.getName(), "tex"));
        //
        // 每一行现在的宽度, 每一行最高的高度, 总高度
        int widthLine = 0, heightLine = 0, totalHeight = 0;
        ////
        for (int i = 0; i < pngList.size();) {
            Png pngFile = pngList.get(i);
            ////
            int length = widthLine + pngFile.width;
            // 总宽度大于默认的宽度
            if(length > width) {
                // 总高度++
                totalHeight = totalHeight + heightLine + spacing;
                // 行宽度重置 行最高值重置
                widthLine = 0; heightLine = 0;
                // 跳过本次循环
                continue;
            }
            ///
            Bitmap png = BitmapFactory.decodeFile(pngFile.path);
            //位置 要绘制的bitmap区域
            Rect srcRect = new Rect(0, 0, png.getWidth(), png.getHeight());
            //要绘制的位置
            Rect destRect = new Rect(widthLine, totalHeight, png.getWidth() + widthLine, png.getHeight() + totalHeight);
            //画
            can.drawBitmap(png, srcRect, destRect, paint);
            //
            png.recycle();
            // u1 u2  0 -> 1
            String u1 = toPix(destRect.left, width), u2 = toPix(destRect.right, width);
            // v1 v2  1 -> 0
            String v1 = toPix(height - destRect.bottom, height), v2 = toPix(height - destRect.top, height);
            // 写配置内容
            writeXmlValue(pngFile.path, stringBuilder, u1, u2, v1, v2);
            //
            // 记录最高的图片高度
            heightLine = Math.max(heightLine, pngFile.height);
            // 记录当前行的宽度
            widthLine = widthLine + spacing + pngFile.width;
            // 下一张图片
            i++;
        }
        // 保存图片
        String saveFile = file.getParent() + File.separator + "SW_"+ replaceSuffix(file.getName(), "png");
        saveBitmap2file(bitmap, saveFile);
        // 写配置文件
        stringBuilder.append("\t").append("</Elements>").append("\n");
        stringBuilder.append("</Atlas>");
        /// 写出配置文件
        writeXml(file.getParent() + File.separator + "SW_"+ replaceSuffix(file.getName(), "xml"), stringBuilder);
        //
        stringBuilder.delete(0, stringBuilder.length() - 1);
    }

    // 配置文件头
    private static StringBuilder writeXmlHead(String name) {
        ///
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<Atlas>").append("\n");
        stringBuilder.append("\t").append("<Texture filename=")
                .append("\"").append(name).append("\"").append("/>").append("\n");
        stringBuilder.append("\t").append("<Elements>").append("\n");
        return stringBuilder;
    }

    // 配置文件内容
    private static void writeXmlValue(String filePath,  StringBuilder stringBuilder,
                                      String u1, String u2, String v1, String v2) {
        String name = filePath.substring(filePath.lastIndexOf(File.separator) + File.separator.length());
        stringBuilder.append("\t\t").append("<Element name=").append("\"")
                .append(replaceSuffix(name, "tex")).append("\"")
                .append(" u1=").append("\"").append(u1).append("\"")
                .append(" u2=").append("\"").append(u2).append("\"")
                .append(" v1=").append("\"").append(v1).append("\"")
                .append(" v2=").append("\"").append(v2).append("\"").append("/>").append("\n");
    }

    // 写出配置文件
    @SuppressWarnings("all")
    private static void writeXml(String path, StringBuilder stringBuilder) {
        File file = new File(path);
        try {
            if(file.exists()) {
                file.createNewFile();
            }
            /////
            try (FileWriter fileWriter = new FileWriter(file)){
                fileWriter.write(stringBuilder.toString());
                fileWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 转换为坐标
     * @param num       图片长度
     * @param length    最终图片长度
     * @return          百分比
     */
    private static String toPix(int num, int length) {
        try {
            if(num > 0 && length > 0) {
                double pix = (double) num / length;
                ///
                DecimalFormat df = new DecimalFormat("#.000000");
                String format = df.format(new BigDecimal(pix));
                if(format.charAt(0) == '.')
                    format = "0" + format;
                return format;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0.000000";
    }

    // 取4 或 8的整数
    private static int rounding(int num) {
        for(;;) {
            String value = String.valueOf(num);
            if(value.charAt(value.length() - 1) == '4' || value.charAt(value.length() - 1) == '8') {
                return num;
            } else {
                num++;
            }
        }
    }

    // 计算合成图片的宽
    private static int countWidth(List<Png> list, int averageWidth, int averageHeight) {
        // 大图宽, 最小的差值
        int width = 0, min = Integer.MAX_VALUE;
        // 循环排列 找出宽高差值最小的区间
        for (int i = 1; i <= list.size(); i++) {
            // 行数
            int heightLine = (int) Math.ceil((float)list.size() / i);
            // 当前排列下图片大致宽度
            int tmpWidth = averageWidth * i;
            // 当前排列下图片大致高度
            int tmpHeight = averageHeight * heightLine;
            // 差值
            int difference;
            if(tmpWidth > tmpHeight) {
                difference = tmpWidth - tmpHeight;
            } else if(tmpHeight > tmpWidth){
                difference = tmpHeight - tmpWidth;
            } else {
                width = averageWidth * i;
                width += (i - 1) * spacing;
                break;
            }
            // 差值比之前的小 重新设置数据
            if(difference < min) {
                min = difference;
                width = tmpWidth + (i - 1) * spacing;
            }
        }
        return width;
    }

    /**
     * 平均分布集合数据
     * @param pngList 集合
     * @return        集合
     */
    private static List<Png> equalization(List<Png> pngList) {
        // 排序
        Collections.sort(pngList, (o1, o2) -> o2.width - o1.width);
        ///
        int length = pngList.size() / 2;
        ///
        List<Png> data = new ArrayList<>(pngList.size());
        for (int i = 0; i < length; i++) {
            ///
            Png tmpTop = pngList.get(i);
            Png tmpMin = pngList.get(pngList.size() - i - 1);
            //
		    data.add(tmpTop);
            data.add(tmpMin);
        }
        ///
        if(pngList.size() % 2 != 0) {
            data.add(pngList.get(length));
        }
        return data;
    }

    /**
     * 替换后缀名
     * @param name  名称
     * @return      替换后缀名的名称
     */
    public static String replaceSuffix(String name, String suffix) {
        // 如果后缀跟需要替换的相同
        if(name.endsWith(suffix)) {
            return name;
        } else {
            //
            if(name.lastIndexOf(".") != -1){
                name = name.substring(0, name.lastIndexOf("."));
            }
            return name + "." +suffix;
        }
    }

    /**
     * 获取图片信息 不加载至内存
     * @param f 图片文件
     * @return  图片信息
     */
    private static BitmapFactory.Options getBitmapWH(MFile f) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        try {
            //设置为true,代表加载器不加载图片,而是把图片的宽高读出来
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f.getPath(), opts);
        } catch (Exception e){
            e.printStackTrace();
        }
        return opts;
    }

    @SuppressWarnings("all")
    private static void saveBitmap2file(Bitmap bmp, String savePath) {
        File filePic = new File(savePath);
        try {
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            ToastUtils.showShort("保存失败！");
            e.printStackTrace();
        }
    }
}
