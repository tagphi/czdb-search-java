package net.cz88.czdb.entity;

/**
 * The DataBlock class represents a data block in the database.
 * It contains a region and a pointer to the data in the database file.
 * The memory structure of a DataBlock object is as follows:
 * +----------------+-----------+
 * | String         | int       |
 * +----------------+-----------+
 * | region         | dataPtr   |
 * +----------------+-----------+
 */
public class DataBlock {
    /**
     * The region of the data block.
     * It is a string representing the geographical region that the data block covers.
     */
    private String region;

    /**
     * The pointer to the data in the database file.
     * It is an integer representing the offset of the data from the start of the database file.
     */
    private int dataPtr;

    /**
     * Constructs a new DataBlock with the specified region and data pointer.
     *
     * @param region  the region of the data block
     * @param dataPtr the pointer to the data in the database file
     */
    public DataBlock(String region, int dataPtr) {
        this.region = region;
        this.dataPtr = dataPtr;
    }

    /**
     * Constructs a new DataBlock with the specified region and a default data pointer of 0.
     *
     * @param region the region of the data block
     */
    public DataBlock(String region) {
        this(region, 0);
    }

    /**
     * Returns the region of this data block.
     *
     * @return the region of this data block
     */
    public String getRegion() {
        return region;
    }

    /**
     * Sets the region of this data block to the specified value.
     *
     * @param region the new region
     * @return this data block
     */
    public DataBlock setRegion(String region) {
        this.region = region;
        return this;
    }

    /**
     * Returns the data pointer of this data block.
     *
     * @return the data pointer of this data block
     */
    public int getDataPtr() {
        return dataPtr;
    }

    /**
     * Sets the data pointer of this data block to the specified value.
     *
     * @param dataPtr the new data pointer
     * @return this data block
     */
    public DataBlock setDataPtr(int dataPtr) {
        this.dataPtr = dataPtr;
        return this;
    }

    /**
     * Returns a string representation of this data block, which is a concatenation of the region and data pointer, separated by a pipe character.
     *
     * @return a string representation of this data block
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(region).append('|').append(dataPtr);
        return sb.toString();
    }
}