package com.cc.fileManage.module.manifest;

import com.cc.fileManage.module.manifest.data.MfFile;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class ManifestParser {

    private static final String OUTPUT_DIR = "parser_manifest/build/";

    public static final void main(String[] args) {

        String fileName = "parser_manifest/res/compiled_manifest.xml";
        ManifestParser parser = new ManifestParser();
        MfFile mfFile = new MfFile();
        if (mfFile != null) {
            System.out.println(mfFile.toString());
        } else {
            System.out.println("Parse failed: " + fileName);
        }

        System.out.println("Results:");
        // Dump raw chunks to build directory
        try {
            String targetFile = OUTPUT_DIR + "raw_chunks.txt";
            FileOutputStream fos = new FileOutputStream(targetFile);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(mfFile.toString());
            writer.close();
            System.out.println("Raw chunk: " + targetFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Dump format xml to build director
        try {
            String targetFile = OUTPUT_DIR + "parsed_mf.xml";
            FileOutputStream fos = new FileOutputStream(targetFile);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(mfFile.toXmlString());
            writer.close();
            System.out.println("Xml file: " + targetFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public MfFile parse(String mfFileName) {
//        RandomAccessFile racFile = null;
//        try {
//            racFile = FileUtil.loadAsRAF(mfFileName);
//            MfFile mfFile = new MfFile();
//            mfFile.parse(racFile);
//            return mfFile;
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            FileUtil.closeQuietly(racFile);
//        }
//        return null;
//    }
}
