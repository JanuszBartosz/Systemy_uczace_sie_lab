package main.java;

import main.java.data.DataReader;

import java.util.List;

public class Application {

    public static void main(String[] args){

        DataReader dataReader = new DataReader("iris.data");

        List<String> data = dataReader.readFromFile();

        System.out.println("");
    }

}
