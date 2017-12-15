package main.java.model;

import main.java.Params;
import main.java.data.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Model {

    protected final Data data;
    final String[][] trainingData;
    final String[][] testData;
    Map<String, Map<String, Double>> confusionTable;

    Model(Data data, int foldNumber) {
        this.data = data;
        Data.Crosvalidator crosvalidator = data.createCrosvalidator(Params.numberFolds, foldNumber);
        this.trainingData = crosvalidator.getTrainingData();
        this.testData = crosvalidator.getTestData();
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
                //.filter(e -> e.getValue() != Double.NaN)
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.averagingDouble(Map.Entry::getValue)));
    }
    Map<String, Map<String, Double>> makeConfusionTable(Map<String, Map<String, Double>> confusionMatrix) {
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
        return emptyConfusionTable;
    }

    Map<String, Map<String, Double>> makeEmptyConfusionMatrix() {

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
