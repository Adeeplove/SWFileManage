package com.cc.fileManage.module.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author bilux (i.bilux@gmail.com)
 */

public class Utils {

    /**
     * 从指定流中读取指定长度的字节
     *
     * @param mStreamer     流
     * @return              字节数组
     * @throws IOException  IO异常
     */
    public static byte[] read(PositionInputStream mStreamer, int length) throws IOException {
        byte[] bytes = new byte[length];
        if(mStreamer.read(bytes) > 0)
            return bytes;
        return bytes;
    }

    /**
     * 从字节数组中读取8-bit shor值
     *
     * @param mStreamer     流
     * @return              8-bit short值
     * @throws IOException  IO异常
     */
    public static short readUInt8(PositionInputStream mStreamer) throws IOException {
        byte[] bytes = new byte[1];
        if(mStreamer.read(bytes) > 0)
            return getUInt8(bytes);
        return 0;
    }

    /**
     * 从字节数组中读取short值
     *
     * @param mStreamer     流
     * @return              16-bit short值
     * @throws IOException  IO异常
     */
    public static int readShort(PositionInputStream mStreamer) throws IOException {
        byte[] bytes = new byte[2];
        if(mStreamer.read(bytes) > 0)
            return getShort(bytes);
        return 0;
    }

    /**
     * 从字节数组中读取int值
     *
     * @param mStreamer     流
     * @return              32-bit int值
     * @throws IOException  IO异常
     */
    public static long readInt(PositionInputStream mStreamer) throws IOException {
        byte[] bytes = new byte[4];
        if(mStreamer.read(bytes) > 0)
            return getInt(bytes);
        return 0;
    }

    /**
     * 读取并将字符(16-bit)转换为字符串。以0x00结束，填充字节0。
     *
     * @param mStreamer         流
     * @param length            读取的字节长度
     * @return                  字符串
     * @throws IOException      IO异常
     */
    public static String readString(PositionInputStream mStreamer, int length) throws IOException {
        byte[] bytes = new byte[length];
        if(mStreamer.read(bytes) > 0)
            return new String(bytes, StandardCharsets.UTF_8);
        return null;
    }

    /**
     * 读取并将字符(16-bit)转换为字符串。以0x00结束，填充字节0。
     *
     * @param mStreamer     流
     * @param length        读取的字节长度
     * @return              字符串
     * @throws IOException  IO异常
     */
    public static String readString16(PositionInputStream mStreamer, int length) throws IOException {
        byte[] bytes = new byte[length];
        StringBuilder builder = new StringBuilder();
        if(mStreamer.read(bytes) > 0) {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            byte[] buf_2 = new byte[2];
            while (in.read(buf_2) != -1) {
                int code = getShort(buf_2);
                if (code == 0x00)
                    break;  // End of String
                else
                    builder.append((char) code);
            }
        }
        return builder.toString();
    }

    /**
     * 从字节数组获取UInt8
     *
     * @param bytes 字节数组
     * @return      8-bit short值
     */
    public static short getUInt8(byte[] bytes) {
        return (short) (bytes[0] & 0xFF);
    }
    
    /**
     * 从字节数组中读取short值
     *
     * @param bytes 字节数组
     * @return      16-bit short值
     */
    public static int getShort(byte[] bytes) {
        return (bytes[1] & 0xff) << 8 | (bytes[0] & 0xff);
    }


    //        return (long) bytes[3]
    //                << 24 & 0xff000000
    //                | bytes[2]
    //                << 16 & 0xff0000
    //                | bytes[1]
    //                << 8 & 0xff00
    //                | bytes[0] & 0xFF;
    /**
     * 从字节数组中读取int值
     *
     * @param bytes 字节数组
     * @return      32-bit int值
     */
    public static long getInt(byte[] bytes) {
        long ret = 0;
        for (int i=3; i>=0; --i) {
            ret <<= 8;
            ret |= (bytes[i] & 0xff);
        }
        return ret;
    }

    /**
     * 从指定的流读取ULEB编码字节数组
     * @param stream        流
     * @return              ULEB编码字节数组
     */
    public static byte[] readULEB128BytesFrom(PositionInputStream stream) throws IOException {
        byte[] buf = new byte[1];
        long len = 0;
        long remain = stream.length() - stream.getPosition();
        for (int i=0; i < remain; ++i) {
            byte b = (byte) stream.read();
            buf[i] = b;
            ++len;
            if ((b & 0x80) == 0) {
                break;
            }
            if (len == buf.length) {
                byte[] expand = new byte[buf.length * 2];
                System.arraycopy(buf, 0, expand, 0, buf.length);
                buf = expand;
            }
        }
        if (len < buf.length) {
            byte[] ret = new byte[(int) len];
            System.arraycopy(buf, 0, ret, 0, (int) len);
            return ret;
        } else {
            return buf;
        }
    }

    /**
     * 读取ULEB编码字节数组
     * @param mStreamer     流
     * @return              ULEB编码字节数组
     */
    public static byte[] readULEB128Bytes(PositionInputStream mStreamer) throws IOException{
        byte[] buf = new byte[5];
        int len = 0;
        boolean hitEnd;
        do {
            byte[] b = read(mStreamer, 1);
            buf[len] = b[0];
            hitEnd = (b[0] & 0x80) == 0;
            ++len;
        } while (!hitEnd);
        byte[] ret = new byte[len];
        System.arraycopy(buf, 0, ret, 0, ret.length);
        return ret;
    }

    /**
     * ULEB编码转Int
     * @param bytes     字节数组
     * @return          long
     */
    public static long parseULEB128Int(byte[] bytes) {
        int len = bytes.length;
        long res = 0;
        for (int i = 0; i < len; ++i) {
            res |= (long) (bytes[i] & 0x7f) << (7*i);
        }
        return res;
    }
}
