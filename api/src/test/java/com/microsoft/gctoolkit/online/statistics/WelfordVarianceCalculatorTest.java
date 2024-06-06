package com.microsoft.gctoolkit.online.statistics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WelfordVarianceCalculatorTest {

    @Test
    void insufficientSamples() {
        WelfordVarianceCalculator calculator = new WelfordVarianceCalculator();
        calculator.update(1.23d);
        assertThrows(NotEnoughSampleException.class, calculator::getValue);
    }

    @Test
    void getVariance() {
        WelfordVarianceCalculator calculator = new WelfordVarianceCalculator();
        calculator.update(1421.23);
        calculator.update(2897.34);
        calculator.update(3907.45);
        assertEquals(1563418.8054333332, calculator.getValue(), 0.0001d);
    }

    @Test
    void getVarianceWithSmallDifference() {
        WelfordVarianceCalculator calculator = new WelfordVarianceCalculator();
        calculator.update(71899123.1273789);
        calculator.update(71899123.1378323);
        calculator.update(71899123.1478654);
        assertEquals(0.00010493893, calculator.getValue(), 0.0001d);
    }

}