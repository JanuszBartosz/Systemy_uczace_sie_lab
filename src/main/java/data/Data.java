package main.java.data;

import cern.colt.matrix.ObjectFactory2D;
import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import main.java.data.discretizatorImpl.DiscretizatorFactory;
import main.java.data.discretizatorImpl.EqualFrequencyDiscretizator;
import main.java.data.discretizatorImpl.EqualWidthDiscretizator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Data {

    private final ObjectMatrix2D dataSet;
    private final List<String> attributeNames;
    private final List<String> attributeTypes;
    private final List<String> classNames;
    private Crosvalidator crosvalidator;

    public Data(List<List<String>> dataSet, List<String> attributeNames, List<String> attributeTypes, List<String> classNames) {
        String[][] dataSet2DArray = dataSet.stream().map(List::toArray).toArray(String[][]::new);
        this.dataSet = ObjectFactory2D.dense.make(dataSet2DArray);
        this.attributeNames = attributeNames;
        this.attributeTypes = attributeTypes;
        this.classNames = classNames;
        discretize();
    }

    public ObjectMatrix2D getDataSet() {
        return dataSet;
    }

    public Crosvalidator createCrosvalidator(int numberChunks, int foldNumber) {
        this.crosvalidator = new Crosvalidator(numberChunks, foldNumber);
        return this.crosvalidator;
    }

    public void discretize() {

        List<Integer> realColumns = new ArrayList<>();

        for (int i = 0; i < attributeTypes.size(); i++) {
            if (attributeTypes.get(i).equals("real"))
                realColumns.add(i);
        }

        for (Integer column : realColumns) {
            ObjectMatrix1D columnView = dataSet.viewColumn(column);
            columnView.assign(DiscretizatorFactory.getDiscretizator(columnView.toArray()));
        }
    }


    public Crosvalidator getCrosvalidator() {
        return crosvalidator;
    }


    public class Crosvalidator {

        private List<List<String>> crosvalidationDataSet;

        private ObjectMatrix2D[] trainingData;

        private ObjectMatrix2D testData;

        Crosvalidator(int numberChunks, int foldNumber) {

            //this.crosvalidationDataSet = new LinkedList<>(dataSet);
            int chunkSize = crosvalidationDataSet.size() / numberChunks;
            int trainingDataSize = (numberChunks - 1) * chunkSize;
            int testDataSize = crosvalidationDataSet.size() - trainingDataSize;

            this.testData = dataSet.viewPart(foldNumber * testDataSize, 0,testDataSize, dataSet.columns());
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
