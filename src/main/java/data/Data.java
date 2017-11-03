package main.java.data;

import main.java.data.discretizator.impl.DiscretizatorFactory;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Data {

    private String[][] dataSet;
    private final List<String> attributeNames;
    private final List<String> attributeTypes;
    private final List<String> classNames;
    private Crosvalidator crosvalidator;

    public Data(List<List<String>> dataSet, List<String> attributeNames, List<String> attributeTypes, List<String> classNames) {
        Collections.shuffle(dataSet);
        this.dataSet = dataSet.stream()
                .map(List::toArray)
                .toArray(String[][]::new);
        this.attributeNames = attributeNames;
        this.attributeTypes = attributeTypes;
        this.classNames = classNames;
        discretize();
    }

    public String[][] getDataSet() {
        return dataSet;
    }

    public List<String> getClassNames() {
        return classNames;
    }

    public List<String> getAttributeNames() {
        return attributeNames;
    }

    public Crosvalidator createCrosvalidator(int numberFolds, int foldNumber) {
        this.crosvalidator = new Crosvalidator(numberFolds, foldNumber);
        return this.crosvalidator;
    }

    private void discretize() {

        List<Integer> realIdx = new ArrayList<>();

        for (int i = 0; i < attributeTypes.size(); i++) {
            if (attributeTypes.get(i).equals("real"))
                realIdx.add(i);
        }

        String [][] transposedDataSet = transpose(this.dataSet);

        for (Integer idx : realIdx) {

            transposedDataSet[idx] = Arrays.stream(transposedDataSet[idx]).map(DiscretizatorFactory.getDiscretizator(transposedDataSet[idx])).toArray(String[]::new);
        }

        this.dataSet = transpose(transposedDataSet);
    }

    private String[][] transpose(String[][] matrix) {

        String[][] transposedMatrix = new String[matrix[0].length][matrix.length];

        for (int row = 0; row < transposedMatrix.length; row++) {
            for (int col = 0; col < transposedMatrix[0].length; col++) {
                transposedMatrix[row][col] = matrix[col][row];
            }
        }
        return transposedMatrix;
    }


    public Crosvalidator getCrosvalidator() {
        return this.crosvalidator;
    }


    public class Crosvalidator {

        private String[][] trainingData;

        private String[][] testData;

        Crosvalidator(int numberChunks, int foldNumber) {

            int chunkSize = dataSet.length / numberChunks;
            int trainingDataSize = (numberChunks - 1) * chunkSize;
            int testDataSize = dataSet.length - trainingDataSize;
            int testDataBeginIndex = foldNumber * chunkSize;
            this.testData = Arrays.copyOfRange(dataSet, testDataBeginIndex, testDataBeginIndex + testDataSize);

            String[][] firstChunk = Arrays.copyOfRange(dataSet, 0, testDataBeginIndex);
            String[][] secondChunk = Arrays.copyOfRange(dataSet, testDataBeginIndex + testDataSize, dataSet.length);
            this.trainingData = ArrayUtils.addAll(firstChunk, secondChunk);
        }

        public String[][] getTrainingData() {
            return this.trainingData;
        }

        public String[][] getTestData() {
            return this.testData;
        }
    }
}
