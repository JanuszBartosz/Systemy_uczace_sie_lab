package main.java.data.discretizator.impl;

import main.java.Params;

import java.util.function.Function;

public class DiscretizatorFactory {

    private DiscretizatorFactory(){}

    public static Function<String, String> getDiscretizator(String[] attributes) {

        switch (Params.type) {
            case WIDTH:
                return new EqualWidthDiscretizator(Params.numberBins, attributes);
            case FREQUENCY:
                return new EqualFrequencyDiscretizator(Params.numberBins, attributes);
            default:
                return new EqualWidthDiscretizator(Params.numberBins, attributes);
        }
    }
}
