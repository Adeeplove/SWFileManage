package com.cc.fileManage.module.manifest.data;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.PrintUtils;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 16/07/2017.
 */

public class StringChunk {

    public long chunkType;
    public long chunkSize;
    public long stringCount;
    public long styleCount;
    public long unknown;
    public long stringPoolOffset;
    public long stylePoolOffset;
    public long[] stringOffset; // Offset of each string
    public long[] styleOffset;  // Offset of each string

    public int[] stringLens;    // Length of each string
    public int[] styleLens;     // Length of each style
    public String[] strings;    // Content of each string
    public String[] styles;     // Content of each style

    public static StringChunk parseFrom(PositionInputStream stream) throws IOException {
        StringChunk chunk = new StringChunk();
        // Chunk header
        chunk.chunkType = Utils.readInt(stream);
        chunk.chunkSize = Utils.readInt(stream);
        chunk.stringCount = Utils.readInt(stream);
        chunk.styleCount = Utils.readInt(stream);
        chunk.unknown = Utils.readInt(stream);
        chunk.stringPoolOffset = Utils.readInt(stream);
        chunk.stylePoolOffset = Utils.readInt(stream);
        chunk.stringOffset = new long[(int) chunk.stringCount];
        chunk.styleOffset = new long[(int) chunk.styleCount];
        for (int i=0; i<chunk.stringOffset.length; ++i) {
            chunk.stringOffset[i] = Utils.readInt(stream);
        }
        for (int i=0; i<chunk.styleOffset.length; ++i) {
            chunk.styleOffset[i] = Utils.readInt(stream);
        }
        // String Content
        chunk.strings = new String[(int) chunk.stringCount];
        chunk.stringLens = new int[(int) chunk.stringCount];
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<chunk.stringCount; ++i) {
            builder.setLength(0);
            int len = chunk.stringLens[i] = Utils.readShort(stream);   // The leading two bytes are length of string
            for (int j=0; j<len; ++j) {
                builder.append((char) Utils.readShort(stream));
            }
            char end0x00 = (char) Utils.readShort(stream);
            chunk.strings[i] = builder.toString();
        }
        // Style content
        chunk.styles = new String[(int) chunk.styleCount];
        chunk.styleLens = new int[(int) chunk.styleCount];
        for (int i=0; i<chunk.styleCount; ++i) {
            builder.setLength(0);
            int len = chunk.styleLens[i] = Utils.readShort(stream);   // The leading two bytes are length of string
            for (int j=0; j<len; ++j) {
                builder.append((char) Utils.readShort(stream));
            }
            char end0x00 = (char) Utils.readShort(stream);
            chunk.styles[i] = builder.toString();
        }
        return chunk;
    }

    public String getString(int index) {
        return index >= 0 && index < strings.length ? strings[index] : null;
    }

    public String getString(long index) {
        return index >=0 && index < strings.length ? strings[(int) index] : null;
    }

    public String getStyle(int index) {
        return index >= 0 && index < styles.length ? styles[index] : null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(1024);
        String formH = "%-16s %s\n";

        builder.append("-- String Chunk --").append('\n');
        builder.append(String.format(formH, "chunkType", PrintUtils.hex4(chunkType)));
        builder.append(String.format(formH, "chunkSize", PrintUtils.hex4(chunkSize)));
        builder.append(String.format(formH, "stringCount", PrintUtils.hex4(stringCount)));
        builder.append(String.format(formH, "styleCount", PrintUtils.hex4(styleCount)));
        builder.append(String.format(formH, "unknown", PrintUtils.hex4(unknown)));
        builder.append(String.format(formH, "stringPoolOffset", PrintUtils.hex4(stringPoolOffset)));
        builder.append(String.format(formH, "stylePoolOffset", PrintUtils.hex4(stylePoolOffset)));

        builder.append("|----|----------|-----|---------").append('\n');
        builder.append("| Id |  Offset  | Len | Content").append('\n');
        builder.append("|----|----------|-----|---------").append('\n');
        String formC = "|%-4d| %-8s | %-3d | %s\n";
        for (int i=0; i<stringCount; ++i) {
            builder.append(String.format(formC, i, PrintUtils.hex4(stringOffset[i]), stringLens[i], strings[i]));
        }
        for (int i=0; i<styleCount; ++i) {
            builder.append(String.format(formC, i, PrintUtils.hex4(styleOffset[i]), styleLens[i], styles[i]));
        }
        return builder.toString();
    }
}
