package com.cc.fileManage.entity;

import com.cc.fileManage.utils.LSUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
 * TexTool 实体类
 * 2023 0423 by so what
 */
public class TEXFile
{
    // tex格式枚举
    public enum PixelFormat
    {
        DXT1(0, "DXT1"), DXT3(1, "DXT3"), DXT5(2, "DXT5"),
        ARGB(4, "ARGB"), RGB(5, "RGB"), Un18(18, "?"),
        Unknown(7, "Unknown");

        private final int num;
        private final String name;

        PixelFormat(int num, String name){
            this.num = num;
            this.name = name;
        }

        public int getValue(){
            return num;
        }

        public String getName(){
            return name;
        }

        public static TEXFile.PixelFormat getType(int dataTypeCode){
            for(PixelFormat enums : PixelFormat.values()){
                if(enums.num == dataTypeCode){
                    return enums;
                }
            }
            return Unknown;
        }
    }

    // tex类型枚举
    public enum TextureType
    {
        OneD("1D",1),
        TwoD("2D",2),
        ThreeD("3D",3),
        CubeMap("立方映射",4);

        private final int num;
        private final String des;
        TextureType(String des, int num){
            this.des = des;
            this.num = num;
        }

        public int getValue() {
            return num;
        }

        public String getDes(){
            return des;
        }

        public static TEXFile.TextureType getType(int dataTypeCode){
            for(TextureType enums : TextureType.values()){
                if(enums.num == dataTypeCode){
                    return enums;
                }
            }
            return OneD;
        }
    }

    // 头数据结构
    public static class HeaderStruct
    {
        public long platform;
        public long pixelFormat;
        public long textureType;
        public long numMips;
        public long flags;
        public long remainder;
    }

    // 贴图信息
    public static class Mipmap
    {
        public int width;
        public int height;
        public int pitch;
        public long dataSize;
        public byte[] data;
        ///
        public Mipmap() {}
        public Mipmap(int w, int h, int p, byte[] d) {
            width = w; height = h; pitch = p; data = d;
        }
    }

    // 饥荒tex文件头
    public static final String KTEXHeader = "KTEX";

    // 头数据
    private final HeaderStruct header = new HeaderStruct();

    // 文件数据
    private byte[] raw;

    // get
    public HeaderStruct getHeader() {
        return header;
    }

    // set
    public void setRaw(byte[] raw) {
        this.raw = raw;
    }


    public TEXFile() {}

    public TEXFile(InputStream steam) throws Exception
    {
        try (DataInputStream reader = new DataInputStream(steam)){
            //四个字节数组
            byte[] b = new byte[KTEXHeader.length()];
            //读四个字节
            int l = reader.read(b);

            //是否是KTEX文件
            if(l != 4 || !new String(b).equals(KTEXHeader))
                throw new Exception("不是'KTEX'文件!");

            //读取无符号整数
            long header = readUInt32(reader);
            ///
            this.header.platform = header & 15;
            this.header.pixelFormat = (header >> 4)  & 31;
            this.header.textureType = (header >> 9)  & 15;
            this.header.numMips = (header >> 13) & 31;
            this.header.flags = (header >> 18) & 3;
            this.header.remainder = (header >> 20) & 4095;
            this.raw = new byte[0];
            //
            //32m
            int maxSize = 32 * 1048576;
            //读取完剩下的流
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int rc;
            while ((rc = reader.read(buff, 0, buff.length)) > 0)
            {
                if(swapStream.size() >= maxSize){
                    this.raw = byteMerger(this.raw, swapStream.toByteArray());
                    swapStream.close();
                    swapStream = new ByteArrayOutputStream();
                }
                swapStream.write(buff, 0, rc);
            }
            swapStream.close();
            //保存
            this.raw = byteMerger(this.raw, swapStream.toByteArray());
        }
    }

    public Mipmap[] getMipmaps() throws IOException
    {
        Mipmap[] mipmapArray = new Mipmap[(int)header.numMips];
        try (DataInputStream reader = new DataInputStream(new ByteArrayInputStream(raw))){
            for (int i = 0; i < header.numMips; i++) {
                mipmapArray[i] = new Mipmap();

                mipmapArray[i].width = readUInt16(reader);
                mipmapArray[i].height = readUInt16(reader);
                mipmapArray[i].pitch = readUInt16(reader);
                mipmapArray[i].dataSize = readUInt32(reader);
            }

            for (int i = 0; i < header.numMips; i++){
                byte[] by = new byte[(int)mipmapArray[i].dataSize];
                if(reader.read(by) > 0)
                    mipmapArray[i].data = by;
            }
        }
        return mipmapArray;
    }

    public Mipmap[] getMipmapsSummary() throws IOException
    {
        Mipmap[] mipmapArray = new Mipmap[(int)header.numMips];
        try (DataInputStream reader = new DataInputStream(new ByteArrayInputStream(raw))){
            for (int i = 0; i < header.numMips; i++) {
                mipmapArray[i] = new Mipmap();
                mipmapArray[i].width = readUInt16(reader);
                mipmapArray[i].height = readUInt16(reader);
                mipmapArray[i].pitch = readUInt16(reader);
                mipmapArray[i].dataSize = readUInt32(reader);
            }
        }
        return mipmapArray;
    }

