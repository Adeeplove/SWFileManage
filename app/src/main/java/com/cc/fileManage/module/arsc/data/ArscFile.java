package com.cc.fileManage.module.arsc.data;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.BoundedInputStream;
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

        ChunkHeader header = ChunkHeader.parseFrom(getInputStream(0, ResFileHeaderChunk.LENGTH));
        ////
        if (header.type != RES_TABLE_TYPE) {
            return;
        }
        // Post load file header.
        stream.reset();
        ///
        BoundedInputStream bound = getInputStream(0, header.headerSize);
        arscHeader = ResFileHeaderChunk.parseFrom(bound);
        bound.skipRemaining();
        //
        do {
            stream.markNow();
            header = ChunkHeader.parseFrom(getInputStream(stream.getPointer(), ChunkHeader.LENGTH));
            stream.reset();
            ///
            switch (header.type) {
                case RES_STRING_POOL_TYPE:
                    long pos = stream.getPointer();
                    bound = getInputStream(pos, header.chunkSize);
                    //
                    resStringPoolChunk = ResStringPoolChunk.parseFrom(bound);
                    bound.skipRemaining();
                    break;
                case RES_TABLE_PACKAGE_TYPE:
                    long p = stream.getPointer();
                    bound = getInputStream(p, header.chunkSize);
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

    private BoundedInputStream getInputStream(long start, long remaining) {
        return new BoundedInputStream(stream, start, remaining);
    }

    public void close() {
        try {
            if(stream != null) stream.close();
        } catch (Exception ignored) {}
    }
}
