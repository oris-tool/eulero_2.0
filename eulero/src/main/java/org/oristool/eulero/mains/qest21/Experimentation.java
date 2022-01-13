package org.oristool.eulero.mains.qest21;

import org.oristool.eulero.analysisheuristics.AnalysisHeuristic1;
import org.oristool.eulero.analysisheuristics.AnalysisHeuristic2;
import org.oristool.eulero.analysisheuristics.AnalysisHeuristic3;
import org.oristool.eulero.analysisheuristics.AnalysisHeuristicStrategy;
import org.oristool.eulero.graph.Activity;
import org.oristool.eulero.graph.Analytical;
import org.oristool.eulero.graph.DAG;
import org.oristool.eulero.mains.TestCaseHandler;
import org.oristool.eulero.mains.TestCaseResult;
import org.oristool.eulero.math.approximation.*;
import org.oristool.eulero.models.ModelBuilder;
import org.oristool.eulero.models.RandomModelBuilder;
import org.oristool.eulero.models.blocksettings.*;
import org.oristool.eulero.models.qest21.*;
import org.oristool.eulero.models.test.ComplexDAGBuilder;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.Marking;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class Experimentation {
    public static void main(String[] args) {
        String savePathPrefix = System.getProperty("user.dir") + "/results/AutomatedTest/ExpMixture/Forward_Uniform_DoubleEXP_C3_StoOrd";
        //Approximator approximator = new SplineTruncatedEXP(1);
        Approximator approximator = new DoubleTruncatedEXPApproximation();
        //Approximator approximator = new SplineBodyEXPTailApproximation(2);
        //Approximator approximator = new EXPMixtureApproximation();
        //Approximator approximator = new TruncatedExponentialApproximation();
        //StochasticTransitionFeature feature = StochasticTransitionFeature.newExpolynomial("1.35820981381836543 * 3 * Exp[-4 x]", new OmegaBigDecimal("0"), new OmegaBigDecimal("1"));
        StochasticTransitionFeature feature = StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);
        ArrayList<StochasticTransitionFeature> features = new ArrayList<>();
        ArrayList<BigDecimal> weights = new ArrayList<>();
        DBMZone bodyDomain = new DBMZone(Variable.X);
        bodyDomain.setCoefficient(Variable.X, Variable.TSTAR, new OmegaBigDecimal("0.35"));
        bodyDomain.setCoefficient(Variable.TSTAR, Variable.X, new OmegaBigDecimal("-0.08"));

        Expolynomial density = Expolynomial.fromString("1.7219599781778252 * Exp[3.4 x]");
        GEN gen = new GEN(bodyDomain, density);
        StochasticTransitionFeature feat = StochasticTransitionFeature.of(
                new PartitionedGEN(List.of(gen)));
        features.add(feat);
        weights.add(BigDecimal.valueOf(1 - 0.675));

        features.add(StochasticTransitionFeature.newExpolynomial(
                "4.976915353853855 * Exp[-2.2257 x]", new OmegaBigDecimal("0.35"), new OmegaBigDecimal("2.0")
        ));
        weights.add(BigDecimal.valueOf(0.675));



        BigDecimal timeLimit = BigDecimal.valueOf(1);
        BigDecimal timeTick = BigDecimal.valueOf(0.01);
        BigDecimal forwardReductionFactor = BigDecimal.valueOf(1);
        BigDecimal timeError = BigDecimal.valueOf(0.001) ;
        int groundTruthRuns = 30000;
        boolean save = true;
        boolean plot = true;

        BigInteger C = BigInteger.valueOf(3);
        BigInteger R = BigInteger.valueOf(1000000009);
        boolean verbose = true;
        AnalysisHeuristicStrategy strategy1 = new AnalysisHeuristic1(C, R, approximator, verbose);
        AnalysisHeuristicStrategy strategy2 = new AnalysisHeuristic2(C, R, approximator, verbose);
        AnalysisHeuristicStrategy strategy3 = new AnalysisHeuristic3(C, R, approximator, verbose);


        //String[] testToRun = {"A", "B", "C", "D", "E", "F", "G", "H"};
        String[] testToRun = {"RandomModel"};
        ArrayList<AnalysisHeuristicStrategy> strategies = new ArrayList<>();
        strategies.add(strategy1);
        strategies.add(strategy2);
        //strategies.add(strategy3);

        // Test A
        if(Arrays.asList(testToRun).contains("A")){
            System.out.println("Starting Test A.");
            String testCaseName = "Test A";

            ModelBuilder testABuilder = new TestABuilder(feature);
            TestCaseHandler testCaseHandlerA = new TestCaseHandler(testCaseName, testABuilder, strategies, groundTruthRuns, 1575, savePathPrefix + '/' + testCaseName, false, verbose);
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

            TestCaseHandler testCaseHandlerB = new TestCaseHandler(testCaseName, testBBuilder, strategies, groundTruthRuns, 498, savePathPrefix + '/' +  testCaseName, false, verbose);
            ArrayList<TestCaseResult> resultsB = null;
            try {
                resultsB = testCaseHandlerB.runTestCase(timeLimit, timeTick, forwardReductionFactor, timeError);
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

            TestCaseHandler testCaseHandlerC = new TestCaseHandler(testCaseName, testCBuilder, strategies, groundTruthRuns, 577, savePathPrefix + '/' + testCaseName, false, verbose);
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

            TestCaseHandler testCaseHandlerD = new TestCaseHandler(testCaseName, testDBuilder, strategies, groundTruthRuns, 588, savePathPrefix + '/' + testCaseName, false, verbose);
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

            TestCaseHandler testCaseHandlerE = new TestCaseHandler(testCaseName, testEBuilder, strategies, groundTruthRuns, 1142, savePathPrefix + '/' + testCaseName, false, verbose);
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

            TestCaseHandler testCaseHandlerF = new TestCaseHandler(testCaseName, testFBuilder, strategies, groundTruthRuns, 987, savePathPrefix + '/' + testCaseName, false, verbose);
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

            TestCaseHandler testCaseHandlerG = new TestCaseHandler(testCaseName, testGBuilder, strategies, groundTruthRuns, 573, savePathPrefix + '/' + testCaseName, false, verbose);
            ArrayList<TestCaseResult> resultsG = null;
            try {
                resultsG = testCaseHandlerG.runTestCase(timeLimit, timeTick, timeError);
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

            //testHBuilder.buildModel().analyze("5", "0.01", "0.001");
            TestCaseHandler testCaseHandlerH = new TestCaseHandler(testCaseName, testHBuilder, strategies, groundTruthRuns, 466, savePathPrefix + '/' + testCaseName, false, verbose);
            ArrayList<TestCaseResult> resultsH = null;
            try {
                resultsH = testCaseHandlerH.runTestCase(timeLimit, timeTick, timeError);
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

        if(Arrays.asList(testToRun).contains("Approssimante")) {
            String testCaseName = "Approssimante";

            Analytical q_0 = new Analytical("SimDAG_0_Q", feature);
            Analytical r_0 = new Analytical("SimDAG_0_R", feature);
            Analytical s_0 = new Analytical("SimDAG_0_S", feature);
            Analytical t_0 = new Analytical("SimDAG_0_T", feature);
            Analytical v_0 = new Analytical("SimDAG_0_V", feature);
            DAG simDag_0 = DAG.empty("SimDAG_0");
            q_0.addPrecondition(simDag_0.begin());
            r_0.addPrecondition(simDag_0.begin());
            s_0.addPrecondition(simDag_0.begin());
            t_0.addPrecondition(q_0, r_0);
            v_0.addPrecondition(r_0, s_0);
            simDag_0.end().addPrecondition(t_0, v_0);
            simDag_0.setEFT(simDag_0.low());
            simDag_0.setLFT(simDag_0.upp());


            Analytical q_0Copy = new Analytical("SimDAG_0_Q", feature);
            Analytical r_0Copy = new Analytical("SimDAG_0_R", feature);
            Analytical s_0Copy = new Analytical("SimDAG_0_S", feature);
            Analytical t_0Copy = new Analytical("SimDAG_0_U", feature);
            Analytical v_0Copy = new Analytical("SimDAG_0_V", feature);

            DAG simDag_0Copy = DAG.empty("SimDAG_0");
            q_0Copy.addPrecondition(simDag_0Copy.begin());
            r_0Copy.addPrecondition(simDag_0Copy.begin());
            s_0Copy.addPrecondition(simDag_0Copy.begin());
            t_0Copy.addPrecondition(q_0Copy, r_0Copy);
            v_0Copy.addPrecondition(r_0Copy, s_0Copy);
            simDag_0Copy.end().addPrecondition(t_0Copy, v_0Copy);
            simDag_0Copy.setEFT(simDag_0Copy.low());
            simDag_0Copy.setLFT(simDag_0Copy.upp());

            long time = System.nanoTime();
            TransientSolution<DeterministicEnablingState, RewardRate> s1 = simDag_0Copy.analyze(simDag_0Copy.LFT().toString(), timeTick.toString(), timeError.toString());
            System.out.println("GT done in " + String.format("%.3f seconds",
                    (System.nanoTime() - time) / 1e9) + "...");
            Approximator approximator2 = new TruncatedExponentialApproximation();
            Analytical newOneCopy = new Analytical(simDag_0Copy.name() + "_approximated", approximator.getApproximatedStochasticTransitionFeatures(
                    strategy1.analyze(simDag_0Copy, simDag_0Copy.LFT(), timeTick, forwardReductionFactor, timeError),
                    simDag_0Copy.EFT().doubleValue(),
                    simDag_0Copy.LFT().doubleValue(),
                    timeTick
            ), approximator.stochasticTransitionFeatureWeights());
            TransientSolution<Marking, RewardRate> s2 = newOneCopy.forwardAnalyze(newOneCopy.LFT().toString(), timeTick.toString(), timeError.toString());

            Analytical newOne = new Analytical(simDag_0.name() + "_approximated", approximator2.getApproximatedStochasticTransitionFeatures(
                    strategy1.analyze(simDag_0, simDag_0.LFT(), timeTick, forwardReductionFactor, timeError),
                    simDag_0.EFT().doubleValue(),
                    simDag_0.LFT().doubleValue(),
                    timeTick
            ), approximator2.stochasticTransitionFeatureWeights());
            TransientSolution<Marking, RewardRate> s3 = newOne.forwardAnalyze(simDag_0.LFT().toString(), timeTick.toString(), timeError.toString());
            System.out.println("Sim done in " + String.format("%.3f seconds",
                    (System.nanoTime() - time) / 1e9) + "...");
            time = System.nanoTime();


            ActivityViewer.CompareResults("Test", List.of("Real", "Appr", "apprVecch"), List.of(
                    new TestCaseResult("Real", s1, 0, simDag_0Copy.LFT().divide(timeTick).intValue(), timeTick.doubleValue(), 0),
                    new TestCaseResult("Appr", s2, 0, simDag_0Copy.LFT().divide(timeTick).intValue(), timeTick.doubleValue(), 0),
                    new TestCaseResult("ApprVecch", s3, 0, simDag_0Copy.LFT().divide(timeTick).intValue(), timeTick.doubleValue(), 0)));

        }

        if(Arrays.asList(testToRun).contains("ComplexDAG")){
            System.out.println("Starting Test on ComplexDAG.");
            ModelBuilder complexDAGBuilder = new ComplexDAGBuilder(features, weights);
            String testCaseName = "Complex DAG";

            TestCaseHandler testCaseHandler = new TestCaseHandler(testCaseName, complexDAGBuilder, strategies, groundTruthRuns, 466, savePathPrefix + '/' + testCaseName, false, verbose);
            ArrayList<TestCaseResult> resultsH = null;
            try {
                resultsH = testCaseHandler.runTestCase(timeLimit, timeTick, timeError);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if(plot){
                testCaseHandler.plotResults(resultsH);
            }
        }

        if(Arrays.asList(testToRun).contains("ApproximationOfExpol")){
            String testCaseName = "Complex DAG";

            Analytical q_0 = new Analytical("SimDAG_0_Q", features, weights);

            TransientSolution<DeterministicEnablingState, RewardRate> s1 = q_0.analyze(q_0.LFT().toString(), timeTick.toString(), timeError.toString());
            Analytical newOneCopy = new Analytical(q_0.name() + "_approximated", approximator.getApproximatedStochasticTransitionFeatures(
                    strategy1.analyze(q_0, q_0.LFT(), timeTick, forwardReductionFactor, timeError),
                    q_0.EFT().doubleValue(),
                    q_0.LFT().doubleValue(),
                    timeTick
            ), approximator.stochasticTransitionFeatureWeights());
            TransientSolution<Marking, RewardRate> s3 = newOneCopy.forwardAnalyze(q_0.LFT().toString(), timeTick.toString(), timeError.toString());
            ActivityViewer.CompareResults("Test", List.of("Real", "Appr", "apprVecch"), List.of(
                    new TestCaseResult("Real", s1, 0, q_0.LFT().divide(timeTick).intValue(), timeTick.doubleValue(), 0),
                    new TestCaseResult("Appr", s3, 0, q_0.LFT().divide(timeTick).intValue(), timeTick.doubleValue(), 0)));

        }

        if(Arrays.asList(testToRun).contains("RandomModel")){
            String testCaseName = "Random Model";
            ArrayList<Set<BlockTypeSetting>> settings = new ArrayList<>();

            // Level 1 setting --> TOP
            Set<BlockTypeSetting> l1Settings = new HashSet<>();
            // // AND setting
            BlockTypeSetting l1AND = new ANDBlockSetting(0, 2);
            l1Settings.add(l1AND);
            // // SEQ setting
            BlockTypeSetting l1SEQ = new SEQBlockSetting(0, 3);
            l1Settings.add(l1SEQ);
            // // Xor setting
            BlockTypeSetting l1XOR = new XORBlockSetting(0, 3);
            l1Settings.add(l1XOR);
            // // DAG setting
            BlockTypeSetting l1DAG = new DAGBlockSetting(1, 3, 3, 3, 3, 2, 1, 2);
            l1Settings.add(l1DAG);
            settings.add(l1Settings);


            // Level 2 setting  --> Middle
            /*Set<BlockTypeSetting> l2Settings = new HashSet<>();
            // // AND setting
            BlockTypeSetting l2AND = new ANDBlockSetting(0, 3);
            l2Settings.add(l2AND);
            // // SEQ setting
            BlockTypeSetting l2SEQ = new SEQBlockSetting(0, 2);
            l2Settings.add(l2SEQ);
            // // Xor setting
            BlockTypeSetting l2XOR = new XORBlockSetting(0, 3);
            l2Settings.add(l2XOR);
            // // DAG setting
            BlockTypeSetting l2DAG = new DAGBlockSetting(1, 3, 3, 3, 3, 2, 1, 2);
            l2Settings.add(l2DAG);
            settings.add(l2Settings);


            // Level 3 setting --> Bottom
            Set<BlockTypeSetting> l3Settings = new HashSet<>();
            // // AND setting
            BlockTypeSetting l3AND = new ANDBlockSetting(0, 2);
            l3Settings.add(l3AND);
            // // SEQ setting
            BlockTypeSetting l3SEQ = new SEQBlockSetting(0, 3);
            l3Settings.add(l3SEQ);
            // // Xor setting
            BlockTypeSetting l3XOR = new XORBlockSetting(0, 4);
            l3Settings.add(l3XOR);
            // // DAG setting
            BlockTypeSetting l3DAG = new DAGBlockSetting(1, 3, 3, 3, 3, 2, 1, 2);
            l3Settings.add(l3DAG);
            settings.add(l3Settings);*/

            ModelBuilder builder = new RandomModelBuilder(feature, settings);
            TestCaseHandler testCaseHandlerH = new TestCaseHandler(testCaseName, builder, strategies, groundTruthRuns, 466, savePathPrefix + '/' + testCaseName, false, verbose);
            ArrayList<TestCaseResult> resultsH = null;
            try {
                resultsH = testCaseHandlerH.runTestCase(timeLimit, timeTick, timeError);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if(plot){
                testCaseHandlerH.plotResults(resultsH);
            }
        }
    }
}
