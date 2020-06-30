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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author puce
 */
public class ATest {

    @Test
    public void test() throws URISyntaxException, IOException {
        URI isoURI = new URI("iso:" + ATest.class.getResource("/test.iso").toURI().toString());
        ISOFileSystemProvider isoFileSystemProvider = new ISOFileSystemProvider();
        try (FileSystem isoFileSystem = isoFileSystemProvider.newFileSystem(isoURI, null)) {
            isoFileSystem.getRootDirectories().forEach(rootDirectory -> {
                        System.out.println("Root directory: " + rootDirectory);
                        try {
                            Files.walkFileTree(rootDirectory, new TestFileVisitor());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );
        }
    }

    private static class TestFileVisitor implements FileVisitor<Path> {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            System.out.println("Dir: " + dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            System.out.println("File: " + file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}
