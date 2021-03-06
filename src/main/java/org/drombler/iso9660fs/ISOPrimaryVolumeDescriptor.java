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
 * Copyright 2014 Drombler.org. All Rights Reserved.
 *
 * Contributor(s): .
 */
package org.drombler.iso9660fs;

import org.drombler.iso9660fs.impl.ISOPath;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.ZonedDateTime;

import static org.drombler.iso9660fs.ISOUtils.readUnused;
import static org.drombler.iso9660fs.impl.ISOPath.toISOPath;

/**
 * @author puce
 */
public class ISOPrimaryVolumeDescriptor extends ISOVolumeDescriptor {

    private static final int SYSTEM_IDENTIFIER_LENGTH = 32;
    private static final int VOLUME_IDENTIFIER_LENGTH = 32;
    private static final byte FILE_STRUCTURE_VERSION = 0x01;

    private final String systemIdentifier;
    private final String volumeIdentifier;
    private final long volumeSpaceSize;
    private final int volumeSetSize;
    private final int volumeSequenceNumber;
    private final int logicalBlockSize;
    private final long pathTableSize;
    private final long locationOfTypeLPathTable;
    private final long locationOfOptionalTypeLPathTable;
    private final long locationOfTypeMPathTable;
    private final long locationOfOptionalTypeMPathTable;
    private final String volumeSetIdentifier;
    private final String copyrightFileIdentifier;
    private final String abstractFileIdentifier;
    private final String bibliographicFileIdentifier;
    private final ZonedDateTime volumeCreationDateTime;
    private final ZonedDateTime volumeModificationDateTime;
    private final ZonedDateTime volumeExpirationDateTime;
    private final ZonedDateTime volumeEffectiveDateTime;
    private final byte fileStructureVersion;
    private final byte[] directoryEntryForRootDirectory;
    private final ISODirectoryRecord rootDirectoryDescriptor;
    private ISOPathTable typeLPathTable;
    private ISOPathTable optionalTypeLPathTable;
    private ISOPathTable typeRPathTable;
    private ISOPathTable optionalTypeRPathTable;

    public ISOPrimaryVolumeDescriptor(ByteBuffer byteBuffer) {
        super(ISOVolumeDescriptorType.PRIMARY_VOLUME_DESCRIPTOR, byteBuffer);
        readUnused(byteBuffer, 1);
        this.systemIdentifier = ISOUtils.getStringATrimmed(byteBuffer, SYSTEM_IDENTIFIER_LENGTH);
        this.volumeIdentifier = ISOUtils.getStringDTrimmed(byteBuffer, VOLUME_IDENTIFIER_LENGTH);
        readUnused(byteBuffer, 8);
        this.volumeSpaceSize = ISOUtils.getUnsignedInt32LSBMSB(byteBuffer);
        readUnused(byteBuffer, 32);
        this.volumeSetSize = ISOUtils.getUnsignedInt16LSBMSB(byteBuffer);
        this.volumeSequenceNumber = ISOUtils.getUnsignedInt16LSBMSB(byteBuffer);
        this.logicalBlockSize = ISOUtils.getUnsignedInt16LSBMSB(byteBuffer);
        this.pathTableSize = ISOUtils.getUnsignedInt32LSBMSB(byteBuffer);
        this.locationOfTypeLPathTable = ISOUtils.getUnsignedInt32LSB(byteBuffer);
        this.locationOfOptionalTypeLPathTable = ISOUtils.getUnsignedInt32LSB(byteBuffer);
        this.locationOfTypeMPathTable = ISOUtils.getUnsignedInt32MSB(byteBuffer);
        this.locationOfOptionalTypeMPathTable = ISOUtils.getUnsignedInt32MSB(byteBuffer);
        this.directoryEntryForRootDirectory = ISOUtils.getBytes(byteBuffer, 34);
//        if (directoryEntryForRootDirectory[0] != 0) {
//            throw new IllegalArgumentException("0x00 expected but was: 0x" + Integer.toHexString(directoryEntryForRootDirectory[0]));
//        }
        this.rootDirectoryDescriptor = new ISODirectoryRecord(ByteBuffer.wrap(directoryEntryForRootDirectory));
        this.volumeSetIdentifier = ISOUtils.getStringDTrimmed(byteBuffer, 128);
        ISOUtils.getBytes(byteBuffer, 128);
        ISOUtils.getBytes(byteBuffer, 128);
        ISOUtils.getBytes(byteBuffer, 128);
        this.copyrightFileIdentifier = ISOUtils.getStringDTrimmed(byteBuffer, 38);

        this.abstractFileIdentifier = ISOUtils.getStringDTrimmed(byteBuffer, 36);

        this.bibliographicFileIdentifier = ISOUtils.getStringDTrimmed(byteBuffer, 37);
        this.volumeCreationDateTime = ISOUtils.getDecDateTime(byteBuffer);
        this.volumeModificationDateTime = ISOUtils.getDecDateTime(byteBuffer);
        this.volumeExpirationDateTime = ISOUtils.getDecDateTime(byteBuffer);
        this.volumeEffectiveDateTime = ISOUtils.getDecDateTime(byteBuffer);
        this.fileStructureVersion = byteBuffer.get();
        if (fileStructureVersion != FILE_STRUCTURE_VERSION) {
            throw new IllegalArgumentException(
                    "The File Structure Version  must be " + FILE_STRUCTURE_VERSION + " but was: " + fileStructureVersion);
        }
        ISOUtils.readUnused(byteBuffer, 1);
        ISOUtils.getBytes(byteBuffer, 512);
        ISOUtils.getBytes(byteBuffer, 653);
    }

