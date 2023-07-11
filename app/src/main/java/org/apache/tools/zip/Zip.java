package org.apache.tools.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

public interface Zip extends AutoCloseable{

    boolean canReadEntryData(ZipEntry zipEntry);

    String getEncoding();

    Enumeration<ZipEntry> getEntries();

    Iterable<ZipEntry> getEntries(String name);

    ZipEntry getEntry(String name);

    InputStream getInputStream(ZipEntry ze) throws IOException;

    Enumeration<ZipEntry> getEntriesInPhysicalOrder();

    Iterable<ZipEntry> getEntriesInPhysicalOrder(String name);

    void close() throws IOException;
}
