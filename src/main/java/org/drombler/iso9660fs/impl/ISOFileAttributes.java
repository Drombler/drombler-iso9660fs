package org.drombler.iso9660fs.impl;

import org.drombler.iso9660fs.ISODirectoryRecord;
import org.drombler.iso9660fs.ISOFileFlag;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class ISOFileAttributes implements BasicFileAttributes {

    private ISODirectoryRecord directoryRecord;

    /* package-private */ ISOFileAttributes(ISODirectoryRecord directoryRecord) {
        this.directoryRecord = directoryRecord;
    }

    @Override
    public FileTime lastModifiedTime() {
        return FileTime.from(directoryRecord.getRecordingDateTime().toInstant());
    }

    @Override
    public FileTime lastAccessTime() {
        return lastModifiedTime();
    }

    @Override
    public FileTime creationTime() {
        return lastModifiedTime();
    }

    @Override
    public boolean isRegularFile() {
        return !isDirectory();
    }

    @Override
    public boolean isDirectory() {
        return directoryRecord.getFileFlags().contains(ISOFileFlag.DIRECTORY);
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public long size() {
        return directoryRecord.getDataLength();
    }

    @Override
    public ISODirectoryRecord fileKey() {
        return directoryRecord; // TODO: correct?
    }
}
