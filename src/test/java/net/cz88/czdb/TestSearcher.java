package net.cz88.czdb;

import org.junit.Test;
import net.cz88.czdb.exception.IpFormatException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestSearcher {
    private static final String IPV6_DB_PATH = "/Users/liucong/Downloads/RTBAsia/";
    private static final String IPV4_DB_PATH = "/Users/liucong/Downloads/RTBAsia/rtbasiav4.czdb";
    private static final String IPV6_IP = "2001:4:113:0:0:0:0:0";
    private static final String IPV4_IP = "14.9.15.0";
    private static final String KEY = "LDZ63Uu6DjwdaNdeDnC3iA==";

    @Test
    public void ipv6MemoryQueryTest() throws Exception {
        performQuery(IPV6_DB_PATH, IPV6_IP);
    }

    @Test
    public void ipv4MemoryQueryTest() throws Exception {
        performQuery(IPV4_DB_PATH, IPV4_IP);
    }

    @Test
    public void inputStreamSearcherTest() throws Exception {
        performQueryWithStream(IPV4_DB_PATH, IPV4_IP);
    }

    private void performQuery(String dbPath, String ip) throws Exception {
        performQuery(dbPath, ip, false);
    }

    private void performQueryWithStream(String dbPath, String ip) throws Exception {
        performQuery(dbPath, ip, true);
    }

    private void performQuery(String dbPath, String ip, boolean useStream) throws Exception {
        try {
            DbSearcher searcher;
            if (useStream) {
                searcher = new DbSearcher(Files.newInputStream(new File(dbPath).toPath()), QueryType.MEMORY, KEY);
            } else {
                searcher = new DbSearcher(dbPath, QueryType.BTREE, KEY);
            }
            double sTime = System.nanoTime();
            String region = searcher.search(ip);
            double cTime = (System.nanoTime() - sTime) / 1000000;
            System.out.printf("%s \nin %.5f millseconds\n", region, cTime);
            searcher.close();
        } catch (IOException | IpFormatException e) {
            e.printStackTrace();
        }
    }
}