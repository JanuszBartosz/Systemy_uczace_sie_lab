package main.java.data.discretizator.impl;

import cern.colt.function.ObjectFunction;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;

public class EqualWidthDiscretizator implements ObjectFunction {

    private double numberBins;
    private double divisor;
    private double offset;


    public EqualWidthDiscretizator(double numberBins, Object[] attributes) {
        this.numberBins = numberBins;
        DoubleSummaryStatistics stats = Arrays.stream(attributes)
                .map(Object::toString)
                .mapToDouble(Double::parseDouble)
                .summaryStatistics();

        offset = stats.getMin();
        //TODO: Check if this correction by 0.5 is OK.
        divisor = (stats.getMax() - offset) / numberBins + 0.5d;
    }

    @Override
    public Object apply(Object argument) {
        return String.valueOf((int) (Double.parseDouble(argument.toString()) - offset) / divisor);
    }
}
