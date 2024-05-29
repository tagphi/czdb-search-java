package net.cz88.czdb;

import org.junit.Test;
import net.cz88.czdb.exception.IpFormatException;

import java.io.*;

/**
 * This class is used to test the search functionality of the DbSearcher class.
 * It contains two test methods, one for IPv4 and one for IPv6.
 * Each test method creates a DbSearcher instance, reads a file of IP addresses and regions,
 * and checks if the search results match the expected regions.
 * If a search result does not match the expected region, it writes the error to a log file.
 */
public class TestSearchAll {

    public static final String COLUMN_DIVIDER = ",";

    /**
     * This method tests the search functionality of the DbSearcher class for IPv6.
     * It reads a file of IPv6 addresses and regions, and checks if the search results match the expected regions.
     * If a search result does not match the expected region, it writes the error to a log file.
     */
    @Test
    public void testIpv6Db() {
        try {
            // Create a DbSearcher instance for IPv6
            DbSearcher _searcher = new DbSearcher("/Users/liucong/Downloads/ipv6.czdb", QueryType.MEMORY, "Mef4JXjVsTvGAeFj9Z06FQ==");

            // Read the file of IPv6 addresses and regions
            BufferedReader bfr = new BufferedReader(new FileReader("/Users/liucong/Downloads/cz_ipv6_reduce_region_10000.txt"));

            // Create a log file for errors
            BufferedWriter bwr = new BufferedWriter(new FileWriter("../../data/error_ipv6_log.txt", true));

            // Execute the test
            execute(_searcher, bwr, bfr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method tests the search functionality of the DbSearcher class for IPv4.
     * It reads a file of IPv4 addresses and regions, and checks if the search results match the expected regions.
     * If a search result does not match the expected region, it writes the error to a log file.
     */
    @Test
    public void testIpv4Db() {
        try {
            // Create a DbSearcher instance for IPv4
            DbSearcher _searcher = new DbSearcher("/Users/liucong/Downloads/ipv4.czdb", QueryType.MEMORY, "Mef4JXjVsTvGAeFj9Z06FQ==");

            // Read the file of IPv4 addresses and regions
            BufferedReader bfr = new BufferedReader(new FileReader("/Users/liucong/Downloads/cz_ipv4_reduce_region_10000.txt"));

            // Create a log file for errors
            BufferedWriter bwr = new BufferedWriter(new FileWriter("../../data/error_ipv4_log.txt", true));

            // Execute the test
            execute(_searcher, bwr, bfr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method executes the test for a given DbSearcher instance, log file, and file of IP addresses and regions.
     * It reads each line of the file, extracts the IP addresses and expected region, and checks if the search results match the expected region.
     * If a search result does not match the expected region, it writes the error to the log file.
     *
     * @param searcher The DbSearcher instance to use for the search.
     * @param bwr The BufferedWriter to use for writing errors to the log file.
     * @param bfr The BufferedReader to use for reading the file of IP addresses and regions.
     * @throws IOException If an I/O error occurs.
     */
    private static void execute(DbSearcher searcher, BufferedWriter bwr, BufferedReader bfr) throws IOException {
        // Initialize the error count and line count
        int errCount = 0;
        int lineCount = 0;

        // The current line of the file
        String str = null;

        // Read each line of the file
        while ((str = bfr.readLine()) != null) {
            // Extract the IP addresses and expected region from the line
            StringBuffer line = new StringBuffer(str);
            int first_idx = line.indexOf(COLUMN_DIVIDER);
            String first_ip = line.substring(0, first_idx);
            line = new StringBuffer(line.substring(first_idx + 1));
            int second_idx = line.indexOf(COLUMN_DIVIDER);
            String second_ip = line.substring(0, second_idx);
            String source_region = line.substring(second_idx + 1);

            // Search for the first IP and check if the result matches the expected region
            System.out.println("+---[Info]: Step1, search for first IP: " + first_ip);
            String fdata = null;
            try {
                fdata = searcher.search(first_ip);
            } catch (IpFormatException e) {
                e.printStackTrace();
            }
            if (!source_region.equalsIgnoreCase(fdata)) {
                // Write the error to the log file
                System.out.println("[Error]: Search first IP failed, DB region = " + fdata);
                bwr.write("[Source]: Region: " + fdata);
                bwr.newLine();
                bwr.write("[Source]: First Ip: " + first_ip);
                bwr.newLine();
                bwr.write("[DB]: Region: " + fdata);
                bwr.newLine();
                bwr.flush();
                errCount++;
            }

            // Search for the second IP and check if the result matches the expected region
            System.out.println("+---[Info]: Step2, search for second IP: " + second_ip);
            String sdata = null;
            try {
                sdata = searcher.search(second_ip);
            } catch (IpFormatException e) {
                e.printStackTrace();
            }
            if (!source_region.equalsIgnoreCase(sdata)) {
                // Write the error to the log file
                System.out.println("[Error]: Search second IP failed, DB region = " + sdata);
                bwr.write("[Source]: Region: " + sdata);
                bwr.newLine();
                bwr.write("[Source]: First Ip: " + second_ip);
                bwr.newLine();
                bwr.write("[DB]: Region: " + sdata);
                bwr.newLine();
                bwr.flush();
                errCount++;
            }

            // Increment the line count
            lineCount++;
        }

        // Close the log file and the file of IP addresses and regions
        bwr.close();
        bfr.close();

        // Print the test results
        System.out.println("+---Done, search complished");
        System.out.println("+---Statistics, Error count = " + errCount
                + ", Total line = " + lineCount
                + ", Fail ratio = " + ((float) (errCount / lineCount)) * 100 + "%");
    }
}