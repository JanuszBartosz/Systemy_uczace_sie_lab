package main.java;

import main.java.data.Data;
import main.java.data.DataReader;

import java.util.List;

public class Application {

    public static void main(String[] args){

        DataReader dataReader = new DataReader("iris.data");

        List<List<String>> dataSet = dataReader.readFromFile();
        Data data = new Data(dataSet);

        Data.Crosvalidator crosvalidator = data.createCrosvalidator(10, 1);
        crosvalidator.getTestData();
        crosvalidator.getTrainingData();
        System.out.println("");
    }

}
