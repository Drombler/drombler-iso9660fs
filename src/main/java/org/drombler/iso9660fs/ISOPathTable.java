package org.drombler.iso9660fs;

import org.drombler.iso9660fs.impl.PathReverseIterator;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.drombler.iso9660fs.impl.ISOPath.checkISOPath;

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

    /*package-private*/ ISOPathTableEntry lookup(Path path) {
        checkISOPath(path);
        for (PathReverseIterator iterator = new PathReverseIterator(path); iterator.hasNext(); ) {
            Path pathPart = iterator.next();
            return pathTableEntries.stream()
                    .filter(isoPathTableEntry -> matchesFileName(isoPathTableEntry, pathPart))
                    .filter(isoPathTableEntry -> matchesPath(isoPathTableEntry, iterator))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private boolean matchesPath(ISOPathTableEntry isoPathTableEntry, PathReverseIterator iterator) {
        boolean matches = true;
        while (isoPathTableEntry.getParentDirectoryNumber() >= 0 && iterator.hasNext()) {
            Path parent = iterator.next();
            matches = matches && matchesFileName(pathTableEntries.get(isoPathTableEntry.getParentDirectoryNumber()), parent);
            if (!matches) {
                break;
            }
        }
        return isoPathTableEntry.getParentDirectoryNumber() < 0 && !iterator.hasNext() && matches;
    }

    private boolean matchesFileName(ISOPathTableEntry isoPathTableEntry, Path path) {
        return isoPathTableEntry.getDirectoryIdentifier().equals(path.getFileName().toString());
    }
}
