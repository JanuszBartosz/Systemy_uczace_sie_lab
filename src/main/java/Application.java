package main.java;

import main.java.data.Data;
import main.java.data.DataReader;
import main.java.model.NaiveBayes;

import java.util.List;
import java.util.Map;

public class Application {

    public static void main(String[] args) {

        DataReader dataReader = new DataReader("iris.data", "iris.names");

        Data data = dataReader.readData();

        NaiveBayes naiveBayes = new NaiveBayes(data);
        Map<String, Double> score = naiveBayes.doScoring();
        System.out.println("");
    }
}
