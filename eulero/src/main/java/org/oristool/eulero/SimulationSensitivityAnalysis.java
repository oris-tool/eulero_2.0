package org.oristool.eulero;

import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.eulero.models.*;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class SimulationSensitivityAnalysis {
    public static void main(String[] args) {
        String SAVE_PATH = System.getProperty("user.dir") + "/results";
        Approximator approximator = new EXPMixtureApproximation();
        StochasticTransitionFeature feature = StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);
        BigDecimal timeLimit = BigDecimal.valueOf(8);
        BigDecimal timeTick = BigDecimal.valueOf(0.01);
        int runs = 500000;
        int runStep = 100000;
        boolean save = true;

        String[] testToRun = {"A", "B", "C", "D", "E", "F", "G", "H"};

        StringBuilder myStringBuilder = new StringBuilder();

        // Test A
        if(Arrays.asList(testToRun).contains("A")){
            System.out.println("Starting Sensitivity Analysis - Test 1.");
            ModelBuilder testABuilder = new TestABuilder(feature, approximator);
            myStringBuilder.append(
                    MainHelper.simulationSensitivityAnalysis("1", testABuilder, timeLimit, timeTick, runs, runStep));
        }

        // Test B
        if(Arrays.asList(testToRun).contains("B")){
            System.out.println("Starting Sensitivity Analysis - Test 2.");
            ModelBuilder testBBuilder = new TestBBuilder(feature, approximator);
            myStringBuilder.append(
                    MainHelper.simulationSensitivityAnalysis("2", testBBuilder, timeLimit, timeTick, runs, runStep));
        }

        // Test C
        if(Arrays.asList(testToRun).contains("C")){
            System.out.println("Starting Sensitivity Analysis - Test 3.");
            ModelBuilder testCBuilder = new TestCBuilder(feature, approximator);
            myStringBuilder.append(
                    MainHelper.simulationSensitivityAnalysis("3", testCBuilder, timeLimit, timeTick, runs, runStep));
        }

        // Test D
        if(Arrays.asList(testToRun).contains("D")){
            System.out.println("Starting Sensitivity Analysis - Test 4.");
            ModelBuilder testDBuilder = new TestDBuilder(feature, approximator);
            myStringBuilder.append(
                    MainHelper.simulationSensitivityAnalysis("4", testDBuilder, timeLimit, timeTick, runs, runStep));
        }

        // Test E
        if(Arrays.asList(testToRun).contains("E")){
            System.out.println("Starting Sensitivity Analysis - Test 5.");
            ModelBuilder testEBuilder = new TestEBuilder(feature, approximator);
            myStringBuilder.append(
                    MainHelper.simulationSensitivityAnalysis("5", testEBuilder, timeLimit, timeTick, runs, runStep));
        }

        // Test F
        if(Arrays.asList(testToRun).contains("F")){
            System.out.println("Starting Sensitivity Analysis - Test 6.");
            ModelBuilder testFBuilder = new TestFBuilder(feature, approximator);
            myStringBuilder.append(
                    MainHelper.simulationSensitivityAnalysis("6", testFBuilder, timeLimit, timeTick, runs, runStep));
        }

        // Test G
        if(Arrays.asList(testToRun).contains("G")){
            System.out.println("Starting Sensitivity Analysis - Test 7.");
            ModelBuilder testGBuilder = new TestGBuilder(feature, approximator);
            myStringBuilder.append(
                    MainHelper.simulationSensitivityAnalysis("7", testGBuilder, timeLimit, timeTick, runs, runStep));
        }

        // Test H
        if(Arrays.asList(testToRun).contains("H")){
            System.out.println("Starting Sensitivity Analysis - Test 8.");
            ModelBuilder testHBuilder = new TestHBuilder(feature, approximator);
            myStringBuilder.append(
                    MainHelper.simulationSensitivityAnalysis("8", testHBuilder, timeLimit, timeTick, runs, runStep));
        }

        if(save){
            // TODO check path exists
            try {
                Files.writeString(Path.of(SAVE_PATH + "/simulationSensitivityAnalysis.txt"), myStringBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
