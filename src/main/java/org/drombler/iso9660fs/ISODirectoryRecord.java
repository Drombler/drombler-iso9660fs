/*
 *         COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Notice
 *
 * The contents of this file are subject to the COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL)
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/cddl1.txt
 *
 * The Original Code is Drombler.org. The Initial Developer of the
 * Original Code is Florian Brunner (Sourceforge.net user: puce).
 * Copyright 2020 Drombler.org. All Rights Reserved.
 *
 * Contributor(s): .
 */
package org.drombler.iso9660fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author Florian
 */
public class ISODirectoryRecord {

    private final short length;
    private final short extendedAttributeRecordLength;
    private final long locationOfExtend; // logical block number of the first logical block allocated to the file
    private final long dataLength;
    private final ZonedDateTime recordingDateTime;
    private final Set<ISOFileFlag> fileFlags;
    private final short interleavedModeFileUnitSize;
    private final short interleavedModeInterleaveGapSize;
    private final int volumeSequenceNumber;
    private final short fileIdentifierLength;
    private final String fileIdentifier;
    private final List<ISODirectoryRecord> children = new ArrayList<>();

    public ISODirectoryRecord(ByteBuffer byteBuffer) {
        this.length = ISOUtils.getUnsignedByte(byteBuffer);
        this.extendedAttributeRecordLength = ISOUtils.getUnsignedByte(byteBuffer);
        this.locationOfExtend = ISOUtils.getUnsignedInt32LSBMSB(byteBuffer);
        this.dataLength = ISOUtils.getUnsignedInt32LSBMSB(byteBuffer);
        this.recordingDateTime = ISOUtils.getDirectoryDateTime(byteBuffer);
        this.fileFlags = ISOFileFlag.convertBitSet(byteBuffer.get());
        this.interleavedModeFileUnitSize = ISOUtils.getUnsignedByte(byteBuffer);
        this.interleavedModeInterleaveGapSize = ISOUtils.getUnsignedByte(byteBuffer);
        this.volumeSequenceNumber = ISOUtils.getUnsignedInt16LSBMSB(byteBuffer);
        this.fileIdentifierLength = ISOUtils.getUnsignedByte(byteBuffer);
        this.fileIdentifier = ISOUtils.getStringDTrimmed(byteBuffer, fileIdentifierLength);

        if (ISOUtils.isEven(fileIdentifierLength)) {
            ISOUtils.readUnused(byteBuffer, 1);
        }
    }


    /**
     * @return the length
     */
    public short getLength() {
        return length;
    }

    /**
     * @return the extendedAttributeRecordLength
     */
    public short getExtendedAttributeRecordLength() {
        return extendedAttributeRecordLength;
    }

    /**
     * @return the locationOfExtend
     */
    public long getLocationOfExtend() {
        return locationOfExtend;
    }

    /**
     * @return the dataLength
     */
    public long getDataLength() {
        return dataLength;
    }

    /**
     * @return the recordingDateTime
     */
    public ZonedDateTime getRecordingDateTime() {
        return recordingDateTime;
    }

    /**
     * @return the fileFlags
     */
    public Set<ISOFileFlag> getFileFlags() {
        return fileFlags;
    }

    /**
     * @return the interleavedModeFileUnitSize
     */
    public short getInterleavedModeFileUnitSize() {
        return interleavedModeFileUnitSize;
    }

    /**
     * @return the interleavedModeInterleaveGapSize
     */
    public short getInterleavedModeInterleaveGapSize() {
        return interleavedModeInterleaveGapSize;
    }

    /**
     * @return the volumeSequenceNumber
     */
    public int getVolumeSequenceNumber() {
        return volumeSequenceNumber;
    }

    /**
     * @return the fileIdentifierLength
     */
    public short getFileIdentifierLength() {
        return fileIdentifierLength;
    }

    /**
     * @return the fileIdentifier
     */
    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public List<ISODirectoryRecord> getChildren() {
        return children;
    }

