package net.cz88.czdb.entity;

import net.cz88.czdb.utils.ByteUtil;

/**
 * Represents the header block of a HyperHeader structure.
 * The HyperHeaderBlock class encapsulates the following information:
 * 1. version: A 4-byte long representing the version of the HyperHeaderBlock.
 * 2. clientId: A 4-byte long representing the client ID.
 * 3. encryptedBlockSize: A 4-byte integer representing the size of the encrypted data block.
 * 4. encryptedData: A byte array of size encryptedBlockSize, representing the encrypted data.
 * 5. random bytes: Some random bytes, the size of which is kept in the encryptedData.
 *
 * The memory structure of the HyperHeaderBlock is as follows:
 * |------------------|------------------|----------------------|---------------------------|-------------------|
 * | version (4 bytes)| clientId (4 bytes)| encryptedBlockSize (4 bytes) | encryptedData (variable length) | random bytes |
 * |------------------|------------------|----------------------|---------------------------|-------------------|
 *
 */
public class HyperHeaderBlock {
    public static final int HEADER_SIZE = 12;
    protected long version;
    protected long clientId;
    protected int encryptedBlockSize;
    protected byte[] encryptedData;
    protected DecryptedBlock decryptedBlock;

    /**
     * Gets the version of the HyperHeaderBlock.
     * @return The version of the HyperHeaderBlock.
     */
    public long getVersion() {
        return version;
    }

    /**
     * Sets the version of the HyperHeaderBlock.
     * @param version The version to set.
     */
    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * Gets the client ID of the HyperHeaderBlock.
     * @return The client ID of the HyperHeaderBlock.
     */
    public long getClientId() {
        return clientId;
    }

    /**
     * Sets the client ID of the HyperHeaderBlock.
     * @param clientId The client ID to set.
     */
    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    /**
     * Gets the size of the encrypted data block.
     * @return The size of the encrypted data block.
     */
    public long getEncryptedBlockSize() {
        return encryptedBlockSize;
    }

    /**
     * Sets the size of the encrypted data block.
     * @param encryptedBlockSize The size of the encrypted data block to set.
     */
    public void setEncryptedBlockSize(int encryptedBlockSize) {
        this.encryptedBlockSize = encryptedBlockSize;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(byte[] encryptedData) {
        this.encryptedData = encryptedData;
    }

    public DecryptedBlock getDecryptedBlock() {
        return decryptedBlock;
    }

    public void setDecryptedBlock(DecryptedBlock decryptedBlock) {
        this.decryptedBlock = decryptedBlock;
    }

    /**
     * Converts the HyperHeaderBlock instance into a byte array.
     * This method serializes the HyperHeaderBlock instance into a byte array, which can be used for storage or transmission.
     * The byte array is structured as follows:
     * - The first 4 bytes represent the version of the HyperHeaderBlock.
     * - The next 4 bytes represent the client ID.
     * - The following 4 bytes represent the length of the encrypted data.
     *
     * @return A byte array representing the serialized HyperHeaderBlock instance.
     */
    public byte[] toBytes() {
        byte[] bytes = new byte[12];
        ByteUtil.writeIntLong(bytes, 0, version);
        ByteUtil.writeIntLong(bytes, 4, clientId);
        ByteUtil.writeIntLong(bytes, 8, encryptedBlockSize);
        return bytes;
    }

    /**
     * Deserializes a HyperHeaderBlock instance from a 12 length byte array.
     * This method takes a byte array and constructs a HyperHeaderBlock instance from it.
     * The byte array is expected to be structured as follows:
     * - The first 4 bytes represent the version of the HyperHeaderBlock.
     * - The next 4 bytes represent the client ID.
     * - The following 4 bytes represent the length of the encrypted data.
     *
     * @param bytes The byte array to deserialize.
     * @return A HyperHeaderBlock instance constructed from the byte array.
     */
    public static HyperHeaderBlock fromBytes(byte[] bytes) {
        long version = ByteUtil.getIntLong(bytes, 0);
        long clientId = ByteUtil.getIntLong(bytes, 4);
        long encryptedBlockSize = ByteUtil.getIntLong(bytes, 8);

        HyperHeaderBlock headerBlock = new HyperHeaderBlock();
        headerBlock.setVersion(version);
        headerBlock.setClientId(clientId);
        headerBlock.setEncryptedBlockSize((int)encryptedBlockSize);

        return headerBlock;
    }

    /**
     * Returns the total size of the HyperHeaderBlock.
     * The size is calculated as the sum of the following:
     * - The size of the header (12 bytes)
     * - The size of the encrypted data block
     * - The size of the random bytes
     *
     * @return The total size of the HyperHeaderBlock in bytes.
     */
    public int getHeaderSize() {
        return 12 + encryptedBlockSize + decryptedBlock.getRandomSize();
    }
}