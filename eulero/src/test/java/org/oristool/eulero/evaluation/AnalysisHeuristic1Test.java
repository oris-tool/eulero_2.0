package org.oristool.eulero.evaluation;

import org.junit.jupiter.api.Test;
import org.oristool.eulero.evaluation.heuristic.EvaluationResult;
import org.oristool.eulero.evaluation.heuristic.AnalysisHeuristic1;
import org.oristool.eulero.evaluation.heuristic.AnalysisHeuristicStrategy;
import org.oristool.eulero.workflow.Simple;
import org.oristool.eulero.workflow.DAG;
import org.oristool.eulero.workflow.Repeat;
import org.oristool.eulero.workflow.Xor;
import org.oristool.eulero.evaluation.approximator.Approximator;
import org.oristool.eulero.evaluation.approximator.EXPMixtureApproximation;
import org.oristool.eulero.evaluation.approximator.SplineBodyEXPTailApproximation;
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
        //Approximator approximator = new SplineBodyEXPTailApproximation(3);
        Approximator approximator = new EXPMixtureApproximation();
        AnalysisHeuristicStrategy analyzer = new AnalysisHeuristic1(C, R, approximator, true);

        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(8));

        StochasticTransitionFeature unif2_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(2), BigDecimal.valueOf(10));

        DAG dag = DAG.sequence("S1",
                new Simple("A1", unif0_10),
                DAG.forkJoin("F1",
                        new Simple("A2", unif0_10),
                        new Simple("A3", unif2_10)
                ),
                new Simple("A4", unif2_10),
                DAG.forkJoin("F2",
                        new Simple("A5", unif0_10),
                        new Xor(
                                "X",
                                List.of(
                                        new Simple("A6", unif0_10),
                                        new Simple("A7", unif2_10)
                                ),
                                List.of(0.3, 0.7)
                        )
                )
        );

        // Todo, fai ritornare il risultato dalla classe AnalysisHeuristic
        EvaluationResult simulation = new EvaluationResult("Simulation",
                dag.simulate(timeLimit.toString(), step.toString(), simulationRuns), dag.EFT().divide(step).intValue(), dag.LFT().divide(step).intValue(), step.doubleValue(), 0
        );

        EvaluationResult analysis = new EvaluationResult("Simulation", analyzer.analyze(dag, timeLimit, step, error, ""), dag.EFT().divide(step).intValue(), dag.LFT().divide(step).intValue(), step.doubleValue(), 0);
        ActivityViewer.CompareResults("XOR-TEST", false, "XOR-TEST", List.of("Simulation", "Analysis"), simulation, analysis);
        Thread.sleep(20000);
    }

    @Test
    void TestWellNestedAndSimpleRepetition() throws InterruptedException {
        BigDecimal timeLimit = BigDecimal.valueOf(8.0);
        BigDecimal step = BigDecimal.valueOf(0.01);
        BigDecimal error = BigDecimal.valueOf(0.01);
        int simulationRuns = 20000;

        BigInteger C =  BigInteger.valueOf(3);
        BigInteger R =  BigInteger.valueOf(10);
        Approximator approximator = new SplineBodyEXPTailApproximation(3);
        //Approximator approximator = new EXPMixtureApproximation();
        AnalysisHeuristicStrategy analyzer = new AnalysisHeuristic1(C, R, approximator, true);

        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(0.8));

        StochasticTransitionFeature unif2_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(0.2), BigDecimal.valueOf(1.0));

        DAG dag = DAG.sequence("S1",
                new Simple("A1", unif0_10),
                DAG.forkJoin("F1",
                        new Simple("A2", unif0_10),
                        new Simple("A3", unif2_10)
                ),
                new Simple("A4", unif2_10),
                DAG.forkJoin("F2",
                        new Repeat("SimpleREP", 0.4,
                                DAG.sequence("SR1",
                                        new Simple("SR21", unif0_10),
                                        DAG.forkJoin("SR3",
                                                new Simple("SR4", unif0_10),
                                                new Simple("SR5", unif2_10)
                                        ),
                                        new Simple("SR6", unif0_10)
                                )
                        ),
                        new Xor(
                                "X",
                                List.of(
                                        new Simple("A6", unif0_10),
                                        new Simple("A7", unif2_10)
                                ),
                                List.of(0.3, 0.7)
                        )
                )
        );

        // Todo, fai ritornare il risultato dalla classe AnalysisHeuristic
        EvaluationResult simulation = new EvaluationResult("Simulation",
                dag.simulate(timeLimit.toString(), step.toString(), simulationRuns), dag.EFT().divide(step).intValue(), dag.LFT().divide(step).intValue(), step.doubleValue(), 0
        );

        EvaluationResult analysis = new EvaluationResult("Simulation", analyzer.analyze(dag, timeLimit, step, error, ""), dag.EFT().divide(step).intValue(), dag.LFT().divide(step).intValue(), step.doubleValue(), 0);
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
        //Approximator approximator = new SplineBodyEXPTailApproximation(3);
        Approximator approximator = new EXPMixtureApproximation();
        AnalysisHeuristicStrategy analyzer = new AnalysisHeuristic1(C, R, approximator, true);

        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(0.8));

        StochasticTransitionFeature unif2_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(0.2), BigDecimal.valueOf(1.0));

        DAG dag = DAG.sequence("S1",
                new Simple("A4", unif2_10),
                DAG.forkJoin("F2",
                        new Repeat("SimpleREP", 0.3,
                                DAG.sequence("SR1",
                                        new Simple("SR21", unif0_10),
                                        DAG.forkJoin("SR3",
                                                new Simple("SR4", unif0_10),
                                                new Simple("A1", unif0_10),
                                                DAG.forkJoin("F1",
                                                        new Simple("A2", unif0_10),
                                                        new Simple("A3", unif2_10)
                                                )
                                        ),
                                        new Simple("SR6", unif0_10)
                                )
                        ),
                        new Xor(
                                "X",
                                List.of(
                                        new Simple("A6", unif0_10),
                                        new Simple("A7", unif2_10)
                                ),
                                List.of(0.3, 0.7)
                        )
                )
        );

        EvaluationResult simulation = new EvaluationResult("Simulation",
                dag.simulate(timeLimit.toString(), step.toString(), simulationRuns), dag.EFT().divide(step).intValue(), dag.LFT().divide(step).intValue(), step.doubleValue(), 0
        );

        EvaluationResult analysis = new EvaluationResult("Simulation", analyzer.analyze(dag, timeLimit, step, step, ""), dag.EFT().divide(step).intValue(), dag.LFT().divide(step).intValue(), step.doubleValue(), 0);
        ActivityViewer.CompareResults("XOR-TEST", false, "", List.of("Simulation", "Analysis"), simulation, analysis);
        Thread.sleep(20000);
    }

    @Test
    void TestComplexDAG() throws InterruptedException {
        BigDecimal timeLimit = BigDecimal.valueOf(4.8);
        BigDecimal step = BigDecimal.valueOf(0.01);
        int simulationRuns = 30000;

        BigInteger C =  BigInteger.valueOf(2);
        BigInteger R =  BigInteger.valueOf(8);
        Approximator approximator = new SplineBodyEXPTailApproximation(1);
        //Approximator approximator = new EXPMixtureApproximation();
        AnalysisHeuristicStrategy analyzer = new AnalysisHeuristic1(C, R, approximator, true);

        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(0.8));

        Simple q = new Simple("Q", unif0_10);
        Simple r = new Simple("R", unif0_10);
        Simple s = new Simple("S", unif0_10);
        Simple v = new Simple("V", unif0_10);

        DAG tu = DAG.forkJoin("TU",
                DAG.sequence("T",
                        new Simple("T1", unif0_10),
                        new Simple("T2", unif0_10)
                ), new Simple("U", unif0_10)
        );

        DAG wx = DAG.forkJoin("WX",
                DAG.sequence("X",
                        new Simple("X1", unif0_10),
                        new Simple("X2", unif0_10)
                ),
                new Simple("W", unif0_10)
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

        EvaluationResult simulation = new EvaluationResult("Simul",
                pComplex.simulate(timeLimit.toString(), step.toString(), simulationRuns), pComplex.EFT().divide(step).intValue(), pComplex.LFT().divide(step).intValue(), step.doubleValue(), 0
        );

        EvaluationResult analysis = new EvaluationResult("Analysi", analyzer.analyze(pComplex, timeLimit, step, step, ""), pComplex.EFT().divide(step).intValue(), pComplex.LFT().divide(step).intValue(), step.doubleValue(), 0);
        ActivityViewer.CompareResults("XOR-TEST", false, "", List.of("Simulation", "Analysis"), simulation, analysis);
        Thread.sleep(20000);
    }
}
