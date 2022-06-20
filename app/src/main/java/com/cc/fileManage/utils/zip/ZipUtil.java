package com.cc.fileManage.utils.zip;

import android.text.TextUtils;

import com.cc.fileManage.utils.CharUtil;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtil {

    //是否能打开此zip包
    public static boolean canOpen(String path){
        File f = new File(path);
        if(!f.exists() || f.isDirectory()){
            return false;
        }
        //=====
        try (ZipFile zip = new ZipFile(f)){
            zip.close();
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 获取压缩包内所有的条目
     * zipPath 压缩包路径
     */
    //原API
    public static List<String> getZipAllEntry(String zipPath)
    {
        List<String> list = new ArrayList<>();;
        //文件不存在
        File file = new File(zipPath);
        if(!file.exists()){
            return list;
        }
        //
        try (ZipFile zip = new ZipFile(zipPath)){
            //迭代
            Enumeration<? extends ZipEntry> e = zip.entries();
            while (e.hasMoreElements()) {
                ZipEntry zipEntry = e.nextElement();
                list.add(zipEntry.getName());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取压缩包内指定的条目
     * zipPath 压缩包路径
     */
    public static byte[] getZipEntryToByteArray(String zipPath, String entry)
    {
        try (ZipFile zip = new ZipFile(zipPath)) {
            //实例化
            //
            ZipEntry ze = zip.getEntry(entry);
            if (ze != null) {
                return GeneralZipUtil.inputStreamToByteArray(zip.getInputStream(ze));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * 解压zip文件
     * zipPath zip文件路径
     * outPath 解压存放路径
     */
    public static void unZip(String zipPath, String outPath, boolean createParent){
        //文件
        File fileN = new File(zipPath);
        //
        try (ZipFile zipFile = new ZipFile(zipPath)){
            //检查
            outPath = outPath.endsWith(File.separator) ? outPath : outPath + File.separator;
            //是否创建父目录
            outPath = createParent ? outPath + fileN.getName() : outPath;

            //遍历
            Enumeration<? extends ZipEntry> emu = zipFile.entries();
            while(emu.hasMoreElements()){
                ZipEntry entry = emu.nextElement();
                //文件夹
                if (entry.isDirectory()){
                    //创建
                    File file = new File(outPath + entry.getName());
                    file.mkdirs();
                }else {
                    //写出文件
                    GeneralZipUtil.writeToFile(outPath + entry.getName(), zipFile.getInputStream(entry));
                }
            }
        }catch(Exception e){
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
    public static void unZipFolder(String zipPath, String outPath ,String outFolderName, String zipEntry){
        //
        try (ZipFile zipFile = new ZipFile(zipPath)) {
            //实例化zip文件
            //待解压的文件夹路径
            zipEntry = zipEntry.endsWith(File.separator) ? zipEntry : zipEntry + File.separator;
            //校验解压父目录
            outFolderName = !TextUtils.isEmpty(outFolderName) && !outFolderName.endsWith(File.separator) ?
                    outFolderName + File.separator : outFolderName;
            //检查
            outPath = outPath.endsWith(File.separator) ? outPath : outPath + File.separator;

            //遍历zip所有条目
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                //条目
                ZipEntry entry = e.nextElement();
                //条目名
                String entryName = entry.getName();
                //直接跳过
                if (entryName.equals(zipEntry)) continue;
                //这个条目包含待解压的文件夹
                if (entryName.contains(zipEntry) && !entryName.endsWith("/")) {
                    //解压路径
                    String entryOutPath = entryName.substring(entryName.indexOf(zipEntry) + zipEntry.length());
                    //outFolderName不为空或"" 添加自定义解压父目录
                    if (!TextUtils.isEmpty(outFolderName)) {
                        entryOutPath = outFolderName + entryOutPath;
                    }
                    //写出
                    GeneralZipUtil.writeToFile(outPath + entryOutPath, zipFile.getInputStream(entry));
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
    public static boolean unZipFile(String zipPath, String outPath ,String outFolderName, String zipEntry){
        //
        try (ZipFile zipFile = new ZipFile(zipPath)) {
            //校验解压父目录
            outFolderName = !TextUtils.isEmpty(outFolderName) && !outFolderName.endsWith(File.separator) ?
                    outFolderName + File.separator : outFolderName;
            //检查
            outPath = outPath.endsWith(File.separator) ? outPath : outPath + File.separator;

            ZipEntry entry = zipFile.getEntry(zipEntry);
            if (entry == null) {
                zipFile.close();
                return false;
            }
            //解压路径
            String entryOutPath = TextUtils.isEmpty(outFolderName) ?
                    zipEntry : outFolderName + GeneralZipUtil.getZipEntryName(entry.getName());
            //写出
            GeneralZipUtil.writeToFile(outPath + entryOutPath, zipFile.getInputStream(entry));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取压缩包第一层目录
     * @param zipPath 压缩包路径
     * @param boo false只获取文件夹 true获取文件夹和文件
     * @return
     */
    public static Set<String> getZipFirstFolder(String zipPath, boolean boo){
        Set<String> list = new HashSet<>();
        //文件不存在
        File file = new File(zipPath);
        if(!file.exists()){
            return list;
        }
        //==========================
        try (ZipFile zip = new ZipFile(zipPath)){
            //迭代
            Enumeration<? extends ZipEntry> e = zip.entries();
            while (e.hasMoreElements()) {
                //每一个条目
                ZipEntry entry = e.nextElement();
                String entryName = entry.getName();
                //包含/
                if(entryName.contains("/")){
                    //截取
                    String name = entryName.substring(0, entryName.indexOf("/") + 1);
                    list.add(name);
                }
                else{
                    //添加
                    if(boo) list.add(entryName);
                }
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    /**
     * 查找模组图标
     * @param filePath  压缩包路径
     * @return
     */
    public static byte[] findTexIcon(String filePath){
        String modinfo = "";
        //存放
        Map<String,ZipEntry> map = new HashMap<>();
        try (ZipFile zip = new ZipFile(filePath)) {
            //第一个tex
            ZipEntry ze = null;
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                if (!e.isDirectory()) {

                    if (e.getName().endsWith("modinfo.lua")) {
                        //父目录名称
                        modinfo = GeneralZipUtil.getZipEntryParent(e.getName());
                    } else if (e.getName().toLowerCase().endsWith(".tex")) {
                        //保存
                        ze = ze == null ? e : ze;
                        //父目录
                        String parent = GeneralZipUtil.getZipEntryParent(e.getName());
                        map.put(parent, e);
                        //如果跟modinfo父目录一样 直接跳出循环
                        if (parent.equals(modinfo)) {
                            break;
                        }
                    }
                }
            }
            //
            //
            if (map.get(modinfo) != null) {
                InputStream inputStream = zip.getInputStream(map.get(modinfo));
                if(inputStream != null)
                    return GeneralZipUtil.inputStreamToByteArray(inputStream);
            } else if (ze != null) {
                InputStream inputStream = zip.getInputStream(ze);
                if(inputStream != null)
                    return GeneralZipUtil.inputStreamToByteArray(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取压缩包内所有的模组
    public static Set<String> getAllMods(String zipPath){
        //======
        Set<String> set = new HashSet<>();
        if(zipPath == null || !new File(zipPath).exists()){
            return set;
        }
        try (ZipFile zip = new ZipFile(zipPath)){
            String oldName = "null";
            Enumeration<? extends ZipEntry> e = zip.entries();
            while (e.hasMoreElements()) {
                String zipEntryName = e.nextElement().getName();
                //
                if(zipEntryName.startsWith(oldName)){
                    continue;
                }
                if(zipEntryName.startsWith("mods/")){
                    if(CharUtil.charNum(zipEntryName,"/") == 2){
                        //==
                        oldName = zipEntryName;
                        String name = zipEntryName
                                .substring(zipEntryName.indexOf("/")+1, zipEntryName.lastIndexOf("/"));
                        set.add(name);
                    }
                }
            }
        }catch(Exception ignored){
        }
        return set;
    }

    //获取压缩包内所有的模组跟模组图片
    public static Map<String, String> getAllModsAndTexPath(String zipPath){
        //===
        Map<String, String> map = new HashMap<>();
        if(zipPath == null || !new File(zipPath).exists()){
            return map;
        }
        try (ZipFile zip = new ZipFile(zipPath)){
            //===
            Enumeration<? extends ZipEntry> e = zip.entries();
            while (e.hasMoreElements()) {
                String zipEntryName = e.nextElement().getName();
                if(zipEntryName.startsWith("mods/")){
                    //==
                    if(CharUtil.charNum(zipEntryName, "/") == 2) {
                        String name = zipEntryName.
                                substring(zipEntryName.indexOf("/")+1, zipEntryName.lastIndexOf("/"));
                        if(map.get(name) != null){
                            continue;
                        }else{
                            map.put(name, null);
                        }
                        //==================
                        if(zipEntryName.endsWith(".tex")){
                            map.put(name, zipEntryName);
                        }
                    }
                }
            }
        }catch(Exception ignored){
        }
        return map;
    }

    /**
     *     读取饥荒模组配置文件
     *     mods/modsettings.lua
     *     assets/mods/modsettings.lua
     */
    public static String readModsSettings(String zipPath, String zipEntry){
        try (ZipFile cz = new ZipFile(zipPath)){
            //=====================
            ZipEntry ze = cz.getEntry(zipEntry);
            if(ze != null){
                return GeneralZipUtil.readFileByInputStream(cz.getInputStream(ze));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
