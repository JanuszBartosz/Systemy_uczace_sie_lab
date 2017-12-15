package main.java.model;

import main.java.data.Data;
import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class KNearestNeighbours extends Model {

    private int K;
    private double[][] trainingDataReal;
    private double[][] testDataReal;
    private Map<String, Double> classMapStringToDouble;
    private Map<Double, String> classMapDoubleToString;

    KNearestNeighbours(Data data, int foldNumber, int K, int distanceParam, VotingType type) {
        super(data, foldNumber);
        this.K = K;
        this.classMapStringToDouble = data.getClassNames().stream().collect(Collectors.toMap(v -> v, v -> (double) data.getClassNames().indexOf(v)));
        this.classMapDoubleToString = data.getClassNames().stream().collect(Collectors.toMap(v -> (double) data.getClassNames().indexOf(v), v -> v));
        String s = "pp";
        this.trainingDataReal = Arrays.stream(trainingData)
                .map(a -> Arrays.stream(a)
                        .mapToDouble(v -> {Double d = classMapStringToDouble.get(v);
                            return d == null ? Double.parseDouble(v): d;}).toArray())
                .toArray(double[][]::new);

        this.testDataReal = Arrays.stream(testData)
                .map(a -> Arrays.stream(a)
                        .mapToDouble(v -> {Double d = classMapStringToDouble.get(v);
                        return d == null ? Double.parseDouble(v): d;}).toArray())
                .toArray(double[][]::new);
        run(distanceParam, type);
    }

    void run(int distanceParam, VotingType type) {
        Map<String, Map<String, Double>> confusionMatrix = makeEmptyConfusionMatrix(); //Predicted <Real, Count>

        for (double[] row : testDataReal) {
            Set<Pair<double[], Double>> set = new TreeSet<>(Comparator.comparing(Pair::getSecond));

            for (double[] trainingRow : trainingDataReal) {

                set.add(new Pair<>(trainingRow, minkovskiDistance(row, trainingRow, distanceParam)));
            }

            Double prediction = performVoting(set, type);

            String predictedClass = classMapDoubleToString.get(prediction);
            String real = classMapDoubleToString.get(row[row.length - 1]);
            confusionMatrix.get(predictedClass).compute(real, (k, v) -> v + 1.0d);
        }
        this.confusionTable = makeConfusionTable(confusionMatrix);
    }

    private Double standardVoting(Set<Pair<double[], Double>> set) {
        return set.stream()
                .limit(K)
                .collect(Collectors.groupingBy(v -> v.getFirst()[v.getFirst().length - 1], Collectors.counting()))
                .entrySet()
                .stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .orElseGet(null)
                .getKey();
    }

    private Double weightedVoting(Set<Pair<double[], Double>> set) {

        return set.stream()
                .limit(K)
                .collect(Collectors.groupingBy(v -> v.getFirst()[v.getFirst().length - 1], Collectors.summingDouble(Pair::getSecond)))
                .entrySet()
                .stream()
                .min(Comparator.comparing(Map.Entry::getValue))
                .orElseGet(null)
                .getKey();
    }

    private Double rankingVoting(Set<Pair<double[], Double>> set) {
        List<Pair<double[], Double>> kNeighbours = set.stream().limit(K).collect(Collectors.toList());
        List<Pair<double[], Integer>> ranking = new ArrayList<>(K);
        for (int i = 0; i < kNeighbours.size(); i++) {
            ranking.add(new Pair<>(kNeighbours.get(i).getFirst(), K - i));
        }

        return ranking.stream()
                .collect(Collectors.groupingBy(v -> v.getFirst()[v.getFirst().length - 1], Collectors.summingInt(Pair::getSecond)))
                .entrySet()
                .stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .orElseGet(null)
                .getKey();
    }

    private double minkovskiDistance(double[] array1, double[] array2, int p) {
        double sum = 0.0;
        for (int i = 0; i < array1.length - 1; i++) {
            sum += Math.pow(Math.abs(array1[i] - array2[i]), p);
        }
        return nthroot(p, sum);
    }


    private double nthroot(int n, double A) {
        return nthroot(n, A, .001);
    }

    private double nthroot(int n, double A, double p) {
        if (A < 0) {
            System.err.println("A < 0");// we handle only real positive numbers
            return -1;
        } else if (A == 0) {
            return 0;
        }
        double x_prev = A;
        double x = A / n;  // starting "guessed" value...
        while (Math.abs(x - x_prev) > p) {
            x_prev = x;
            x = ((n - 1.0) * x + A / Math.pow(x, n - 1.0)) / n;
        }
        return x;
    }

    private Double performVoting(Set<Pair<double[], Double>> set, VotingType type) {
        switch (type) {
            case STANDARD:
                return standardVoting(set);
            case WEIGHTED:
                return weightedVoting(set);
            case RANKING:
                return rankingVoting(set);
            default:
                return standardVoting(set);
        }
    }

    public enum VotingType {
        STANDARD, WEIGHTED, RANKING;
    }
}
