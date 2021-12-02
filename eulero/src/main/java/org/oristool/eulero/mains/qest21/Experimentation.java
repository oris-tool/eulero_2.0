package org.oristool.eulero.mains.qest21;

import org.oristool.eulero.analysisheuristics.AnalysisHeuristic1;
import org.oristool.eulero.analysisheuristics.AnalysisHeuristic2;
import org.oristool.eulero.analysisheuristics.AnalysisHeuristic3;
import org.oristool.eulero.analysisheuristics.AnalysisHeuristicStrategy;
import org.oristool.eulero.mains.TestCaseHandler;
import org.oristool.eulero.mains.TestCaseResult;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.eulero.models.ModelBuilder;
import org.oristool.eulero.models.qest21.*;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experimentation {
    public static void main(String[] args) {
        String savePathPrefix = System.getProperty("user.dir") + "/results/AutomatedTest/ExpMixture";
        String GTCDF = "/CDF";
        String GTtimes = "/times";
        String GTPathSuffix = "/GroundTruth.txt";
        //Approximator approximator = new SplineBodyEXPTailApproximation(2);
        Approximator approximator = new EXPMixtureApproximation();
        StochasticTransitionFeature feature = StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);
        BigDecimal timeLimit = BigDecimal.valueOf(8);
        BigDecimal timeTick = BigDecimal.valueOf(0.01) ;
        BigDecimal timeError = timeTick.divide(BigDecimal.valueOf(10));
        int groundTruthRuns = 500000;
        boolean save = true;
        boolean plot = true;
        boolean GTFromFile = true;

        BigInteger C = BigInteger.valueOf(2);
        BigInteger R = BigInteger.valueOf(8);
        AnalysisHeuristicStrategy strategy1 = new AnalysisHeuristic1(C, R, approximator);
        AnalysisHeuristicStrategy strategy2 = new AnalysisHeuristic2(C, R, approximator);
        AnalysisHeuristicStrategy strategy3 = new AnalysisHeuristic3(C, R, approximator);


        String[] testToRun = {"A","B", "C", "D", "E",  "F", "G", "H"};

        // Test A
        if(Arrays.asList(testToRun).contains("A")){
            System.out.println("Starting Test A.");
            String testCaseName = "Test A";

            ModelBuilder testABuilder = new TestABuilder(feature);
            TestCaseHandler testCaseHandlerA = new TestCaseHandler(testCaseName, testABuilder, List.of(strategy1, strategy2, strategy3) , groundTruthRuns, 161, savePathPrefix + testCaseName, false);
            ArrayList<TestCaseResult> resultsA = testCaseHandlerA.runTestCase(timeLimit, timeTick, timeError);

            if(save){
                testCaseHandlerA.storeResults(resultsA, savePathPrefix);
            }
            if(plot){
                testCaseHandlerA.plotResults(resultsA);
            }
        }

        // Test B
        if(Arrays.asList(testToRun).contains("B")){
            System.out.println("Starting Test B.");
            ModelBuilder testBBuilder = new TestBBuilder(feature);
            String testCaseName = "Test B";

            TestCaseHandler testCaseHandlerB = new TestCaseHandler(testCaseName, testBBuilder, List.of(strategy1, strategy2, strategy3) , groundTruthRuns, 185, savePathPrefix + testCaseName, false);
            ArrayList<TestCaseResult> resultsB = testCaseHandlerB.runTestCase(timeLimit, timeTick, timeError);

            if(save){
                testCaseHandlerB.storeResults(resultsB, savePathPrefix);
            }
            if(plot){
                testCaseHandlerB.plotResults(resultsB);
            }
        }

        // Test C
        if(Arrays.asList(testToRun).contains("C")){
            System.out.println("Starting Test C.");
            ModelBuilder testCBuilder = new TestCBuilder(feature);
            String testCaseName = "Test C";

            TestCaseHandler testCaseHandlerC = new TestCaseHandler(testCaseName, testCBuilder, List.of(strategy1, strategy2, strategy3) , groundTruthRuns, 221, savePathPrefix + testCaseName, false);
            ArrayList<TestCaseResult> resultsC = testCaseHandlerC.runTestCase(timeLimit, timeTick, timeError);

            if(save){
                testCaseHandlerC.storeResults(resultsC, savePathPrefix);
            }
            if(plot){
                testCaseHandlerC.plotResults(resultsC);
            }
        }


        // Test D
        if(Arrays.asList(testToRun).contains("D")){
            System.out.println("Starting Test D.");
            ModelBuilder testDBuilder = new TestDBuilder(feature);
            String testCaseName = "Test D";

            TestCaseHandler testCaseHandlerD = new TestCaseHandler(testCaseName, testDBuilder, List.of(strategy1, strategy2, strategy3) , groundTruthRuns, 229, savePathPrefix + testCaseName, false);
            ArrayList<TestCaseResult> resultsD = testCaseHandlerD.runTestCase(timeLimit, timeTick, timeError);

            if(save){
                testCaseHandlerD.storeResults(resultsD, savePathPrefix);
            }
            if(plot){
                testCaseHandlerD.plotResults(resultsD);
            }
        }

        // Test E
        if(Arrays.asList(testToRun).contains("E")){
            System.out.println("Starting Test E.");
            ModelBuilder testEBuilder = new TestEBuilder(feature);
            String testCaseName = "Test E";

            TestCaseHandler testCaseHandlerE = new TestCaseHandler(testCaseName, testEBuilder, List.of(strategy1, strategy2, strategy3) , groundTruthRuns, 647, savePathPrefix + testCaseName, false);
            ArrayList<TestCaseResult> resultsE = testCaseHandlerE.runTestCase(timeLimit, timeTick, timeError);

            if(save){
                testCaseHandlerE.storeResults(resultsE, savePathPrefix);
            }
            if(plot){
                testCaseHandlerE.plotResults(resultsE);
            }
        }

        // Test F
        if(Arrays.asList(testToRun).contains("F")){
            System.out.println("Starting Test F.");
            ModelBuilder testFBuilder = new TestFBuilder(feature);
            String testCaseName = "Test F";

            TestCaseHandler testCaseHandlerF = new TestCaseHandler(testCaseName, testFBuilder, List.of(strategy1, strategy2, strategy3) , groundTruthRuns, 647, savePathPrefix + testCaseName, false);
            ArrayList<TestCaseResult> resultsF = testCaseHandlerF.runTestCase(timeLimit, timeTick, timeError);

            if(save){
                testCaseHandlerF.storeResults(resultsF, savePathPrefix);
            }
            if(plot){
                testCaseHandlerF.plotResults(resultsF);
            }
        }

        // Test G
        if(Arrays.asList(testToRun).contains("G")){
            System.out.println("Starting Test G.");
            ModelBuilder testGBuilder = new TestGBuilder(feature);
            String testCaseName = "Test G";

            TestCaseHandler testCaseHandlerG = new TestCaseHandler(testCaseName, testGBuilder, List.of(strategy1, strategy2, strategy3) , groundTruthRuns, 470, savePathPrefix + testCaseName, false);
            ArrayList<TestCaseResult> resultsG = testCaseHandlerG.runTestCase(timeLimit.add(BigDecimal.valueOf(4)), timeTick, timeError);

            if(save){
                testCaseHandlerG.storeResults(resultsG, savePathPrefix);
            }
            if(plot){
                testCaseHandlerG.plotResults(resultsG);
            }
        }

        // Test H
        if(Arrays.asList(testToRun).contains("H")){
            System.out.println("Starting Test H.");
            ModelBuilder testHBuilder = new TestHBuilder(feature);
            String testCaseName = "Test H";

            TestCaseHandler testCaseHandlerH = new TestCaseHandler(testCaseName, testHBuilder, List.of(strategy1, strategy2, strategy3) , groundTruthRuns, 500, savePathPrefix + testCaseName, false);
            ArrayList<TestCaseResult> resultsH = testCaseHandlerH.runTestCase(timeLimit.add(BigDecimal.valueOf(4)), timeTick, timeError);

            if(save){
                testCaseHandlerH.storeResults(resultsH, savePathPrefix);
            }
            if(plot){
                testCaseHandlerH.plotResults(resultsH);
            }
        }
    }
}
