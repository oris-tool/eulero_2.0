/* This program is part of the ORIS Tool.
 * Copyright (C) 2011-2020 The ORIS Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.oristool.eulero.graph;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.oristool.eulero.MainHelper;
import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;
import org.oristool.eulero.solver.CostEstimator;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.TransientSolutionViewer;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

class PetriTest {

    @Test
    void testSTPN() {
        StochasticTransitionFeature unif01 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);
        
        Analytical q = new Analytical("Q", unif01);
        Analytical r = new Analytical("R", unif01);
        Analytical s = new Analytical("S", unif01);
        
        DAG t = DAG.sequence("T", 
                new Analytical("T1", unif01),
                new Analytical("T2", unif01));
        Analytical u = new Analytical("U", unif01);
        DAG tu = DAG.forkJoin("TU", t, u);

        DAG v = DAG.sequence("V", 
                new Analytical("V1", unif01),
                new Analytical("V2", unif01));
        
        Analytical w = new Analytical("W", unif01);
        DAG x = DAG.sequence("X", 
                new Analytical("X1", unif01),
                new Analytical("X2", unif01));

        DAG wx = DAG.forkJoin("WX", w, x);
        
        DAG p = DAG.empty("P");
        q.addPrecondition(p.begin());
        r.addPrecondition(p.begin());
        s.addPrecondition(p.begin());
        tu.addPrecondition(q, r);
        v.addPrecondition(r);
        wx.addPrecondition(r, s);
        p.end().addPrecondition(tu, v, wx);
        
        System.out.println(p.petriArcs());
        new TransientSolutionViewer(p.analyze("10", "0.1", "0.1"));
    }

    @Test
    void testSequence() throws InterruptedException {
        StochasticTransitionFeature unif01 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);
        
        DAG t = DAG.sequence("T", 
                new Analytical("T1", unif01),
                new Analytical("T2", unif01));
        
        System.out.println(t.petriArcs());
        
        ActivityViewer.plot("SEQ-TEST", List.of("Analysis", "Simulation"),
                t.analyze("5", "0.01", "0.001"), t.simulate("5", "0.01", 10000));
        Thread.sleep(30000);
    }
    
    @Test
    void testForkJoin() throws InterruptedException {
        StochasticTransitionFeature unif01 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);
        
        DAG t = DAG.forkJoin("T", 
                new Analytical("T1", unif01),
                new Repeat("REP", 0.2, DAG.sequence("SEQ", new Analytical("T2", unif01))));
        
        System.out.println(t.yamlRecursive());
        System.out.println(t.petriArcs());
        
        new TransientSolutionViewer(t.analyze("5", "0.1", "0.01"));
        
        Thread.sleep(10000);
    }
    
    @Test
    void testEnrico() throws InterruptedException {
        StochasticTransitionFeature unif01 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);
        
        DAG t = DAG.forkJoin("MAIN",
                    DAG.sequence("AB", 
                            new Analytical("A", unif01), 
                            new Analytical("B", unif01)),
                    DAG.sequence("CRH", 
                            new Analytical("C", unif01),
                            new Repeat("R", 0.1, 
                                    DAG.forkJoin("DEFG", 
                                            DAG.sequence("DE", 
                                                    new Analytical("D", unif01),
                                                    new Analytical("E", unif01)),
                                            DAG.sequence("FG", 
                                                    new Analytical("F", unif01),
                                                    new Analytical("G", unif01)))),
                            new Analytical("H", unif01)));
                    
        System.out.println(t.yamlRecursive());
        System.out.println(t.petriArcs());
        System.out.println(CostEstimator.edgeCount(t.classGraph()));
        
        ActivityViewer.plot("PETRI-TEST", List.of("Analysis", "Simulation"),
                t.analyze("5", "0.01", "0.001"), t.simulate("5", "0.01", 10000));
        
        Thread.sleep(130000);
    }

    @Test
    void AnalyticalTest() throws InterruptedException {
        Analytical activity = Analytical.erlang("Test", 2, BigDecimal.valueOf(0.2));
        TransientSolution<DeterministicEnablingState, RewardRate> solution = activity.analyze("50", "0.01", "0.001");
        double[] cdf = new double[solution.getSolution().length];
        for(int i = 0; i < cdf.length; i++){
            cdf[i] = solution.getSolution()[i][0][0];
        }

        int min = IntStream.range(0, cdf.length).filter(index -> cdf[index] < 0.001).max().orElse(0);
        int max = IntStream.range(0, cdf.length).filter(index -> cdf[index] > 0.999).min().orElse(cdf.length - 1);
        double[] cutCdf = Arrays.stream(cdf).filter(x -> x >= 0.001 && x <= 0.999).toArray();

        Numerical activityApproximated = new Numerical("Appr", BigDecimal.valueOf(0.01), min, max, cutCdf);
        TransientSolution<DeterministicEnablingState, RewardRate> approximationSolution = activityApproximated.analyze("50", "0.01", "0.001");
        ActivityViewer.plot("PETRI-TEST", List.of("Original", "Approximation"), solution, approximationSolution);
        Thread.sleep(20000);
    }

    @Test
    void TestRepetitionApproximation() throws InterruptedException {
        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(8));

        StochasticTransitionFeature unif2_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(2), BigDecimal.valueOf(10));

        StochasticTransitionFeature erl =
                StochasticTransitionFeature.newErlangInstance(2, BigDecimal.valueOf(0.2));

        DAG dag = DAG.sequence("DAG",
                new Analytical("U1", unif0_10),
                DAG.forkJoin("F",
                    new Analytical("U2", unif0_10),
                    new Analytical("U3", unif0_10)
                ),
                new Analytical("E1", unif2_10)
        );
        Repeat e = new Repeat("REP", 0.2, dag);

        TransientSolution<DeterministicEnablingState, RewardRate> solution = e.analyze("50", "0.01", "0.001");
        double[] cdf = new double[solution.getSolution().length];
        for(int i = 0; i < cdf.length; i++){
            cdf[i] = solution.getSolution()[i][0][0];
        }

        int min = IntStream.range(0, cdf.length).filter(index -> cdf[index] < 0.001).max().orElse(0);
        int max = IntStream.range(0, cdf.length).filter(index -> cdf[index] > 0.999).min().orElse(cdf.length - 1);
        double[] cutCdf = Arrays.stream(cdf).filter(x -> x >= 0.001 && x <= 0.999).toArray();

        Numerical activityApproximated = new Numerical("Appr", BigDecimal.valueOf(0.01), min, max, cutCdf);
        TransientSolution<DeterministicEnablingState, RewardRate> approximationSolution = activityApproximated.analyze("50", "0.01", "0.001");
        ActivityViewer.plot("SEQ-TEST", List.of("Original", "Approximation"), solution, approximationSolution);
        Thread.sleep(20000);
    }

    @Test
    void TestSEQNumericalAnalysis() throws InterruptedException {
        BigDecimal step = BigDecimal.valueOf(0.01);
        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(3));

        StochasticTransitionFeature unif2_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(0), BigDecimal.valueOf(4));

        DAG dag = DAG.sequence("DAG",
                new Analytical("U1", unif0_10),
                new Analytical("E1", unif2_10)
        );

        MainHelper.ResultWrapper test1 = new MainHelper.ResultWrapper(dag.getNumericalCDF(BigDecimal.valueOf(10), step), dag.EFT().divide(step).intValue(), dag.LFT().divide(step).intValue(), step.doubleValue());
        MainHelper.ResultWrapper test2 = new MainHelper.ResultWrapper(dag.analyze("10", step.toString(), "0.01"), dag.EFT().divide(step).intValue(), dag.LFT().divide(step).intValue(), step.doubleValue());
        ActivityViewer.CompareResults("SEQ-TEST", false, "", List.of("NumericalEvaluation", "AnalysisEvaluation"), test1, test2);
        Thread.sleep(20000);
    }

    @Test
    void TestANDNumericalAnalysis() throws InterruptedException {
        BigDecimal step = BigDecimal.valueOf(0.01);
        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(8));

        StochasticTransitionFeature unif2_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(2), BigDecimal.valueOf(10));

        DAG dag = DAG.forkJoin("DAG",
                new Analytical("U1", unif0_10),
                new Analytical("E1", unif2_10)
        );

        MainHelper.ResultWrapper test1 = new MainHelper.ResultWrapper(dag.getNumericalCDF(BigDecimal.valueOf(14), step), dag.EFT().divide(step).intValue(), dag.LFT().divide(step).intValue(), step.doubleValue());
        MainHelper.ResultWrapper test2 = new MainHelper.ResultWrapper(dag.analyze("14", step.toString(), "0.01"), dag.EFT().divide(step).intValue(), dag.LFT().divide(step).intValue(), step.doubleValue());
        ActivityViewer.CompareResults("AND-TEST", false, "", List.of("NumericalEvaluation", "AnalysisEvaluation"), test1, test2);
        Thread.sleep(20000);
    }

    @Test
    void TestXORNumericalAnalysis() throws InterruptedException {
        BigDecimal step = BigDecimal.valueOf(0.01);
        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(8));

        StochasticTransitionFeature unif2_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(2), BigDecimal.valueOf(10));

        Xor xor = new Xor(
            "Xor",
            List.of(
                new Analytical("U1", unif0_10),
                new Analytical("E1", unif2_10)
            ),
            List.of(0.3, 0.7)
        );

        double[] we = xor.getNumericalCDF(BigDecimal.valueOf(14), step);

        MainHelper.ResultWrapper test1 = new MainHelper.ResultWrapper(we, xor.EFT().divide(step).intValue(), xor.LFT().divide(step).intValue(), step.doubleValue());
        MainHelper.ResultWrapper test2 = new MainHelper.ResultWrapper(xor.analyze("14", step.toString(), "0.01"), xor.EFT().divide(step).intValue(), xor.LFT().divide(step).intValue(), step.doubleValue());
        ActivityViewer.CompareResults("XOR-TEST", false, "", List.of("NumericalEvaluation", "AnalysisEvaluation"), test1, test2);
        Thread.sleep(20000);
    }
}