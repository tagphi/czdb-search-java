package net.cz88.czdb.entity;

import net.cz88.czdb.utils.ByteUtil;

/**
 * The HeaderBlock class represents a header block in the database.
 * It contains an index start IP address and an index pointer.
 * It also provides a method to get the bytes for database storage.
 *
 * Memory layout of a HeaderBlock instance:
 *
 * +-----------------+-----------------+
 * | indexStartIp    | indexPtr        |
 * | (byte array)    | (int)           |
 * +-----------------+-----------------+
 *
 * @author chenxin
 */
public class HeaderBlock {
    /**
     * The index start IP address.
     */
    private byte[] indexStartIp;

    /**
     * The index pointer.
     */
    private int indexPtr;

    /**
     * Constructs a new HeaderBlock with the specified index start IP address and index pointer.
     *
     * @param indexStartIp the index start IP address
     * @param indexPtr the index pointer
     */
    public HeaderBlock(byte[] indexStartIp, int indexPtr) {
        this.indexStartIp = indexStartIp;
        this.indexPtr = indexPtr;
    }

    /**
     * Returns the index start IP address of this header block.
     *
     * @return the index start IP address of this header block
     */
    public byte[] getIndexStartIp() {
        return indexStartIp;
    }

    /**
     * Sets the index start IP address of this header block to the specified value.
     *
     * @param indexStartIp the new index start IP address
     * @return this header block
     */
    public HeaderBlock setIndexStartIp(byte[] indexStartIp) {
        this.indexStartIp = indexStartIp;
        return this;
    }

    /**
     * Returns the index pointer of this header block.
     *
     * @return the index pointer of this header block
     */
    public int getIndexPtr() {
        return indexPtr;
    }

    /**
     * Sets the index pointer of this header block to the specified value.
     *
     * @param indexPtr the new index pointer
     * @return this header block
     */
    public HeaderBlock setIndexPtr(int indexPtr) {
        this.indexPtr = indexPtr;
        return this;
    }

    /**
     * Returns the bytes for database storage.
     * The returned byte array is 20 bytes long, with the first 16 bytes being the index start IP address and the last 4 bytes being the index pointer.
     *
     * @return a byte array representing this header block for database storage
     */
    public byte[] getBytes() {
        byte[] b = new byte[20];

        System.arraycopy(indexStartIp, 0, b, 0, Math.min(indexStartIp.length, 16));
        ByteUtil.writeIntLong(b, 16, indexPtr);
        return b;
    }
}