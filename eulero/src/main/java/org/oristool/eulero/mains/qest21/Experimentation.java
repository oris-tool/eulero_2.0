package org.oristool.eulero.mains.qest21;

import org.oristool.eulero.analysisheuristics.AnalysisHeuristic1;
import org.oristool.eulero.analysisheuristics.AnalysisHeuristic2;
import org.oristool.eulero.analysisheuristics.AnalysisHeuristic3;
import org.oristool.eulero.analysisheuristics.AnalysisHeuristicStrategy;
import org.oristool.eulero.graph.Activity;
import org.oristool.eulero.mains.TestCaseHandler;
import org.oristool.eulero.mains.TestCaseResult;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.eulero.models.ModelBuilder;
import org.oristool.eulero.models.ModelBuilder_Deprecated;
import org.oristool.eulero.models.qest21.*;
import org.oristool.eulero.models.qest21_deprecated.TestABuilderDeprecated;
import org.oristool.eulero.models.qest21_deprecated.TestBBuilderDeprecated;
import org.oristool.eulero.models.qest21_deprecated.TestDBuilderDeprecated;
import org.oristool.eulero.models.qest21_deprecated.TestHBuilderDeprecated;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experimentation {
    public static void main(String[] args) {
        String savePathPrefix = System.getProperty("user.dir") + "/results/AutomatedTest/ExpMixture/Regenerative+Uniform_C3";
        String GTCDF = "/CDF";
        String GTtimes = "/times";
        String GTPathSuffix = "/GroundTruth.txt";
        //Approximator approximator = new SplineBodyEXPTailApproximation(3);
        Approximator approximator = new EXPMixtureApproximation();
        //StochasticTransitionFeature feature = StochasticTransitionFeature.newExpolynomial("1.1302477 * 3 * Exp[-4 x] + 1.1302477 *  x^1 * Exp[-2 x]", new OmegaBigDecimal("0"), new OmegaBigDecimal("1"));
        //StochasticTransitionFeature feature = StochasticTransitionFeature.newExpolynomial("8.809103208401715 * x^1 * Exp[-0.8 x] + -8.809103208401715 * x^2 * Exp[-0.8 x]", new OmegaBigDecimal("0"), new OmegaBigDecimal("1"));
        //StochasticTransitionFeature feature = StochasticTransitionFeature.newExpolynomial("1.35820981381836543 * 3 * Exp[-4 x]", new OmegaBigDecimal("0"), new OmegaBigDecimal("1"));
        StochasticTransitionFeature feature = StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);
        BigDecimal timeLimit = BigDecimal.valueOf(8);
        BigDecimal timeTick = BigDecimal.valueOf(0.01) ;
        BigDecimal forwardReductionFactor = BigDecimal.valueOf(10) ; // TODO passalo come parametro... magari si possono fare ulteriori estensioni
        BigDecimal timeError = timeTick.divide(BigDecimal.valueOf(10));
        int groundTruthRuns = 1000;
        boolean save = true;
        boolean plot = true;
        boolean GTFromFile = true;

        BigInteger C = BigInteger.valueOf(3);
        BigInteger R = BigInteger.valueOf(1000000009);
        AnalysisHeuristicStrategy strategy1 = new AnalysisHeuristic1(C, R, approximator);
        AnalysisHeuristicStrategy strategy2 = new AnalysisHeuristic2(C, R, approximator);
        AnalysisHeuristicStrategy strategy3 = new AnalysisHeuristic3(C, R, approximator);


        //String[] testToRun = {"A","B", "C", "D", "E", "F", "G",  "H"};
        String[] testToRun = {"Caso"};

        // Test A
        if(Arrays.asList(testToRun).contains("A")){
            System.out.println("Starting Test A.");
            String testCaseName = "Test A";

            ModelBuilder testABuilder = new TestABuilder(feature);
            TestCaseHandler testCaseHandlerA = new TestCaseHandler(testCaseName, testABuilder, List.of(strategy1, strategy2, strategy3) , groundTruthRuns, 1575, savePathPrefix + '/' + testCaseName, false);
            ArrayList<TestCaseResult> resultsA = null;
            try {
                resultsA = testCaseHandlerA.runTestCase(timeLimit, timeTick, timeError);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

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

            TestCaseHandler testCaseHandlerB = new TestCaseHandler(testCaseName, testBBuilder, List.of(strategy2) , groundTruthRuns, 498, savePathPrefix + '/' +  testCaseName, false);
            ArrayList<TestCaseResult> resultsB = null;
            try {
                resultsB = testCaseHandlerB.runTestCase(timeLimit, timeTick, timeError);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

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

            TestCaseHandler testCaseHandlerC = new TestCaseHandler(testCaseName, testCBuilder, List.of(strategy1, strategy2, strategy3) , groundTruthRuns, 577, savePathPrefix + '/' + testCaseName, false);
            ArrayList<TestCaseResult> resultsC = null;
            try {
                resultsC = testCaseHandlerC.runTestCase(timeLimit, timeTick, timeError);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

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

            TestCaseHandler testCaseHandlerD = new TestCaseHandler(testCaseName, testDBuilder, List.of(strategy1, strategy2, strategy3) , groundTruthRuns, 588, savePathPrefix + '/' + testCaseName, false);
            ArrayList<TestCaseResult> resultsD = null;
            try {
                resultsD = testCaseHandlerD.runTestCase(timeLimit, timeTick, timeError);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

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

            TestCaseHandler testCaseHandlerE = new TestCaseHandler(testCaseName, testEBuilder, List.of(strategy1, strategy2, strategy3) , groundTruthRuns, 1142, savePathPrefix + '/' + testCaseName, false);
            ArrayList<TestCaseResult> resultsE = null;
            try {
                resultsE = testCaseHandlerE.runTestCase(timeLimit, timeTick, timeError);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

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

            TestCaseHandler testCaseHandlerF = new TestCaseHandler(testCaseName, testFBuilder, List.of(strategy1, strategy2, strategy3) , groundTruthRuns, 987, savePathPrefix + '/' + testCaseName, false);
            ArrayList<TestCaseResult> resultsF = null;
            try {
                resultsF = testCaseHandlerF.runTestCase(timeLimit, timeTick, timeError);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

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

            TestCaseHandler testCaseHandlerG = new TestCaseHandler(testCaseName, testGBuilder, List.of(strategy1, strategy2, strategy3) , groundTruthRuns, 573, savePathPrefix + '/' + testCaseName, false);
            ArrayList<TestCaseResult> resultsG = null;
            try {
                resultsG = testCaseHandlerG.runTestCase(timeLimit.add(BigDecimal.valueOf(4)), timeTick, timeError);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

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

            TestCaseHandler testCaseHandlerH = new TestCaseHandler(testCaseName, testHBuilder, List.of(strategy2) , groundTruthRuns, 466, savePathPrefix + '/' + testCaseName, false);
            ArrayList<TestCaseResult> resultsH = null;
            try {
                resultsH = testCaseHandlerH.runTestCase(timeLimit.add(BigDecimal.valueOf(2)), timeTick, timeError);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if(save){
                testCaseHandlerH.storeResults(resultsH, savePathPrefix);
            }
            if(plot){
                testCaseHandlerH.plotResults(resultsH);
            }
        }

        if(Arrays.asList(testToRun).contains("Caso")){
            System.out.println("Starting Test Caso.");
            String testCaseName = "Test Caso";
            ModelBuilder_Deprecated testBuilder1 = new TestBBuilderDeprecated(feature, approximator);
            long time = System.currentTimeMillis();
            Activity test = testBuilder1.buildModelForAnalysis_Heuristic2(timeLimit, timeTick);
            test.analyze(timeLimit.toString(), timeTick.toString(), timeError.toString());
            System.out.println("Analysis done in " +
                    (System.currentTimeMillis() - time) + "...");

            System.out.println("\n\nStarting Test Caso.");
            ModelBuilder_Deprecated testBuilder2 = new TestDBuilderDeprecated(feature, approximator);
            time = System.currentTimeMillis();
            Activity test2 = testBuilder2.buildModelForAnalysis_Heuristic2(timeLimit, timeTick);
            test2.analyze(timeLimit.toString(), timeTick.toString(), timeError.toString());
            System.out.println("Analysis done in " +
                    (System.currentTimeMillis() - time) + "...");

            System.out.println("\n\nStarting Test Caso.");
            ModelBuilder_Deprecated testBuilder3 = new TestABuilderDeprecated(feature, approximator);
            time = System.currentTimeMillis();
            Activity test3 = testBuilder3.buildModelForAnalysis_Heuristic2(timeLimit, timeTick);
            test3.analyze(timeLimit.toString(), timeTick.toString(), timeError.toString());
            System.out.println("Analysis done in " +
                    (System.currentTimeMillis() - time) + "...");

            System.out.println("\n\nStarting Test Caso.");
            ModelBuilder_Deprecated testBuilder4 = new TestHBuilderDeprecated(feature, approximator);
            time = System.currentTimeMillis();
            Activity test4 = testBuilder4.buildModelForAnalysis_Heuristic2(timeLimit, timeTick);
            test4.analyze(timeLimit.toString(), timeTick.toString(), timeError.toString());
            System.out.println("Analysis done in " +
                    (System.currentTimeMillis() - time) + "...");



            /*ModelBuilder testBuilder = new TestBBuilder(feature);

            TestCaseHandler testCaseHandlerH = new TestCaseHandler(testCaseName, testBuilder, List.of(strategy2) , groundTruthRuns, 466, savePathPrefix + '/' + testCaseName, false);
            ArrayList<TestCaseResult> resultsH = testCaseHandlerH.runTestCase(timeLimit.add(BigDecimal.valueOf(2)), timeTick, timeError);

            if(plot){
                testCaseHandlerH.plotResults(resultsH);
            }*/
        }
    }
}
