package net.cz88.czdb.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Utility class for debugging IP addresses by converting byte arrays to readable strings
 */
public class IpDebugUtil {

    /**
     * Convert byte array to readable IP string (auto-detect IPv4/IPv6 based on length)
     * 
     * @param ipBytes byte array representing IP address
     * @return readable IP string or error message
     */
    public static String bytesToIpString(byte[] ipBytes) {
        if (ipBytes == null) {
            return "null";
        }
        
        if (ipBytes.length == 4) {
            return bytesToIpv4String(ipBytes);
        } else if (ipBytes.length == 16) {
            return bytesToIpv6String(ipBytes);
        } else {
            return String.format("Invalid IP length: %d bytes [%s]", 
                               ipBytes.length, 
                               bytesToHexString(ipBytes));
        }
    }

    /**
     * Convert 4-byte array to IPv4 string
     * 
     * @param ipv4Bytes 4-byte array representing IPv4 address
     * @return IPv4 string in dotted decimal notation
     */
    public static String bytesToIpv4String(byte[] ipv4Bytes) {
        if (ipv4Bytes == null || ipv4Bytes.length != 4) {
            return String.format("Invalid IPv4 bytes: %s", 
                               ipv4Bytes == null ? "null" : bytesToHexString(ipv4Bytes));
        }

        try {
            InetAddress addr = InetAddress.getByAddress(ipv4Bytes);
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            // Fallback to manual conversion
            return String.format("%d.%d.%d.%d",
                               ipv4Bytes[0] & 0xFF,
                               ipv4Bytes[1] & 0xFF,
                               ipv4Bytes[2] & 0xFF,
                               ipv4Bytes[3] & 0xFF);
        }
    }

    /**
     * Convert 16-byte array to IPv6 string
     * 
     * @param ipv6Bytes 16-byte array representing IPv6 address
     * @return IPv6 string in standard notation
     */
    public static String bytesToIpv6String(byte[] ipv6Bytes) {
        if (ipv6Bytes == null || ipv6Bytes.length != 16) {
            return String.format("Invalid IPv6 bytes: %s", 
                               ipv6Bytes == null ? "null" : bytesToHexString(ipv6Bytes));
        }

        try {
            InetAddress addr = InetAddress.getByAddress(ipv6Bytes);
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            // Fallback to manual conversion
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i += 2) {
                if (i > 0) {
                    sb.append(":");
                }
                int value = ((ipv6Bytes[i] & 0xFF) << 8) | (ipv6Bytes[i + 1] & 0xFF);
                sb.append(String.format("%x", value));
            }
            return sb.toString();
        }
    }

    /**
     * Convert byte array to hex string for debugging
     * 
     * @param bytes byte array
     * @return hex string representation
     */
    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(String.format("%02X", bytes[i] & 0xFF));
        }
        return sb.toString();
    }

    /**
     * Convert byte array to hex string with compact format
     * 
     * @param bytes byte array
     * @return compact hex string (no spaces)
     */
    public static String bytesToHexStringCompact(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }

    /**
     * Debug method to show detailed IP information
     * 
     * @param ipBytes byte array representing IP address
     * @return detailed debug string
     */
    public static String debugIpBytes(byte[] ipBytes) {
        if (ipBytes == null) {
            return "IP bytes: null";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("IP bytes (%d): [%s]\n", ipBytes.length, bytesToHexString(ipBytes)));
        sb.append(String.format("IP string: %s\n", bytesToIpString(ipBytes)));
        
        if (ipBytes.length == 4) {
            sb.append("Type: IPv4\n");
            sb.append(String.format("Decimal: %d.%d.%d.%d",
                                   ipBytes[0] & 0xFF,
                                   ipBytes[1] & 0xFF,
                                   ipBytes[2] & 0xFF,
                                   ipBytes[3] & 0xFF));
        } else if (ipBytes.length == 16) {
            sb.append("Type: IPv6\n");
            sb.append("Groups: ");
            for (int i = 0; i < 16; i += 2) {
                if (i > 0) {
                    sb.append(":");
                }
                int value = ((ipBytes[i] & 0xFF) << 8) | (ipBytes[i + 1] & 0xFF);
                sb.append(String.format("%04X", value));
            }
        } else {
            sb.append("Type: Invalid length");
        }
        
        return sb.toString();
    }

    /**
     * Compare two IP byte arrays and show differences
     * 
     * @param ip1 first IP byte array
     * @param ip2 second IP byte array
     * @return comparison result string
     */
    public static String compareIpBytes(byte[] ip1, byte[] ip2) {
        StringBuilder sb = new StringBuilder();
        sb.append("IP1: ").append(debugIpBytes(ip1)).append("\n");
        sb.append("IP2: ").append(debugIpBytes(ip2)).append("\n");
        
        if (ip1 == null || ip2 == null) {
            sb.append("Comparison: Cannot compare with null values");
            return sb.toString();
        }
        
        if (ip1.length != ip2.length) {
            sb.append(String.format("Comparison: Different lengths (%d vs %d)", ip1.length, ip2.length));
            return sb.toString();
        }
        
        boolean equal = true;
        sb.append("Byte-by-byte comparison:\n");
        for (int i = 0; i < ip1.length; i++) {
            boolean byteEqual = ip1[i] == ip2[i];
            if (!byteEqual) {
                equal = false;
            }
            sb.append(String.format("  [%2d]: %02X vs %02X %s\n", 
                                   i, 
                                   ip1[i] & 0xFF, 
                                   ip2[i] & 0xFF,
                                   byteEqual ? "✓" : "✗"));
        }
        
        sb.append("Result: ").append(equal ? "EQUAL" : "DIFFERENT");
        return sb.toString();
    }

    /**
     * Convert IP string to byte array (for testing purposes)
     * 
     * @param ipString IP address string
     * @return byte array representation
     */
    public static byte[] ipStringToBytes(String ipString) {
        try {
            InetAddress addr = InetAddress.getByName(ipString);
            return addr.getAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }
} 