    public byte[] getDirectoryEntryForRootDirectory() {
        return directoryEntryForRootDirectory;
    }


    public String getSystemIdentifier() {
        return systemIdentifier;
    }

    public String getVolumeIdentifier() {
        return volumeIdentifier;
    }

    public long getVolumeSpaceSize() {
        return volumeSpaceSize;
    }

    public int getVolumeSetSize() {
        return volumeSetSize;
    }

    public int getVolumeSequenceNumber() {
        return volumeSequenceNumber;
    }

    public int getLogicalBlockSize() {
        return logicalBlockSize;
    }

    public long getPathTableSize() {
        return pathTableSize;
    }

    public long getLocationOfTypeLPathTable() {
        return locationOfTypeLPathTable;
    }

    public long getLocationOfOptionalTypeLPathTable() {
        return locationOfOptionalTypeLPathTable;
    }

    public long getLocationOfTypeMPathTable() {
        return locationOfTypeMPathTable;
    }

    public long getLocationOfOptionalTypeMPathTable() {
        return locationOfOptionalTypeMPathTable;
    }

    public String getVolumeSetIdentifier() {
        return volumeSetIdentifier;
    }

    public String getCopyrightFileIdentifier() {
        return copyrightFileIdentifier;
    }

    public String getAbstractFileIdentifier() {
        return abstractFileIdentifier;
    }

    public String getBibliographicFileIdentifier() {
        return bibliographicFileIdentifier;
    }

    public ZonedDateTime getVolumeCreationDateTime() {
        return volumeCreationDateTime;
    }

    public ZonedDateTime getVolumeModificationDateTime() {
        return volumeModificationDateTime;
    }

    public ZonedDateTime getVolumeExpirationDateTime() {
        return volumeExpirationDateTime;
    }

    public ZonedDateTime getVolumeEffectiveDateTime() {
        return volumeEffectiveDateTime;
    }

    public byte getFileStructureVersion() {
        return fileStructureVersion;
    }

    /**
     * @return the rootDirectoryDescriptor
     */
    public ISODirectoryRecord getRootDirectoryDescriptor() {
        return rootDirectoryDescriptor;
    }

