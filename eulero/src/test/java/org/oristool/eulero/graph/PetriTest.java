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

import org.junit.jupiter.api.Test;
import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.eulero.math.approximation.HistogramApproximator;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;
import org.oristool.eulero.solver.CostEstimator;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.TransientSolutionViewer;
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
        
        ActivityViewer.plot(List.of("Analysis", "Simulation"),
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
        
        ActivityViewer.plot(List.of("Analysis", "Simulation"),
                t.analyze("5", "0.01", "0.001"), t.simulate("5", "0.01", 10000));
        
        Thread.sleep(30000);
    }

    @Test
    void AnalyticalHistogramTest() throws InterruptedException {
        BigDecimal low = new BigDecimal(1.95088);
        BigDecimal upp = new BigDecimal(21.4594);
        ArrayList<BigDecimal> histogramValues = new ArrayList<>(
                Arrays.asList(BigDecimal.valueOf(0.00223337), BigDecimal.valueOf(0.0635344), BigDecimal.valueOf(0.194387),
                        BigDecimal.valueOf(0.0706512), BigDecimal.valueOf(0.00253338), BigDecimal.valueOf(0.0000666678),
                        BigDecimal.valueOf(0.00321672), BigDecimal.valueOf(0.0284505), BigDecimal.valueOf(0.0976016),
                        BigDecimal.valueOf(0.127585), BigDecimal.valueOf(0.0700178), BigDecimal.valueOf(0.0256171),
                        BigDecimal.valueOf(0.0267171), BigDecimal.valueOf(0.040584), BigDecimal.valueOf(0.0540176),
                        BigDecimal.valueOf(0.059251), BigDecimal.valueOf(0.0528509), BigDecimal.valueOf(0.039234),
                        BigDecimal.valueOf(0.0228504), BigDecimal.valueOf(0.0117002), BigDecimal.valueOf(0.00478341),
                        BigDecimal.valueOf(0.00161669), BigDecimal.valueOf(0.000466674), BigDecimal.valueOf(0.0000333339)));
        HistogramApproximator approximator = new EXPMixtureApproximation();
        HistogramDistribution histogram = new HistogramDistribution("AHistogram", low, upp, histogramValues);


        AnalyticalHistogram t = new AnalyticalHistogram("A", histogram, approximator);

        t.petriArcs();
        new TransientSolutionViewer(t.analyze("7", "0.1", "0.01"));
        Thread.sleep(20000);
    }

    @Test
    void testGraphWithHistograms() throws InterruptedException {
        HistogramApproximator approximator = new EXPMixtureApproximation();

        BigDecimal low0 = new BigDecimal(1.9452);
        BigDecimal upp0 = new BigDecimal(12.2337);
        ArrayList<BigDecimal> histogramValues0 = new ArrayList<>(
                Arrays.asList(BigDecimal.valueOf(0.000175004), BigDecimal.valueOf(0.00427511), BigDecimal.valueOf(0.0273507),
                        BigDecimal.valueOf(0.0938273), BigDecimal.valueOf(0.160504), BigDecimal.valueOf(0.138703),
                        BigDecimal.valueOf(0.0599765), BigDecimal.valueOf(0.0137503), BigDecimal.valueOf(0.00140004),
                        BigDecimal.valueOf(0.0000500013), BigDecimal.valueOf(0.000125003), BigDecimal.valueOf(0.000575014),
                        BigDecimal.valueOf(0.00255006), BigDecimal.valueOf(0.0100003), BigDecimal.valueOf(0.0266507),
                        BigDecimal.valueOf(0.0564264), BigDecimal.valueOf(0.0903273), BigDecimal.valueOf(0.105903),
                        BigDecimal.valueOf(0.0940524), BigDecimal.valueOf(0.0641516), BigDecimal.valueOf(0.0325758),
                        BigDecimal.valueOf(0.0122253), BigDecimal.valueOf(0.00362509), BigDecimal.valueOf(0.00080002)));

        HistogramDistribution histogram0 = new HistogramDistribution("Histogram0", low0, upp0, histogramValues0);


        BigDecimal low1 = new BigDecimal(1.95088);
        BigDecimal upp1 = new BigDecimal(21.4594);
        ArrayList<BigDecimal> histogramValues1 = new ArrayList<>(
                Arrays.asList(BigDecimal.valueOf(0.00223337), BigDecimal.valueOf(0.0635344), BigDecimal.valueOf(0.194387),
                        BigDecimal.valueOf(0.0706512), BigDecimal.valueOf(0.00253338), BigDecimal.valueOf(0.0000666678),
                        BigDecimal.valueOf(0.00321672), BigDecimal.valueOf(0.0284505), BigDecimal.valueOf(0.0976016),
                        BigDecimal.valueOf(0.127585), BigDecimal.valueOf(0.0700178), BigDecimal.valueOf(0.0256171),
                        BigDecimal.valueOf(0.0267171), BigDecimal.valueOf(0.040584), BigDecimal.valueOf(0.0540176),
                        BigDecimal.valueOf(0.059251), BigDecimal.valueOf(0.0528509), BigDecimal.valueOf(0.039234),
                        BigDecimal.valueOf(0.0228504), BigDecimal.valueOf(0.0117002), BigDecimal.valueOf(0.00478341),
                        BigDecimal.valueOf(0.00161669), BigDecimal.valueOf(0.000466674), BigDecimal.valueOf(0.0000333339)));
        HistogramDistribution histogram1 = new HistogramDistribution("Histogram1", low1, upp1, histogramValues1);


        BigDecimal low2 = new BigDecimal(0.414154);
        BigDecimal upp2 = new BigDecimal(5.76728);
        ArrayList<BigDecimal> histogramValues2 = new ArrayList<>(
                Arrays.asList(BigDecimal.valueOf(0.000250394), BigDecimal.valueOf(0.000676065), BigDecimal.valueOf(0.00182788),
                        BigDecimal.valueOf(0.00856349), BigDecimal.valueOf(0.415154), BigDecimal.valueOf(0.12079),
                        BigDecimal.valueOf(0.0441946), BigDecimal.valueOf(0.0631745), BigDecimal.valueOf(0.0782232),
                        BigDecimal.valueOf(0.0823797), BigDecimal.valueOf(0.0697098), BigDecimal.valueOf(0.0540852),
                        BigDecimal.valueOf(0.0319002), BigDecimal.valueOf(0.0179783), BigDecimal.valueOf(0.00803766),
                        BigDecimal.valueOf(0.00305481)));
        HistogramDistribution histogram2 = new HistogramDistribution("Histogram2", low2, upp2, histogramValues2);

        DAG t = DAG.sequence("MAIN",
                    DAG.sequence("AB",
                            new AnalyticalHistogram("A", histogram0, approximator),
                            new AnalyticalHistogram("B", histogram1, approximator)),
                    DAG.forkJoin("CRH",
                            new AnalyticalHistogram("C", histogram2, approximator),
                            new AnalyticalHistogram("R", histogram0, approximator)));

        System.out.println(t.yamlRecursive());
        System.out.println(t.petriArcs());

        new TransientSolutionViewer(t.analyze("20", "0.1", "0.01"));

        AnalyticalHistogram t0 = new AnalyticalHistogram("A", histogram0, approximator);
        AnalyticalHistogram t1 = new AnalyticalHistogram("B", histogram1, approximator);
        AnalyticalHistogram t2 = new AnalyticalHistogram("C", histogram2, approximator);
        t0.petriArcs();
        t1.petriArcs();
        t2.petriArcs();
        new TransientSolutionViewer(t0.analyze("13", "0.1", "0.01"));
        new TransientSolutionViewer(t1.analyze("22", "0.1", "0.01"));
        new TransientSolutionViewer(t2.analyze("7", "0.1", "0.01"));

        Thread.sleep(20000);
    }

    @Test
    void NumericalTest() throws InterruptedException {
        Numerical a = Numerical.uniform("A", BigDecimal.valueOf(1), BigDecimal.valueOf(3), BigDecimal.valueOf(0.1));
        Numerical b = Numerical.uniform("B", BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(0.1));
        Numerical c = Numerical.uniform("C", BigDecimal.valueOf(5), BigDecimal.valueOf(6), BigDecimal.valueOf(0.1));
        Numerical d = Numerical.uniform("D", BigDecimal.valueOf(5), BigDecimal.valueOf(8), BigDecimal.valueOf(0.1));
        ArrayList<Numerical> activities = new ArrayList<>();
        activities.add(a);
        activities.add(b);
        activities.add(c);
        activities.add(d);

        Numerical sequence = Numerical.seq(activities);
        sequence.petriArcs();
        new TransientSolutionViewer(sequence.analyze("30", "0.1", "0.01"));
        Thread.sleep(20000);
    }
}