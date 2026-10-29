package com.spe.calculator.service;

import com.spe.calculator.exception.DivisionByZeroException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorServiceTest {

    private static final double DELTA = 1e-9;

    private final CalculatorService service = new CalculatorService();

    @Test
    void addPositiveNumbers() {
        assertEquals(5, service.add(2, 3), DELTA);
    }

    @Test
    void addNegativeNumbers() {
        assertEquals(-7, service.add(-3, -4), DELTA);
    }

    @Test
    void addDecimals() {
        assertEquals(0.3, service.add(0.1, 0.2), DELTA);
    }

    @Test
    void subtractPositiveNumbers() {
        assertEquals(6, service.subtract(10, 4), DELTA);
    }

    @Test
    void subtractGivesNegativeResult() {
        assertEquals(-6, service.subtract(4, 10), DELTA);
    }

    @Test
    void subtractDecimals() {
        assertEquals(1.25, service.subtract(3.75, 2.5), DELTA);
    }

    @Test
    void multiplyPositiveNumbers() {
        assertEquals(42, service.multiply(6, 7), DELTA);
    }

    @Test
    void multiplyByNegative() {
        assertEquals(-42, service.multiply(6, -7), DELTA);
    }

    @Test
    void multiplyDecimals() {
        assertEquals(0.75, service.multiply(1.5, 0.5), DELTA);
    }

    @Test
    void multiplyByZero() {
        assertEquals(0, service.multiply(123, 0), DELTA);
    }

    @Test
    void dividePositiveNumbers() {
        assertEquals(3.5, service.divide(7, 2), DELTA);
    }

    @Test
    void divideNegativeNumbers() {
        assertEquals(2, service.divide(-8, -4), DELTA);
    }

    @Test
    void divideDecimals() {
        assertEquals(0.5, service.divide(0.25, 0.5), DELTA);
    }

    @Test
    void divideByZeroThrowsException() {
        DivisionByZeroException ex = assertThrows(DivisionByZeroException.class, () -> service.divide(5, 0));
        assertEquals("Division by zero is not allowed", ex.getMessage());
    }

    @Test
    void nonFiniteInputThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> service.add(Double.NaN, 1));
    }

    @Test
    void overflowThrowsException() {
        assertThrows(ArithmeticException.class, () -> service.multiply(1e308, 10));
    }
}
