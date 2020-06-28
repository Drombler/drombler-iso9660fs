package org.drombler.iso9660fs;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ISOPathTable {

    private final ISOEncodingType encodingType;
    private final List<ISOPathTableEntry> pathTableEntries = new ArrayList<>();

    public ISOPathTable(ISOEncodingType encodingType, ByteBuffer byteBuffer) {
        this.encodingType = encodingType;
        while (byteBuffer.remaining() > 0) {
            pathTableEntries.add(new ISOPathTableEntry(encodingType, byteBuffer));
        }
    }

    public List<ISOPathTableEntry> getPathTableEntries() {
        return pathTableEntries;
    }

    public ISOEncodingType getEncodingType() {
        return encodingType;
    }

    @Override
    public String toString() {
        return "ISOPathTable{" +
                "encodingType=" + encodingType +
                '}';
    }
}
