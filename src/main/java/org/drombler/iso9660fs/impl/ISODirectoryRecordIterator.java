package org.drombler.iso9660fs.impl;

import org.drombler.iso9660fs.ISODirectoryRecord;

import java.util.Iterator;

public class ISODirectoryRecordIterator implements Iterator<ISODirectoryRecord> {

    private ISODirectoryRecord startRecord;

    public ISODirectoryRecordIterator(ISODirectoryRecord startRecord) {
        this.startRecord = startRecord;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public ISODirectoryRecord next() {
        return null;
    }
}
