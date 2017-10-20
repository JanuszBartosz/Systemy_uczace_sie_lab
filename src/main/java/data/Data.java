package main.java.data;

import cern.colt.matrix.ObjectFactory2D;
import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import main.java.data.discretizatorImpl.DiscretizatorFactory;

import java.util.ArrayList;
import java.util.List;

public class Data {

    private final ObjectMatrix2D dataSet;
    private final List<String> attributeNames;
    private final List<String> attributeTypes;
    private final List<String> classNames;
    private Crosvalidator crosvalidator;

    public Data(List<List<String>> dataSet, List<String> attributeNames, List<String> attributeTypes, List<String> classNames) {
        String[][] dataSet2DArray = dataSet.stream().map(List::toArray).toArray(String[][]::new);
        this.dataSet = ObjectFactory2D.dense.make(dataSet2DArray);
        this.attributeNames = attributeNames;
        this.attributeTypes = attributeTypes;
        this.classNames = classNames;
        discretize();
    }

    public ObjectMatrix2D getDataSet() {
        return dataSet;
    }

    public List<String> getClassNames() {
        return classNames;
    }

    public Crosvalidator createCrosvalidator(int numberChunks, int foldNumber) {
        this.crosvalidator = new Crosvalidator(numberChunks, foldNumber);
        return this.crosvalidator;
    }

    public void discretize() {

        List<Integer> realColumns = new ArrayList<>();

        for (int i = 0; i < attributeTypes.size(); i++) {
            if (attributeTypes.get(i).equals("real"))
                realColumns.add(i);
        }

        for (Integer column : realColumns) {
            ObjectMatrix1D columnView = dataSet.viewColumn(column);
            columnView.assign(DiscretizatorFactory.getDiscretizator(columnView.toArray()));
        }
    }


    public Crosvalidator getCrosvalidator() {
        return crosvalidator;
    }


    public class Crosvalidator {

        private ObjectMatrix2D trainingData;

        private ObjectMatrix2D testData;

        Crosvalidator(int numberChunks, int foldNumber) {

            int chunkSize = dataSet.rows() / numberChunks;
            int trainingDataSize = (numberChunks - 1) * chunkSize;
            int testDataSize = dataSet.rows() - trainingDataSize;
            int testDataBeginIndex = foldNumber * testDataSize;
            this.testData = dataSet.viewPart(testDataBeginIndex, 0, testDataSize, dataSet.columns());

            ObjectMatrix2D[]  trainingDataChunks = new ObjectMatrix2D[2];
            trainingDataChunks[0] = dataSet.viewPart(0, 0, testDataBeginIndex, dataSet.columns());
            trainingDataChunks[1] = dataSet.viewPart(testDataBeginIndex + testDataSize, 0 , dataSet.rows() - (testDataBeginIndex + testDataSize), dataSet.columns());
            this.trainingData = ObjectFactory2D.dense.appendRows(trainingDataChunks[0], trainingDataChunks[1]);
        }

        public ObjectMatrix2D getTrainingData() {
            return this.trainingData;
        }

        public ObjectMatrix2D getTestData() {
            return this.testData;
        }
    }
}
