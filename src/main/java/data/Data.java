package main.java.data;

import java.util.LinkedList;
import java.util.List;

public class Data {

    private List<List<String>> dataSet;
    private Crosvalidator crosvalidator;

    public Data(List<List<String>> dataSet) {
        this.dataSet = dataSet;
    }

    public List<List<String>> getDataSet() {
        return dataSet;
    }

    public Crosvalidator createCrosvalidator(int numberChunks, int foldNumber){
        this.crosvalidator = new Crosvalidator(numberChunks, foldNumber);
        return this.crosvalidator;
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
