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

    private Path filePath;
    private Path directoryPath = Paths.get("data");

    public DataReader(String filePath) {
        this.filePath = directoryPath.resolve(filePath);
    }

    public List<List<String>> readFromFile() {

        try (Stream<String> lines = Files.lines(filePath)) {

            return lines
                    .filter(l -> !l.isEmpty())
                    .map(l -> Arrays.asList(l.split(",")))
                    .collect(Collectors.toCollection(LinkedList::new));

        } catch (IOException e) {
            Logger.getGlobal().throwing("DataReader", "readFromFile()", e);
            return Collections.emptyList();
        }
    }
}
