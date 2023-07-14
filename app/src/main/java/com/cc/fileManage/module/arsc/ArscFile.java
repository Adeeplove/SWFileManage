package com.cc.fileManage.module.arsc;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.RandomInputStream;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 26/07/2017.
 *
 * @author bilux (i.bilux@gmail.com)
 *
 */
public class ArscFile {

    private static final String TAG = ArscFile.class.getSimpleName();

    private static final int RES_TABLE_TYPE = 0x0002;
    private static final int RES_STRING_POOL_TYPE = 0x0001;
    private static final int RES_TABLE_PACKAGE_TYPE = 0x0200;

    public ResFileHeaderChunk arscHeader;
    public ResStringPoolChunk resStringPoolChunk;
    public ResTablePackageChunk resTablePackageChunk;
    //
    private final RandomInputStream stream;

    public ArscFile(RandomInputStream stream) {
        this.stream = stream;
    }

    public void parse() throws IOException {
        long length = stream.length();

        ChunkHeader header = ChunkHeader.parseFrom(stream.getInputStream(0, ResFileHeaderChunk.LENGTH));
        ////
        if (header.type != RES_TABLE_TYPE) {
            return;
        }
        // Post load file header.
        stream.reset();
        ///
        RandomInputStream.CutStream bound = stream.getInputStream(0, header.headerSize);
        arscHeader = ResFileHeaderChunk.parseFrom(bound);
        bound.skipRemaining();
        //
        do {
            stream.markNow();
            header = ChunkHeader.parseFrom(stream.getInputStream(stream.getPointer(), ChunkHeader.LENGTH));
            stream.reset();
            ///
            switch (header.type) {
                case RES_STRING_POOL_TYPE:
                    long pos = stream.getPointer();
                    bound = stream.getInputStream(pos, header.chunkSize);
                    //
                    resStringPoolChunk = ResStringPoolChunk.parseFrom(bound);
                    bound.skipRemaining();
                    break;
                case RES_TABLE_PACKAGE_TYPE:
                    long p = stream.getPointer();
                    bound = stream.getInputStream(p, header.chunkSize);
                    ///
                    resTablePackageChunk = ResTablePackageChunk.parseFrom(bound, resStringPoolChunk);
                    bound.skipRemaining();
                    break;
                default:
            }
        } while (stream.getPointer() < length);
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(arscHeader) + '\n' +
                resStringPoolChunk + '\n' +
                resTablePackageChunk + '\n';
    }

    public String buildPublicXml() {
        return resTablePackageChunk.buildEntry2String();
    }

    public ResTableEntry getResource(int resId) {
        long pkgId = (resId & 0xff000000L) >> 24;
        //short packageId = (short) (resId >> 24 & 0xff);
        if (resTablePackageChunk.pkgId == pkgId) {
            return resTablePackageChunk.getResource(resId);
        } else {
            return null;
        }
    }

    public void close() {
        try {
            if(stream != null) stream.close();
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        // testing
        int resId = 0x7f030001;
        short packageId = (short) (resId >> 24 & 0xff);
        short typeId = (short) ((resId >> 16) & 0xff);
        int entryIndex = (int) (resId & 0xffff);
//                ResTableEntry res = arscFile.getResource(resId);
//                if (res != null) {
//                    System.out.println(res);
//                } else {
//                    System.out.println("Resource ID 0x" + String.format("%04x", resId) + " cannot be found.");
//                }
    }
}