    public void loadDirectory(SeekableByteChannel byteChannel, ISOPrimaryVolumeDescriptor volumeDescriptor, boolean recursive) throws IOException {
        if (volumeSequenceNumber != volumeDescriptor.getVolumeSequenceNumber()) {
            throw new IllegalArgumentException(); // TODO: message
        }
        ByteBuffer byteBuffer = volumeDescriptor.createByteBuffer(byteChannel, locationOfExtend, dataLength);

        boolean endReached = false;
        while (byteBuffer.position() < dataLength && !endReached) {
            int startPosition = byteBuffer.position();
            Optional<ISODirectoryRecord> directoryDescriptorOptional = loadDirectory(byteBuffer);
            if (directoryDescriptorOptional.isPresent()) {
                ISODirectoryRecord directoryDescriptor = directoryDescriptorOptional.get();
                if (recursive && directoryDescriptor.getFileFlags().contains(ISOFileFlag.DIRECTORY)) {
                    directoryDescriptor.loadDirectory(byteChannel, volumeDescriptor, recursive);
                }
                byteBuffer.position(startPosition + directoryDescriptor.length);
            }
            endReached = directoryDescriptorOptional.isEmpty();
        }
    }

    private Optional<ISODirectoryRecord> loadDirectory(ByteBuffer byteBuffer) {
        ISODirectoryRecord directoryDescriptor = new ISODirectoryRecord(byteBuffer);
        if (directoryDescriptor.length > 0) {
            children.add(directoryDescriptor);
            return Optional.of(directoryDescriptor);
        } else {
            return Optional.empty();
        }
    }

    public SeekableByteChannel newByteChannel(SeekableByteChannel byteChannel, ISOPrimaryVolumeDescriptor volumeDescriptor) {
        return new SeekableByteChannel() {
            private long position;
            private boolean open = true;
            private final Object lock = new Object();

            @Override
            public int read(ByteBuffer dst) throws IOException {
                synchronized (lock) {
                    checkOpen();
                    return 0; // TODO: implement: read from extend
                }
            }

            @Override
            public int write(ByteBuffer src) throws IOException {
                throw new NonWritableChannelException();
            }

            @Override
            public long position() throws IOException {
                synchronized (lock) {
                    checkOpen();
                    return position;
                }
            }

            @Override
            public SeekableByteChannel position(long newPosition) throws IOException {
                synchronized (lock) {
                    checkOpen();
                    this.position = newPosition;
                    return this;
                }
            }

            @Override
            public long size() throws IOException {
                synchronized (lock) {
                    checkOpen();
                    return dataLength;
                }
            }

            @Override
            public SeekableByteChannel truncate(long size) throws IOException {
                throw new NonWritableChannelException();
            }

            @Override
            public boolean isOpen() {
                synchronized (lock) {
                    return open;
                }
            }

            @Override
            public void close() throws IOException {
                synchronized (lock) {
                    this.open = false;
                }
            }

            private void checkOpen() throws ClosedChannelException {
                if (!isOpen()) {
                    throw new ClosedChannelException();
                }
            }
        };
    }


    @Override
    public int hashCode() {
        return Objects.hash(length, extendedAttributeRecordLength, locationOfExtend, dataLength, recordingDateTime,
                fileFlags, interleavedModeFileUnitSize, interleavedModeInterleaveGapSize, volumeSequenceNumber,
                fileIdentifierLength, fileIdentifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ISODirectoryRecord that = (ISODirectoryRecord) o;
        return length == that.length &&
                extendedAttributeRecordLength == that.extendedAttributeRecordLength &&
                locationOfExtend == that.locationOfExtend &&
                dataLength == that.dataLength &&
                interleavedModeFileUnitSize == that.interleavedModeFileUnitSize &&
                interleavedModeInterleaveGapSize == that.interleavedModeInterleaveGapSize &&
                volumeSequenceNumber == that.volumeSequenceNumber &&
                fileIdentifierLength == that.fileIdentifierLength &&
                recordingDateTime.equals(that.recordingDateTime) &&
                fileFlags.equals(that.fileFlags) &&
                fileIdentifier.equals(that.fileIdentifier);
    }


    @Override
    public String toString() {
        return "ISODirectoryDescriptor{"
                + "length=" + length
                + ", extendedAttributeRecordLength=" + extendedAttributeRecordLength
                + ", locationOfExtend=" + locationOfExtend
                + ", dataLength=" + dataLength
                + ", recordingDateTime=" + recordingDateTime
                + ", fileFlags=" + fileFlags
                + ", interleavedModeFileUnitSize=" + interleavedModeFileUnitSize
                + ", interleavedModeInterleaveGapSize=" + interleavedModeInterleaveGapSize
                + ", volumeSequenceNumber=" + volumeSequenceNumber
                + ", fileIdentifierLength=" + fileIdentifierLength
                + ", fileIdentifier=" + fileIdentifier + '}';
    }
}
