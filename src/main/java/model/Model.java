package main.java.model;

import main.java.Params;
import main.java.data.Data;

import java.util.HashMap;
import java.util.Map;

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
