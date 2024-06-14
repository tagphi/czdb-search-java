package net.cz88.czdb;

import org.junit.Test;
import net.cz88.czdb.exception.IpFormatException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TestSearcher {
    @Test
    public void ipv6MemoryQueryTest() throws Exception {
        performQuery("/Users/liucong/Downloads/ipv6.czdb", "2001:4:113:0:0:0:0:0");
    }

    @Test
    public void ipv4MemoryQueryTest() throws Exception {
        performQuery("/Users/liucong/Downloads/offline_db.czdb", "180.164.101.20");
    }

    @Test
    public void inputStreamSearcherTest() throws Exception {
        DbSearcher searcher = new DbSearcher(new FileInputStream(new File("/Users/liucong/Downloads/offline_db.czdb")), QueryType.MEMORY, "3fEhuZUEvDzRjKv9qvAzTQ==");
        String region = searcher.search("180.164.101.20");
        System.out.println(region);
    }

    private void performQuery(String dbPath, String ip) throws Exception {
        try {
            DbSearcher searcher = new DbSearcher(dbPath, QueryType.BINARY, "3fEhuZUEvDzRjKv9qvAzTQ==");
            double sTime = System.nanoTime();
            String region = searcher.search(ip);
            double cTime = (System.nanoTime() - sTime) / 1000000;
            System.out.printf("%s in %.5f millseconds\n", region, cTime);
            searcher.close();
        } catch (IOException | IpFormatException e) {
            e.printStackTrace();
        }
    }
}