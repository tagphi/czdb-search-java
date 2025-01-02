package net.cz88.czdb;

import net.cz88.czdb.utils.ByteUtil;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MncCodeSearcher {
    private byte[] data;

    public MncCodeSearcher(String filePath) throws IOException {
        this(new FileInputStream(filePath));
    }

    public MncCodeSearcher(InputStream inputStream) throws IOException {
        List<byte[]> dataList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    byte[] ipBytes = new byte[16];
                    byte[] mccBytes = new byte[2];
                    byte[] mncBytes = new byte[2];
                    byte lengthByte = 0;

                    System.arraycopy(InetAddress.getByName(parts[0] + ".0").getAddress(), 0, ipBytes, 0, Math.min(InetAddress.getByName(parts[0]).getAddress().length, 16));
                    int mcc = Integer.parseInt(parts[1]);
                    int mnc = Integer.parseInt(parts[2]);
                    ByteUtil.write(mccBytes, 0, mcc, 2);
                    ByteUtil.write(mncBytes, 0, mnc, 2);

                    // Store lengths in the lengthByte
                    lengthByte |= (parts[2].length() & 0x0F) << 4; // High 4 bits for MNC length
                    lengthByte |= (parts[1].length() & 0x0F);      // Low 4 bits for MCC length

                    ByteBuffer buffer = ByteBuffer.allocate(21).order(ByteOrder.LITTLE_ENDIAN);
                    buffer.put(ipBytes);
                    buffer.put(mccBytes);
                    buffer.put(mncBytes);
                    buffer.put(lengthByte);

                    dataList.add(buffer.array());
                }
            }
        }

        this.data = new byte[dataList.size() * 21];
        for (int i = 0; i < dataList.size(); i++) {
            System.arraycopy(dataList.get(i), 0, this.data, i * 21, 21);
        }
    }

    public MncRecord binarySearch(String ip) throws UnknownHostException {
        // Convert the IP to the first IP of its C segment
        String[] ipParts = ip.split("\\.");
        ipParts[3] = "0";
        String cSegmentIp = String.join(".", ipParts);

        // Convert the C segment IP to bytes
        byte[] ipBytes = new byte[16];
        System.arraycopy(InetAddress.getByName(cSegmentIp).getAddress(), 0, ipBytes, 0, Math.min(InetAddress.getByName(cSegmentIp).getAddress().length, 16));

        int left = 0;
        int right = data.length / 21 - 1;

        while (left <= right) {
            int mid = (left + right) / 2;
            byte[] midBytes = Arrays.copyOfRange(data, mid * 21, mid * 21 + 16);

            int cmp = compareBytes(ipBytes, midBytes, 16);
            if (cmp == 0) {
                byte[] recordBytes = Arrays.copyOfRange(data, mid * 21, (mid + 1) * 21);
                return new MncRecord(recordBytes);
            } else if (cmp < 0) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return null;
    }

    private static int compareBytes(byte[] bytes1, byte[] bytes2, int length) {
        for (int i = 0; i < bytes1.length && i < bytes2.length && i < length; i++) {
            if (bytes1[i] * bytes2[i] > 0) {
                if (bytes1[i] < bytes2[i]) {
                    return -1;
                } else if (bytes1[i] > bytes2[i]) {
                    return 1;
                }
            } else if (bytes1[i] * bytes2[i] < 0) {
                if (bytes1[i] > 0) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (bytes1[i] * bytes2[i] == 0 && bytes1[i] + bytes2[i] != 0) {
                if (bytes1[i] == 0) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
        if (bytes1.length >= length && bytes2.length >= length) {
            return 0;
        } else {
            return Integer.compare(bytes1.length, bytes2.length);
        }
    }

    public class MncRecord {
        private String ip;
        private int mcc;
        private int mnc;
        private int mccLength;
        private int mncLength;

        public MncRecord(byte[] recordBytes) throws UnknownHostException {
            byte[] ipBytes = Arrays.copyOfRange(recordBytes, 0, 4);
            this.ip = InetAddress.getByAddress(ipBytes).getHostAddress();
            this.mcc = ByteUtil.getInt2(recordBytes, 16);
            this.mnc = ByteUtil.getInt2(recordBytes, 18);
            byte lengthByte = recordBytes[20];
            this.mccLength = lengthByte & 0x0F; // Low 4 bits
            this.mncLength = (lengthByte >> 4) & 0x0F; // High 4 bits
        }

        public String getIp() {
            return ip;
        }

        public String getMcc() {
            String mccString = String.valueOf(mcc);
            while (mccString.length() < mccLength) {
                mccString = "0" + mccString;
            }
            return mccString;
        }

        public String getMnc() {
            String mncString = String.valueOf(mnc);
            while (mncString.length() < mncLength) {
                mncString = "0" + mncString;
            }
            return mncString;
        }
    }

    public static void main(String[] args) {
        try {
            // Initialize MncCodeSearcher with a sample file path
            MncCodeSearcher searcher = new MncCodeSearcher("/Users/liucong/Downloads/maxmind_ipinfo_ipc_mnc.txt");

            // Perform a binary search for a sample IP
            MncCodeSearcher.MncRecord record = searcher.binarySearch("45.147.64.198");

            // Print the results
            if (record != null) {
                System.out.println("IP: " + record.getIp());
                System.out.println("MCC: " + record.getMcc());
                System.out.println("MNC: " + record.getMnc());
            } else {
                System.out.println("Record not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}