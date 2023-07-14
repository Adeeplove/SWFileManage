package com.cc.fileManage.module.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class IUtils {

    public enum Endian {
        Little, Big
    }

    public static short readUInt8(InputStream stream) throws IOException {
        return (short) (stream.read() & 0xff);
    }

    public static short readShortLow(InputStream stream) throws IOException {
        byte[] bytes = new byte[2];
        if(stream.read(bytes) > 0) {
            return getShort(bytes, Endian.Little);
        }
        return 0;
    }

    public static short readShort(InputStream stream) throws IOException {
        byte[] bytes = new byte[2];
        if(stream.read(bytes) > 0) {
            return getShort(bytes, Endian.Big);
        }
        return 0;
    }

    public static int readIntLow(InputStream stream) throws IOException {
        byte[] bytes = new byte[4];
        if(stream.read(bytes) > 0) {
            return getInt(bytes, Endian.Little);
        }
        return 0;
    }

    public static int readInt(InputStream stream) throws IOException {
        byte[] bytes = new byte[4];
        if(stream.read(bytes) > 0) {
            return getInt(bytes, Endian.Big);
        }
        return 0;
    }

    public static long readLong(InputStream stream) throws IOException {
        return ((long)(readInt(stream)) << 32) + (readInt(stream) & 0xFFFFFFFFL);
    }

    public static float readFloat(InputStream stream) throws IOException {
        return Float.intBitsToFloat(readInt(stream));
    }

    public static double readDouble(InputStream stream) throws IOException {
        return Double.longBitsToDouble(readLong(stream));
    }

    public static boolean readBoolean(InputStream stream) throws IOException {
        int ch = stream.read();
        return (ch != 0);
    }

    public static short getShort(byte[] buf, Endian endian) {
        if(endian == Endian.Little) {
            return (short) ((buf[0] & 0xff) | (buf[1] & 0xff)  << 8);
        }
        return (short) ((buf[0] & 0xff) << 8 | (buf[1] & 0xff));
    }

    public static int getInt(byte[] buf, Endian endian) {
        int ret = 0;
        if (endian == Endian.Little) {
            for (int i=3; i>=0; --i) {
                ret <<= 8;
                ret |= (buf[i] & 0xff);
            }
        } else {
            for (int i=0; i<=3; ++i) {
                ret <<= 8;
                ret |= (buf[i] & 0xff);
            }
        }
        return ret;
    }

    /**
     * 读取并将字符(16-bit)转换为字符串。以0x00结束，填充字节0。
     * @param length            读取的字节长度
     * @return                  字符串
     * @throws IOException      IO异常
     */
    public static String readString(InputStream stream, int length) throws IOException {
        byte[] bytes = new byte[length];
        if(stream.read(bytes) > 0)
            return new String(bytes, StandardCharsets.UTF_8);
        return null;
    }

    /**
     * 读取并将字符(16-bit)转换为字符串。以0x00结束，填充字节0。
     * @param length        读取的字节长度
     * @return              字符串
     * @throws IOException  IO异常
     */
    public static String readString16(InputStream stream, int length) throws IOException {
        byte[] bytes = new byte[length];
        StringBuilder builder = new StringBuilder();
        if(stream.read(bytes) > 0) {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            byte[] buf_2 = new byte[2];
            while (in.read(buf_2) != -1) {
                int code = getShort(buf_2, Endian.Little);
                if (code == 0x00)
                    break;  // End of String
                else
                    builder.append((char) code);
            }
        }
        return builder.toString();
    }

    /**
     * 从指定的流读取ULEB编码字节数组
     * @param stream        流
     * @return              ULEB编码字节数组
     */
    public static byte[] readULEB128BytesFrom(InputStream stream) throws IOException {
        byte[] buf = new byte[1];
        long len = 0;
        long remain = stream.available();
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
     * @param stream        流
     * @return              ULEB编码字节数组
     */
    public static byte[] readULEB128Bytes(InputStream stream) throws IOException{
        byte[] buf = new byte[5];
        int len = 0;
        boolean hitEnd;
        do {
            byte[] b = new byte[1];
            stream.read(b);
            ///
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
