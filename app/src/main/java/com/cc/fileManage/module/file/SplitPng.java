package com.cc.fileManage.module.file;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;

import com.blankj.utilcode.util.ToastUtils;
import com.cc.fileManage.entity.TEXFile;
import com.cc.fileManage.entity.file.ManageFile;
import com.cc.fileManage.utils.CommonUtil;
import com.cc.fileManage.utils.TexFileUtil;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class SplitPng {

    private static class Xml {

        private String u1, u2, v1, v2;
        private String name;

        public boolean isEmpty() {
            return TextUtils.isEmpty(name) || TextUtils.isEmpty(u1) || TextUtils.isEmpty(u2)
                    || TextUtils.isEmpty(v1) || TextUtils.isEmpty(v2);
        }

        public void setValue(String name, String value) {
            switch (name) {
                case "name":
                    this.name = value;
                    break;
                case "u1":
                    this.u1 = value;
                    break;
                case "u2":
                    this.u2 = value;
                    break;
                case "v1":
                    this.v1 = value;
                    break;
                case "v2":
                    this.v2 = value;
                    break;
            }
        }
    }

    public static void splitPng(List<ManageFile> data) {
        ManageFile xml = null;
        ManageFile png = null;
        ///
        for (ManageFile m : data) {
            if(m.getName().endsWith(".png") || m.getName().endsWith(".tex"))
                png = m;
            else if(m.getName().endsWith(".xml"))
                xml = m;
        }
        if(xml == null || png == null) {
            ToastUtils.showShort("必须一张图片及对应XML配置文件");
            return;
        }
        /// 读配置文件
        List<Xml> xmlList = loadXml(xml);
        if(xmlList.size() > 0) {
            split(xmlList, png);
        } else {
            ToastUtils.showShort("读取XML配置文件出错!");
        }
    }

    // 拆
    private static void split(List<Xml> data, ManageFile manageFile) {
        //画笔
        Paint paint = new Paint();
        //抗锯齿
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        //防抖动
        paint.setDither(true);
        /// 拼接的大图
        Bitmap png = loadImage(manageFile);
        if(png != null) {
            // 写出路径
            String outPath = new File(manageFile.getPath()).getParent() + File.separator;
            //
            for (Xml xml : data) {
                // left right 0 -> 1
                int left = (int) Math.round(Double.parseDouble(xml.u1) * png.getWidth());
                int right = (int) Math.round(Double.parseDouble(xml.u2) * png.getWidth());
                // top bottom 1 -> 0
                int top = (int) Math.round(Double.parseDouble(xml.v2) * png.getHeight());
                int bottom = (int) Math.round(Double.parseDouble(xml.v1) * png.getHeight());
                ///
                if(right > 0 && top > 0) {
                    // 创建目标图片
                    Bitmap bitmap = Bitmap.createBitmap(right - left, top - bottom, Bitmap.Config.ARGB_8888);
                    //画布
                    Canvas can = new Canvas(bitmap);
                    //源bitmap区域
                    Rect srcRect = new Rect(left, png.getHeight() - top, right, png.getHeight() - bottom);
                    //要绘制的位置
                    Rect destRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    //画
                    can.drawBitmap(png, srcRect, destRect, paint);
                    // 保存路径
                    String saveFile = outPath + MergePng.replaceSuffix(xml.name, "png");
                    // 写出
                    saveBitmap2file(bitmap, saveFile);
                    //
                    bitmap.recycle();
                }
            }
            png.recycle();
        }
    }

    /**
     * 读取bitmap图片
     * @param manageFile    png或tex文件
     * @return              bitmap
     */
    private static Bitmap loadImage(ManageFile manageFile) {
        try {
            if(manageFile.getName().toLowerCase().endsWith(".png")) {
                return BitmapFactory.decodeFile(manageFile.getPath());
            } else {
                //实例化
                File file = new File(manageFile.getPath());
                TEXFile texFile = TexFileUtil.openTexFile(file);
                if(texFile == null) return null;
                //
                Bitmap bitmap = TexFileUtil.loadTexBitmap(texFile);
                if(bitmap != null)
                    return CommonUtil.scaleImageView(bitmap);   //反转y轴
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    /**
     * 读取xml配置文件
     * @param manageFile    xml配置文件
     * @return              xml数据
     */
    public static List<Xml> loadXml(ManageFile manageFile) {
        List<Xml> xmlList = new ArrayList<>();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(new File(manageFile.getPath()));
            Node elementsNode = document.getElementsByTagName("Elements").item(0);
            NodeList nodes = elementsNode.getChildNodes();
            //////////////////////////
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if(node.getNodeName().equals("Element")) {
                    NamedNodeMap namedNodeMap = node.getAttributes();
                    if(namedNodeMap != null && namedNodeMap.getLength() > 0) {
                        Xml xml = loadAttributes(namedNodeMap);
                        if(!xml.isEmpty())
                            xmlList.add(xml);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xmlList;
    }

    /**
     * 读取xml节点属性数据
     * @param namedNodeMap  xml节点
     * @return              xml属性数据
     */
    public static Xml loadAttributes(NamedNodeMap namedNodeMap) {
        Xml xml = new Xml();
        for (int i = 0; i < namedNodeMap.getLength(); i++) {
            Node node = namedNodeMap.item(i);
            xml.setValue(node.getNodeName(), node.getNodeValue());
        }
        return xml;
    }
}
