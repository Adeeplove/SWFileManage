package com.cc.fileManage.module.stream;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author bilux (i.bilux@gmail.com)
 */
public class PositionInputStream extends FilterInputStream {

    private long position = 0;
    private long markedPosition = 0;
    //
    private long length = 0;

    public static PositionInputStream getInstance(byte[] bytes) {
        return new PositionInputStream(new ByteArrayInputStream(bytes)).setLength(bytes.length);
    }

    public PositionInputStream(InputStream inputStream) {
        super(inputStream);
    }

    public PositionInputStream(InputStream inputStream, long length) {
        super(inputStream);
        this.length = length;
    }

    public long length() {
        return length;
    }

    private PositionInputStream setLength(long length) {
        this.length = length;
        return this;
    }

    public synchronized long getPosition() {
        return position;
    }

    @Override
    public synchronized int read() throws IOException {
        int p = in.read();
        if (p != -1)
            position++;
        return p;
    }
    
    @Override
    public synchronized int read(byte[] b) throws IOException {
        int p = in.read(b);
        if (p > 0)
            position += p;        
        return p;
    }
    
    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        int p = in.read(b, off, len);
        if (p > 0)
            position += p;        
        return p;
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        long p = in.skip(n);
        if (p > 0)
            position += p;
        return p;
    }

    public synchronized long seek(long n) throws IOException {
        in.reset();
        position = 0;
        long p = in.skip(n);
        if (p > 0)
            position += p;
        return p;
    }
    
    @Override
    public synchronized void reset() throws IOException {
        in.reset();
        position = markedPosition;
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
        markedPosition = position;
    }
}
