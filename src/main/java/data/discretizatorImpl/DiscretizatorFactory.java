package main.java.data.discretizatorImpl;

import cern.colt.function.ObjectFunction;
import main.java.Params;

public class DiscretizatorFactory {

    private DiscretizatorFactory(){}

    static public ObjectFunction getDiscretizator(Object[] attributes) {

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
