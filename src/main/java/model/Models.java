package main.java.model;

import main.java.Params;
import main.java.data.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Models {

    public Map<String, Double> runBayes(Data data) {

        List<Map<String, Double>> scores = new ArrayList<>();
        for (int foldNumber = 0; foldNumber < Params.numberFolds; foldNumber++) {
            scores.add(new NaiveBayes(data, foldNumber).doScoring());
        }

        return scores.stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.averagingDouble(Map.Entry::getValue)));
    }

    public Map<String, Double> runIla(Data data) {

        List<Map<String, Double>> scores = new ArrayList<>();
        for (int foldNumber = 0; foldNumber < Params.numberFolds; foldNumber++) {
            scores.add(new InductiveLearningAlgorithm(data, foldNumber).doScoring());
        }

        return scores.stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.averagingDouble(Map.Entry::getValue)));
    }

    public Map<String, Double> runKNN(Data data, int K, int distParam, KNearestNeighbours.VotingType type, boolean normalize) {

        List<Map<String, Double>> scores = new ArrayList<>();
        for (int foldNumber = 0; foldNumber < Params.numberFolds; foldNumber++) {
            scores.add(new KNearestNeighbours(data, foldNumber, K, distParam, type, normalize).doScoring());
        }
        return scores.stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.averagingDouble(Map.Entry::getValue)));
    }

    public Map<String, Double> runNaiveBayesBaggingEnsemble(Data data, int trainingDataSize, int classifiersNumber) {
        List<Map<String, Double>> scores = new ArrayList<>();
        for (int foldNumber = 0; foldNumber < Params.numberFolds; foldNumber++) {
            scores.add(new NaiveBayesBaggingEnsemble(data, foldNumber, trainingDataSize, classifiersNumber).doScoring());
        }

        return scores.stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.averagingDouble(Map.Entry::getValue)));
    }

    public Map<String, Double> runNaiveBayesBoostingEnsemble(Data data, int trainingDataSize, int classifiersNumber) {
        List<Map<String, Double>> scores = new ArrayList<>();
        for (int foldNumber = 0; foldNumber < Params.numberFolds; foldNumber++) {
            scores.add(new NaiveBayesBaggingEnsemble(data, foldNumber, trainingDataSize, classifiersNumber).doScoring());
        }

        return scores.stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.averagingDouble(Map.Entry::getValue)));
    }
}
