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

import org.junit.jupiter.api.Test;
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
        
        new TransientSolutionViewer(t.analyze("5", "0.1", "0.01"));
        Thread.sleep(10000);
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
        
        new TransientSolutionViewer(t.analyze("5", "0.1", "0.01"));
        Thread.sleep(20000);
    }
}
