package net.cz88.czdb;

import java.util.Base64;

public class Decryptor {
    private final byte[] keyBytes;

    public Decryptor(String key) {
        this.keyBytes = Base64.getDecoder().decode(key);
    }

    public byte[] decrypt(byte[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ keyBytes[i % keyBytes.length]);
        }
        return result;
    }
}
