package main.java.model;

import main.java.Params;
import main.java.data.Data;
import org.apache.commons.math3.util.Combinations;

import java.util.*;
import java.util.stream.Collectors;

public class InductiveLearningAlgorithm {


    private final Data data;
    private final String[][] trainingData;
    private final String[][] testData;
    Map<String, List<String[]>> dataSubTables;
    private final List<String> attributeNames;

    public InductiveLearningAlgorithm(Data data, int foldNumber) {
        this.data = data;
        this.attributeNames = data.getAttributeNames();
        Data.Crosvalidator crosvalidator = data.createCrosvalidator(Params.numberFolds, foldNumber);
        this.trainingData = crosvalidator.getTrainingData();
        this.testData = crosvalidator.getTestData();
        this.dataSubTables = partitionTrainingData();
    }

    Map<String, List<String[]>> partitionTrainingData() {

        int arrayLength = this.trainingData[0].length;

        return Arrays.stream(this.trainingData).collect(Collectors.groupingBy(a -> a[arrayLength - 1]));
    }

    private void run() {

    }

    private void processSubTable(List<String[]> currentSubtable, List<List<String[]>> otherSubtables) {

        int attrCombCount = 1;
        boolean[] marked = new boolean[currentSubtable.size()];

        Combinations subsets = new Combinations(attributeNames.size(), attrCombCount);

        for (int[] subset : subsets) {

            for (String[] row : currentSubtable) {
                if (checkIfUnique(subset, row, otherSubtables)) {
                    countOccurences(subset, row, currentSubtable);
                }
            }
        }
    }


    private boolean checkIfUnique(int[] attrSubset, String[] currentRow, List<List<String[]>> otherSubtables) {

        for (List<String[]> otherSubtable : otherSubtables) {

            for (String[] row : otherSubtable) {

                boolean match = true;
                for (int attrIdx : attrSubset) {

                    if (!currentRow[attrIdx].equals(row[attrIdx])) {
                        match = false;
                    }
                }
                if (match)
                    return false;
            }
        }
        return true;
    }

    private int countOccurences(int[] attrSubset, String[] currentRow, List<String[]> currentSubtable) {

        int cnt = 0;
        for (String[] row : currentSubtable) {

            boolean match = true;
            for (int attrIdx : attrSubset) {
                if (!currentRow[attrIdx].equals(row[attrIdx])) {
                    match = false;
                }
            }
            if (match)
                cnt++;
        }
        return cnt;
    }


    private String[] getSubArrayByIndexes(String[] array, int[] indexes) {
        String[] subArray = new String[indexes.length];
        for (int i = 0; i < subArray.length; i++) {
            subArray[i] = array[indexes[i]];
        }
        return subArray;
    }
}
