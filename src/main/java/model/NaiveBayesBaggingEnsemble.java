package main.java.model;

import main.java.data.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class NaiveBayesBaggingEnsemble extends Model {


    private List<NaiveBayes> classifiers;

    NaiveBayesBaggingEnsemble(Data data, int foldNumber, double trainingDataSize, int classifiersNumber) {
        super(data, foldNumber);

        this.classifiers = new ArrayList<>(classifiersNumber);

        for (int i = 0; i < classifiersNumber; i++) {
            classifiers.add(new NaiveBayes(data, makeTrainingDataForBagging(trainingDataSize), this.testData));
        }

        run();
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

    private String[][] makeTrainingDataForBagging(double dataSize) {

        Random random = new Random();
        int trainingDataSize = (int) ((double)trainingData.length * dataSize);

        String[][] randomTrainingData = new String[trainingDataSize][trainingData[0].length];

        for (int i = 0; i < trainingDataSize; i++) {
            randomTrainingData[i] = trainingData[random.nextInt(trainingData.length)];
        }
        return randomTrainingData;
    }
}
