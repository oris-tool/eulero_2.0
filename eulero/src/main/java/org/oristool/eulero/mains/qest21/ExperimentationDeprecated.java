package org.oristool.eulero.mains.qest21;

import org.oristool.eulero.MainHelper;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.approximation.SplineBodyEXPTailApproximation;
import org.oristool.eulero.models.*;
import org.oristool.eulero.models.qest21_deprecated.*;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.Arrays;

public class ExperimentationDeprecated {
    public static void main(String[] args) {
        String GTPathPrefix = System.getProperty("user.dir") + "/results/XORSplineTest/Test";
        String GTCDF = "/CDF";
        String GTtimes = "/times";
        String GTPathSuffix = "/GroundTruth.txt";
        Approximator approximator = new SplineBodyEXPTailApproximation(3);//new EXPMixtureApproximation();
        StochasticTransitionFeature feature = StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);
        BigDecimal timeLimit = BigDecimal.valueOf(8);
        BigDecimal timeTick = BigDecimal.valueOf(0.01) ;
        BigDecimal timeError = BigDecimal.valueOf(0.001);
        int groundTruthRuns = 500000;
        boolean save = true;
        boolean GTFromFile = true;

        String[] testToRun = {/*"A", "B", "C", "D", "E",*/ "F",/* "G","H"*/};

        // Test A
        if(Arrays.asList(testToRun).contains("A")){
            System.out.println("Starting Test 1.");
            ModelBuilder_Deprecated testABuilder = new TestABuilderDeprecated(feature, approximator);
            if(!GTFromFile){
                MainHelper.test("1", testABuilder, timeLimit, timeTick, timeError, groundTruthRuns, 161, save);
            } else {
                MainHelper.test("1", testABuilder, timeLimit, timeTick, timeError, GTPathPrefix + "1" + GTCDF + GTPathSuffix, GTPathPrefix + "1" + GTtimes + GTPathSuffix, 161, save);
            }

        }

        // Test B
        if(Arrays.asList(testToRun).contains("B")){
            System.out.println("Starting Test 2.");
            ModelBuilder_Deprecated testBBuilder = new TestBBuilderDeprecated(feature, approximator);
            if(!GTFromFile){
                MainHelper.test("2", testBBuilder, timeLimit, timeTick, timeError, groundTruthRuns, 185, save);
            } else {
                MainHelper.test("2", testBBuilder, timeLimit, timeTick, timeError, GTPathPrefix + "2" + GTCDF + GTPathSuffix, GTPathPrefix + "2" + GTtimes + GTPathSuffix, 185, save);
            }
        }

        // Test C
        if(Arrays.asList(testToRun).contains("C")){
            System.out.println("Starting Test 3.");
            ModelBuilder_Deprecated testCBuilder = new TestCBuilderDeprecated(feature, approximator);
            if(!GTFromFile){
                MainHelper.test("3", testCBuilder, timeLimit, timeTick, timeError, groundTruthRuns, 221, save);
            } else {
                MainHelper.test("3", testCBuilder, timeLimit, timeTick, timeError, GTPathPrefix + "3" + GTCDF + GTPathSuffix, GTPathPrefix + "3" + GTtimes + GTPathSuffix, 221, save);
            }
        }

        // Test D
        if(Arrays.asList(testToRun).contains("D")){
            System.out.println("Starting Test 4.");
            ModelBuilder_Deprecated testDBuilder = new TestDBuilderDeprecated(feature, approximator);
            if(!GTFromFile){
                MainHelper.test("4", testDBuilder, timeLimit, timeTick, timeError, groundTruthRuns, 229, save);
            } else {
                MainHelper.test("4", testDBuilder, timeLimit, timeTick, timeError, GTPathPrefix + "4" + GTCDF + GTPathSuffix, GTPathPrefix + "4" + GTtimes + GTPathSuffix, 229, save);
            }
        }

        // Test E
        if(Arrays.asList(testToRun).contains("E")){
            System.out.println("Starting Test 5.");
            ModelBuilder_Deprecated testEBuilder = new TestEBuilderDeprecated(feature, approximator);
            if(!GTFromFile){
                MainHelper.test("5", testEBuilder, timeLimit, timeTick, timeError, groundTruthRuns, 647, save);
            } else {
                MainHelper.test("5", testEBuilder, timeLimit, timeTick, timeError, GTPathPrefix + "5" + GTCDF + GTPathSuffix, GTPathPrefix + "5" + GTtimes + GTPathSuffix, 647, save);
            }
        }

        // Test F
        if(Arrays.asList(testToRun).contains("F")){
            System.out.println("Starting Test 6.");
            ModelBuilder_Deprecated testFBuilder = new TestFBuilderDeprecated(feature, approximator);
            if(!GTFromFile){
                MainHelper.test("6", testFBuilder, timeLimit, timeTick, timeError, groundTruthRuns, 647, save);
            } else {
                MainHelper.test("6", testFBuilder, timeLimit, timeTick, timeError, GTPathPrefix + "6" + GTCDF + GTPathSuffix, GTPathPrefix + "6" + GTtimes + GTPathSuffix, 647, save);
            }
        }

        // Test G
        if(Arrays.asList(testToRun).contains("G")){
            System.out.println("Starting Test 7.");
            ModelBuilder_Deprecated testGBuilder = new TestGBuilderDeprecated(feature, approximator);
            if(!GTFromFile){
                MainHelper.test("7", testGBuilder, timeLimit.add(BigDecimal.valueOf(4)), timeTick, timeError, groundTruthRuns, 470, save);
            } else {
                MainHelper.test("7", testGBuilder, timeLimit.add(BigDecimal.valueOf(4)), timeTick, timeError, GTPathPrefix + "7" + GTCDF + GTPathSuffix, GTPathPrefix + "7" + GTtimes + GTPathSuffix, 470, save);
            }
        }

        // Test H
        if(Arrays.asList(testToRun).contains("H")){
            System.out.println("Starting Test 8.");
            ModelBuilder_Deprecated testHBuilder = new TestHBuilderDeprecated(feature, approximator);
            if(!GTFromFile){
                MainHelper.test("8", testHBuilder, timeLimit.add(BigDecimal.valueOf(4)), timeTick, timeError, groundTruthRuns, 500, save);
            } else {
                MainHelper.test("8", testHBuilder, timeLimit.add(BigDecimal.valueOf(4)), timeTick, timeError, GTPathPrefix + "8" + GTCDF + GTPathSuffix, GTPathPrefix + "8" + GTtimes + GTPathSuffix, 500, save);
            }
        }
    }
}