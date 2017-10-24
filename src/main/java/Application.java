package main.java;

import main.java.data.Data;
import main.java.data.DataReader;
import main.java.model.Models;
import main.java.model.NaiveBayes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Application {

    public static void main(String[] args) {

        DataReader dataReader = new DataReader("iris.data", "iris.names");

        Data data = dataReader.readData();

        Models models = new Models();
        Map<String, Double> finalScore = models.runBayes(data);
        System.out.println(finalScore);
    }
}

