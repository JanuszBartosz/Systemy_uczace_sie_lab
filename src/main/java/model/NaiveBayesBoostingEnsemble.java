package main.java.model;

import main.java.data.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class NaiveBayesBoostingEnsemble extends Model {

    private List<Pair<NaiveBayes, Double>> classifiers;
    private List<MutablePair<String[], Double>> weightedTrainingData;

    NaiveBayesBoostingEnsemble(Data data, int foldNumber, double trainingDataSize, int classifiersNumber) {
        super(data, foldNumber);

        double initialWeight = 1.0 / trainingData.length;
        this.weightedTrainingData = Arrays.stream(trainingData)
                .map(s -> new MutablePair<>(s, initialWeight))
                .collect(Collectors.toList());

        trainingDataSize = trainingDataSize == 0 ? trainingData.length : trainingDataSize;
        this.classifiers = new ArrayList<>(classifiersNumber);

        makeClassifiers(trainingDataSize, classifiersNumber);
        run();
    }

    void makeClassifiers(double trainingDataSize, int classifiersNumber) {

        List<Map<String, Double>> classifierScores = new ArrayList<>(classifiersNumber);
        List<NaiveBayes> tmpClassifiers = new ArrayList<>(classifiersNumber);
        for (int i = 0; i < classifiersNumber; i++) {

            List<MutablePair<String[], Double>> randomTrainingData = makeTrainingDataForBoosting(trainingDataSize);
            String[][] trainingDataArray = randomTrainingData.stream().map(Pair::getLeft).toArray(String[][]::new);

            NaiveBayes classifier = new NaiveBayes(data, trainingDataArray, this.testData);

            // Scoring classifier.
            classifier.run(weightedTrainingData);
            Map<String, Double> score = classifier.doScoring();
            Double averageScore = score.values().stream()
                    .filter(v -> !Double.isNaN(v))
                    .collect(Collectors.averagingDouble(v -> v));

            // Updating training data weights.
            double error = 1.0 - averageScore;
            double classifierWeight = Math.log((1.0 - error) / error);
            for (MutablePair<String[], Double> observation : weightedTrainingData) {
                String predicted = classifier.predict(observation.getLeft());
                String real = observation.getLeft()[observation.getLeft().length - 1];
                if (!predicted.equals(real)) {
                    observation.setRight(observation.getRight() * Math.exp(classifierWeight));
                }
            }
            classifiers.add(new ImmutablePair<>(classifier, classifierWeight));
        }

    }

    void run() {

        Map<String, Map<String, Double>> confusionMatrix = makeEmptyConfusionMatrix(); //Predicted <Real, Count>

        for (String[] observation : testData) {

            List<Pair<String, Double>> predictions = new ArrayList<>(classifiers.size());

            for (Pair<NaiveBayes, Double> classifier : classifiers) {
                predictions.add(new ImmutablePair<>(classifier.getLeft().predict(observation), classifier.getRight()));
            }

            String predicted = predictions.stream()
                    .collect(Collectors.groupingBy(Pair::getLeft, Collectors.summingDouble(Pair::getRight)))
                    .entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue()).get()
                    .getKey();

            String real = observation[testData[0].length - 1];
            confusionMatrix.get(predicted).compute(real, (k, v) -> v + 1.0d);
        }
        this.confusionTable = makeConfusionTable(confusionMatrix);
    }

    private List<MutablePair<String[], Double>> makeTrainingDataForBoosting(double dataSize) {

        Random random = new Random();
        int trainingDataSize = (int) ((double) trainingData.length * dataSize);
        List<MutablePair<String[], Double>> randomTrainingData = new ArrayList<>(trainingDataSize);

        int rouletteSize = 10000;
        List<Integer> roulette = new ArrayList<>(rouletteSize);

        double[] weights = weightedTrainingData.stream().mapToDouble(Pair::getRight).toArray();
        double sum = DoubleStream.of(weights).sum();
        int[] counts = DoubleStream.of(weights).map(w -> (w / sum) * rouletteSize).mapToInt(v -> (int) (v + 0.5d)).toArray();

        for (int i = 0; i < counts.length; i++) {
            for (int j = 0; j < counts[i]; j++) {
                roulette.add(i);
            }
        }

        for (int i = 0; i < trainingDataSize; i++) {
            randomTrainingData.add(weightedTrainingData.get(roulette.get(random.nextInt(roulette.size()))));
        }
        return randomTrainingData;
    }
}
