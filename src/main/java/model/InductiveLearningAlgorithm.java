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
    private Map<String, List<String[]>> dataSubTables;
    private final List<String> attributeNames;
    private final Map<String, Integer> attributeNamesMap;
    private List<Pair<List<Pair<String, String>>, String>> rules;
    private Map<String, Map<String, Double>> confusionTable;

    public InductiveLearningAlgorithm(Data data, int foldNumber) {
        this.data = data;
        this.attributeNames = data.getAttributeNames();
        Data.Crosvalidator crosvalidator = data.createCrosvalidator(Params.numberFolds, foldNumber);
        this.rules = new ArrayList<>();
        this.trainingData = crosvalidator.getTrainingData();
        this.testData = crosvalidator.getTestData();
        this.dataSubTables = partitionTrainingData();
        this.attributeNamesMap = new HashMap<>();

        for (int i = 0; i < attributeNames.size(); i++) {
            this.attributeNamesMap.put(attributeNames.get(i), i);
        }
        extractRules();
    }

    public Map<String, Double> doScoring() {
        List<Map<String, Double>> scores = new ArrayList<>();

        //Accuracy
        for (Map.Entry<String, Map<String, Double>> classConfusionTable : confusionTable.entrySet()) {
            Double tp = classConfusionTable.getValue().get("TP");
            Double tn = classConfusionTable.getValue().get("TN");
            Double fp = classConfusionTable.getValue().get("FP");
            Double fn = classConfusionTable.getValue().get("FN");
            Map<String, Double> score = new HashMap<>();
            score.put("ACC", (tp + tn) / (tp + tn + fp + fn));
            score.put("PREC", tp / (tp + fp));
            score.put("REC", tp / (tp + fn));
            score.put("FSCR", 2 * tp / (2 * tp + fp + fn));
            scores.add(score);
        }

        return scores.stream().flatMap(m -> m.entrySet().stream())
                //.filter(e -> e.getValue() != Double.NaN)
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.averagingDouble(Map.Entry::getValue)));
    }

    private Map<String, List<String[]>> partitionTrainingData() {

        int arrayLength = this.trainingData[0].length;

        return Arrays.stream(this.trainingData).collect(Collectors.groupingBy(a -> a[arrayLength - 1]));
    }

    private void run() {
        Map<String, Map<String, Double>> confusionMatrix = makeEmptyConfusionMatrix(); //Predicted <Real, Count>

        for (int rowIdx = 0; rowIdx < testData.length; rowIdx++) {

            String predicted = "";
            for (int colIdx = 0; colIdx < testData[0].length - 1; colIdx++) {
                String attr = testData[rowIdx][colIdx];

                //predicted =

            }

            String real = testData[rowIdx][testData[0].length - 1];
            confusionMatrix.get(predicted).compute(real, (k, v) -> v + 1.0d);
        }

        Map<String, Map<String, Double>> emptyConfusionTable = makeEmptyConfusionTable();
        for (String className : data.getClassNames()) {
            Map<String, Double> classMap = emptyConfusionTable.get(className);
            classMap.compute("TP", (k, v) -> v + confusionMatrix.get(className).get(className));
            classMap.compute("FP", (k, v) -> v + confusionMatrix.get(className)
                    .entrySet().stream()
                    .filter(e -> !e.getKey().equals(className))
                    .map(Map.Entry::getValue)
                    .reduce(Double::sum).orElse(0.0d));
            classMap.compute("FN", (k, v) -> v + confusionMatrix.entrySet().stream()
                    .filter(e -> !e.getKey().equals(className))
                    .map(Map.Entry::getValue)
                    .map(m -> m.get(className))
                    .reduce(Double::sum).orElse(0.0d));
            classMap.compute("TN", (k, v) -> v + confusionMatrix.entrySet().stream()
                    .filter(e -> !e.getKey().equals(className))
                    .map(Map.Entry::getValue)
                    .flatMap(m -> m.entrySet().stream())
                    .filter(e -> !e.getKey().equals(className))
                    .map(Map.Entry::getValue)
                    .reduce(Double::sum).orElse(0.0d));
        }
        this.confusionTable = emptyConfusionTable;
    }

    private String matchesRule(String[] row, Pair<List<Pair<String, String>>, String> rule){

        return null;
    }

    private void extractRules() {

        for (List<String[]> currentSubtable : dataSubTables.values()) {
            List<List<String[]>> otherSubtables = dataSubTables.values().stream().filter(s -> s != currentSubtable).collect(Collectors.toList());
            processSubTable(new LinkedList<>(currentSubtable), otherSubtables);
        }

        rules.sort(Comparator.comparing(r->r.getLeft().size()));
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

            if (maxCombination == null) {
                attrCombCount++;

                if (attrCombCount > attributeNames.size() - 1)
                    break;
                else
                    continue;
            }

            this.rules.add(createRule(maxCombination));
            removeClassifiedRows(maxCombination, currentSubtable);

            if (currentSubtable.isEmpty()) {
                break;
            }
        }
    }


    private void removeClassifiedRows(Pair<int[], String[]> maxCombination, List<String[]> currentSubtable) {

        String[] attributeCombination = makeAttributeCombination(maxCombination);

        currentSubtable.removeIf(r -> Arrays.deepEquals(attributeCombination, makeAttributeCombination(maxCombination.getLeft(), r)));
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

    private Map<String, Map<String, Double>> makeEmptyConfusionMatrix() {

        Map<String, Map<String, Double>> confusionMatrix = new HashMap<>();
        for (String className : data.getClassNames()) {
            Map<String, Double> map = new HashMap<>();
            for (String className2 : data.getClassNames()) {
                map.put(className2, 0.0d);
            }
            confusionMatrix.put(className, map);
        }
        return confusionMatrix;
    }

    private Map<String, Map<String, Double>> makeEmptyConfusionTable() {

        Map<String, Map<String, Double>> confusionMatrix = new HashMap<>();
        for (String className : data.getClassNames()) {
            Map<String, Double> map = new HashMap<>();
            map.put("TP", 0.0d);
            map.put("FP", 0.0d);
            map.put("TN", 0.0d);
            map.put("FN", 0.0d);
            confusionMatrix.put(className, map);
        }
        return confusionMatrix;
    }


}
