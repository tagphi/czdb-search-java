package net.cz88.czdb.entity;

import net.cz88.czdb.utils.ByteUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Represents a decrypted block of data before encryption, containing a client ID, expiration date, and the start pointer of an IP database.
 * The class provides functionality to serialize this data into a byte array and to encrypt it using AES encryption.
 * <p>
 *  * +----------------+----------------+----------------+----------------+
 *  * | clientId       | expirationDate |                |                |
 *  * | (12 bits)      | (20 bits)      |                |                |
 *  * +----------------+----------------+----------------+----------------+
 *  * | startPtr (32 bits)                                                  |
 *  * +--------------------------------------------------------------------+
 *  * | Reserved (64 bits)                                                 |
 *  * +--------------------------------------------------------------------+
 */
public class DecryptedBlock {
    // The client ID, occupying the first 12 bits of the 4-byte segment.
    private int clientId;
    // The expiration date, occupying the last 20 bits of the 4-byte segment, formatted as yyMMdd.
    private int expirationDate;
    // random bytes size
    private int randomSize;

    /**
     * Gets the client ID.
     * @return The client ID.
     */
    public int getClientId() {
        return clientId;
    }

    /**
     * Sets the client ID.
     * @param clientId The client ID to set.
     */
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    /**
     * Gets the expiration date.
     * @return The expiration date.
     */
    public int getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets the expiration date.
     * @param expirationDate The expiration date to set.
     */
    public void setExpirationDate(int expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Gets the size of the random bytes.
     * This method returns the size of the random bytes that are used in the encryption process.
     * The random bytes size is crucial for ensuring the uniqueness and security of the encryption.
     *
     * @return The size of the random bytes.
     */
    public int getRandomSize() {
        return randomSize;
    }

    /**
     * Sets the size of the random bytes.
     * This method allows setting the size of the random bytes that are to be used in the encryption process.
     * Adjusting the size of the random bytes can impact the security and performance of the encryption.
     *
     * @param randomSize The size of the random bytes to set.
     */
    public void setRandomSize(int randomSize) {
        this.randomSize = randomSize;
    }

    /**
     * Serializes the DecryptedBlock instance into a byte array.
     * The array is structured as follows: the first 4 bytes contain the client ID and expiration date,
     * the next 4 bytes contain the start pointer, and the last 8 bytes are reserved and initialized to 0.
     * @return A 16-byte array representing the serialized DecryptedBlock instance.
     */
    public byte[] toBytes() {
        byte[] b = new byte[16];
        ByteUtil.writeIntLong(b, 0, (this.clientId << 20) | this.expirationDate);
        ByteUtil.writeIntLong(b, 4, this.randomSize);
        // The reserved 8 bytes are already initialized to 0 by default.
        return b;
    }

    /**
     * Encrypts the provided byte array using AES encryption with a specified key.
     * The key is expected to be a base64 encoded string representing the AES key.
     * @param data The byte array to encrypt.
     * @param key The base64 encoded string representing the AES key.
     * @return The encrypted byte array.
     * @throws Exception If an error occurs during encryption.
     */
    public byte[] encrypt(byte[] data, String key) throws Exception {
        // Decode the base64 encoded key
        byte[] keyBytes = Base64.getDecoder().decode(key);

        // Initialize the Cipher with AES encryption
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        // Encrypt the data
        return cipher.doFinal(data);
    }

    public byte[] toEncryptedBytes(String key) throws Exception {
        return encrypt(toBytes(), key);
    }

    public static DecryptedBlock decrypt(String key, byte[] encryptedBytes) throws Exception {
        // Decode the base64 encoded key
        byte[] keyBytes = Base64.getDecoder().decode(key);

        // Initialize the Cipher with AES encryption
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        // Decrypt the data
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : decryptedBytes) {
            sb.append(String.format("%02X ", b));
        }
        System.out.println(sb.toString());

        // Parse the decrypted bytes
        DecryptedBlock decryptedBlock = new DecryptedBlock();
        decryptedBlock.setClientId((int) ByteUtil.getIntLong(decryptedBytes, 0) >> 20);
        decryptedBlock.setExpirationDate((int) ByteUtil.getIntLong(decryptedBytes, 0) & 0xFFFFF);
        decryptedBlock.setRandomSize((int) ByteUtil.getIntLong(decryptedBytes, 4));
        return decryptedBlock;
    }
}