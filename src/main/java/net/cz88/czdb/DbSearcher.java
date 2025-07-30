package net.cz88.czdb;

import net.cz88.czdb.constant.DbConstant;
import net.cz88.czdb.entity.DataBlock;
import net.cz88.czdb.entity.HyperHeaderBlock;
import net.cz88.czdb.entity.IndexBlock;
import net.cz88.czdb.exception.IpFormatException;
import net.cz88.czdb.utils.ByteUtil;
import net.cz88.czdb.utils.HyperHeaderDecoder;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;


/**
 * The DbSearcher class provides methods to search for data in a database.
 * It supports three types of search algorithms: memory, binary, and B-tree.
 * The type of the database (IPv4 or IPv6) and the type of the query (MEMORY, BINARY, BTREE) are determined at runtime.
 * The class also provides methods to initialize the search parameters based on the query type, and to get the region through the IP address.
 * The DbSearcher class uses a RandomAccessFile to read from and write to the database file.
 * For B-tree search, it uses a 2D byte array and an integer array to represent the start IP and the data pointer of each index block.
 * For memory and binary search, it uses a byte array to represent the original binary string of the database.
 * The class also provides a method to close the database.
 */
public class DbSearcher {
    // Enum representing the type of the database (IPv4 or IPv6)
    private DbType dbType;

    // Length of the IP bytes
    private int ipBytesLength;

    // Enum representing the type of the query (MEMORY, BINARY, BTREE)
    private final QueryType queryType;

    // Total size of the header block in the database
    private long totalHeaderBlockSize;

    /**
     * Handler for accessing the database file.
     * It is used to read from and write to the file.
     */
    private RandomAccessFile raf = null;

    /**
     * These are used only for B-tree search.
     * HeaderSip is a 2D byte array representing the start IP of each index block.
     * HeaderPtr is an integer array representing the data pointer of each index block.
     * headerLength is the number of index blocks in the header.
     */
    private byte[][] HeaderSip = null;
    private int[] HeaderPtr = null;
    private int headerLength;

    /**
     * These are used for memory and binary search.
     * firstIndexPtr is the pointer to the first index block.
     * lastIndexPtr is the pointer to the last index block.
     * totalIndexBlocks is the total number of index blocks.
     */
    private long firstIndexPtr = 0;
    private int totalIndexBlocks = 0;

    /**
     * This is used only for memory search.
     * It is the original binary string of the database.
     */
    private byte[] dbBinStr = null;

    private long columnSelection = 0;
    private byte[] geoMapData = null;

    /**
     * Constructor for DbSearcher class.
     * Initializes the DbSearcher instance based on the provided database file, query type, and key.
     * Depending on the query type, it calls the appropriate initialization method.
     *
     * @param dbFile The path to the database file.
     * @param queryType The type of the query (MEMORY, BINARY, BTREE).
     * @param key The key used for decrypting the header block of the database file.
     * @throws Exception If an error occurs during the decryption of the header block or the initialization of the RandomAccessFile.
     */
    public DbSearcher(String dbFile, QueryType queryType, String key) throws Exception {
        this.queryType = queryType;

        HyperHeaderBlock headerBlock;

        try (InputStream is = Files.newInputStream(Paths.get(dbFile))) {
            headerBlock = HyperHeaderDecoder.decrypt(is, key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        raf = new Cz88RandomAccessFile(dbFile, "r", headerBlock.getHeaderSize());

        // set db type
        raf.seek(0);
        byte[] superBytes = new byte[DbConstant.SUPER_PART_LENGTH];
        raf.readFully(superBytes, 0, superBytes.length);
        dbType = (superBytes[0] & 1) == 0 ? DbType.IPV4 : DbType.IPV6;
        ipBytesLength = dbType == DbType.IPV4 ? 4 : 16;

        // load geo setting
        loadGeoSetting(raf, key);

        if (queryType == QueryType.MEMORY) {
            initializeForMemorySearch();
        } else if (queryType == QueryType.BTREE) {
            initBtreeModeParam(raf);
        }
    }

    public DbSearcher(InputStream is, QueryType queryType, String key) throws Exception {
        if (queryType != QueryType.MEMORY) {
            throw new UnsupportedOperationException("input stream initialize only support memory mode");
        }

        this.queryType = queryType;
        HyperHeaderBlock headerBlock = HyperHeaderDecoder.decrypt(is, key);
        int rdSize = headerBlock.getDecryptedBlock().getRandomSize();

        // skip rdSize bytes
        is.skip(rdSize);

        try {
            // load all bytes from is to a byte[] buffer
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = is.read(b)) != -1) {
                buffer.write(b, 0, n);
            }

            dbBinStr = buffer.toByteArray();
            loadGeoSetting(dbBinStr, key);
            initMemoryOrBinaryModeParam(dbBinStr, dbBinStr.length);
        } finally {
            is.close();
        }
    }

