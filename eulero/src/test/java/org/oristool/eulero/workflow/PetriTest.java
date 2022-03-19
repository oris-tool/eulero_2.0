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

package org.oristool.eulero.workflow;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.oristool.eulero.solver.CostEstimator;
import org.oristool.eulero.ui.ActivityViewer;
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
        
        Simple q = new Simple("Q", unif01);
        Simple r = new Simple("R", unif01);
        Simple s = new Simple("S", unif01);
        
        DAG t = DAG.sequence("T", 
                new Simple("T1", unif01),
                new Simple("T2", unif01));
        Simple u = new Simple("U", unif01);
        DAG tu = DAG.forkJoin("TU", t, u);

        DAG v = DAG.sequence("V", 
                new Simple("V1", unif01),
                new Simple("V2", unif01));
        
        Simple w = new Simple("W", unif01);
        DAG x = DAG.sequence("X", 
                new Simple("X1", unif01),
                new Simple("X2", unif01));

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
                new Simple("T1", unif01),
                new Simple("T2", unif01));
        
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
                new Simple("T1", unif01),
                new Repeat("REP", 0.2, DAG.sequence("SEQ", new Simple("T2", unif01))));
        
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
                            new Simple("A", unif01),
                            new Simple("B", unif01)),
                    DAG.sequence("CRH", 
                            new Simple("C", unif01),
                            new Repeat("R", 0.1, 
                                    DAG.forkJoin("DEFG", 
                                            DAG.sequence("DE", 
                                                    new Simple("D", unif01),
                                                    new Simple("E", unif01)),
                                            DAG.sequence("FG", 
                                                    new Simple("F", unif01),
                                                    new Simple("G", unif01)))),
                            new Simple("H", unif01)));
                    
        System.out.println(t.yamlRecursive());
        System.out.println(t.petriArcs());
        System.out.println(CostEstimator.edgeCount(t.classGraph()));
        
        ActivityViewer.plot("PETRI-TEST", List.of("Analysis", "Simulation"),
                t.analyze("5", "0.01", "0.001"), t.simulate("5", "0.01", 10000));
        
        Thread.sleep(130000);
    }

    @Test
    void AnalyticalTest() throws InterruptedException {
        Simple activity = Simple.erlang("Test", 2, BigDecimal.valueOf(0.2));
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
                new Simple("U1", unif0_10),
                DAG.forkJoin("F",
                    new Simple("U2", unif0_10),
                    new Simple("U3", unif0_10)
                ),
                new Simple("E1", unif2_10)
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
    //TODO rimuovere
    /*@Test
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
    }*/
}