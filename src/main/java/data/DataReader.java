package main.java.data;

import jdk.internal.joptsimple.internal.Strings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataReader {

    Path filePath;
    Path directoryPath = Paths.get("data");

    public DataReader(String filePath){
        this.filePath = directoryPath.resolve(filePath);
    }
    public List<String> readFromFile(){

        try(Stream<String> lines = Files.lines(filePath)) {

            lines.map((l)->l.split(",")).collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
