package org.oristool.eulero;

import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.eulero.models.*;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.Arrays;

public class MainRiccardo {
    public static void main(String[] args) {
        Approximator approximator = new EXPMixtureApproximation();
        StochasticTransitionFeature feature = StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);
        BigDecimal timeLimit = BigDecimal.valueOf(8);
        BigDecimal timeTick = BigDecimal.valueOf(0.01);
        BigDecimal timeError = BigDecimal.valueOf(0.001);
        int runs = 2000;

        String[] testToRun = {"H"};//{"A", "B", "C", "D", "E", "F", "G", "H"};

        // Test A
        if(Arrays.asList(testToRun).contains("A")){
            ModelBuilder testABuilder = new TestABuilder(feature, approximator);
            MainHelper.test("A", testABuilder, timeLimit, timeTick, timeError, runs);
        }

        // Test B
        if(Arrays.asList(testToRun).contains("B")){
            ModelBuilder testBBuilder = new TestBBuilder(feature, approximator);
            MainHelper.test("B", testBBuilder, timeLimit, timeTick, timeError, runs);
        }

        // Test C
        if(Arrays.asList(testToRun).contains("C")){
            ModelBuilder testCBuilder = new TestCBuilder(feature, approximator);
            MainHelper.test("C", testCBuilder, timeLimit, timeTick, timeError, runs);
        }

        // Test D
        if(Arrays.asList(testToRun).contains("D")){
            ModelBuilder testDBuilder = new TestDBuilder(feature, approximator);
            MainHelper.test("D", testDBuilder, timeLimit, timeTick, timeError, runs);
        }

        // Test E
        if(Arrays.asList(testToRun).contains("E")){
            ModelBuilder testEBuilder = new TestEBuilder(feature, approximator);
            MainHelper.test("E", testEBuilder, timeLimit, timeTick, timeError, runs);
        }

        // Test F
        if(Arrays.asList(testToRun).contains("F")){
            ModelBuilder testFBuilder = new TestFBuilder(feature, approximator);
            MainHelper.test("F", testFBuilder, timeLimit, timeTick, timeError, runs);
        }

        // Test G
        if(Arrays.asList(testToRun).contains("G")){
            ModelBuilder testGBuilder = new TestGBuilder(feature, approximator);
            MainHelper.test("G", testGBuilder, timeLimit.add(BigDecimal.valueOf(4)), timeTick, timeError, runs);
        }

        // Test H
        if(Arrays.asList(testToRun).contains("H")){
            ModelBuilder testHBuilder = new TestHBuilder(feature, approximator);
            MainHelper.test("H", testHBuilder, timeLimit.add(BigDecimal.valueOf(4)), timeTick, timeError, runs);
        }
    }
}