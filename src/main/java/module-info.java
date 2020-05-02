
import java.nio.file.spi.FileSystemProvider;
import org.drombler.iso9660fs.impl.ISOFileSystemProvider;

/**
 * NIO.2 File API provider for ISO 9660 files.
 */
module org.drombler.iso9660fs {
    exports org.drombler.iso9660fs;

    provides FileSystemProvider with ISOFileSystemProvider;
}
