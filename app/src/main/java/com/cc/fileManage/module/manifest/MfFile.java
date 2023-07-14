package com.cc.fileManage.module.manifest;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.RandomInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by xueqiulxq on 16/07/2017.
 */

public class MfFile {

    public MfHeader header;
    public StringChunk stringChunk;
    public ResourceIdChunk resourceIdChunk;
    public StartNamespaceChunk startNamespaceChunk;
    public List<StartTagChunk> startTagChunks;
    public List<EndTagChunk> endTagChunks;
    public List<TagChunk> tagChunks;
    public EndNamespaceChunk endNamespaceChunk;

    public MfFile() {
        startTagChunks = new ArrayList<>();
        endTagChunks = new ArrayList<>();
        tagChunks = new ArrayList<>();
    }

    public void parse(RandomInputStream stream) throws IOException {
        ///
        RandomInputStream.CutStream cutStream = stream.getInputStream(0, MfHeader.LENGTH);
        header = parseHeader(cutStream);
        cutStream.skipRemaining();
        //
        int chunkIdx = 0;
        do {
            stream.markNow();
            //
            long pos = stream.getPointer();
            //
            cutStream = stream.getInputStream(pos, ChunkInfo.LENGTH);
            ChunkInfo info = ChunkInfo.parseFrom(cutStream);
            stream.reset();
            //
            info.chunkIndex = chunkIdx++;
            //
            cutStream = stream.getInputStream(pos, info.chunkSize);
            ////
            StartTagChunk startTagChunk;
            EndTagChunk endTagChunk;
            switch ((int)info.chunkType) {
                case ChunkInfo.STRING_CHUNK_ID:
                    stringChunk = parseStringChunk(cutStream);
                    break;
                case ChunkInfo.RESOURCE_ID_CHUNK_ID:
                    resourceIdChunk = parseResourceIdChunk(cutStream);
                    break;
                case ChunkInfo.START_NAMESPACE_CHUNK_ID:
                    startNamespaceChunk = parseStartNamespaceChunk(cutStream);
                    break;
                case ChunkInfo.START_TAG_CHUNK_ID:
                    startTagChunk = parseStartTagChunk(cutStream);
                    startTagChunks.add(startTagChunk);
                    tagChunks.add(startTagChunk);
                    break;
                case ChunkInfo.EDN_TAG_CHUNK_ID:
                    endTagChunk = parseEndTagChunk(cutStream);
                    endTagChunks.add(endTagChunk);
                    tagChunks.add(endTagChunk);
                    break;
                case ChunkInfo.CHUNK_ENDNS_CHUNK_ID:
                    endNamespaceChunk = parseEndNamespaceChunk(cutStream);
                    break;
                default:
                    break;
            }
            cutStream.skipRemaining();
        } while (stream.getPointer() < header.fileLength);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(4096);
        builder.append('\n');
        builder.append(header).append('\n');
        builder.append(stringChunk).append('\n');
        builder.append(resourceIdChunk).append('\n');
        builder.append(startNamespaceChunk).append('\n');

        builder.append("-- StartTag Chunks --").append('\n');
        for (int i=0; i<startTagChunks.size(); ++i) {
            builder.append("StartTagChunk_").append(i).append('\n');
            builder.append(startTagChunks.get(i)).append('\n');
        }
        builder.append("-- EndTag Chunks --").append('\n');
        for (int i=0; i<endTagChunks.size(); ++i) {
            builder.append("EndTagChunk_").append(i).append('\n');
            builder.append(endTagChunks.get(i)).append('\n');
        }
        builder.append(endNamespaceChunk).append('\n');
        return builder.toString();
    }

    /**
     * Convert MfFile object to XML string.
     * @return xml
     */
    public String toXmlString() {
        StringBuilder builder = new StringBuilder(4096);
        int depth = 0;
        builder.append("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n");
        for (TagChunk tagChunk : tagChunks) {
            if (tagChunk instanceof StartTagChunk) {
                builder.append(createStartTagXml((StartTagChunk) tagChunk, depth));
                ++depth;
            } else if (tagChunk instanceof EndTagChunk) {
                --depth;
                builder.append(createEndTagXml((EndTagChunk) tagChunk, depth));
            }
        }
        return builder.toString();
    }


    private String createStartTagXml(StartTagChunk chunk, int depth) {
        StringBuilder builder = new StringBuilder(256);
        String lineIndent = makeLineIndent(depth, 4);
        if ("manifest".equals(chunk.nameStr)) {
            builder.append("<manifest\n");
            for (Map.Entry<String, String> entry : startNamespaceChunk.prefix2UriMap.entrySet()) {
                builder.append("    ").append("xmlns:").append(entry.getKey()).append("=")
                        .append("\"").append(entry.getValue()).append("\"");
            }
        } else {
            builder.append(lineIndent);
            builder.append("<").append(chunk.nameStr);
        }
        List<AttributeEntry> attrEntries = chunk.attributes;
        if (attrEntries.size() > 0) {
            for (AttributeEntry entry : attrEntries) {

                builder.append("\n");
                builder.append(lineIndent).append("    ");
                String prefixName = startNamespaceChunk.uri2prefixMap.get(entry.namespaceUriStr);
                if (prefixName != null) {
                    builder.append(prefixName).append(':');
                }
                builder.append(entry.nameStr).append('=')
                        .append("\"").append(entry.dataStr).append("\"");
            }
        }
        builder.append(" >\n");
        return builder.toString();
    }

    private String createEndTagXml(EndTagChunk chunk, int depth) {
        StringBuilder builder = new StringBuilder(256);
        String lineIndent = makeLineIndent(depth, 4);
        builder.append(lineIndent);
        builder.append("</").append(chunk.nameStr).append(">").append('\n');
        return builder.toString();
    }

    private String makeLineIndent(int depth, int indent) {
        StringBuilder builder = new StringBuilder(depth * indent);
        for (int i=0; i<depth * indent; ++i) {
            builder.append(' ');
        }
        return builder.toString();
    }

    public MfHeader parseHeader(RandomInputStream.CutStream cutStream) throws IOException {
        return MfHeader.parseFrom(cutStream);
    }

    public StringChunk parseStringChunk(RandomInputStream.CutStream cutStream) throws IOException {
        return StringChunk.parseFrom(cutStream);
    }

    public ResourceIdChunk parseResourceIdChunk(RandomInputStream.CutStream cutStream) throws IOException {
        return ResourceIdChunk.parseFrom(cutStream);
    }

    public StartNamespaceChunk parseStartNamespaceChunk(RandomInputStream.CutStream cutStream) throws IOException {
        return StartNamespaceChunk.parseFrom(cutStream, stringChunk);
    }

    public StartTagChunk parseStartTagChunk(RandomInputStream.CutStream cutStream) throws IOException {
        return StartTagChunk.parseFrom(cutStream, stringChunk);
    }

    public EndTagChunk parseEndTagChunk(RandomInputStream.CutStream cutStream) throws IOException {
        return EndTagChunk.parseFrom(cutStream, stringChunk);
    }

    public EndNamespaceChunk parseEndNamespaceChunk(RandomInputStream.CutStream cutStream) throws IOException {
        return EndNamespaceChunk.parseFrom(cutStream, stringChunk);
    }
}
