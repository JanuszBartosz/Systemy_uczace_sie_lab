package main.java;

import main.java.data.Data;
import main.java.data.DataReader;
import main.java.data.discretizator.impl.DiscretizatorType;
import main.java.model.InductiveLearningAlgorithm;
import main.java.model.Models;
import main.java.model.NaiveBayes;

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

        List<String> datasetFilenames = Arrays.asList("wine");

        //InductiveLearningAlgorithm ila = new InductiveLearningAlgorithm(new DataReader("ecoli").readData(), 1);

        Models models = new Models();

        Files.createDirectories(Paths.get("scores"));

        for (String filename : datasetFilenames) {

            Data data;
            List<Map<String, Double>> allScores = new ArrayList<>();

            for (int numberBins = 2; numberBins <= 10; numberBins += 2) {
                Params.numberBins = numberBins;
                Params.type = DiscretizatorType.WIDTH;
                data = new DataReader(filename).readData();
                allScores.add(models.runBayes(data));
                Params.type = DiscretizatorType.FREQUENCY;
                data = new DataReader(filename).readData();
                allScores.add(models.runBayes(data));
            }

            List<String> lines = new ArrayList<>();

            for (Map<String, Double> score : allScores) {
                lines.add(score.values().stream().map(Object::toString).collect(Collectors.joining(",")));
            }
            Files.deleteIfExists(Paths.get("scores", filename + "_score.txt"));
            Files.write(Paths.get("scores", filename + "_score.txt"), lines);
        }
    }
}

