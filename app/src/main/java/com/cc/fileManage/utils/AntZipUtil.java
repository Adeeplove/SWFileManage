package com.cc.fileManage.utils;

import android.content.Context;

import com.cc.fileManage.entity.file.MFile;
import com.cc.fileManage.entity.file.OnFileDataListener;

import org.apache.tools.zip.Zip;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class AntZipUtil {

    /**
     * 打开压缩包
     * @param file  压缩包
     * @return      zip
     * @throws IOException  IO异常
     */
    public static Zip open(String file) throws IOException {
        File f = new File(file);
        if(f.exists() && f.isFile() && f.canRead()) {
            return new ZipFile(file);
        } else {
            throw new IOException("打开失败: "+file);
        }
    }

    /**
     * 获取压缩包指定条目的数据转字节数组
     * @param zip       压缩包
     * @param entry     条目
     * @return          字节数组
     * @throws Exception    异常
     */
    public static byte[] zipEntryToArray(Zip zip, String entry) throws Exception{
        try (zip) {
            //实例化
            ZipEntry ze = zip.getEntry(entry);
            if (ze != null) {
                return streamToByteArray(zip.getInputStream(ze));
            }
        }
        return null;
    }

    /**
     *
     * 解压zip文件
     * zipPath zip文件路径
     * outPath 解压存放路径
     */
    public static void unZip(Context context, Zip zip, String outPath) {
        try (zip){
            //检查
            String fullPath = outPath.endsWith(File.separator) ? outPath : outPath + File.separator;
            //遍历
            Enumeration<ZipEntry> emu = zip.getEntries();
            while(emu.hasMoreElements()) {
                ZipEntry entry = emu.nextElement();
                //创建
                MFile file = MFile.create(context, fullPath + entry.getName());
                //文件夹
                if (entry.isDirectory()){
                    if(!file.exists()) {
                        file.mkdirs();
                    }
                } else {
                    writeToFile(zip.getInputStream(entry), file);
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     *
     * zipPath 压缩包路径
     * outPath 解压存放路径
     * outFolderName 指定解压出来的文件夹父目录名 为null就不创建父目录
     * zipEntry 指定解压压缩包内的文件夹
     */
    public static void unZipDir(Context context, Zip zip, String outPath, String outEntry){
        try (zip) {
            // 检查
            String fullPath = outPath.endsWith(File.separator) ? outPath : outPath + File.separator;
            //遍历
            Enumeration<ZipEntry> emu = zip.getEntriesInPhysicalOrder();
            boolean child = true;
            while(emu.hasMoreElements()) {
                ZipEntry entry = emu.nextElement();
                String name = entry.getName();
                ///
                if(name.startsWith(outEntry)) {
                    child = false;
                    //解压路径
                    String dir = entry.getName().substring(outEntry.length());
                    //
                    MFile file = MFile.create(context, fullPath + dir);
                    if(entry.isDirectory()) {
                        if(!file.exists()) {
                            file.mkdirs();
                        }
                    } else {
                        if (!file.exists()) {
                            file.mkdirsF();
                        }
                        //写出
                        writeToFile(zip.getInputStream(entry), file);
                    }
                } else if(!child) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * zipPath 压缩包路径
     * outPath 解压存放路径
     * outFolderName 指定解压出来的文件夹父目录名 为null就不创建父目录
     * zipEntry 待解压的文件夹
     */
    public static void unZipFile(Context context, Zip zip, String outPath, String outEntry) {
        //
        try (zip) {
            //检查
            String fullPath = outPath.endsWith(File.separator) ? outPath : outPath + File.separator;
            ZipEntry entry = zip.getEntry(outEntry);
            if (entry != null) {
                MFile file = MFile.create(context, fullPath + getEntryName(entry.getName()));
                if (!file.exists()) {
                    file.mkdirsF();
                }
                writeToFile(zip.getInputStream(entry), file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 写出文件
     * @param file          写出的文件
     * @param inputStream   输入流
     */
    public static void writeToFile(InputStream inputStream, MFile file) throws Exception {
        try (BufferedInputStream bis = new BufferedInputStream(inputStream)){
            //文件写出
            byte [] buf = new byte[1024];
            try (OutputStream fos = file.openOutStream();
                 BufferedOutputStream bos = new BufferedOutputStream(fos, buf.length)){
                int len;
                while((len = bis.read(buf)) != -1){
                    bos.write(buf,0, len);
                }
                bos.flush();
            }
        }
    }

    /**
     * 输入流转byte
     * @param input     InputStream
     * @return          byte[]
     */
    public static byte[] streamToByteArray(InputStream input) throws OutOfMemoryError, IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            //将stream转换成byte
            byte[] buffer = new byte[128];
            int len;
            while ((len = input.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * 获取压缩包第一层目录
     * @param zip         压缩包
     * @param includeFile 是否包含文件
     * @return            Set<String> 压缩包第一层目录
     */
    public static Set<String> listMenu(Zip zip, boolean includeFile) {
        Set<String> hash = new HashSet<>();
        try (zip) {
            // 迭代
            Enumeration<? extends ZipEntry> e = zip.getEntries();
            while (e.hasMoreElements()) {
                // 每一个条目
                ZipEntry entry = e.nextElement();
                String entryName = entry.getName();
                // 包含/
                if(entryName.contains(File.separator)) {
                    //截取
                    String name = entryName.substring(0, entryName.indexOf(File.separator) + 1);
                    hash.add(name);
                } else if(includeFile){
                    hash.add(entryName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hash;
    }

    /**
     * 压缩整个文件夹中的所有文件，生成指定名称的zip压缩包
     * @param filePath 文件所在目录
     * @param zipPath  压缩后zip文件名称
     * @param dirFlag  zip文件中第一层是否包含一级目录,true包含|false没有
     */
    public static void compress(Context context, String filePath, String zipPath, boolean dirFlag) {
        try {
            MFile file = MFile.create(context, filePath);   // 要被压缩的文件夹
            ////
            MFile zip = MFile.create(context, zipPath);
            MFile parent = zip.getParentFile();
            if(parent != null && !parent.exists()){
                parent.mkdirs();
            }
            ////
            try (ZipOutputStream zipOut = new ZipOutputStream(zip.openOutStream())) {
                if(file.isDirectory()) {
                    ////
                    file.listFiles(context, new OnFileDataListener() {
                        @Override
                        public void onData(MFile child, String readPath) {
                            if(dirFlag) {
                                recursionZip(context, zipOut, child, file.getName() + File.separator);
                            }else{
                                recursionZip(context, zipOut, child, "");
                            }
                        }

                        @Override
                        public void onNoPermission() {}
                        @Override
                        public void onNoExist() {}
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 递归
    private static void recursionZip(Context context, ZipOutputStream zipOut, MFile file, String baseDir) {
        if(file.isDirectory()){
            file.listFiles(context, new OnFileDataListener() {
                @Override
                public void onData(MFile child, String readPath) {
                    recursionZip(context, zipOut, child, baseDir + file.getName() + File.separator);
                }
                @Override
                public void onNoPermission() {}
                @Override
                public void onNoExist() {}
            });
        } else {
            try (InputStream input = file.openInputStream()) {
                byte[] buf = new byte[1024];
                zipOut.putNextEntry(new ZipEntry(baseDir + file.getName()));
                int len;
                while((len = input.read(buf)) != -1){
                    zipOut.write(buf, 0, len);
                }
            } catch (Exception ignored) {}
        }
    }

    /**
     * 读取InputStream字符内容
     * @param inputStream 输入流
     * @return 文件内容
     */
    public static String readInputStream(InputStream inputStream) throws IOException{
        try (Scanner scanner = new Scanner(inputStream)) {
            StringBuilder content = new StringBuilder();
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine());
            }
            return content.toString();
        } catch(Exception e){
            throw new IOException(e);
        }
    }

    /**
     * 获取条目父目录名称
     * @param entry  条目
     * @return       条目父目录名称
     */
    public static String getEntryParentName(String entry){
        return getEntryName(getEntryParent(entry));
    }

    /**
     * 获取条目名称
     * @param entry 条目
     * @return      名称
     */
    public static String getEntryName(String entry) {
        if(entry.contains(File.separator)) {
            int end = entry.length() - 1;
            int start = entry.charAt(end) == File.separatorChar ? end - 1 : end;
            ///
            StringBuilder builder = new StringBuilder();
            ///
            for (; start >= 0; start--) {
                char c = entry.charAt(start);
                if(c == File.separatorChar) break;
                builder.insert(0, c);
            }
            return builder.toString();
        }
        return entry;
    }

    /**
     * 获取父目录
     * @param entry 条目
     * @return      父目录
     */
    public static String getEntryParent(String entry) {
        if(entry.contains(File.separator)) {
            int end = entry.length() - 1;
            int start = entry.charAt(end) == File.separatorChar ? end - 1 : end;
            ///
            for (; start >= 0; start--) {
                char c = entry.charAt(start);
                if(c == File.separatorChar) break;
            }
            if(start > 0)
                return entry.substring(0, start);
        }
        return entry;
    }
}
