package net.cz88.czdb.entity;

import net.cz88.czdb.DbType;
import net.cz88.czdb.utils.ByteUtil;

/**
 * This class represents an index block in the database.
 * An index block contains the start IP, end IP, data pointer, and data length.
 * The start IP and end IP are used to determine the range of IP addresses that the data block covers.
 * The data pointer is used to locate the data block in the database.
 * The data length is used to read the data block from the database.
 */
public class IndexBlock {
    /**
     * The start IP address of the range that the data block covers.
     * It is a byte array of length 16.
     */
    private byte[] startIp;

    /**
     * The end IP address of the range that the data block covers.
     * It is a byte array of length 16.
     */
    private byte[] endIp;

    /**
     * The pointer to the data block in the database.
     * It is an integer representing the offset of the data block from the start of the database.
     */
    private int dataPtr;

    /**
     * The length of the data block in bytes.
     * It is an integer representing the number of bytes to read from the data pointer.
     */
    private int dataLen;

    private final DbType dbType;

    /**
     * Constructor for the IndexBlock class.
     * It initializes the start IP, end IP, data pointer, and data length with the provided values.
     *
     * @param startIp The start IP address of the range that the data block covers.
     * @param endIp The end IP address of the range that the data block covers.
     * @param dataPtr The pointer to the data block in the database.
     * @param dataLen The length of the data block in bytes.
     */
    public IndexBlock(byte[] startIp, byte[] endIp, int dataPtr, int dataLen, DbType dbType) {
        this.startIp = startIp;
        this.endIp = endIp;
        this.dataPtr = dataPtr;
        this.dataLen = dataLen;
        this.dbType = dbType;
    }

    public byte[] getStartIp() {
        return startIp;
    }

    public IndexBlock setStartIp(byte[] startIp) {
        this.startIp = startIp;
        return this;
    }

    public byte[] getEndIp() {
        return endIp;
    }

    public IndexBlock setEndIp(byte[] endIp) {
        this.endIp = endIp;
        return this;
    }

    public int getDataPtr() {
        return dataPtr;
    }

    public IndexBlock setDataPtr(int dataPtr) {
        this.dataPtr = dataPtr;
        return this;
    }

    public int getDataLen() {
        return dataLen;
    }

    public IndexBlock setDataLen(int dataLen) {
        this.dataLen = dataLen;
        return this;
    }

    public static int getIndexBlockLength(DbType dbType) {
        // 16 bytes for start IP, 16 bytes for end IP if IPV6
        // or 4 bytes for start IP and 4 bytes for end IP if IPV4
        // + 4 bytes for data ptr and data len
        return dbType == DbType.IPV4 ? 12 : 36;
    }

    /**
     * Returns a byte array representing the index block.
     * The byte array is structured as follows:
     * +------------+-----------+-----------+
     * | 4/16 bytes    | 4/16bytes   | 4bytes    |
     * +------------+-----------+-----------+
     *  start ip      end ip      data ptr + len
     *
     * @return A byte array representing the index block.
     */
    public byte[] getBytes() {
        int ipBytesLength = DbType.IPV4 == dbType ? 4 : 16;

        byte[] b = new byte[getIndexBlockLength(this.dbType)];
        System.arraycopy(startIp, 0, b, 0, ipBytesLength);
        System.arraycopy(endIp, 0, b, ipBytesLength, ipBytesLength);

        //write the data ptr and the length
        long mix = dataPtr | ((dataLen << 24) & 0xFF000000L);
        ByteUtil.writeIntLong(b, ipBytesLength * 2, mix);

        return b;
    }
}
