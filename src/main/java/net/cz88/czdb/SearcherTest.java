package net.cz88.czdb;

import net.cz88.czdb.exception.IpFormatException;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This class is used to test the DbSearcher functionality.
 * It uses Apache Commons CLI for command line arguments parsing.
 */
public class SearcherTest {
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

        // Create a DbSearcher and enter query loop
        try {
            DbSearcher searcher = new DbSearcher(dbFilePath, queryType, key);

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

            searcher.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}