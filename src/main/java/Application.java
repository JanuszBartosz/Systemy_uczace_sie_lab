package main.java;

import main.java.data.Data;
import main.java.data.DataReader;
import main.java.model.NaiveBayes;

import java.util.List;

public class Application {

    public static void main(String[] args) {

        DataReader dataReader = new DataReader("iris.data", "iris.names");

        Data data = dataReader.readData();

        Data.Crosvalidator crosvalidator = data.createCrosvalidator(10, 1);
        NaiveBayes naiveBayes = new NaiveBayes(crosvalidator.getTrainingData(), crosvalidator.getTestData());
        System.out.println("");
    }
}
