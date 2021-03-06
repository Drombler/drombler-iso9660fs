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
package org.drombler.iso9660fs.impl;

import org.drombler.iso9660fs.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author puce
 */
public class ISOFileSystem extends FileSystem {

    private static final String SEPARATOR = "/";
    public static final String EMPTY_PATH_STRING = "";
    public static final String CURRENT_PATH_STRING = ".";
    public static final String PARENT_PATH_STRING = "..";

    private static final String BASIC_FILE_ATTRIBUTES_NAME = "basic";

    private static final Set<String> SUPPORTED_FILE_ATTRIBUTE_VIEWS = Set.of(BASIC_FILE_ATTRIBUTES_NAME);

    private final ISOFileSystemProvider fileSystemProvider;

    private final Path fileSystemPath;
    private final Map<String, ?> env;
    private final FileStore fileStore;
    private final List<FileStore> fileStores;
    private final ISOPath rootDirectory = new ISOPath(this, SEPARATOR, true);
    private final List<Path> rootDirectories = Collections.singletonList(rootDirectory);
    private final Path emptyPath = new ISOPath(this, EMPTY_PATH_STRING, false);
    private final ISOPath currentDirectory = new ISOPath(this, CURRENT_PATH_STRING, false);
    private final ISOPath parentDirectory = new ISOPath(this, PARENT_PATH_STRING, false);
    private final SeekableByteChannel byteChannel;
    private boolean open = true;

    private ISOPrimaryVolumeDescriptor primaryVolumeDescriptor;
    private ISODirectoryRecord rootDirectoryDescriptor;

    ISOFileSystem(ISOFileSystemProvider fileSystemProvider, Path fileSystemPath, Map<String, ?> env) throws IOException {
        this.fileSystemProvider = fileSystemProvider;
        this.fileSystemPath = fileSystemPath;
        this.env = env;
        this.fileStore = new ISOFileStore(fileSystemPath);
        this.fileStores = Collections.singletonList(fileStore);
        System.out.println("FileSystemPath: " + fileSystemPath);
        this.byteChannel = Files.newByteChannel(fileSystemPath);
        init();
    }

    private void init() throws IOException {
        readVolumeDescriptors();
    }

    private void readVolumeDescriptors() throws IOException {
        final int KiB_32 = 32768;
        byteChannel.position(KiB_32);

        ByteBuffer byteBuffer = ByteBuffer.allocate(ISOVolumeDescriptor.SECTOR_LENGTH);
        final int numBytes = byteChannel.read(byteBuffer);
        if (numBytes != ISOVolumeDescriptor.SECTOR_LENGTH) {
            throw new IOException("Too few data to read: " + numBytes);
        }
        byteBuffer.position(0);
        ISOVolumeDescriptor volumeDescriptor = ISOVolumeDescriptor.createISOVolumeDescriptor(byteBuffer);
        System.out.println(volumeDescriptor);
        if (volumeDescriptor.getType() == ISOVolumeDescriptorType.PRIMARY_VOLUME_DESCRIPTOR) {
            this.primaryVolumeDescriptor = (ISOPrimaryVolumeDescriptor) volumeDescriptor;
            this.rootDirectoryDescriptor = primaryVolumeDescriptor.getRootDirectoryDescriptor();
            primaryVolumeDescriptor.loadPathTables(byteChannel);
            this.rootDirectoryDescriptor.loadDirectory(byteChannel, primaryVolumeDescriptor, false);
        }

    }

    @Override
    public FileSystemProvider provider() {
        return fileSystemProvider;
    }

    @Override
    public void close() throws IOException {
        open = false;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public boolean isReadOnly() {
        return fileStore.isReadOnly();
    }

    @Override
    public String getSeparator() {
        return SEPARATOR;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return rootDirectories;
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return fileStores;
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return SUPPORTED_FILE_ATTRIBUTE_VIEWS;
    }

    @Override
    public Path getPath(String first, String... more) {
        StringBuilder sb = new StringBuilder(first);
        for (String nameElement : more) {
            if (!nameElement.equals(EMPTY_PATH_STRING)) {
                if (sb.length() > 0) {
                    sb.append(getSeparator());
                }
                sb.append(nameElement);
            }
        }
        return ISOPath.valueOf(sb.toString(), this);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException("This is a read-only file system! There's nothing to watch!");
    }

    public ISOPrimaryVolumeDescriptor getPrimaryVolumeDescriptor() {
        return primaryVolumeDescriptor;
    }

    /* package-private */ ISOPath getRootDirectory() {
        return rootDirectory;
    }

    /* package-private */ Path getEmptyPath() {
        return emptyPath;
    }

    /* package-private */ ISOPath getDefaultDirectory() {
        return rootDirectory;
    }

    /* package-private */ Path getFileSystemPath() {
        return fileSystemPath;
    }

    /* package-private */ ISOPath getCurrentDirectory() {
        return currentDirectory;
    }

    /* package-private */ ISOPath getParentDirectory() {
        return parentDirectory;
    }

    /* package-private */ SeekableByteChannel newByteChannel(Path path) {
        if (!path.getFileSystem().equals(this)) {
            throw new IllegalArgumentException("The specified path belongs to a different FileSystem! Path: " + path);
        }
        return null;
    }

    /* package-private */ BasicFileAttributes getAttributes(ISOPath path) throws IOException {
        ISODirectoryRecord directoryRecord = getDirectoryRecord(path);
        return new ISOFileAttributes(directoryRecord);
    }

    /* package-private */ ISODirectoryRecord getDirectoryRecord(ISOPath path) throws IOException {
        if (path.equals(getRootDirectory())) {
            return rootDirectoryDescriptor;
        } else {
            ISOPathTableEntry pathTableEntry = primaryVolumeDescriptor.lookupPathTable(path);

            if (pathTableEntry != null) {
                return primaryVolumeDescriptor.loadDirectoryRecord(byteChannel, pathTableEntry);
            } else if (path.getParent() != null) { // path table only contains entries for directories
                pathTableEntry = primaryVolumeDescriptor.lookupPathTable(path.getParent());
                ISODirectoryRecord directoryRecord = primaryVolumeDescriptor.loadDirectoryRecord(byteChannel, pathTableEntry);
                directoryRecord.loadDirectory(byteChannel, primaryVolumeDescriptor, false);
                return directoryRecord.getChildren().stream()
                        .filter(childRecord -> childRecord.getFileIdentifier().equals(path.getFileName().toString()))
                        .findFirst()
                        .orElseThrow(() -> new FileNotFoundException("No directory record for path:" + path));
            }
            throw new FileNotFoundException("No directory record for path:" + path);
        }
    }

    @Override
    public String toString() {
        return "ISOFileSystem{" +
                "fileSystemPath=" + fileSystemPath +
                '}';
    }

}
