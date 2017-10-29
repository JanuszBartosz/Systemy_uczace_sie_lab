package main.java.data.discretizator.impl;

import cern.colt.function.ObjectFunction;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;

public class EqualWidthDiscretizator implements ObjectFunction {

    private List<Pair<Double, Double>> bins;

    EqualWidthDiscretizator(double numberBins, Object[] attributes) {
        DoubleSummaryStatistics stats = Arrays.stream(attributes)
                .map(a -> (String) a)
                .mapToDouble(Double::parseDouble)
                .summaryStatistics();

        double binSize = (stats.getMax() - stats.getMin()) / numberBins;
        bins = new ArrayList<>();
        for (int i = 0; i < numberBins; i++) {
            bins.add(new ImmutablePair<>(stats.getMin() + binSize * i, i == numberBins - 1 ? stats.getMax() : stats.getMin() + binSize * i + binSize));
        }

        assert (bins.get(bins.size() - 1).getRight() == stats.getMax());
    }

    @Override
    public Object apply(Object argument) {
        double attribute = Double.parseDouble((String) argument);

        for (int i = 0; i < bins.size(); i++) {
            if (attribute >= bins.get(i).getLeft() && attribute <= bins.get(i).getRight()) {
                return String.valueOf(i);
            }
        }
        return "DISCRETIZATION_ERROR";
    }
}