    private void loadGeoSetting(RandomAccessFile raf, String key) {
        // set position to end index ptr + ip byte length * 2 + 4
        try {
            raf.seek(DbConstant.END_INDEX_PTR);
            byte[] data = new byte[4];
            raf.readFully(data);

            long endIndexPtr = ByteUtil.getIntLong(data, 0);

            long columnSelectionPtr = endIndexPtr + IndexBlock.getIndexBlockLength(dbType);
            raf.seek(columnSelectionPtr);
            raf.readFully(data);

            this.columnSelection = ByteUtil.getIntLong(data, 0);

            // not geo mapping
            if (columnSelection == 0) {
                return;
            }

            long geoMapPtr = columnSelectionPtr + 4;
            raf.seek(geoMapPtr);
            raf.readFully(data);
            int geoMapSize = (int)ByteUtil.getIntLong(data, 0);

            raf.seek(geoMapPtr + 4);
            geoMapData = new byte[geoMapSize];
            raf.readFully(geoMapData);

            Decryptor decryptor = new Decryptor(key);
            geoMapData = decryptor.decrypt(geoMapData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadGeoSetting(byte[] dbBytes, String key) {
        ByteBuffer buffer = ByteBuffer.wrap(dbBytes);

        // Set ipBytesLength
        byte[] superBytes = new byte[DbConstant.SUPER_PART_LENGTH];
        buffer.get(superBytes);
        dbType = (superBytes[0] & 1) == 0 ? DbType.IPV4 : DbType.IPV6;
        ipBytesLength = dbType == DbType.IPV4 ? 4 : 16;

        ((Buffer)buffer).position(DbConstant.END_INDEX_PTR);
        byte[] data = new byte[4];
        buffer.get(data);

        long endIndexPtr = ByteUtil.getIntLong(data, 0);

        long columnSelectionPtr = endIndexPtr + IndexBlock.getIndexBlockLength(dbType);
        ((Buffer)buffer).position((int) columnSelectionPtr);
        buffer.get(data);

        this.columnSelection = ByteUtil.getIntLong(data, 0);

        // not geo mapping
        if (columnSelection == 0) {
            return;
        }

        long geoMapPtr = columnSelectionPtr + 4;
        ((Buffer)buffer).position((int) geoMapPtr);
        buffer.get(data);
        int geoMapSize = (int)ByteUtil.getIntLong(data, 0);

        ((Buffer)buffer).position((int) geoMapPtr + 4);
        geoMapData = new byte[geoMapSize];
        buffer.get(geoMapData);

        Decryptor decryptor = new Decryptor(key);
        geoMapData = decryptor.decrypt(geoMapData);
    }

    /**
     * Initializes the DbSearcher instance for memory search.
     * Reads the entire database file into memory and then initializes the parameters for memory or binary search.
     *
     * @throws IOException If an error occurs during reading from the database file.
     */
    private void initializeForMemorySearch() throws IOException {
        dbBinStr = new byte[(int) raf.length()];
        raf.seek(0L);
        raf.readFully(dbBinStr, 0, dbBinStr.length);
        raf.close();
        initMemoryOrBinaryModeParam(dbBinStr, dbBinStr.length);
    }

    private void initMemoryOrBinaryModeParam(byte[] bytes, long fileSize) {
        totalHeaderBlockSize = ByteUtil.getIntLong(bytes, DbConstant.HEADER_BLOCK_PTR);
        long fileSizeInFile = ByteUtil.getIntLong(bytes, DbConstant.FILE_SIZE_PTR);
        if (fileSizeInFile != fileSize) {
            throw new RuntimeException(String.format("db file size error, excepted [%s], real [%s]", fileSizeInFile, fileSize));
        }
        firstIndexPtr = ByteUtil.getIntLong(bytes, DbConstant.FIRST_INDEX_PTR);
        long lastIndexPtr = ByteUtil.getIntLong(bytes, DbConstant.END_INDEX_PTR);
        totalIndexBlocks = (int) ((lastIndexPtr - firstIndexPtr) / IndexBlock.getIndexBlockLength(dbType)) + 1;

        byte[] b = new byte[(int) totalHeaderBlockSize];
        System.arraycopy(bytes, DbConstant.SUPER_PART_LENGTH, b, 0, b.length);
        initHeaderBlock(b);
    }

    private void initBtreeModeParam(RandomAccessFile raf) throws IOException {
        // set db type
        raf.seek(0);
        byte[] superBytes = new byte[DbConstant.SUPER_PART_LENGTH];
        raf.readFully(superBytes, 0, superBytes.length);
        totalHeaderBlockSize = ByteUtil.getIntLong(superBytes, DbConstant.HEADER_BLOCK_PTR);

        long fileSizeInFile = ByteUtil.getIntLong(superBytes, DbConstant.FILE_SIZE_PTR);
        long realFileSize = raf.length();

        if (fileSizeInFile != realFileSize) {
            throw new RuntimeException(String.format("db file size error, excepted [%s], real [%s]", fileSizeInFile, realFileSize));
        }

        byte[] b = new byte[(int) totalHeaderBlockSize];
        raf.readFully(b, 0, b.length);

        initHeaderBlock(b);
    }

    private void initHeaderBlock(byte[] headerBytes) {
        int indexLength = 20;

        int len = headerBytes.length / indexLength, idx = 0;
        HeaderSip = new byte[len][16];
        HeaderPtr = new int[len];
        long dataPtr;
        for (int i = 0; i < headerBytes.length; i += indexLength) {
            dataPtr = ByteUtil.getIntLong(headerBytes, i + 16);
            if (dataPtr == 0) {
                break;
            }
            System.arraycopy(headerBytes, i, HeaderSip[idx], 0, 16);
            HeaderPtr[idx] = (int) dataPtr;
            idx++;
        }
        headerLength = idx;
    }

    /**
     * This method is used to search for a region in the database based on the provided IP address.
     * It supports three types of search algorithms: memory, binary, and B-tree.
     * The type of the search algorithm is determined by the queryType attribute of the DbSearcher instance.
     * The method first converts the IP address to a byte array, then performs the search based on the query type.
     * If the search is successful, it returns the region of the found data block.
     * If the search is unsuccessful, it returns null.
     *
     * @param ip The IP address to search for. It is a string in the standard IP address format.
     * @return The region of the found data block if the search is successful, null otherwise.
     * @throws IpFormatException If the provided IP address is not in the correct format.
     * @throws IOException If an I/O error occurs during the search.
     */
    public String search(String ip) throws IpFormatException, IOException {
        // Validate the IP address based on dbType
        validateIp(ip);

        // Convert the IP address to a byte array
        byte[] ipBytes = getIpBytes(ip);

        // The data block to be found
        DataBlock dataBlock = null;

        // Perform the search based on the query type
        switch (queryType) {
            case MEMORY:
                // Perform a memory search
                dataBlock = memorySearch(ipBytes);
                break;
            case BTREE:
                // Perform a B-tree search
                dataBlock = bTreeSearch(ipBytes);
                break;
            default:
                break;
        }

        // Return the region of the found data block if the search is successful, null otherwise
        if (dataBlock == null) {
            return null;
        } else {
            return dataBlock.getRegion(geoMapData, columnSelection);
        }
    }

    /**
     * This method performs a memory search to find a data block in the database based on the provided IP address.
     * It uses a binary search algorithm to search the index blocks and find the data.
     * If the search is successful, it returns the data block containing the region and the data pointer.
     * If the search is unsuccessful, it returns null.
     *
     * @param ip The IP address to search for. It is a byte array representing the IP address.
     * @return The data block containing the region and the data pointer if the search is successful, null otherwise.
     */
    private DataBlock memorySearch(byte[] ip) {
        // The length of an index block
        int blockLen = IndexBlock.getIndexBlockLength(this.dbType);

        // Use searchInHeader to get the search range
        int[] sptrNeptr = searchInHeader(ip);
        int sptr = sptrNeptr[0], eptr = sptrNeptr[1];

        if (sptr == 0) {
            return null;
        }

        // Calculate the number of index blocks in the search range
        // Initialize the search range
        int l = 0, h = (eptr - sptr) / blockLen - 1;

        // The start IP and end IP of the current index block
        byte[] sip = new byte[ipBytesLength], eip = new byte[ipBytesLength];

        // The data pointer of the found data block
        int dataPtr = 0;
        int dataLen = 0;

        // Perform a binary search on the index blocks
        while (l <= h) {
            int m = (l + h) >> 1;
            int p = (int) (sptr + m * blockLen);
            System.arraycopy(dbBinStr, p, sip, 0, ipBytesLength);
            System.arraycopy(dbBinStr, p + ipBytesLength, eip, 0, ipBytesLength);

            int cmpStart = compareBytes(ip, sip, ipBytesLength);
            int cmpEnd = compareBytes(ip, eip, ipBytesLength);

            // If the IP is less than the start IP, search the left half
            if (cmpStart >= 0 && cmpEnd <= 0) {
                // IP is in this block
                dataPtr = (int)ByteUtil.getIntLong(dbBinStr, p + ipBytesLength * 2);
                dataLen = ByteUtil.getInt1(dbBinStr, p + ipBytesLength * 2 + 4);
                break;
            } else if (cmpStart < 0) {
                // IP is less than this block, search in the left half
                h = m - 1;
            } else {
                // IP is greater than this block, search in the right half
                l = m + 1;
            }
        }

        //not matched
        if (dataPtr == 0) {
            return null;
        }

        // Get the region from the database binary string
        byte[] region = new byte[dataLen];
        System.arraycopy(dbBinStr, dataPtr, region, 0, dataLen);

        // Return the data block containing the region and the data pointer
        return new DataBlock(region, dataPtr);
    }

    int[] searchInHeader(byte[] ip) {
        int l = 0, h = headerLength - 1, sptr = 0, eptr = 0;

        while (l <= h) {
            int m = (l + h) >> 1;
            int cmp = compareBytes(ip, HeaderSip[m], ipBytesLength);

            if (cmp < 0) {
                h = m - 1;
            } else if (cmp > 0) {
                l = m + 1;
            } else {
                sptr = HeaderPtr[m > 0 ? m - 1 : m];
                eptr = HeaderPtr[m];
                break;
            }
        }

        // less than header range
        if (l == 0 && h <= 0) {
            return new int[]{0, 0};
        }

        if (l > h) {
            if (l < headerLength) {
                sptr = HeaderPtr[l - 1];
                eptr = HeaderPtr[l];
            } else if (h >= 0 && h + 1 < headerLength) {
                sptr = HeaderPtr[h];
                eptr = HeaderPtr[h + 1];
            } else { // search to last header line, possible in last index block
                sptr = HeaderPtr[headerLength - 1];
                int blockLen = IndexBlock.getIndexBlockLength(this.dbType);

                eptr = sptr + blockLen;
            }
        }

        return new int[]{sptr, eptr};
    }

    /**
     * get the region with a int ip address with b-tree algorithm
     *
     * @param ip
     * @throws IOException
     */
    private DataBlock bTreeSearch(byte[] ip) throws IOException {
        int[] sptrNeptr = searchInHeader(ip);
        int sptr = sptrNeptr[0], eptr = sptrNeptr[1];

        if (sptr == 0) {
            return null;
        }

        //2. search the index blocks to define the data
        int blockLen = eptr - sptr, blen = IndexBlock.getIndexBlockLength(this.dbType);

        //include the right border block
        byte[] iBuffer = new byte[blockLen + blen];
        raf.seek(sptr);
        raf.readFully(iBuffer, 0, iBuffer.length);

        int l = 0;
        int h = blockLen / blen - 1;
        byte[] sip = new byte[ipBytesLength], eip = new byte[ipBytesLength];

        int dataPtr = 0;
        int dataLen = 0;

        while (l <= h) {
            int m = (l + h) >> 1;
            int p = m * blen;
            System.arraycopy(iBuffer, p, sip, 0, ipBytesLength);
            System.arraycopy(iBuffer, p + ipBytesLength, eip, 0, ipBytesLength);

            int cmpStart = compareBytes(ip, sip, ipBytesLength);
            int cmpEnd = compareBytes(ip, eip, ipBytesLength);

            if (cmpStart >= 0 && cmpEnd <= 0) {
                // IP is in this block
                dataPtr = (int)ByteUtil.getIntLong(iBuffer, p + ipBytesLength * 2);
                dataLen = ByteUtil.getInt1(iBuffer, p + ipBytesLength * 2 + 4);

                break;
            } else if (cmpStart < 0) {
                // IP is less than this block, search in the left half
                h = m - 1;
            } else {
                // IP is greater than this block, search in the right half
                l = m + 1;
            }
        }

        //not matched
        if (dataPtr == 0) {
            return null;
        }

        //3. get the data
        raf.seek(dataPtr);
        byte[] region = new byte[dataLen];
        raf.readFully(region, 0, region.length);
        return new DataBlock(region, dataPtr);
    }

    /**
     * get by index ptr
     *
     * @param ptr
     * @throws IOException
     */
    private DataBlock getByIndexPtr(long ptr) throws IOException {
        raf.seek(ptr);
        byte[] buffer = new byte[36];
        raf.readFully(buffer, 0, buffer.length);
        long extra = ByteUtil.getIntLong(buffer, 32);

        int dataLen = (int) ((extra >> 24) & 0xFF);
        int dataPtr = (int) ((extra & 0x00FFFFFF));

        raf.seek(dataPtr);
        byte[] region = new byte[dataLen];
        raf.readFully(region, 0, region.length);

        return new DataBlock(region, dataPtr);
    }

    /**
     * get db type
     *
     * @return
     */
    public DbType getDbType() {
        return dbType;
    }

    /**
     * get query type
     *
     * @return
     */
    public QueryType getQueryType() {
        return queryType;
    }

    /**
     * close the db
     *
     * @throws IOException
     */
    public void close() {
        try {
            //let gc do its work
            HeaderSip = null;
            HeaderPtr = null;
            dbBinStr = null;

            if (raf != null) {
                raf.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getIpBytes(String ip) throws IpFormatException, RuntimeException {
        byte[] ipBytes;
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            ipBytes = inetAddress.getAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        if (ipBytes == null) {
            throw new IpFormatException(String.format("ip [%s] format error for %s", ip, dbType));
        }
        return ipBytes;
    }

    /**
     * This method compares two byte arrays up to a specified length.
     * It is used to compare IP addresses in byte array format.
     * The comparison is done byte by byte, and the method returns as soon as a difference is found.
     * If the bytes at the current position in both arrays are positive or negative, the method compares their values.
     * If the bytes at the current position in both arrays have different signs, the method considers the negative byte as larger.
     * If one of the bytes at the current position is zero and the other is not, the method considers the zero byte as smaller.
     * If the method has compared all bytes up to the specified length and found no differences, it compares the lengths of the byte arrays.
     * If the lengths are equal, the byte arrays are considered equal.
     * If one byte array is longer than the other, it is considered larger.
     *
     * @param bytes1 The first byte array to compare. It represents an IP address.
     * @param bytes2 The second byte array to compare. It represents an IP address.
     * @param length The number of bytes to compare in each byte array.
     * @return A negative integer if the first byte array is less than the second, zero if they are equal, or a positive integer if the first byte array is greater than the second.
     */
    private static int compareBytes(byte[] bytes1, byte[] bytes2, int length) {
        for (int i = 0; i < bytes1.length && i < bytes2.length && i < length; i++) {
            if (bytes1[i] * bytes2[i] > 0) {
                if (bytes1[i] < bytes2[i]) {
                    return -1;
                } else if (bytes1[i] > bytes2[i]) {
                    return 1;
                }
            } else if (bytes1[i] * bytes2[i] < 0) {
                // When the signs are different, the negative byte is considered larger
                if (bytes1[i] > 0) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (bytes1[i] * bytes2[i] == 0 && bytes1[i] + bytes2[i] != 0) {
                // When one byte is zero and the other is not, the zero byte is considered smaller
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

    private String byte2IpString(byte[] bytes) {
        try {
            InetAddress address = InetAddress.getByAddress(bytes);
            return address.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final String IPV6_PATTERN =
            "([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|" +      // 1:2:3:4:5:6:7:8
                    "([0-9a-fA-F]{1,4}:){1,7}:|" +                     // 1::                              1:2:3:4:5:6:7::
                    "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" +     // 1::8             1:2:3:4:5:6::8
                    "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|" + // 1::7:8         1:2:3:4:5::7:8
                    "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|" + // 1::6:7:8       1:2:3:4::6:7:8
                    "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|" + // 1::5:6:7:8     1:2:3::5:6:7:8
                    "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|" + // 1::4:5:6:7:8   1:2::4:5:6:7:8
                    "[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|" +   // 1::3:4:5:6:7:8  1::3:4:5:6:7:8
                    ":((:[0-9a-fA-F]{1,4}){1,7}|:)|" +                 // ::2:3:4:5:6:7:8 ::8
                    "fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|" + // fe80::7:8%eth0   fe80::7:8%1
                    "::(ffff(:0{1,4}){0,1}:){0,1}" +                   // ::255.255.255.255, ::ffff:0:255.255.255.255
                    "([0-9]{1,3}\\.){3,3}[0-9]{1,3}|" +                // IPv4 mapped IPv6 addresses
                    "([0-9a-fA-F]{1,4}:){1,4}:" +                      // 1:2:3:4:5:6:1.2.3.4
                    "([0-9]{1,3}\\.){3,3}[0-9]{1,3}";

    private static final Pattern V6_PATTERN = Pattern.compile("^(" + IPV6_PATTERN + ")$");

    private void validateIp(String ip) throws IllegalArgumentException {
        String ipv4Pattern = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        if ((dbType == DbType.IPV4 && !ip.matches(ipv4Pattern)) || (dbType == DbType.IPV6 && !V6_PATTERN.matcher(ip).matches())) {
            throw new IllegalArgumentException("Invalid IP address for the specified database type.");
        }
    }
}

