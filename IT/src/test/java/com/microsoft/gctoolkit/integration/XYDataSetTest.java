// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.integration;

import com.microsoft.gctoolkit.integration.collections.XYDataSet;
import com.microsoft.gctoolkit.integration.collections.XYDataSet.Point;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("classPath")
public class XYDataSetTest {

    @Test
    public void shouldScaleOnlyY_AxisDataSet() {
        var xyDataSet = new XYDataSet();
        xyDataSet.add(new Point(50, 100));
        XYDataSet scaledPoint = xyDataSet.scaleSeries(2);
        Assertions.assertEquals(50, scaledPoint.getItems().get(0).getX());
        Assertions.assertEquals(200.0, scaledPoint.getItems().get(0).getY());
    }

    @Test
    public void shouldReturnMaximumValueOfY_Axis() {
        var xyDataSet = new XYDataSet();
        xyDataSet.add(new Point(50, 100));
        xyDataSet.add(new Point(100, 140));
        xyDataSet.add(new Point(75, 230));
        // added a bigger X than Y value
        xyDataSet.add(new Point(1075, 10));
        Assertions.assertEquals(230, xyDataSet.maxOfY().getAsDouble());
    }

    @Test
    public void shouldReturnScaledAndTranslatedX_AxisDataSet() {
        var xyDataSet = new XYDataSet();
        xyDataSet.add(new Point(50, 100));
        var translated = xyDataSet.scaleAndTranslateXAxis(2, 20);
        Assertions.assertEquals(120.0, translated.getItems().get(0).getX());
        Assertions.assertEquals(100, translated.getItems().get(0).getY());
    }

    @Test
    public void maxOnEmptyDataSet() {
        Assertions.assertTrue(new XYDataSet().maxOfY().isEmpty());
    }

}
