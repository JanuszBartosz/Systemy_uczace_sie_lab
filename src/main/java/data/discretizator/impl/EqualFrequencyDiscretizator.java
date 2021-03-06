package main.java.data.discretizator.impl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class EqualFrequencyDiscretizator implements Function<String, String> {

    private List<Pair<Double, Double>> bins;

    EqualFrequencyDiscretizator(double numberBins, String[] attributes) {
        this.bins = new ArrayList<>();

        int largerBinSize = (int) Math.ceil(attributes.length / numberBins);
        int smallerBinSize = (int) Math.floor(attributes.length / numberBins);
        int numberLargerBins = (int) (attributes.length % numberBins == 0 ? numberBins : attributes.length % numberBins);
        double[] doubleAttributes = Arrays.stream(attributes)
                .mapToDouble(Double::parseDouble)
                .sorted()
                .toArray();

        int lastIndex = 0;
        for (int i = 0; i < numberLargerBins; i++) {
            lastIndex = i * largerBinSize + largerBinSize - 1;
            bins.add(new ImmutablePair<>(doubleAttributes[i * largerBinSize], doubleAttributes[lastIndex]));
        }
        lastIndex++;
        for (int i = 0; i < numberBins - numberLargerBins; i++) {
            bins.add(new ImmutablePair<>(
                    doubleAttributes[i * smallerBinSize + lastIndex],
                    doubleAttributes[i * smallerBinSize + lastIndex + smallerBinSize - 1]
            ));
        }
    }

    @Override
    public String apply(String argument) {
        double attribute = Double.parseDouble(argument);

        for (int i = 0; i < bins.size(); i++) {
            if (attribute >= bins.get(i).getLeft() && attribute <= bins.get(i).getRight()) {
                return String.valueOf(i);
            }
        }
        return "DISCRETIZATION_ERROR";
    }
}
