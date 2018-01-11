package main.java.model;

import main.java.Params;
import main.java.data.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:bartosz.janusz@fingo.pl">Bartosz Janusz - FINGO</a>
 */
class NaiveBayes extends Model{

    private Map<String, Double> probAPriori;                //<Class, Probability>
    private List<Map<String, Map<String, Double>>> chances; // Columns <Attribute <Class, Probability>>

    NaiveBayes(Data data, int foldNumber) {
        super(data, foldNumber);
        computeProbAPriori();
        computeChances();
        run();
    }

    NaiveBayes(Data data, String[][] trainingData, String[][] testData){
        super(data, trainingData, testData);
        computeProbAPriori();
        computeChances();
    }

    private String predict(String[] observation){

        Map<String, Double> probability = makeEmptyClassMap(0.0d);

        for (int colIdx = 0; colIdx < observation.length - 1; colIdx++) {
            String attr = observation[colIdx];
            Map<String, Double> chance = chances.get(colIdx).get(attr);

            for (Map.Entry<String, Double> entry : chance.entrySet()) {
                probability.compute(entry.getKey(), (k, v) -> v + entry.getValue() * probAPriori.get(entry.getKey()));
            }
        }

        return Collections.max(probability.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private void run() {
        Map<String, Map<String, Double>> confusionMatrix = makeEmptyConfusionMatrix(); //Predicted <Real, Count>

        for (int rowIdx = 0; rowIdx < testData.length; rowIdx++) {
            Map<String, Double> probability = makeEmptyClassMap(0.0d);

            for (int colIdx = 0; colIdx < testData[0].length - 1; colIdx++) {
                String attr = testData[rowIdx][colIdx];
                Map<String, Double> chance = chances.get(colIdx).get(attr);

                for (Map.Entry<String, Double> entry : chance.entrySet()) {
                    probability.compute(entry.getKey(), (k, v) -> v + entry.getValue() * probAPriori.get(entry.getKey()));
                }
            }

            String predicted = Collections.max(probability.entrySet(), Map.Entry.comparingByValue()).getKey();
            String real = testData[rowIdx][testData[0].length - 1];
            confusionMatrix.get(predicted).compute(real, (k, v) -> v + 1.0d);
        }

        this.confusionTable = makeConfusionTable(confusionMatrix);
    }

    private void computeProbAPriori() {

        String[] classColumn = Arrays.stream(trainingData).map(a -> a[a.length - 1]).toArray(String[]::new);

        Map<String, Double> occurrences = new HashMap<>();

        for (String className : data.getClassNames()) {
            occurrences.put(className, 1.0);
        }

        for (String className : classColumn) {
            occurrences.replace(className, occurrences.get(className) + 1);
        }

        this.probAPriori = occurrences.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() / classColumn.length));
    }

    private void computeChances() {

        List<Map<String, Map<String, Double>>> allOccurrences = makeEmptyOccurrencesMap();

        for (int colIdx = 0; colIdx < trainingData[0].length - 1; colIdx++) {

            for (int rowIdx = 0; rowIdx < trainingData.length; rowIdx++) {

                String attr = trainingData[rowIdx][colIdx];
                String attrClass = trainingData[rowIdx][trainingData[0].length - 1];
                allOccurrences.get(colIdx).get(attr).compute(attrClass, (k, v) -> v + 1.0d);
            }

            for (Map<String, Double> occurrencesPerClass : allOccurrences.get(colIdx).values()) {
                Double sum = occurrencesPerClass.values().stream().reduce(Double::sum).orElse(0.0d);
                for (Map.Entry<String, Double> entry : occurrencesPerClass.entrySet()) {
                    occurrencesPerClass.compute(entry.getKey(), (k, v) -> v / sum);
                }
            }
        }
        this.chances = allOccurrences;
    }

    private List<Map<String, Map<String, Double>>> makeEmptyOccurrencesMap() {
        List<Map<String, Map<String, Double>>> allOccurrences = new ArrayList<>();

        for (int col = 0; col < data.getDataSet()[0].length - 1; col++) {
            allOccurrences.add(new HashMap<>());

            for (int row = 0; row < data.getDataSet().length; row++) {
                allOccurrences.get(col).putIfAbsent(data.getDataSet()[row][col], makeEmptyClassMap(1.0d));
            }
        }
        return allOccurrences;
    }

    private Map<String, Double> makeEmptyClassMap(double initialValue) {
        Map<String, Double> classMap = new HashMap<>();
        for (String className : data.getClassNames()) {
            classMap.put(className, initialValue);
        }
        return classMap;
    }
}
