package main.java.model;

import main.java.Params;
import main.java.data.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.Combinations;

import java.util.*;
import java.util.stream.Collectors;

public class InductiveLearningAlgorithm {


    private final Data data;
    private final String[][] trainingData;
    private final String[][] testData;
    Map<String, List<String[]>> dataSubTables;
    private final List<String> attributeNames;
    private List<Pair<List<Pair<String, String>>, String>> rules;

    public InductiveLearningAlgorithm(Data data, int foldNumber) {
        this.data = data;
        this.attributeNames = data.getAttributeNames();
        Data.Crosvalidator crosvalidator = data.createCrosvalidator(Params.numberFolds, foldNumber);
        this.rules = new ArrayList<>();
        this.trainingData = crosvalidator.getTrainingData();
        this.testData = crosvalidator.getTestData();
        this.dataSubTables = partitionTrainingData();
        run();
    }

    Map<String, List<String[]>> partitionTrainingData() {

        int arrayLength = this.trainingData[0].length;

        return Arrays.stream(this.trainingData).collect(Collectors.groupingBy(a -> a[arrayLength - 1]));
    }

    private void run() {

        for (List<String[]> currentSubtable : dataSubTables.values()) {
            List<List<String[]>> otherSubtables = dataSubTables.values().stream().filter(s -> s != currentSubtable).collect(Collectors.toList());
            processSubTable(new LinkedList<>(currentSubtable), otherSubtables);
        }
    }

    private void processSubTable(List<String[]> currentSubtable, List<List<String[]>> otherSubtables) {

        int attrCombCount = 1;

        while (true) {
            Combinations subsets = new Combinations(attributeNames.size() - 1, attrCombCount);

            Pair<int[], String[]> maxCombination = null;

            for (int[] subset : subsets) {

                int maxOccurrences = 0;

                for (String[] row : currentSubtable) {
                    Pair<int[], String[]> currentCombination = new ImmutablePair<>(subset, row);
                    if (Arrays.deepEquals(makeAttributeCombination(currentCombination), makeAttributeCombination(maxCombination))) {
                        continue;
                    }
                    if (checkIfUnique(subset, row, otherSubtables)) {
                        int occurrences = countOccurrences(subset, row, currentSubtable);
                        if (occurrences > maxOccurrences) {
                            maxCombination = currentCombination;
                            maxOccurrences = occurrences;
                        }
                    }
                }
            }

            if (maxCombination != null) {
                this.rules.add(createRule(maxCombination));
                removeClassifiedRows(maxCombination, currentSubtable);
                break;
            }
            attrCombCount++;
        }
    }

    private void removeClassifiedRows(Pair<int[], String[]> maxCombination, List<String[]> currentSubtable) {

        currentSubtable.removeIf(maxCombination.getRight()::equals);
    }

    private Pair<List<Pair<String, String>>, String> createRule(Pair<int[], String[]> maxCombination) {

        List<Pair<String, String>> conjunction = new ArrayList<>();
        for (int i = 0; i < maxCombination.getLeft().length; i++) {
            conjunction.add(new ImmutablePair<>(
                    attributeNames.get(maxCombination.getLeft()[i]),
                    maxCombination.getRight()[maxCombination.getLeft()[i]]
            ));
        }

        return new ImmutablePair<>(
                conjunction,
                maxCombination.getRight()[maxCombination.getRight().length - 1]
        );
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

    private int countOccurrences(int[] attrSubset, String[] currentRow, List<String[]> currentSubtable) {

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


    private String[] makeAttributeCombination(int[] indexes, String[] array) {
        String[] subArray = new String[indexes.length];
        for (int i = 0; i < subArray.length; i++) {
            subArray[i] = array[indexes[i]];
        }
        return subArray;
    }

    private String[] makeAttributeCombination(Pair<int[], String[]> pair) {

        if (pair == null)
            return null;

        String[] subArray = new String[pair.getLeft().length];
        for (int i = 0; i < subArray.length; i++) {
            subArray[i] = pair.getRight()[pair.getLeft()[i]];
        }
        return subArray;
    }
}
