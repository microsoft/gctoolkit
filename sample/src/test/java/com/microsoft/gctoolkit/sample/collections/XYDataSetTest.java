// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.sample.collections;

import com.microsoft.gctoolkit.sample.collections.XYDataSet.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XYDataSetTest {

    @Test
    public void shouldScaleOnlyY_AxisDataSet() {
        var xyDataSet = new XYDataSet();
        xyDataSet.add(new Point(50, 100));
        XYDataSet scaledPoint = xyDataSet.scaleSeries(2);
        assertEquals(50, scaledPoint.getItems().get(0).getX());
        assertEquals(200.0, scaledPoint.getItems().get(0).getY());
    }

    @Test
    public void shouldReturnMaximumValueOfY_Axis() {
        var xyDataSet = new XYDataSet();
        xyDataSet.add(new Point(50, 100));
        xyDataSet.add(new Point(100, 140));
        xyDataSet.add(new Point(75, 230));
        // added a bigger X than Y value
        xyDataSet.add(new Point(1075, 10));
        assertEquals(230, xyDataSet.maxOfY().getAsDouble());
    }

    @Test
    public void shouldReturnScaledAndTranslatedX_AxisDataSet() {
        var xyDataSet = new XYDataSet();
        xyDataSet.add(new Point(50, 100));
        var translated = xyDataSet.scaleAndTranslateXAxis(2, 20);
        assertEquals(120.0, translated.getItems().get(0).getX());
        assertEquals(100, translated.getItems().get(0).getY());
    }

    @Test
    public void maxOnEmptyDataSet() {
        assertTrue(new XYDataSet().maxOfY().isEmpty());
    }

}
