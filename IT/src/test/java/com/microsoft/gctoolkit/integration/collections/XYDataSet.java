package com.microsoft.gctoolkit.integration.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Stream;

public class XYDataSet {
    private final List<com.microsoft.gctoolkit.integration.collections.XYDataSet.Point> dataSeries;

    public XYDataSet() {
        dataSeries = new ArrayList<>();
    }

    public XYDataSet(com.microsoft.gctoolkit.integration.collections.XYDataSet series) {
        dataSeries = new ArrayList<>(series.getItems());
    }

    public void add(Number x, Number y) {
        dataSeries.add(new com.microsoft.gctoolkit.integration.collections.XYDataSet.Point(x, y));
    }

    public void add(com.microsoft.gctoolkit.integration.collections.XYDataSet.Point item) {
        dataSeries.add(item);
    }

    protected void addAll(List<com.microsoft.gctoolkit.integration.collections.XYDataSet.Point> items) {
        dataSeries.addAll(items);
    }

    public boolean isEmpty() {
        return dataSeries.isEmpty();
    }

    /**
     * Returns an immutable List of the items in this DataSet.
     */
    public List<com.microsoft.gctoolkit.integration.collections.XYDataSet.Point> getItems() {
        return List.copyOf(dataSeries);
    }

    public com.microsoft.gctoolkit.integration.collections.XYDataSet scaleSeries(double scaleFactor) {
        com.microsoft.gctoolkit.integration.collections.XYDataSet scaled = new com.microsoft.gctoolkit.integration.collections.XYDataSet();
        for (com.microsoft.gctoolkit.integration.collections.XYDataSet.Point item : dataSeries) {
            scaled.add(item.getX(), item.getY().doubleValue() * scaleFactor);
        }
        return scaled;
    }

    /**
     * Returns the largest Y value in the XYDataSet as an OptionalDouble,
     * with an empty optional if the dataset is empty.
     */
    public OptionalDouble maxOfY() {
        return dataSeries.stream()
                .map(com.microsoft.gctoolkit.integration.collections.XYDataSet.Point::getY)
                .mapToDouble(Number::doubleValue)
                .max();
    }

    public com.microsoft.gctoolkit.integration.collections.XYDataSet scaleAndTranslateXAxis(double scale, double offset) {
        com.microsoft.gctoolkit.integration.collections.XYDataSet translatedSeries = new com.microsoft.gctoolkit.integration.collections.XYDataSet();
        for (com.microsoft.gctoolkit.integration.collections.XYDataSet.Point dataPoint : dataSeries) {
            double scaledXCoordinate = (scale * dataPoint.getX().doubleValue()) + offset;
            translatedSeries.add(scaledXCoordinate, dataPoint.getY());
        }
        return translatedSeries;
    }

    public int size() {
        return dataSeries.size();
    }

    public Stream<com.microsoft.gctoolkit.integration.collections.XYDataSet.Point> stream() {
        return dataSeries.stream();
    }

    public static class Point {
        private final Number x;
        private final Number y;

        public Point(Number x, Number y) {
            this.x = x;
            this.y = y;
        }

        public Number getX() {
            return x;
        }

        public Number getY() {
            return y;
        }

        @Override
        public String toString() {
            return x + "," + y;
        }
    }
}
