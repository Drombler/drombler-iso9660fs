package org.drombler.iso9660fs.impl;

import org.drombler.iso9660fs.ISODirectoryRecord;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;

public class ISODirectoryStream implements DirectoryStream<Path> {

    private boolean closed = false;

    private final Iterator<ISODirectoryRecord> directoryRecordIterator;

    public ISODirectoryStream(ISOPath isoDirPath) throws IOException {
        ISODirectoryRecord startDirectoryRecord = isoDirPath.getFileSystem().getDirectoryRecord(isoDirPath);
        this.directoryRecordIterator = new ISODirectoryRecordIterator(startDirectoryRecord);
    }

    @Override
    public Iterator<Path> iterator() {
        return new Iterator<Path>() {
            @Override
            public boolean hasNext() {
                return !closed && directoryRecordIterator.hasNext();
            }

            @Override
            public Path next() {
                return null;
            }
        };
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
    }
}
