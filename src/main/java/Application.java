package main.java;

import main.java.data.Data;
import main.java.data.DataReader;
import main.java.data.discretizator.impl.DiscretizatorType;
import main.java.model.KNearestNeighbours;
import main.java.model.Models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Application {

    public static void main(String[] args) throws IOException {

        List<String> datasetFilenames = Arrays.asList("wine", "ecoli", "vertebral", "wholesale","iris" );
        List<Integer> distParams = Arrays.asList(1, 2, 5);

        Models models = new Models();

        Files.createDirectories(Paths.get("scores"));

        for (String filename : datasetFilenames) {

            Data data = new DataReader(filename).readData();
            List<String> lines = new ArrayList<>();
            List<Map<String, Double>> allScores = new ArrayList<>();
            for (Integer distParam : distParams) {
//                for (KNearestNeighbours.VotingType type : KNearestNeighbours.VotingType.values()) {


                    //for (int K = 2; K <= 20; K += 2) {
                        allScores.add(models.runKNN(data, 10, distParam, KNearestNeighbours.VotingType.STANDARD, false));
                    }



                    for (Map<String, Double> score : allScores) {
                        lines.add(score.values().stream().map(Object::toString).collect(Collectors.joining(",")));
                    }
                    Files.deleteIfExists(Paths.get("scores", filename, "STANDARD" + "_" + "distParam" + ".txt"));
                    Files.write(Paths.get("scores", filename, "STANDARD" + "_" + "distParam" + ".txt"), lines);

            //}
        }
    }
}

