package net.cz88.czdb.utils;

import net.cz88.czdb.entity.HyperHeaderBlock;
import net.cz88.czdb.entity.DecryptedBlock;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * This class provides utility methods for decoding HyperHeaderBlock objects.
 */
public class HyperHeaderDecoder {

    /**
     * Reads data from an InputStream and deserializes it into a HyperHeaderBlock.
     * The method first reads the header bytes and extracts the version, clientId, and encryptedBlockSize.
     * Then it reads the encrypted bytes and decrypts them into a DecryptedBlock.
     * It checks if the clientId and expirationDate in the DecryptedBlock match the clientId and version in the HyperHeaderBlock.
     * If they do not match, it throws an exception.
     * If the expirationDate in the DecryptedBlock is less than the current date, it throws an exception.
     * Finally, it creates a new HyperHeaderBlock with the read and decrypted data and returns it.
     *
     * @param is The InputStream to read data from.
     * @param key The key used for decryption.
     * @return A HyperHeaderBlock deserialized from the read data.
     * @throws Exception If an error occurs during the decryption process, or if the clientId or expirationDate do not match the expected values.
     */
    public static HyperHeaderBlock decrypt(InputStream is, String key) throws Exception {
        byte[] headerBytes = new byte[HyperHeaderBlock.HEADER_SIZE];
        is.read(headerBytes);

        int version = (int)ByteUtil.getIntLong(headerBytes, 0);
        int clientId = (int)ByteUtil.getIntLong(headerBytes, 4);
        int encryptedBlockSize = (int)ByteUtil.getIntLong(headerBytes, 8);

        byte[] encryptedBytes = new byte[encryptedBlockSize];
        is.read(encryptedBytes);

        DecryptedBlock decryptedBlock = DecryptedBlock.decrypt(key, encryptedBytes);

        // Check if the clientId in the DecryptedBlock matches the clientId in the HyperHeaderBlock
        if (decryptedBlock.getClientId() != clientId) {
            throw new Exception("Wrong clientId");
        }

        // Check if the expirationDate in the DecryptedBlock is less than the current date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        int currentDate = Integer.parseInt(LocalDate.now().format(formatter));
        if (decryptedBlock.getExpirationDate() < currentDate) {
            throw new Exception("DB is expired");
        }

        HyperHeaderBlock hyperHeaderBlock = new HyperHeaderBlock();
        hyperHeaderBlock.setVersion(version);
        hyperHeaderBlock.setClientId(clientId);
        hyperHeaderBlock.setEncryptedBlockSize(encryptedBlockSize);
        hyperHeaderBlock.setDecryptedBlock(decryptedBlock);

        return hyperHeaderBlock;
    }
}