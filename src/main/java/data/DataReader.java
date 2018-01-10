package main.java.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataReader {

    private Path dataFilePath;
    private Path namesFilePath;

    public DataReader(String filePath) {
        Path directoryPath = Paths.get("data");
        this.dataFilePath = directoryPath.resolve(filePath + ".data");
        this.namesFilePath = directoryPath.resolve(filePath + ".names");
    }

    public Data readData() {
        List<List<String>> dataSet = readDataset();
        List<List<String>> dataNames = readDataNames();

        return new Data(dataSet, dataNames.get(0), dataNames.get(1), dataNames.get(2));
    }

    private List<List<String>> readDataset() {

        try (Stream<String> lines = Files.lines(dataFilePath)) {

            return lines
                    .filter(l -> !l.isEmpty())
                    .map(l -> Arrays.asList(l.split(",")))
                    .collect(Collectors.toCollection(LinkedList::new));
        } catch (IOException e) {
            Logger.getGlobal().throwing("DataReader", "readDataset()", e);
            return Collections.emptyList();
        }
    }

    private List<List<String>> readDataNames() {

        try (Stream<String> lines = Files.lines(namesFilePath)) {

            return lines
                    .filter(l -> !l.isEmpty())
                    .map(l -> Arrays.asList(l.split(",")))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Logger.getGlobal().throwing("DataReader", "readDataNames()", e);
            return Collections.emptyList();
        }
    }
}
