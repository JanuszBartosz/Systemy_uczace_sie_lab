package main.java;

import main.java.data.discretizator.impl.DiscretizatorType;

public class Params {

    private Params(){}

    public static int numberBins = 10;
    public static DiscretizatorType type = DiscretizatorType.FREQUENCY;
    public static int numberFolds = 10;
}
