package main.java.model;

import main.java.data.Data;
import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KNearestNeighbours extends Model {

    private long K;
    private double[][] trainingDataReal;
    private double[][] testDataReal;
    private Map<String, Double> classMapStringToDouble;
    private Map<Double, String> classMapDoubleToString;

    KNearestNeighbours(Data data, int foldNumber, long K) {
        super(data, foldNumber);
        this.K = K;
        this.classMapStringToDouble = data.getClassNames().stream().collect(Collectors.toMap(v -> v, v -> (double) data.getClassNames().indexOf(v)));
        this.classMapDoubleToString = data.getClassNames().stream().collect(Collectors.toMap(v -> (double) data.getClassNames().indexOf(v), v -> v));
        this.trainingDataReal = Arrays.stream(trainingData)
                .map(a -> Arrays.stream(a)
                        .mapToDouble(v -> classMapStringToDouble.getOrDefault(v, Double.parseDouble(v))).toArray())
                .toArray(double[][]::new);

        this.testDataReal = Arrays.stream(testData)
                .map(a -> Arrays.stream(a)
                        .mapToDouble(v -> classMapStringToDouble.getOrDefault(v, Double.parseDouble(v))).toArray())
                .toArray(double[][]::new);
    }

    void run() {
        Map<String, Map<String, Double>> confusionMatrix = makeEmptyConfusionMatrix(); //Predicted <Real, Count>

        for (double[] row : testDataReal) {
            Set<Pair<double[], Double>> set = new TreeSet<>(Comparator.comparing(Pair::getSecond));

            for (double[] trainingRow : trainingDataReal) {

                set.add(new Pair<>(trainingRow, euclideanDistance(row, trainingRow)));
            }

            Double prediction = set.stream()
                    .limit(K)
                    .map(v -> v.getFirst()[v.getFirst().length - 1])
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet()
                    .stream()
                    .max(Comparator.comparing(Map.Entry::getValue))
                    .orElseGet(null)
                    .getKey();

            String predictedClass = classMapDoubleToString.get(prediction);
            String real = classMapDoubleToString.get(row[testDataReal.length - 1]);
            confusionMatrix.get(predictedClass).compute(real, (k, v) -> v + 1.0d);
        }
        this.confusionTable = makeConfusionTable(confusionMatrix);
    }

    private double euclideanDistance(double[] array1, double[] array2) {
        double sum = 0.0;
        for (int i = 0; i < array1.length - 1; i++) {
            sum += Math.pow((array1[i] - array2[i]), 2.0);
        }
        return Math.sqrt(sum);
    }

    private double manhattanDistance()
}
