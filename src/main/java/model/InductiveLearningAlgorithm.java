package main.java.model;

import main.java.Params;
import main.java.data.Data;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class InductiveLearningAlgorithm {


    private final Data data;
    private final String[][] trainingData;
    private final String[][] testData;

    public InductiveLearningAlgorithm(Data data, int foldNumber) {
        this.data = data;
        Data.Crosvalidator crosvalidator = data.createCrosvalidator(Params.numberFolds, foldNumber);
        this.trainingData = crosvalidator.getTrainingData();
        this.testData = crosvalidator.getTestData();
        partitionTrainingData();
    }

    Map<String, String[][]> partitionTrainingData() {

        Arrays.stream(this.trainingData).forEach(a->System.out.println(a));
        return null;
    }
}
