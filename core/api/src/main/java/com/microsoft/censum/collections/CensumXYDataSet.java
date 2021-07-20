package com.microsoft.censum.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;

public class CensumXYDataSet {

    protected List<Point> dataSeries;

    public CensumXYDataSet() {
        dataSeries = new ArrayList<>();
    }

    public CensumXYDataSet(CensumXYDataSet series) {
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

    public List<Point> getItems() {
        return unmodifiableList(dataSeries);
    }

    public CensumXYDataSet scaleSeries(double scaleFactor) {
        CensumXYDataSet scaled = new CensumXYDataSet();
        for (Point item : dataSeries) {
            scaled.add(item.getX(), item.getY().doubleValue() * scaleFactor);
        }
        return scaled;
    }

    public double max() {
        return dataSeries.stream().map(Point::getY).mapToDouble(Number::doubleValue).max().getAsDouble();
    }

    public CensumXYDataSet scaleAndTranslateXAxis(double scale, double offset) {
        CensumXYDataSet translatedSeries = new CensumXYDataSet();
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

        final private Number x;
        final private Number y;

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
