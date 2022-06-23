package com.cc.fileManage.entity;

import com.cc.fileManage.utils.FormatToUtil;

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
 * 2020 0314 by so what
 */
public class TEXFile
{
    public enum Platform{
        PC(12),
        XBOX360(11),
        PS3(10),
        Unknown(0);

        private final int num;
        Platform(int num){
            this.num = num;
        }

        public int getValue() {
            return num;
        }

        public static TEXFile.Platform getType(int dataTypeCode){
            for(Platform enums : Platform.values()){
                if(enums.num == dataTypeCode){
                    return enums;
                }
            }
            return null;
        }
    }

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
            return null;
        }
    }

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
            return null;
        }
    }

    public final String KTEXHeader = "KTEX";

    public static class FileStruct
    {
        public static class HeaderStruct
        {
            public static long Platform;
            public static long PixelFormat;
            public static long TextureType;
            public static long NumMips;
            public static long Flags;
            public static long Remainder;
        }

        public static HeaderStruct Header;
        public static byte[] Raw;
    }

    public FileStruct File;

    public static class Mipmap
    {
        public static int Width;
        public static int Height;
        public static int Pitch;
        public static long DataSize;
        public static byte[] Data;
    }

    public TEXFile(){}

    public TEXFile(InputStream steam) throws Exception
    {
        DataInputStream reader = new DataInputStream(steam);

        //四个字节数组
        byte[] b = new byte[KTEXHeader.length()];
        //读四个字节
        reader.read(b);

        //是否是KTEX文件
        if(!new String(b).equals(KTEXHeader))
            throw new Exception("不是'KTEX'文件!");

        //读取无符号整数
        long header = readUInt32(reader);

        File.Header.Platform = header & 15;
        File.Header.PixelFormat = (header >> 4)  & 31;
        File.Header.TextureType = (header >> 9)  & 15;
        File.Header.NumMips = (header >> 13) & 31;
        File.Header.Flags = (header >> 18) & 3;
        File.Header.Remainder = (header >> 20) & 4095;
        File.Raw = new byte[0];

        // Just a little hack for pre cave updates, can remove later.
        OldRemainder = (header >> 14) & 262143;

        //32m
        int maxSize = 32 * 1048576;
        //读取完剩下的流
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int rc = 0;
        while ((rc = reader.read(buff, 0, buff.length)) > 0)
        {
            if(swapStream.size() >= maxSize){
                File.Raw = byteMerger(File.Raw, swapStream.toByteArray());
                swapStream.close();
                swapStream = new ByteArrayOutputStream();
            }
            swapStream.write(buff, 0, rc);
        }
        swapStream.close();
        //保存
        //File.Raw = swapStream.toByteArray();
        File.Raw = byteMerger(File.Raw, swapStream.toByteArray());
        //关闭流
        reader.close();
    }

    /* A little hacky but it gets the job done. */
    private long OldRemainder;
    public boolean IsPreCaveUpdate() { return OldRemainder == 262143; }

    public Mipmap[] GetMipmaps() throws IOException
    {
        Mipmap[] mipmapArray = new Mipmap[(int)File.Header.NumMips];

        DataInputStream reader = new DataInputStream(new ByteArrayInputStream(File.Raw));

        for (int i = 0; i < File.Header.NumMips; i++) {
            mipmapArray[i] = new Mipmap();

            mipmapArray[i].Width = readUInt16(reader);
            mipmapArray[i].Height = readUInt16(reader);
            mipmapArray[i].Pitch = readUInt16(reader);
            mipmapArray[i].DataSize = readUInt32(reader);
        }

        for (int i = 0; i < File.Header.NumMips; i++){

            byte[] by = new byte[(int)mipmapArray[i].DataSize];
            reader.read(by);

            mipmapArray[i].Data = by;
        }
        //关闭流
        reader.close();

        return mipmapArray;
    }

    public Mipmap[] GetMipmapsSummary() throws IOException
    {
        Mipmap[] mipmapArray = new Mipmap[(int)File.Header.NumMips];


        DataInputStream reader = new DataInputStream(new ByteArrayInputStream(File.Raw));

        for (int i = 0; i < File.Header.NumMips; i++) {
            mipmapArray[i] = new Mipmap();
            mipmapArray[i].Width = readUInt16(reader);
            mipmapArray[i].Height = readUInt16(reader);
            mipmapArray[i].Pitch = readUInt16(reader);
            mipmapArray[i].DataSize = readUInt32(reader);
        }
        reader.close();

        return mipmapArray;
    }

    public Mipmap GetMainMipmap() throws IOException
    {
        Mipmap mipmap = new Mipmap();

        DataInputStream reader = new DataInputStream(new ByteArrayInputStream(File.Raw));

        mipmap.Width = readUInt16(reader);
        mipmap.Height = readUInt16(reader);
        mipmap.Pitch = readUInt16(reader);
        mipmap.DataSize = readUInt32(reader);

        byte[] by = new byte[(int)(File.Header.NumMips - 1) * 10];
        reader.read(by);

        byte[] da = new byte[(int)mipmap.DataSize];
        reader.read(da);
        mipmap.Data = da;

        reader.close();

        return mipmap;
    }

    public Mipmap GetMainMipmapSummary() throws IOException
    {
        Mipmap mipmap = new Mipmap();

        DataInputStream reader = new DataInputStream(new ByteArrayInputStream(File.Raw));

        mipmap.Width = readUInt16(reader);
        mipmap.Height = readUInt16(reader);
        mipmap.Pitch = readUInt16(reader);
        mipmap.DataSize = readUInt32(reader);

        reader.close();

        return mipmap;
    }

    public DataOutputStream SaveFileHeader(OutputStream stream) throws IOException
    {
        DataOutputStream writer = new DataOutputStream(stream);

        //写 KTEX
        writer.write(FormatToUtil.stringToBytes(KTEXHeader));

        int header = 0;

        header |= 4095;
        header <<= 2;
        header |= File.Header.Flags;
        header <<= 5;
        header |= File.Header.NumMips;
        header <<= 4;
        header |= File.Header.TextureType;
        header <<= 5;
        header |= File.Header.PixelFormat;
        header <<= 4;
        header |= File.Header.Platform;

        //写入头信息
        byte[] headerByte = FormatToUtil.toLH(header);
        writer.write(headerByte);

        return writer;
    }

    public void SaveFile(OutputStream stream) throws IOException
    {
        DataOutputStream writer = new DataOutputStream(stream);

        //写 KTEX
        writer.write(FormatToUtil.stringToBytes(KTEXHeader));

        int header = 0;

        header |= 4095;
        header <<= 2;
        header |= File.Header.Flags;
        header <<= 5;
        header |= File.Header.NumMips;
        header <<= 4;
        header |= File.Header.TextureType;
        header <<= 5;
        header |= File.Header.PixelFormat;
        header <<= 4;
        header |= File.Header.Platform;

        //写入头信息
        byte[] headerByte = FormatToUtil.toLH(header);
        writer.write(headerByte);

        //剩余数据
        writer.write(File.Raw);

        writer.close();
    }

    //java默认zip包
    public byte[] FileData() throws IOException
    {
        //byte
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        //写 KTEX
        writer.write(FormatToUtil.stringToBytes(KTEXHeader));

        int header = 0;

        header |= 4095;
        header <<= 2;
        header |= File.Header.Flags;
        header <<= 5;
        header |= File.Header.NumMips;
        header <<= 4;
        header |= File.Header.TextureType;
        header <<= 5;
        header |= File.Header.PixelFormat;
        header <<= 4;
        header |= File.Header.Platform;

        //写入头信息
        byte[] headerByte = FormatToUtil.toLH(header);
        writer.write(headerByte);

        //剩余数据
        writer.write(File.Raw);

        writer.close();

        return writer.toByteArray();
    }

    private int readUInt16(DataInput reader) throws IOException{
        //读取有符号的二字节整数
        short head = reader.readShort();
        //转为低字节序整数
        head = FormatToUtil.reverseShort(head);

        //转为无符号整数
        return FormatToUtil.getUnsignedByte(head);
    }

    private long readUInt32(DataInput reader) throws IOException{
        //读取有符号的四字节整数
        int head = reader.readInt();
        //转为低字节序整数
        head = FormatToUtil.reverseInt(head);

        //转为无符号整数
        return FormatToUtil.getUnsignedInt(head);
    }


    //合并两个byte数组
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