package com.cc.fileManage.module.arsc.data;

import com.cc.fileManage.module.stream.BoundedInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by xueqiulxq on 26/07/2017.
 *
 * @author bilux (i.bilux@gmail.com)
 *
 */
public class ResTablePackageChunk {

    public static final String TAG = ResTablePackageChunk.class.getSimpleName();

    public static final int RES_TABLE_TYPE_SPEC_TYPE = 0x0202;
    public static final int RES_TABLE_TYPE_TYPE = 0x0201;

    // Header Block 0x0120
    public ChunkHeader header;
    public long pkgId;                 // 0x0000007f->UserResources  0x00000001->SystemResources
    public String packageName;
    public long typeStringOffset;   // Offset in this chunk
    public long lastPublicType;     // Num of type string
    public long keyStringOffset;    // Offset in chunk
    public long lastPublicKey;      // Num of key string

    // DataBlock
    public ResStringPoolChunk typeStringPool;
    public ResStringPoolChunk keyStringPool;
    public List<BaseTypeChunk> typeChunks;

    // Create Index
    public Map<Integer, List<BaseTypeChunk>> typeInfoIndexer;

    public static ResTablePackageChunk parseFrom(BoundedInputStream stream, ResStringPoolChunk stringChunk) throws IOException {
        long baseCursor = stream.getPointer();
        //
        ResTablePackageChunk chunk = new ResTablePackageChunk();
        chunk.header = ChunkHeader.parseFrom(stream);
        chunk.pkgId = stream.readIntLow();
        chunk.packageName = stream.readString16(128 * 2);
        chunk.typeStringOffset = stream.readIntLow();
        chunk.lastPublicType = stream.readIntLow();
        chunk.keyStringOffset = stream.readIntLow();
        chunk.lastPublicKey = stream.readIntLow();

        // Data Block
        stream.seek(baseCursor + chunk.typeStringOffset);
        chunk.typeStringPool = ResStringPoolChunk.parseFrom(stream);
        stream.seek(baseCursor + chunk.keyStringOffset);
        chunk.keyStringPool = ResStringPoolChunk.parseFrom(stream);

        // TableTypeSpecType   TableTypeType
        stream.seek(baseCursor + chunk.keyStringOffset + chunk.keyStringPool.header.chunkSize);
        chunk.typeChunks = new ArrayList<>();
        int resCount = 0;
        StringBuilder logInfo = new StringBuilder();
        while (stream.available() > 0) {

            logInfo.setLength(0);
            resCount++;
            ChunkHeader header = ChunkHeader.parseFrom(stream);

            BaseTypeChunk typeChunk = null;
            if (header.type == RES_TABLE_TYPE_SPEC_TYPE) {
                stream.seek(stream.getPointer() - ChunkHeader.LENGTH);
                typeChunk = ResTableTypeSpecChunk.parseFrom(stream, stringChunk);
            } else if (header.type == RES_TABLE_TYPE_TYPE) {
                stream.seek(stream.getPointer() - ChunkHeader.LENGTH);
                typeChunk = ResTableTypeInfoChunk.parseFrom(stream, stringChunk);
            }
            if (typeChunk != null) {
                logInfo.append(typeChunk.getChunkName()).append(" ")
                        .append(String.format("type=%s ", typeChunk.getType()))
                        .append(String.format("count=%s ", typeChunk.getEntryCount()));
            } else {
                logInfo.append("None TableTypeSpecType or TableTypeType!!");
            }

            if (typeChunk != null) {
                chunk.typeChunks.add(typeChunk);
            }
        }

        chunk.createResourceIndex();
        for (int i = 0; i < chunk.typeChunks.size(); ++i) {
            chunk.typeChunks.get(i).translateValues(stringChunk, chunk.typeStringPool, chunk.keyStringPool);
        }

        return chunk;
    }

    private void createResourceIndex() {
        typeInfoIndexer = new HashMap<>();
        for (BaseTypeChunk typeChunk : typeChunks) {
            // The first chunk in typeList should be ResTableTypeSpecChunk
            List<BaseTypeChunk> typeList = typeInfoIndexer.get(typeChunk.getTypeId());
            if (typeList == null) {
                typeList = new ArrayList<BaseTypeChunk>();
                typeInfoIndexer.put(typeChunk.getTypeId(), typeList);
                if (typeChunk.getTypeId() == 2) {
                    int x = 4;
                }
            }
            typeList.add(typeChunk);
        }
    }

    public ResTableEntry getResource(int resId) {
        int typeId = (resId & 0x00ff0000) >> 16;
        //short typeIdx = (short) ((resId >> 16) & 0xff);
        List<BaseTypeChunk> typeList = typeInfoIndexer.get(typeId); // The first chunk in typeList should be ResTableTypeSpecChunk
        for (int i = 1; i < typeList.size(); ++i) {
            if (typeList.get(i) instanceof ResTableTypeInfoChunk) {
                ResTableTypeInfoChunk x = (ResTableTypeInfoChunk) typeList.get(i);
                ResTableEntry entry = x.getResource(resId);
                if (entry != null) {
                    return entry;
                }
            }
        }
        return null;
    }

    public String buildEntry2String() {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>" + System.lineSeparator());
        builder.append("<resources>" + System.lineSeparator());

        for (int i = 0; i < typeChunks.size(); ++i) {
            // All entries exist in ResTableTypeInfoChunk
            if (typeChunks.get(i) instanceof ResTableTypeSpecChunk) {
                // Extract following ResTableTypeInfoChunks
                List<ResTableTypeInfoChunk> typeInfos = new ArrayList<>();
                for (int j = i + 1; j < typeChunks.size(); ++j) {
                    if (typeChunks.get(j) instanceof ResTableTypeInfoChunk) {
                        typeInfos.add((ResTableTypeInfoChunk) typeChunks.get(j));
                    } else {
                        break;
                    }
                }
                i += typeInfos.size();
                // Unique ResTableTypeInfoChunks
                String entry = ResTableTypeInfoChunk.uniqueEntries2String((int) pkgId & 0xff, typeStringPool, keyStringPool, typeInfos);
                builder.append("\t" + entry + System.lineSeparator());
            }
        }

        builder.append("</resources>");
        return builder.toString();
    }
}
