package net.cz88.czdb;

/**
 * The QueryType enum represents the different types of query modes available in the application.
 * It includes MEMORY, BINARY, and BTREE modes.
 */
public enum QueryType {
    /**
     * Represents the MEMORY mode.
     * This mode is thread-safe and stores the data in memory.
     */
    MEMORY,
    /**
     * Represents the BTREE mode.
     * This mode uses a B-tree data structure for querying.
     * It is not thread-safe. Different threads can use different query objects.
     * In case of high concurrency, it may lead to too many open files error.
     * In such cases, either increase the maximum allowed open files in the kernel (fs.file-max) or use the MEMORY mode.
     */
    BTREE
}