package com.data_management;

import java.io.IOException;

/**
 * Interface for reading patient data into DataStorage.
 */
public interface DataReader {
    /**
     * Reads data from a specified source and stores it in the data storage.
     * 
     * @param dataStorage the storage where data will be stored
     * @throws IOException if there is an error reading the data
     */
    void readData(DataStorage dataStorage) throws IOException;
}
