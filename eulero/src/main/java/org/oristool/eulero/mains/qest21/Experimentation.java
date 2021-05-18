package org.oristool.eulero.mains.qest21;

import org.oristool.eulero.MainHelper;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.eulero.models.*;
import org.oristool.eulero.models.qest21.*;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.Arrays;

public class Experimentation {
    public static void main(String[] args) {
        String GTPathPrefix = System.getProperty("user.dir") + "/results/Test";
        String GTCDF = "/CDF";
        String GTtimes = "/times";
        String GTPathSuffix = "/GroundTruth.txt";
        Approximator approximator = new EXPMixtureApproximation();
        StochasticTransitionFeature feature = StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);
        BigDecimal timeLimit = BigDecimal.valueOf(8);
        BigDecimal timeTick = BigDecimal.valueOf(0.01);
        BigDecimal timeError = BigDecimal.valueOf(0.001);
        int groundTruthRuns = 500000;
        boolean save = true;
        boolean GTFromFile = true;

        String[] testToRun = {/*"A", "B", "C", "D",*/ "E", "F"/*, "G", "H"*/};

        // Test A
        if(Arrays.asList(testToRun).contains("A")){
            System.out.println("Starting Test 1.");
            ModelBuilder testABuilder = new TestABuilder(feature, approximator);
            if(!GTFromFile){
                MainHelper.test("1", testABuilder, timeLimit, timeTick, timeError, groundTruthRuns, 171, save);
            } else {
                MainHelper.test("1", testABuilder, timeLimit, timeTick, timeError, GTPathPrefix + "1" + GTCDF + GTPathSuffix, GTPathPrefix + "1" + GTtimes + GTPathSuffix, 171, save);
            }

        }

        // Test B
        if(Arrays.asList(testToRun).contains("B")){
            System.out.println("Starting Test 2.");
            ModelBuilder testBBuilder = new TestBBuilder(feature, approximator);
            if(!GTFromFile){
                MainHelper.test("2", testBBuilder, timeLimit, timeTick, timeError, groundTruthRuns, 175, save);
            } else {
                MainHelper.test("2", testBBuilder, timeLimit, timeTick, timeError, GTPathPrefix + "2" + GTCDF + GTPathSuffix, GTPathPrefix + "2" + GTtimes + GTPathSuffix, 175, save);
            }
        }

        // Test C
        if(Arrays.asList(testToRun).contains("C")){
            System.out.println("Starting Test 3.");
            ModelBuilder testCBuilder = new TestCBuilder(feature, approximator);
            if(!GTFromFile){
                MainHelper.test("3", testCBuilder, timeLimit, timeTick, timeError, groundTruthRuns, 161, save);
            } else {
                MainHelper.test("3", testCBuilder, timeLimit, timeTick, timeError, GTPathPrefix + "3" + GTCDF + GTPathSuffix, GTPathPrefix + "3" + GTtimes + GTPathSuffix, 161, save);
            }
        }

        // Test D
        if(Arrays.asList(testToRun).contains("D")){
            System.out.println("Starting Test 4.");
            ModelBuilder testDBuilder = new TestDBuilder(feature, approximator);
            if(!GTFromFile){
                MainHelper.test("4", testDBuilder, timeLimit, timeTick, timeError, groundTruthRuns, 179, save);
            } else {
                MainHelper.test("4", testDBuilder, timeLimit, timeTick, timeError, GTPathPrefix + "4" + GTCDF + GTPathSuffix, GTPathPrefix + "4" + GTtimes + GTPathSuffix, 179, save);
            }
        }

        // Test E
        if(Arrays.asList(testToRun).contains("E")){
            System.out.println("Starting Test 5.");
            ModelBuilder testEBuilder = new TestEBuilder(feature, approximator);
            if(!GTFromFile){
                MainHelper.test("5", testEBuilder, timeLimit, timeTick, timeError, groundTruthRuns, 1147, save);
            } else {
                MainHelper.test("5", testEBuilder, timeLimit, timeTick, timeError, GTPathPrefix + "5" + GTCDF + GTPathSuffix, GTPathPrefix + "5" + GTtimes + GTPathSuffix, 1147, save);
            }
        }

        // Test F
        if(Arrays.asList(testToRun).contains("F")){
            System.out.println("Starting Test 6.");
            ModelBuilder testFBuilder = new TestFBuilder(feature, approximator);
            if(!GTFromFile){
                MainHelper.test("6", testFBuilder, timeLimit, timeTick, timeError, groundTruthRuns, 595, save);
            } else {
                MainHelper.test("6", testFBuilder, timeLimit, timeTick, timeError, GTPathPrefix + "6" + GTCDF + GTPathSuffix, GTPathPrefix + "6" + GTtimes + GTPathSuffix, 595, save);
            }
        }

        // Test G
        if(Arrays.asList(testToRun).contains("G")){
            System.out.println("Starting Test 7.");
            ModelBuilder testGBuilder = new TestGBuilder(feature, approximator);
            if(!GTFromFile){
                MainHelper.test("7", testGBuilder, timeLimit.add(BigDecimal.valueOf(4)), timeTick, timeError, groundTruthRuns, 230, save);
            } else {
                MainHelper.test("7", testGBuilder, timeLimit.add(BigDecimal.valueOf(4)), timeTick, timeError, GTPathPrefix + "7" + GTCDF + GTPathSuffix, GTPathPrefix + "7" + GTtimes + GTPathSuffix, 230, save);
            }
        }

        // Test H
        if(Arrays.asList(testToRun).contains("H")){
            System.out.println("Starting Test 8.");
            ModelBuilder testHBuilder = new TestHBuilder(feature, approximator);
            if(!GTFromFile){
                MainHelper.test("8", testHBuilder, timeLimit.add(BigDecimal.valueOf(4)), timeTick, timeError, groundTruthRuns, 249, save);
            } else {
                MainHelper.test("8", testHBuilder, timeLimit.add(BigDecimal.valueOf(4)), timeTick, timeError, GTPathPrefix + "8" + GTCDF + GTPathSuffix, GTPathPrefix + "8" + GTtimes + GTPathSuffix, 249, save);
            }
        }
    }
}