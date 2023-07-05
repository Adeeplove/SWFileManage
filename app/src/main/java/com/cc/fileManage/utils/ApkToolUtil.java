package com.cc.fileManage.utils;

import com.blankj.utilcode.util.PathUtils;

import java.io.File;

import brut.androlib.Androlib;
import brut.androlib.AndrolibException;
import brut.androlib.options.BuildOptions;
import brut.androlib.res.AndrolibResources;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResTable;
import brut.androlib.res.decoder.AXmlResourceParser;
import brut.androlib.res.decoder.AndroidManifestResourceParser;
import brut.androlib.res.decoder.ResAttrDecoder;
import brut.androlib.res.decoder.ResFileDecoder;
import brut.androlib.res.decoder.XmlPullStreamDecoder;
import brut.androlib.src.SmaliBuilder;
import brut.common.BrutException;
import brut.directory.ExtFile;
import brut.util.BrutIO;
import brut.util.Duo;

public class ApkToolUtil {

    /**
     * 反编译dex
     * @param dex       dex文件
     * @param outPath   写出目录
     * @param apiLevel  api等级
     * @throws AndrolibException    异常
     */
    public static void decodeDex(File dex, File outPath, int apiLevel) throws AndrolibException {
        new Androlib().decodeSourcesSmali(dex, outPath, dex.getName(), false, apiLevel);
    }

    /**
     * 回编译dex
     * @param parentName        父目录
     * @param fileName          文件夹名称
     * @param apiLevel          api等级
     * @throws BrutException    异常
     */
    public static void buildDex(String parentName, String fileName, int apiLevel) throws BrutException {
        File file = new File(parentName);
        ExtFile smaliDir = new ExtFile(file, fileName);
        if (!smaliDir.exists()) {
            return;
        }
        File dex = new File(file, "classes.dex");
        if (isModified(smaliDir, dex)) {
            dex.delete();
            SmaliBuilder.build(smaliDir, dex, apiLevel);
        }
    }

    public static void installFramework(String frameworkPath, String tag) throws BrutException {
        BuildOptions buildOptions = new BuildOptions();
        buildOptions.frameworkFolderLocation = PathUtils.getInternalAppFilesPath();
        buildOptions.frameworkTag = tag;
        (new Androlib(buildOptions)).installFramework(new File(frameworkPath));
    }

    private static boolean isModified(File working, File stored) {
        return !stored.exists() || BrutIO.recursiveModifiedTime(working) > BrutIO.recursiveModifiedTime(stored);
    }
}
