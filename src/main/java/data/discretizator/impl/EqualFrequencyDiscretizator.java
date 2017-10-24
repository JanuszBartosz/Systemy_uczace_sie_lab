package main.java.data.discretizator.impl;

import cern.colt.function.ObjectFunction;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EqualFrequencyDiscretizator implements ObjectFunction {

    private double numberBins;
    private int largerBinSize;
    private int smallerBinSize;
    private int numberLargerBins;
    List<Pair<Double, Double>> bins;

    public EqualFrequencyDiscretizator(double numberBins, Object[] attributes) {
        this.bins = new ArrayList<>();
        this.numberBins = numberBins;

        this.largerBinSize = (int) Math.ceil(attributes.length / numberBins);
        this.smallerBinSize = (int) Math.floor(attributes.length / numberBins);
        this.numberLargerBins = (int) (attributes.length % numberBins == 0 ? numberBins : attributes.length % numberBins);
        double[] doubleAttributes = Arrays.stream(attributes)
                .map(Object::toString)
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
    public Object apply(Object argument) {
        double attribute = Double.parseDouble(argument.toString());

        for (int i = 0; i < bins.size(); i++) {
            if (attribute >= bins.get(i).getLeft() && attribute <= bins.get(i).getRight()) {
                return String.valueOf(i);
            }
        }
        return "DISCRETIZATION_ERROR";
    }
}
