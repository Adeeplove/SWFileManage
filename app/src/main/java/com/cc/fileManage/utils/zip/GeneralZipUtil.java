package com.cc.fileManage.utils.zip;

import com.cc.fileManage.utils.CharUtil;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Scanner;

/**
 * 通用的zip文件操作
 */
public class GeneralZipUtil {

     /**
      * 压缩整个文件夹中的所有文件，生成指定名称的zip压缩包
      * @param filePath 文件所在目录
      * @param zipPath 压缩后zip文件名称
      * @param dirFlag zip文件中第一层是否包含一级目录，true包含；false没有
      * 2015年6月9日
      */
     public static void zipMultiFile(String filePath ,String zipPath, boolean dirFlag) {
          try {
               File file = new File(filePath);// 要被压缩的文件夹
               File zipFile = new File(zipPath);
               ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
               if(file.isDirectory()){
                    File[] files = file.listFiles();
                    if(files != null){
                         //===============迭代压缩
                         for(File fileSec : files){
                              if(dirFlag){
                                   recursionZip(zipOut, fileSec, file.getName() + File.separator);
                              }else{
                                   recursionZip(zipOut, fileSec, "");
                              }
                         }
                    }
               }
               zipOut.close();
          } catch (Exception e) {
               e.printStackTrace();
          }
     }
     private static void recursionZip(ZipOutputStream zipOut, File file, String baseDir) throws Exception{
          if(file.isDirectory()){
               File[] files = file.listFiles();
               if(files != null){
                    for(File fileSec : files){
                         recursionZip(zipOut, fileSec, baseDir + file.getName() + File.separator);
                    }
               }
          } else{
               byte[] buf = new byte[1024];
               InputStream input = new FileInputStream(file);
               zipOut.putNextEntry(new ZipEntry(baseDir + file.getName()));
               int len;
               while((len = input.read(buf)) != -1){
                    zipOut.write(buf, 0, len);
               }
               input.close();
          }
     }

     /**
      * 写出文件
      * @param outPath       写出的路径
      * @param inputStream   输入流
      */
     public static void writeToFile(String outPath, InputStream inputStream){
          try (BufferedInputStream bis = new BufferedInputStream(inputStream)){
               File file = new File(outPath);
               //=
               File parent = file.getParentFile();
               if(parent != null && !parent.exists()){
                    parent.mkdirs();
               }
               //文件写出
               byte [] buf = new byte[1024];
               try (FileOutputStream fos = new FileOutputStream(file);
                       BufferedOutputStream bos = new BufferedOutputStream(fos, buf.length)){
                    int len;
                    while((len = bis.read(buf,0, buf.length)) != -1){
                         bos.write(buf,0, len);
                    }
                    bos.flush();
               }
          } catch (Exception e){
               e.printStackTrace();
          }
     }

     /**
      * 输入流转byte
      * @param input     InputStream
      * @return          byte[]
      */
     public static byte[] inputStreamToByteArray(InputStream input){
          try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
               //将InputStream转换成byte
               byte[] buffer = new byte[1024];
               int len;
               while ((len = input.read(buffer)) > -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
               }
               byteArrayOutputStream.flush();
               return byteArrayOutputStream.toByteArray();
          } catch (Exception e) {
               e.printStackTrace();
          }
         return null;
     }

     /**
      * @param inputStream 输入流
      * @return 文件内容
      */
     public static String readFileByInputStream(InputStream inputStream) {
          StringBuilder content = new StringBuilder();
          try{
               if(inputStream != null){
                    Scanner scanner = new Scanner(inputStream);
                    while (scanner.hasNextLine()) {
                         content.append(scanner.nextLine());
                    }
                    scanner.close();
               }
          }catch(Exception e){
               e.printStackTrace();
          }
          return content.toString();
     }

     /**
      * 获取zip条目名称
      * @param name zip条目全路径
      * @return     zip条目名称
      */
     public static String getZipEntryName(String name){
          if(name.charAt(name.length() - 1) == '/'){
               if(CharUtil.charNum(name,"/") == 1){
                    name = name.substring(0, name.lastIndexOf("/"));
               }else if(CharUtil.charNum(name,"/") > 1){
                    name = name.substring(0,name.lastIndexOf("/"));
                    name = name.substring(name.lastIndexOf("/") + 1);
               }
          } else{
               if(name.contains("/")){
                    name = name.substring(name.lastIndexOf("/") + 1);
               }
          }
          return name;
     }

     /**
      * 获取zip父条目
      * @param name zip条目全路径
      * @return     zip父条目全路径
      */
     public static String getZipEntryParent(String name){
          //文件夹
          if(name.charAt(name.length() - 1) == '/'){
               if(CharUtil.charNum(name,"/") == 1){
                    name = "";
               }else if(CharUtil.charNum(name,"/") > 1){
                    // xxx/aaa/cc/
                    name = name.substring(0,name.lastIndexOf("/"));
                    // xxx/aaa/
                    name = name.substring(0,name.lastIndexOf("/") + 1);
               }
          }else{
               if(name.contains("/")){
                    name = name.substring(0,name.lastIndexOf("/") + 1);
               }else{
                    name = "";
               }
          }
          return name;
     }

     //获取zip entry 父目录名称
     public static String getZipEntryParentName(String name){
          return getZipEntryName(getZipEntryParent(name));
     }
}