    @SuppressWarnings("all")
    public Mipmap getMainMipmap()
    {
        Mipmap mipmap = new Mipmap();
        try(DataInputStream reader = new DataInputStream(new ByteArrayInputStream(this.raw))) {
            mipmap.width = readUInt16(reader);
            mipmap.height = readUInt16(reader);
            mipmap.pitch = readUInt16(reader);
            mipmap.dataSize = readUInt32(reader);
            ///
            byte[] by = new byte[(int)(this.header.numMips - 1) * 10];
            reader.read(by);
            //
            mipmap.data = new byte[(int)mipmap.dataSize];
            reader.read(mipmap.data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mipmap;
    }

    public Mipmap getMainMipmapSummary() throws IOException
    {
        Mipmap mipmap = new Mipmap();
        try (DataInputStream reader = new DataInputStream(new ByteArrayInputStream(raw))){
            mipmap.width = readUInt16(reader);
            mipmap.height = readUInt16(reader);
            mipmap.pitch = readUInt16(reader);
            mipmap.dataSize = readUInt32(reader);
        }
        return mipmap;
    }

    /**
     * 仅写入tex文件头信息并返回输出流
     * @param stream        DataOutputStream输出流
     * @return              包含tex文件头信息的输出流
     * @throws IOException  IOException
     */
    public DataOutputStream saveFileHeader(OutputStream stream) throws IOException {
        return saveFileHeader(this.header, stream);
    }

    /**
     * 仅写入tex文件头信息并返回输出流
     * @param headerStruct  tex文件头信息
     * @param stream        DataOutputStream输出流
     * @return              包含tex文件头信息的输出流
     * @throws IOException  IOException
     */
    public static DataOutputStream saveFileHeader(HeaderStruct headerStruct, OutputStream stream) throws IOException
    {
        DataOutputStream writer = new DataOutputStream(stream);
        //写 KTEX
        writer.write(LSUtil.stringToBytes(KTEXHeader));
        //
        int header = 0;
        header |= 4095;
        header <<= 2;
        header |= headerStruct.flags;
        header <<= 5;
        header |= headerStruct.numMips;
        header <<= 4;
        header |= headerStruct.textureType;
        header <<= 5;
        header |= headerStruct.pixelFormat;
        header <<= 4;
        header |= headerStruct.platform;
        //写入头信息
        byte[] headerByte = LSUtil.toLH(header);
        writer.write(headerByte);
        return writer;
    }

    /**
     * 保存tex文件
     * @param stream        输出流
     * @throws IOException  IOException
     */
    public void saveFile(OutputStream stream) throws IOException {
        saveFile(this, stream);
    }

    /**
     * 保存tex文件
     * @param texFile       TEXFile
     * @param stream        输出流
     * @throws IOException  IOException
     */
    public static void saveFile(TEXFile texFile, OutputStream stream) throws IOException
    {
        try (DataOutputStream writer = new DataOutputStream(stream)){
            //写 KTEX
            writer.write(LSUtil.stringToBytes(KTEXHeader));
            int header = 0;
            header |= 4095;
            header <<= 2;
            header |= texFile.header.flags;
            header <<= 5;
            header |= texFile.header.numMips;
            header <<= 4;
            header |= texFile.header.textureType;
            header <<= 5;
            header |= texFile.header.pixelFormat;
            header <<= 4;
            header |= texFile.header.platform;

            //写入头信息
            byte[] headerByte = LSUtil.toLH(header);
            writer.write(headerByte);
            //剩余数据
            writer.write(texFile.raw);
        }
    }

    /**
     * 保存tex数据并返回数据字节数组
     * @return              数据的字节数组
     * @throws IOException  IOException
     */
    public byte[] fileData() throws IOException
    {
        try (ByteArrayOutputStream writer = new ByteArrayOutputStream()){
            //写 KTEX
            writer.write(LSUtil.stringToBytes(KTEXHeader));
            //
            int header = 0;
            header |= 4095;
            header <<= 2;
            header |= this.header.flags;
            header <<= 5;
            header |= this.header.numMips;
            header <<= 4;
            header |= this.header.textureType;
            header <<= 5;
            header |= this.header.pixelFormat;
            header <<= 4;
            header |= this.header.platform;
            //写入头信息
            byte[] headerByte = LSUtil.toLH(header);
            writer.write(headerByte);
            //剩余数据
            writer.write(this.raw);
            return writer.toByteArray();
        }
    }

    /**
     * 读取无符号16位整数
     * @param reader        input
     * @return              无符号16位整数
     * @throws IOException  IOException
     */
    private int readUInt16(DataInput reader) throws IOException{
        //读取有符号的二字节整数
        short head = reader.readShort();
        //转为低字节序整数
        head = LSUtil.reverseShort(head);
        //转为无符号整数
        return LSUtil.getUnsignedByte(head);
    }

    /**
     * 读取无符号32位整数
     * @param reader        input
     * @return              无符号32位整数
     * @throws IOException  IOException
     */
    private long readUInt32(DataInput reader) throws IOException{
        //读取有符号的四字节整数
        int head = reader.readInt();
        //转为低字节序整数
        head = LSUtil.reverseInt(head);
        //转为无符号整数
        return LSUtil.getUnsignedInt(head);
    }

    /**
     * 合并两个byte数组
     * @param one   数组1
     * @param two   数组2
     * @return      合并后的数组
     */
    private byte[] byteMerger(byte[] one,byte[] two){
        //新数组
        byte[] outByte = new byte[one.length + two.length];
        int i = 0;

        for(byte b : one){
            outByte[i] = b;
            i++;
        }
        for(byte b : two){
            outByte[i] = b;
            i++;
        }
        return outByte;
    }
}