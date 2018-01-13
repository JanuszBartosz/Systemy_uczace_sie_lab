package main.java.model;

import main.java.data.Data;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class NaiveBayesBoostingEnsemble extends Model {

    private List<NaiveBayes> classifiers;
    private List<MutablePair<String[], Double>> weightedTrainingData;
    private double penalty = 1.0;

    NaiveBayesBoostingEnsemble(Data data, int foldNumber, int trainingDataSize, int classifiersNumber) {
        super(data, foldNumber);

        double initialWeight = 1.0;
        this.weightedTrainingData = Arrays.stream(trainingData)
                .map(s -> new MutablePair<>(s, initialWeight))
                .collect(Collectors.toList());

        trainingDataSize = trainingDataSize == 0 ? trainingData.length : trainingDataSize;
        this.classifiers = new ArrayList<>(classifiersNumber);

        makeClassifiers(trainingDataSize, classifiersNumber);
        run();
    }

    void makeClassifiers(int trainingDataSize, int classifiersNumber){

        for (int i = 0; i < classifiersNumber; i++) {

            List<MutablePair<String[], Double>> randomTrainingData = makeTrainingDataForBoosting(trainingDataSize);
            String[][] trainingDataArray = randomTrainingData.stream().map(Pair::getLeft).toArray(String[][]::new);

            classifiers.add(new NaiveBayes(data, trainingDataArray, this.testData));

            for (MutablePair<String[], Double> trainingObservation : weightedTrainingData) {
                String predicted = classifiers.get(i).predict(trainingObservation.getLeft());
                String real = trainingObservation.getLeft()[trainingObservation.getLeft().length - 1];
                if(!predicted.equals(real)){
                    trainingObservation.setRight(trainingObservation.getRight() + penalty);
                }
            }
        }
    }


    void run() {

        Map<String, Map<String, Double>> confusionMatrix = makeEmptyConfusionMatrix(); //Predicted <Real, Count>

        for (String[] observation : testData) {

            List<String> predictions = new ArrayList<>(classifiers.size());

            for (NaiveBayes classifier : classifiers) {
                predictions.add(classifier.predict(observation));
            }

            String predicted = predictions.stream()
                    .collect(Collectors.groupingBy(p -> p, Collectors.counting()))
                    .entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue()).get()
                    .getKey();

            String real = observation[testData[0].length - 1];
            confusionMatrix.get(predicted).compute(real, (k, v) -> v + 1.0d);
        }
        this.confusionTable = makeConfusionTable(confusionMatrix);
    }

    private List<MutablePair<String[], Double>> makeTrainingDataForBoosting(int dataSize) {

        Random random = new Random();

        List<MutablePair<String[], Double>> randomTrainingData = new ArrayList<>(dataSize);

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

        for (int i = 0; i < dataSize; i++) {
            randomTrainingData.add(weightedTrainingData.get(roulette.get(random.nextInt(roulette.size()))));
        }
        return randomTrainingData;
    }
}
