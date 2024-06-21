package net.cz88.czdb;

import org.junit.Test;
import net.cz88.czdb.exception.IpFormatException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestSearcher {
    @Test
    public void ipv6MemoryQueryTest() throws Exception {
        performQuery("/Users/liucong/Downloads/ipv6.czdb", "2001:4:113:0:0:0:0:0");
    }

    @Test
    public void ipv4MemoryQueryTest() throws Exception {
        performQuery("/Users/liucong/Downloads/province/ipv4.czdb", "180.149.134.141");
    }

    @Test
    public void inputStreamSearcherTest() throws Exception {
        DbSearcher searcher = new DbSearcher(Files.newInputStream(new File("/Users/liucong/Downloads/province/ipv4.czdb").toPath()), QueryType.MEMORY, "NMWCI/IyXc/MI+Oc1lCc7A==");
        String region = searcher.search("180.149.134.141");
        System.out.println(region);
    }

    private void performQuery(String dbPath, String ip) throws Exception {
        try {
            DbSearcher searcher = new DbSearcher(dbPath, QueryType.MEMORY, "NMWCI/IyXc/MI+Oc1lCc7A==");
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