// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.test.collections;

import com.microsoft.gctoolkit.collections.XYDataSet;
import com.microsoft.gctoolkit.collections.XYDataSet.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XYDataSetTest {

    @Test
    void shouldScaleOnlyY_AxisDataSet() {
        XYDataSet xyDataSet = new XYDataSet();
        xyDataSet.add(new Point(50, 100));
        XYDataSet scaledPoint = xyDataSet.scaleSeries(2);
        assertEquals(50, scaledPoint.getItems().get(0).getX());
        assertEquals(200.0, scaledPoint.getItems().get(0).getY());
    }


    @Test
    void shouldReturnMaximumValueOfY_Axis() {
        XYDataSet xyDataSet = new XYDataSet();
        xyDataSet.add(new Point(50, 100));
        xyDataSet.add(new Point(100, 140));
        xyDataSet.add(new Point(75, 230));
        assertEquals(230, xyDataSet.max());
    }

    @Test
    void shouldReturnScaledAndTranslatedX_AxisDataSet() {
        XYDataSet xyDataSet = new XYDataSet();
        xyDataSet.add(new Point(50, 100));
        XYDataSet translated = xyDataSet.scaleAndTranslateXAxis(2, 20);
        assertEquals(120.0, translated.getItems().get(0).getX());
        assertEquals(100, translated.getItems().get(0).getY());
    }
}
