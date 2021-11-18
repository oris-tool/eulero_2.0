package org.oristool.eulero.analysisheuristics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oristool.eulero.MainHelper;
import org.oristool.eulero.graph.Analytical;
import org.oristool.eulero.graph.DAG;
import org.oristool.eulero.graph.Repeat;
import org.oristool.eulero.graph.Xor;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.eulero.math.approximation.SplineBodyEXPTailApproximation;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

class AnalysisHeuristic1Test {

    @Test
    void TestWellNested() throws InterruptedException {
        BigDecimal timeLimit = BigDecimal.valueOf(40);
        BigDecimal step = BigDecimal.valueOf(0.1);
        BigDecimal error = BigDecimal.valueOf(0.1);
        int simulationRuns = 20000;

        BigInteger C =  BigInteger.valueOf(3);
        BigInteger R =  BigInteger.valueOf(10);
        Approximator approximator = new SplineBodyEXPTailApproximation(3);
        AnalysisHeuristicStrategy analyzer = new AnalysisHeuristic1(C, R, approximator);

        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(8));

        StochasticTransitionFeature unif2_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(2), BigDecimal.valueOf(10));

        DAG dag = DAG.sequence("S1",
                new Analytical("A1", unif0_10),
                DAG.forkJoin("F1",
                        new Analytical("A2", unif0_10),
                        new Analytical("A3", unif2_10)
                ),
                new Analytical("A4", unif2_10),
                DAG.forkJoin("F2",
                        new Analytical("A5", unif0_10),
                        new Xor(
                                "X",
                                List.of(
                                        new Analytical("A6", unif0_10),
                                        new Analytical("A7", unif2_10)
                                ),
                                List.of(0.3, 0.7)
                        )
                )
        );

        // Todo, fai ritornare il risultato dalla classe AnalysisHeuristic
        MainHelper.ResultWrapper simulation = new MainHelper.ResultWrapper(
                dag.simulate(timeLimit.toString(), step.toString(), simulationRuns), dag.EFT().divide(step).intValue(), dag.LFT().divide(step).intValue(), step.doubleValue()
        );

        MainHelper.ResultWrapper analysis = new MainHelper.ResultWrapper(analyzer.analyze(dag, timeLimit, step, error), dag.EFT().divide(step).intValue(), dag.LFT().divide(step).intValue(), step.doubleValue());
        ActivityViewer.CompareResults("XOR-TEST", false, "", List.of("Simulation", "Analysis"), simulation, analysis);
        Thread.sleep(20000);
    }

    @Test
    void TestWellNestedAndSimpleRepetition() throws InterruptedException {
        BigDecimal timeLimit = BigDecimal.valueOf(80);
        BigDecimal step = BigDecimal.valueOf(1.0);
        BigDecimal error = BigDecimal.valueOf(0.1);
        int simulationRuns = 20000;

        BigInteger C =  BigInteger.valueOf(3);
        BigInteger R =  BigInteger.valueOf(10);
        Approximator approximator = new SplineBodyEXPTailApproximation(3);
        AnalysisHeuristicStrategy analyzer = new AnalysisHeuristic1(C, R, approximator);

        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(8));

        StochasticTransitionFeature unif2_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(2), BigDecimal.valueOf(10));

        DAG dag = DAG.sequence("S1",
                new Analytical("A1", unif0_10),
                DAG.forkJoin("F1",
                        new Analytical("A2", unif0_10),
                        new Analytical("A3", unif2_10)
                ),
                new Analytical("A4", unif2_10),
                DAG.forkJoin("F2",
                        new Repeat("SimpleREP", 0.4,
                                DAG.sequence("SR1",
                                        new Analytical("SR21", unif0_10),
                                        DAG.forkJoin("SR3",
                                                new Analytical("SR4", unif0_10),
                                                new Analytical("SR5", unif2_10)
                                        ),
                                        new Analytical("SR6", unif0_10)
                                )
                        ),
                        new Xor(
                                "X",
                                List.of(
                                        new Analytical("A6", unif0_10),
                                        new Analytical("A7", unif2_10)
                                ),
                                List.of(0.3, 0.7)
                        )
                )
        );

        // Todo, fai ritornare il risultato dalla classe AnalysisHeuristic
        MainHelper.ResultWrapper simulation = new MainHelper.ResultWrapper(
                dag.simulate(timeLimit.toString(), step.toString(), simulationRuns), dag.EFT().divide(step).intValue(), dag.LFT().divide(step).intValue(), step.doubleValue()
        );

        MainHelper.ResultWrapper analysis = new MainHelper.ResultWrapper(analyzer.analyze(dag, timeLimit, step, error), dag.EFT().divide(step).intValue(), dag.LFT().divide(step).intValue(), step.doubleValue());
        ActivityViewer.CompareResults("XOR-TEST", false, "", List.of("Simulation", "Analysis"), simulation, analysis);
        Thread.sleep(20000);
    }

    @Test
    void TestWellNestedAndComplexRepetition() throws InterruptedException {
        BigDecimal timeLimit = BigDecimal.valueOf(4.8);
        BigDecimal step = BigDecimal.valueOf(0.01);
        int simulationRuns = 20000;

        BigInteger C =  BigInteger.valueOf(3);
        BigInteger R =  BigInteger.valueOf(20);
        Approximator approximator = new SplineBodyEXPTailApproximation(3);
        //Approximator approximator = new EXPMixtureApproximation();
        AnalysisHeuristicStrategy analyzer = new AnalysisHeuristic1(C, R, approximator);

        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(0.8));

        StochasticTransitionFeature unif2_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(0.2), BigDecimal.valueOf(1.0));

        DAG dag = DAG.sequence("S1",
                new Analytical("A4", unif2_10),
                DAG.forkJoin("F2",
                        new Repeat("SimpleREP", 0.3,
                                DAG.sequence("SR1",
                                        new Analytical("SR21", unif0_10),
                                        DAG.forkJoin("SR3",
                                                new Analytical("SR4", unif0_10),
                                                new Analytical("A1", unif0_10),
                                                DAG.forkJoin("F1",
                                                        new Analytical("A2", unif0_10),
                                                        new Analytical("A3", unif2_10)
                                                )
                                        ),
                                        new Analytical("SR6", unif0_10)
                                )
                        ),
                        new Xor(
                                "X",
                                List.of(
                                        new Analytical("A6", unif0_10),
                                        new Analytical("A7", unif2_10)
                                ),
                                List.of(0.3, 0.7)
                        )
                )
        );

        MainHelper.ResultWrapper simulation = new MainHelper.ResultWrapper(
                dag.simulate(timeLimit.toString(), step.toString(), simulationRuns), dag.EFT().divide(step).intValue(), dag.LFT().divide(step).intValue(), step.doubleValue()
        );

        MainHelper.ResultWrapper analysis = new MainHelper.ResultWrapper(analyzer.analyze(dag, timeLimit, step, step), dag.EFT().divide(step).intValue(), dag.LFT().divide(step).intValue(), step.doubleValue());
        ActivityViewer.CompareResults("XOR-TEST", false, "", List.of("Simulation", "Analysis"), simulation, analysis);
        Thread.sleep(20000);
    }

    @Test
    void TestComplexDAG() throws InterruptedException {
        BigDecimal timeLimit = BigDecimal.valueOf(4.8);
        BigDecimal step = BigDecimal.valueOf(0.01);
        int simulationRuns = 200;

        BigInteger C =  BigInteger.valueOf(3);
        BigInteger R =  BigInteger.valueOf(20);
        //Approximator approximator = new SplineBodyEXPTailApproximation(3);
        Approximator approximator = new EXPMixtureApproximation();
        AnalysisHeuristicStrategy analyzer = new AnalysisHeuristic1(C, R, approximator);

        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(0.8));

        Analytical q = new Analytical("Q", unif0_10);
        Analytical r = new Analytical("R", unif0_10);
        Analytical s = new Analytical("S", unif0_10);
        Analytical v = new Analytical("V", unif0_10);

        DAG tu = DAG.forkJoin("TU",
                DAG.sequence("T",
                        new Analytical("T1", unif0_10),
                        new Analytical("T2", unif0_10)
                ), new Analytical("U", unif0_10)
        );

        DAG wx = DAG.forkJoin("WX",
                DAG.sequence("X",
                        new Analytical("X1", unif0_10),
                        new Analytical("X2", unif0_10)
                ),
                new Analytical("W", unif0_10),
                new Analytical("Y", unif0_10)
        );

        DAG pComplex = DAG.empty("P");
        q.addPrecondition(pComplex.begin());
        r.addPrecondition(pComplex.begin());
        s.addPrecondition(pComplex.begin());
        tu.addPrecondition(q, r);
        v.addPrecondition(r);
        wx.addPrecondition(s, r);
        pComplex.end().addPrecondition(tu, v, wx);
        // TODO questo va sistemato e hide
        pComplex.setEFT(pComplex.low());
        pComplex.setLFT(pComplex.upp());

        MainHelper.ResultWrapper simulation = new MainHelper.ResultWrapper(
                pComplex.simulate(timeLimit.toString(), step.toString(), simulationRuns), pComplex.EFT().divide(step).intValue(), pComplex.LFT().divide(step).intValue(), step.doubleValue()
        );

        MainHelper.ResultWrapper analysis = new MainHelper.ResultWrapper(analyzer.analyze(pComplex, timeLimit, step, step), pComplex.EFT().divide(step).intValue(), pComplex.LFT().divide(step).intValue(), step.doubleValue());
        ActivityViewer.CompareResults("XOR-TEST", false, "", List.of("Simulation", "Analysis"), simulation, analysis);
        Thread.sleep(20000);
    }



    // TODO add DAG test;
    // TODO check also R, SimplifiedR, SimplifiedC
}
