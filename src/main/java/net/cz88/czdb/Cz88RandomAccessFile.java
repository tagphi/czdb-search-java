package net.cz88.czdb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This class extends the RandomAccessFile class and adds an offset to the file pointer.
 * The offset is added whenever the seek method is called.
 * This is useful when you want to treat a part of the file as a separate file.
 */
public class Cz88RandomAccessFile extends RandomAccessFile {
    // The offset to be added to the file pointer
    private final int offset;

    /**
     * Constructs a random access file stream to read from, and optionally to write to.
     * The offset is added to the file pointer whenever the seek method is called.
     *
     * @param name the system-dependent filename
     * @param mode the access mode
     * @param offset the offset to be added to the file pointer
     * @throws FileNotFoundException if the mode is "r" but the given string does not denote an existing regular file, or if the mode begins with "rw" but the given string does not denote an existing, writable regular file and a new regular file of that name cannot be created, or if some other error occurs while opening or creating the file
     */
    public Cz88RandomAccessFile(String name, String mode, int offset) throws FileNotFoundException {
        super(name, mode);
        this.offset = offset;
    }

    /**
     * Sets the file-pointer offset, measured from the beginning of this file, at which the next read or write occurs.
     * The offset may be set beyond the end of the file. Setting the offset beyond the end of the file does not change the file length.
     * The file length will change only by writing after the offset has been set beyond the end of the file.
     *
     * @param pos the offset position, measured in bytes from the beginning of the file, at which to set the file pointer
     * @throws IOException if pos is less than 0 or if an I/O error occurs
     */
    @Override
    public void seek(long pos) throws IOException {
        super.seek(pos + offset);
    }

    @Override
    public long length() throws IOException {
        return super.length() - offset;
    }
}