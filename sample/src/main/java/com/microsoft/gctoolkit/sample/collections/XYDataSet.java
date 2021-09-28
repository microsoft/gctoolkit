// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.sample.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Stream;

public class XYDataSet {
    private final List<Point> dataSeries;

    public XYDataSet() {
        dataSeries = new ArrayList<>();
    }

    public XYDataSet(XYDataSet series) {
        dataSeries = new ArrayList<>(series.getItems());
    }

    public void add(Number x, Number y) {
        dataSeries.add(new Point(x, y));
    }

    public void add(Point item) {
        dataSeries.add(item);
    }

    protected void addAll(List<Point> items) {
        dataSeries.addAll(items);
    }

    public boolean isEmpty() {
        return dataSeries.isEmpty();
    }

    /**
     * Returns an immutable List of the items in this DataSet.
     */
    public List<Point> getItems() {
        return List.copyOf(dataSeries);
    }

    public XYDataSet scaleSeries(double scaleFactor) {
        XYDataSet scaled = new XYDataSet();
        for (Point item : dataSeries) {
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
                .map(Point::getY)
                .mapToDouble(Number::doubleValue)
                .max();
    }

    public XYDataSet scaleAndTranslateXAxis(double scale, double offset) {
        XYDataSet translatedSeries = new XYDataSet();
        for (Point dataPoint : dataSeries) {
            double scaledXCoordinate = (scale * dataPoint.getX().doubleValue()) + offset;
            translatedSeries.add(scaledXCoordinate, dataPoint.getY());
        }
        return translatedSeries;
    }

    public int size() {
        return dataSeries.size();
    }

    public Stream<Point> stream() {
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
