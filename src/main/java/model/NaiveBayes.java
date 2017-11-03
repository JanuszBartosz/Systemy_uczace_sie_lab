package main.java.model;

import main.java.Params;
import main.java.data.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:bartosz.janusz@fingo.pl">Bartosz Janusz - FINGO</a>
 */
public class NaiveBayes {

    private Map<String, Double> probAPriori;                //<Class, Probability>
    private List<Map<String, Map<String, Double>>> chances; // Columns <Attribute <Class, Probability>>
    private Map<String, Map<String, Double>> confusionTable;

    private final String[][] trainingData;
    private final String[][] testData;
    private final Data data;


    public NaiveBayes(Data data, int foldNumber) {
        this.data = data;
        Data.Crosvalidator crosvalidator = data.createCrosvalidator(Params.numberFolds, foldNumber);
        this.trainingData = crosvalidator.getTrainingData();
        this.testData = crosvalidator.getTestData();
        computeProbAPriori();
        computeChances();
        run();
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

    private void computeProbAPriori() {

        String[] classColumn = Arrays.stream(trainingData).map(a->a[a.length-1]).toArray(String[]::new);

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
                allOccurrences.get(colIdx).get(attr)
                        .compute(attrClass,
                                (k, v) -> v + 1.0d
                        );
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
