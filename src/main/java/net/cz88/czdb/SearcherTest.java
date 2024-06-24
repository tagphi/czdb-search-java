package net.cz88.czdb;

import net.cz88.czdb.exception.IpFormatException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

/**
 * This class is used to test the DbSearcher functionality.
 * It uses Apache Commons CLI for command line arguments parsing.
 */
public class SearcherTest {
    private static final Logger log = LoggerFactory.getLogger(SearcherTest.class);

    public static void benchmark(DbSearcher searcher, String ipFilePath) throws IOException, IpFormatException {
        BufferedReader reader = new BufferedReader(new FileReader(ipFilePath));
        int totalLines = 0;
        while (reader.readLine() != null) totalLines++;
        reader.close();

        reader = new BufferedReader(new FileReader(ipFilePath));
        String line;
        int totalQueries = 0;
        long totalTime = 0;
        Random random = new Random();

        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\t");
            if (parts.length < 2) {
                continue;
            }

            long startIp = ipToLong(parts[0]);
            long endIp = ipToLong(parts[1]);
            long randomIp = startIp + ((long) (random.nextDouble() * (endIp - startIp)));
            String randomIpStr = longToIp(randomIp);

            long startTime = System.nanoTime();

            try {
                String region = searcher.search(randomIpStr);
            } catch (Exception e ) {
                System.out.println("\nError while searching for IP: " + randomIpStr);
                System.err.println(e.getMessage());
                System.exit(1);
            }
            // Perform the query here
            long endTime = System.nanoTime();

            totalTime += (endTime - startTime);
            totalQueries++;

            // Display progress bar and percentage
            int progressPercentage = (int) ((totalQueries / (double) totalLines) * 100);
            System.out.print("\r[");
            for (int i = 0; i < progressPercentage; i += 2) {
                System.out.print("#");
            }
            for (int i = progressPercentage; i < 100; i += 2) {
                System.out.print(" ");
            }
            System.out.print("] " + progressPercentage + "%");
        }

        reader.close();

        double qps = totalQueries / (totalTime / 1e9);
        System.out.println("\nTotal queries: " + totalQueries);
        System.out.println("Total time: " + (totalTime / 1e9) + " seconds");
        System.out.println("Queries per second: " + qps);
    }

    private static long ipToLong(String ipString) {
        String[] parts = ipString.split("\\.");
        long ip = 0;
        for (int i = 0; i < 4; i++) {
            ip += Long.parseLong(parts[i]) << (24 - (8 * i));
        }
        return ip;
    }

    private static String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
    }

    /**
     * The main method which is the entry point of the application.
     * It takes command line arguments, parses them and uses them to create a DbSearcher object.
     * It then enters a loop where it waits for user input and uses the DbSearcher to perform queries.
     * If the user enters 'q', the loop is exited and the application ends.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Define command line options
        Options options = new Options();
        Option dbFilePathOption = new Option("d", "dbFilePath", true, "The path to the database file.");
        dbFilePathOption.setRequired(true);
        options.addOption(dbFilePathOption);

        Option queryTypeOption = new Option("t", "queryType", true, "The type of the query. The valid types are MEMORY, BINARY, BTREE.");
        queryTypeOption.setRequired(true);
        options.addOption(queryTypeOption);

        Option keyOption = new Option("k", "key", true, "The key used for decrypting the header block of the database file.");
        keyOption.setRequired(true);
        options.addOption(keyOption);

        Option benchmarkOption = new Option("b", "benchmark", false, "Perform benchmark test.");
        options.addOption(benchmarkOption);

        Option ipFilePathOption = new Option("i", "ipFilePath", true, "The path to the IP file for benchmark test.");
        options.addOption(ipFilePathOption);

        // Create a parser and formatter for command line options
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        // Parse command line options
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }

        // Get command line option values
        String dbFilePath = cmd.getOptionValue("dbFilePath");
        QueryType queryType;
        try {
            queryType = QueryType.valueOf(cmd.getOptionValue("queryType").toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid query type. The valid types are MEMORY, BINARY, BTREE.");
            return;
        }
        String key = cmd.getOptionValue("key");
        boolean doBenchmark = cmd.hasOption("benchmark");
        String ipFilePath = cmd.getOptionValue("ipFilePath");

        // Create a DbSearcher and perform benchmark test or interactive query
        try {
            DbSearcher searcher = new DbSearcher(dbFilePath, queryType, key);

            if (doBenchmark) {
                benchmark(searcher, ipFilePath);
            } else {
                interactiveQuery(searcher);
            }

            searcher.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void interactiveQuery(DbSearcher searcher) throws IOException, IpFormatException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input;

        System.out.println("Enter IP address for query, or 'q' to quit:");

        while (!(input = reader.readLine()).equals("q")) {
            try {
                String region = searcher.search(input);
                System.out.println(region);
            } catch (IpFormatException e) {
                System.out.println("Invalid IP address format.");
            }
        }
    }
}