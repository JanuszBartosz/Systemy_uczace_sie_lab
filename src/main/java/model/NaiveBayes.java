package main.java.model;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import main.java.data.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:bartosz.janusz@fingo.pl">Bartosz Janusz - FINGO</a>
 */
public class NaiveBayes {

    private Map<String, Double> probAPriori;
    Map<String, Double> chances;

    ObjectMatrix2D trainingData;
    ObjectMatrix2D testData;
    Data data;


    public NaiveBayes(Data data) {
        this.data = data;
        this.trainingData = data.getCrosvalidator().getTrainingData();
        this.testData = data.getCrosvalidator().getTestData();
        computeProbAPriori();
    }

    private void computeProbAPriori() {

        ObjectMatrix1D classColumn = trainingData.viewColumn(trainingData.columns() - 1);

        Map<String, Double> occurrences = new HashMap<>();

        for (int i = 0; i < classColumn.size(); i++) {
            if(occurrences.containsKey(classColumn.get(i))){
                occurrences.replace(classColumn.get(i).toString(), occurrences.get(classColumn.get(i)) + 1);
            }
            else{
                occurrences.put(classColumn.get(i).toString(), 1.0);
            }
        }

        this.probAPriori = occurrences.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()/classColumn.size()));
    }

    private void computeChances(){

    }
}
