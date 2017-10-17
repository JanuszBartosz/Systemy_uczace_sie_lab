package main.java.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Data {

    private final List<List<String>> dataSet;
    private final List<String> attributeNames;
    private final List<String> attributeTypes;
    private final List<String> classNames;
    private Crosvalidator crosvalidator;

    public Data(List<List<String>> dataSet, List<String> attributeNames, List<String> attributeTypes, List<String> classNames) {
        this.dataSet = dataSet;
        this.attributeNames = attributeNames;
        this.attributeTypes = attributeTypes;
        this.classNames = classNames;
    }

    public List<List<String>> getDataSet() {
        return dataSet;
    }

    public Crosvalidator createCrosvalidator(int numberChunks, int foldNumber) {
        this.crosvalidator = new Crosvalidator(numberChunks, foldNumber);
        return this.crosvalidator;
    }

    public void discretize(Discretizator disc) {

        List<Integer> realColumns = new ArrayList<>();

        for (int i = 0; i < attributeTypes.size(); i++) {
            if (attributeTypes.get(i).equals("real"))
                realColumns.add(i);
        }
        

        for (List<String> record : dataSet) {
            for (Integer i : realColumns) {

            }
        }
    }
}

    public Crosvalidator getCrosvalidator() {
        return crosvalidator;
    }


public class Crosvalidator {

    private List<List<String>> crosvalidationDataSet;

    private List<List<String>> trainingData;

    private List<List<String>> testData;

    Crosvalidator(int numberChunks, int foldNumber) {

        this.crosvalidationDataSet = new LinkedList<>(dataSet);
        int chunkSize = crosvalidationDataSet.size() / numberChunks;
        int trainingDataSize = (numberChunks - 1) * chunkSize;
        int testDataSize = crosvalidationDataSet.size() - trainingDataSize;
        List<List<String>> testDataSublist = crosvalidationDataSet.subList(foldNumber * testDataSize, foldNumber * testDataSize + testDataSize);
        this.testData = new LinkedList<>(testDataSublist);
        testDataSublist.clear();
        this.trainingData = crosvalidationDataSet;
    }

    public List<List<String>> getTrainingData() {
        return trainingData;
    }

    public List<List<String>> getTestData() {
        return testData;
    }
}
}
