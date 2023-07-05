package com.cc.fileManage.module.dex;

import com.cc.fileManage.module.dex.data.DexFile;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class DexParser {

    private static final String TAG = DexParser.class.getSimpleName();

    private static final String OUTPUT_DIR = "parser_dex/build/";

    public DexFile parse(String dexFileName) {
//        RandomAccessFile racFile = null;
//        try {
//            racFile = FileUtil.loadAsRAF(dexFileName);
//            DexFile dexFile = new DexFile();
//            dexFile.parse(racFile);
//            return dexFile;
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return null;
    }

    public static void main(String[] args) {

        String fileName = "parser_dex/res/classes.dex";
        DexParser dexParser = new DexParser();
        DexFile dexFile = dexParser.parse(fileName);
        System.out.println(dexFile);

        System.out.println();
        try {
            String targetFile = OUTPUT_DIR + "raw_chunk.txt";
            FileOutputStream fos = new FileOutputStream(targetFile);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(dexFile.toString());
            writer.close();
            System.out.println("Raw chunk: " + targetFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
