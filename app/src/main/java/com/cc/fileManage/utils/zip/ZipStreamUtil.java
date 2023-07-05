package com.cc.fileManage.utils.zip;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.documentfile.provider.DocumentFile;

import com.cc.fileManage.utils.CharUtil;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipInput;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ZipStreamUtil {

    //是否能打开此zip包
    public static boolean canOpen(Context context, Uri uri, long length){
        try (ZipInput zip = new ZipInput(context, uri, length)) {
            zip.close();
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * 获取压缩包内所有的条目
     * zipPath 压缩包路径
     */
    //原API
    public static List<String> getZipAllEntry(Context context, Uri uri, long length)
    {
        List<String> list = new ArrayList<>();
        try (ZipInput zip = new ZipInput(context, uri, length)){
            //迭代
            Enumeration<? extends ZipEntry> e = zip.getEntries();
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
    public static InputStream getZipEntryToByteArray(Context context, Uri uri, long length, String entry)
    {
        try (ZipInput zip = new ZipInput(context, uri, length)) {
            //实例化
            //
            ZipEntry ze = zip.getEntry(entry);
            if (ze != null) {
                return zip.getInputStream(ze);
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
    public static void unZip(Context context, DocumentFile documentFile, String outPath, boolean createParent){
        //
        try (ZipInput zipFile = new ZipInput(context, documentFile.getUri(), documentFile.length())){
            //检查
            outPath = outPath.endsWith(File.separator) ? outPath : outPath + File.separator;
            //是否创建父目录
            outPath = createParent ? outPath + documentFile.getName() : outPath;

            //遍历
            Enumeration<ZipEntry> emu = zipFile.getEntries();
            while(emu.hasMoreElements()){
                ZipEntry entry = emu.nextElement();
                //文件夹
                if (entry.isDirectory()){
                    //创建
                    File file = new File(outPath + entry.getName());
                    file.mkdirs();
                } else {
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
    public static void unZipFolder(Context context, DocumentFile documentFile,
                                   String outPath, String outFolderName, String zipEntry){
        //
        try (ZipInput zipFile = new ZipInput(context, documentFile.getUri(), documentFile.length())) {
            //实例化zip文件
            //待解压的文件夹路径
            zipEntry = zipEntry.endsWith(File.separator) ? zipEntry : zipEntry + File.separator;
            //校验解压父目录
            outFolderName = !TextUtils.isEmpty(outFolderName) && !outFolderName.endsWith(File.separator) ?
                    outFolderName + File.separator : outFolderName;
            //检查
            outPath = outPath.endsWith(File.separator) ? outPath : outPath + File.separator;

            //遍历zip所有条目
            Enumeration<? extends ZipEntry> e = zipFile.getEntries();
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
    public static boolean unZipFile(Context context, DocumentFile documentFile,
                                    String outPath ,String outFolderName, String zipEntry){
        //
        try (ZipInput zipFile = new ZipInput(context, documentFile.getUri(), documentFile.length())) {
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
     * @param uri 压缩包文件
     * @param boo false只获取文件夹 true获取文件夹和文件
     * @return Set<String>
     */
    public static Set<String> getZipFirstFolder(Context context, Uri uri, long length, boolean boo){
        Set<String> list = new HashSet<>();
        //==========================
        try (ZipInput zip = new ZipInput(context, uri, length)){
            //迭代
            Enumeration<? extends ZipEntry> e = zip.getEntries();
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
     * @param uri  压缩包文件
     * @return  byte[]
     */
    public static byte[] findTexIcon(Context context, Uri uri, long length){
        String modinfo = "";
        //存放
        Map<String,ZipEntry> map = new HashMap<>();
        try (ZipInput zip = new ZipInput(context, uri, length)) {
            //第一个tex
            ZipEntry ze = null;
            Enumeration<? extends ZipEntry> entries = zip.getEntries();
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
    public static Set<String> getAllMods(Context context, Uri uri, long length){
        //======
        Set<String> set = new HashSet<>();
        //
        try (ZipInput zip = new ZipInput(context, uri, length)){
            String oldName = "null";
            Enumeration<? extends ZipEntry> e = zip.getEntries();
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
    public static Map<String, String> getAllModsAndTexPath(Context context, Uri uri, long length){
        //===
        Map<String, String> map = new HashMap<>();
        //
        try (ZipInput zip = new ZipInput(context, uri, length)){
            //===
            Enumeration<? extends ZipEntry> e = zip.getEntries();
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
    public static String readModsSettings(Context context, Uri uri, long length, String zipEntry){
        try (ZipInput cz = new ZipInput(context, uri, length)){
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
