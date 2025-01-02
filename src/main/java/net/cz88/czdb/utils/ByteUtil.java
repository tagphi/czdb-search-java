package net.cz88.czdb.utils;

/**
 * This utility class provides methods for manipulating byte arrays.
 * It includes methods for writing specific bytes to a byte array,
 * writing an integer to a byte array, and getting an integer from a byte array.
 */
public class ByteUtil {
    /**
     * Writes specified bytes to a byte array starting from a given offset.
     *
     * @param b     the byte array to write to
     * @param offset the position in the array to start writing
     * @param v     the value to write
     * @param bytes the number of bytes to write
     */
    public static void write(byte[] b, int offset, long v, int bytes) {
        for (int i = 0; i < bytes; i++) {
            b[offset++] = (byte) ((v >>> (8 * i)) & 0xFF);
        }
    }

    /**
     * Writes an integer to a byte array.
     *
     * @param b     the byte array to write to
     * @param offset the position in the array to start writing
     * @param v     the value to write
     */
    public static void writeIntLong(byte[] b, int offset, long v) {
        b[offset++] = (byte) ((v >> 0) & 0xFF);
        b[offset++] = (byte) ((v >> 8) & 0xFF);
        b[offset++] = (byte) ((v >> 16) & 0xFF);
        b[offset] = (byte) ((v >> 24) & 0xFF);
    }

    /**
     * Gets an integer from a byte array starting from a specified offset.
     *
     * @param b     the byte array to read from
     * @param offset the position in the array to start reading
     * @return the integer value read from the byte array
     */
    public static long getIntLong(byte[] b, int offset) {
        return (
                ((b[offset++] & 0x000000FFL)) |
                        ((b[offset++] << 8) & 0x0000FF00L) |
                        ((b[offset++] << 16) & 0x00FF0000L) |
                        ((b[offset] << 24) & 0xFF000000L)
        );
    }

    /**
     * Gets a 3-byte integer from a byte array starting from a specified offset.
     *
     * @param b     the byte array to read from
     * @param offset the position in the array to start reading
     * @return the integer value read from the byte array
     */
    public static int getInt3(byte[] b, int offset) {
        return (
                (b[offset++] & 0x000000FF) |
                        (b[offset++] & 0x0000FF00) |
                        (b[offset] & 0x00FF0000)
        );
    }

    /**
     * Gets a 2-byte integer from a byte array starting from a specified offset.
     *
     * @param b     the byte array to read from
     * @param offset the position in the array to start reading
     * @return the integer value read from the byte array
     */
    public static int getInt2(byte[] b, int offset) {
        return (
                (b[offset++] & 0x000000FF) |
                        (b[offset] << 8 & 0x0000FF00)
        );
    }

    /**
     * Gets a 1-byte integer from a byte array starting from a specified offset.
     *
     * @param b     the byte array to read from
     * @param offset the position in the array to start reading
     * @return the integer value read from the byte array
     */
    public static int getInt1(byte[] b, int offset) {
        return (
                (b[offset] & 0x000000FF)
        );
    }
}