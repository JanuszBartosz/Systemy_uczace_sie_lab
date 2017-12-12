package main.java.model;

import main.java.data.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.Combinations;

import java.util.*;
import java.util.stream.Collectors;

class InductiveLearningAlgorithm extends Model {


    private Map<String, List<String[]>> dataSubTables;
    private final List<String> attributeNames;
    private final Map<String, Integer> attributeNamesMap;
    private List<Pair<List<Pair<String, String>>, String>> rules;

    InductiveLearningAlgorithm(Data data, int foldNumber) {
        super(data, foldNumber);
        this.attributeNames = data.getAttributeNames();
        this.rules = new ArrayList<>();
        this.dataSubTables = partitionTrainingData();
        this.attributeNamesMap = new HashMap<>();
        for (int i = 0; i < attributeNames.size(); i++) {
            this.attributeNamesMap.put(attributeNames.get(i), i);
        }
        extractRules();
        run();
    }

    Map<String, Double> doScoring() {
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
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.averagingDouble(Map.Entry::getValue)));
    }

    private Map<String, List<String[]>> partitionTrainingData() {

        int arrayLength = this.trainingData[0].length;

        return Arrays.stream(this.trainingData).collect(Collectors.groupingBy(a -> a[arrayLength - 1]));
    }

    private void run() {
        Map<String, Map<String, Double>> confusionMatrix = makeEmptyConfusionMatrix(); //Predicted <Real, Count>

        for (String[] row : testData) {

            String predicted = "";

            for (Pair<List<Pair<String, String>>, String> rule : rules) {
                predicted = matchesRule(row, rule);
                if (predicted != null)
                    break;
            }

            //predicted = predicted == null ? data.mostCommonClass : predicted;

            String real = row[testData[0].length - 1];
            if (predicted != null)
                confusionMatrix.get(predicted).compute(real, (k, v) -> v + 1.0d);
        }

        this.confusionTable = makeConfusionTable(confusionMatrix);
    }

    private void extractRules() {

        for (List<String[]> currentSubtable : dataSubTables.values()) {
            List<List<String[]>> otherSubtables = dataSubTables.values().stream().filter(s -> s != currentSubtable).collect(Collectors.toList());
            processSubTable(new LinkedList<>(currentSubtable), otherSubtables);
        }

        //rules.sort(Comparator.comparing(r -> r.getLeft().size()));
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

    private String matchesRule(String[] row, Pair<List<Pair<String, String>>, String> rule) {

        boolean match = true;
        for (Pair<String, String> attribute : rule.getLeft()) {
            if (!attribute.getRight().equals(row[attributeNamesMap.get(attribute.getLeft())])) {
                match = false;
            }
        }

        return match ? rule.getRight() : null;
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
}