    public void loadPathTables(SeekableByteChannel byteChannel) throws IOException {
        this.typeLPathTable = createPathTable(byteChannel, ISOEncodingType.LSB, locationOfTypeLPathTable);
        if (locationOfOptionalTypeLPathTable != 0) {
            this.optionalTypeLPathTable = createPathTable(byteChannel, ISOEncodingType.LSB, locationOfOptionalTypeLPathTable);
        }
        this.typeRPathTable = createPathTable(byteChannel, ISOEncodingType.MSB, locationOfTypeMPathTable);
        if (locationOfOptionalTypeMPathTable != 0) {
            this.optionalTypeRPathTable = createPathTable(byteChannel, ISOEncodingType.MSB, locationOfOptionalTypeMPathTable);
        }
    }

    public ISODirectoryRecord loadDirectoryRecord(SeekableByteChannel byteChannel, ISOPathTableEntry pathTableEntry) throws IOException {
        ByteBuffer byteBuffer = createByteBuffer(byteChannel, pathTableEntry.getLocationOfExtend(), logicalBlockSize);
        return new ISODirectoryRecord(byteBuffer);
    }

    private ISOPathTable createPathTable(SeekableByteChannel byteChannel, ISOEncodingType encodingType, long locationOfPathTable) throws IOException {
        ByteBuffer byteBuffer = createByteBuffer(byteChannel, locationOfPathTable, pathTableSize);
        return new ISOPathTable(encodingType, byteBuffer);
    }

    public ByteBuffer createByteBuffer(SeekableByteChannel byteChannel, long location, long dataLength) throws IOException {
        return ISOUtils.createByteBuffer(byteChannel, location, dataLength, getLogicalBlockSize());
    }

    public ISOPathTableEntry lookupPathTable(Path path) {
        ISOPath isoPath = toISOPath(path);
        if (isoPath.getFileSystem().getPrimaryVolumeDescriptor() != this) {
            throw new InvalidPathException(path.toString(), "Not a path of this file system: " + isoPath.getFileSystem());
        }
        return typeLPathTable.lookup(path);
    }

    @Override
    public String toString() {
        return "ISOPrimaryVolumeDescriptor{"
                + super.toString()
                + "\n, systemIdentifier=" + systemIdentifier
                + "\n, volumeIdentifier=" + volumeIdentifier
                + "\n, volumeSpaceSize=" + volumeSpaceSize
                + "\n, volumeSetSize=" + volumeSetSize
                + "\n, volumeSequenceNumber=" + volumeSequenceNumber
                + "\n, logicalBlockSize=" + logicalBlockSize
                + "\n, pathTableSize=" + pathTableSize
                + "\n, locationOfTypeLPathTable=" + locationOfTypeLPathTable
                + "\n, locationOfOptionalTypeLPathTable=" + locationOfOptionalTypeLPathTable
                + "\n, locationOfTypeMPathTable=" + locationOfTypeMPathTable
                + "\n, locationOfOptionalTypeMPathTable=" + locationOfOptionalTypeMPathTable
                + "\n, volumeSetIdentifier=" + volumeSetIdentifier
                + "\n, copyrightFileIdentifier=" + copyrightFileIdentifier
                + "\n, abstractFileIdentifier=" + abstractFileIdentifier
                + "\n, bibliographicFileIdentifier=" + bibliographicFileIdentifier
                + "\n, volumeCreationDateTime=" + volumeCreationDateTime
                + "\n, volumeModificationDateTime=" + volumeModificationDateTime
                + "\n, volumeExpirationDateTime=" + volumeExpirationDateTime
                + "\n, volumeEffectiveDateTime=" + volumeEffectiveDateTime
                + "\n, fileStructureVersion=" + fileStructureVersion
                + "\n, rootDirectoryDescriptor=" + rootDirectoryDescriptor + '}';
    }
}
