package org.drombler.iso9660fs;

import java.nio.ByteBuffer;

/**
 * @author Florian
 */


public class ISOPathTableEntry {

    private final short directoryIdentifierLength;
    private final short extendedAttributeRecordLength;
    private final long locationOfExtend;
    private final int parentDirectoryNumber; // index in path table
    private final String directoryIdentifier; // name

    public ISOPathTableEntry(ISOEncodingType encodingType, ByteBuffer byteBuffer) {
        this.directoryIdentifierLength = ISOUtils.getUnsignedByte(byteBuffer);
        this.extendedAttributeRecordLength = ISOUtils.getUnsignedByte(byteBuffer);
        this.locationOfExtend = encodingType.getUnsignedInt32(byteBuffer);
        this.parentDirectoryNumber = encodingType.getUnsignedInt16(byteBuffer);
        this.directoryIdentifier = ISOUtils.getStringDTrimmed(byteBuffer, directoryIdentifierLength);

        if (ISOUtils.isOdd(directoryIdentifierLength)) {
            ISOUtils.readUnused(byteBuffer, 1);
        }
    }

    public short getDirectoryIdentifierLength() {
        return directoryIdentifierLength;
    }

    public short getExtendedAttributeRecordLength() {
        return extendedAttributeRecordLength;
    }

    public long getLocationOfExtend() {
        return locationOfExtend;
    }

    public int getParentDirectoryNumber() {
        return parentDirectoryNumber;
    }

    public String getDirectoryIdentifier() {
        return directoryIdentifier;
    }


//    public ISODirectoryRecord loadDirectoryRecord(SeekableByteChannel byteChannel, ISOPrimaryVolumeDescriptor primaryVolumeDescriptor) {
//        primaryVolumeDescriptor.
//        return null;
//    }

    @Override
    public String toString() {
        return "ISOPathTableEntry{" +
                "directoryIdentifier='" + directoryIdentifier + '\'' +
                '}';
    }

}
