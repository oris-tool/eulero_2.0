package org.oristool.eulero.mains.julero;

import org.oristool.eulero.analysisheuristics.AnalysisHeuristic1;
import org.oristool.eulero.analysisheuristics.AnalysisHeuristic2;
import org.oristool.eulero.analysisheuristics.AnalysisHeuristic3;
import org.oristool.eulero.analysisheuristics.AnalysisHeuristicStrategy;
import org.oristool.eulero.mains.TestCaseHandler;
import org.oristool.eulero.mains.TestCaseResult;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.approximation.DoubleTruncatedEXPApproximation;
import org.oristool.eulero.math.approximation.TruncatedExponentialApproximation;
import org.oristool.eulero.models.ModelBuilder;
import org.oristool.eulero.models.RandomModelBuilder;
import org.oristool.eulero.models.blocksettings.*;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class Experimentation {
    public static void main(String[] args) {
        String savePathPrefix = System.getProperty("user.dir") + "/results/JULERO/Test";

        //Approximator approximator = new DoubleTruncatedEXPApproximation();
        Approximator approximator = new TruncatedExponentialApproximation();
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
        int groundTruthRuns = 20000;
        boolean save = false;
        boolean plot = true;

        BigInteger C = BigInteger.valueOf(3);
        BigInteger R = BigInteger.valueOf(8);
        boolean verbose = true;
        AnalysisHeuristicStrategy strategy1 = new AnalysisHeuristic1(C, R, approximator, verbose);
        AnalysisHeuristicStrategy strategy2 = new AnalysisHeuristic2(C, R, approximator, verbose);
        AnalysisHeuristicStrategy strategy3 = new AnalysisHeuristic3(C, R, approximator, verbose);

        String[] testToRun = {"RandomModel"};
        ArrayList<AnalysisHeuristicStrategy> strategies = new ArrayList<>();
        strategies.add(strategy1);
        strategies.add(strategy2);
        //strategies.add(strategy3);

        if(Arrays.asList(testToRun).contains("RandomModel")){
            String testCaseName = "Random Model";
            ArrayList<Set<BlockTypeSetting>> settings = new ArrayList<>();

            // Level 1 setting --> TOP
            Set<BlockTypeSetting> l1Settings = new HashSet<>();
            // // AND setting
            BlockTypeSetting l1AND = new ANDBlockSetting(0.5, 3);
            l1Settings.add(l1AND);
            // // SEQ setting
            BlockTypeSetting l1SEQ = new SEQBlockSetting(0.4, 3);
            l1Settings.add(l1SEQ);
            // // Xor setting
            BlockTypeSetting l1XOR = new XORBlockSetting(0.2, 3);
            l1Settings.add(l1XOR);
            // // DAG setting
            BlockTypeSetting l1DAG = new DAGBlockSetting(0, 2, 3, 2, 3, 1, 1, 2);
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
            BlockTypeSetting l2DAG = new DAGBlockSetting(1, 2, 2, 2, 3, 1, 1, 2);
            l2Settings.add(l2DAG);
            settings.add(l2Settings);*/


            // Level 3 setting --> Bottom
            /*Set<BlockTypeSetting> l3Settings = new HashSet<>();
            // // AND setting
            BlockTypeSetting l3AND = new ANDBlockSetting(0.4, 2);
            l3Settings.add(l3AND);
            // // SEQ setting
            BlockTypeSetting l3SEQ = new SEQBlockSetting(0.2, 3);
            l3Settings.add(l3SEQ);
            // // Xor setting
            BlockTypeSetting l3XOR = new XORBlockSetting(0.4, 4);
            l3Settings.add(l3XOR);
            // // DAG setting
            BlockTypeSetting l3DAG = new DAGBlockSetting(0, 3, 3, 3, 3, 2, 1, 2);
            l3Settings.add(l3DAG);
            settings.add(l3Settings);*/

            ModelBuilder builder = new RandomModelBuilder(feature, settings);
            TestCaseHandler testCaseHandler = new TestCaseHandler(testCaseName, builder, strategies, groundTruthRuns, 466, savePathPrefix + '/' + testCaseName, false, verbose);
            ArrayList<TestCaseResult> results = null;
            try {
                results = testCaseHandler.runTestCase(timeLimit, timeTick, timeError);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if(save){
                testCaseHandler.storeResults(results, savePathPrefix);
            }

            if(plot){
                testCaseHandler.plotResults(results);
            }
        }
    }
}
