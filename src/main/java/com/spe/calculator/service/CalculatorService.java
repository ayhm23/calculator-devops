package com.spe.calculator.service;

import com.spe.calculator.exception.DivisionByZeroException;
import org.springframework.stereotype.Service;

@Service
public class CalculatorService {

    public double add(double a, double b) {
        return checked(a, b, a + b);
    }

    public double subtract(double a, double b) {
        return checked(a, b, a - b);
    }

    public double multiply(double a, double b) {
        return checked(a, b, a * b);
    }

    public double divide(double a, double b) {
        if (b == 0) {
            throw new DivisionByZeroException();
        }
        return checked(a, b, a / b);
    }

    // "NaN" and "Infinity" parse as valid doubles but can't be written as JSON numbers
    private double checked(double a, double b, double result) {
        if (!Double.isFinite(a) || !Double.isFinite(b)) {
            throw new IllegalArgumentException("Parameters must be finite numbers");
        }
        if (!Double.isFinite(result)) {
            throw new ArithmeticException("Result is out of range");
        }
        return result;
    }
}
