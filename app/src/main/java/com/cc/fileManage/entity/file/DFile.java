package com.cc.fileManage.entity.file;

import android.provider.DocumentsContract;
import androidx.documentfile.provider.DocumentFile;

import java.util.Objects;

/**
 *  android documentFile 特殊类
 */
public class DFile extends ManageFile{

    private DocumentFile documentFile;

    public DFile(){}

    public DFile(String fileName, String fileType, long lastModified, long fileSize){
        this.fileName = fileName;
        this.fileType = fileType.equals(DocumentsContract.Document.MIME_TYPE_DIR) ? "directory" : "stream";
        this.fileLastModified = lastModified;
        this.fileSize = fileSize;
    }

    public DFile(DocumentFile documentFile){
        this(Objects.requireNonNull(documentFile.getName()), documentFile.isDirectory() ? "directory" : "stream",
                documentFile.lastModified(), documentFile.length());
        this.documentFile = documentFile;
    }

    public DocumentFile getDocumentFile() {
        return documentFile;
    }
}
