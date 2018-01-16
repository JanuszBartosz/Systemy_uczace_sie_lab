package main.java;

import main.java.data.Data;
import main.java.data.DataReader;
import main.java.data.discretizator.impl.DiscretizatorType;
import main.java.model.Models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Application {

    public static void main(String[] args) throws IOException {

        List<String> datasetFilenames = Arrays.asList("wine", "ecoli", "vertebral");

        //InductiveLearningAlgorithm ila = new InductiveLearningAlgorithm(new DataReader("ecoli").readData(), 1);

        Models models = new Models();

        Files.createDirectories(Paths.get("scores"));

        for (String filename : datasetFilenames) {

            Data data;
            List<Map<String, Double>> allScores = new ArrayList<>();

            Params.numberBins = 10;
            Params.type = DiscretizatorType.FREQUENCY;
            data = new DataReader(filename).readData();

            for (double trainingDataSize = 0.1; trainingDataSize <= 1.0; trainingDataSize += 0.1){
//            for (int cN = 1; cN < 20 ; cN++) {
                allScores.add(models.runNaiveBayesBoostingEnsemble(data, trainingDataSize, 10));
            }

            List<String> lines = new ArrayList<>();

            for (Map<String, Double> score : allScores) {
                lines.add(score.values().stream().map(Object::toString).collect(Collectors.joining(",")));
            }
            Files.deleteIfExists(Paths.get("scores", "boosting", filename, "trainingDataSize.txt"));
            Files.write(Paths.get("scores", "boosting", filename, "trainingDataSize.txt"), lines);
        }
    }
}

