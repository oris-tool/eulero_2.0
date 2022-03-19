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

package org.oristool.eulero;

import java.math.BigDecimal;
import java.util.List;

import org.oristool.eulero.workflow.Simple;
import org.oristool.eulero.workflow.DAG;
import org.oristool.eulero.workflow.Repeat;
import org.oristool.eulero.workflow.Xor;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

public class Main {
    public static void main(String[] args) {
        StochasticTransitionFeature unif01 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);
        
        Simple a = new Simple("A", unif01);
        Simple b = new Simple("B", unif01);
        Simple c = new Simple("C", unif01);
        Simple d = new Simple("D", unif01);
        Simple f = new Simple("F", unif01);
        
        DAG g = DAG.sequence("G", 
                new Simple("G1", unif01),
                new Simple("G2", unif01));
        
        DAG h = DAG.sequence("H", 
                new Simple("H1", unif01),
                new Simple("H2", unif01));
        
        Xor i = new Xor("I",
                List.of(new Simple("IA", unif01),
                        new Simple("IB", unif01)),
                List.of(0.3, 0.7));
        
        DAG j = DAG.sequence("J", 
                new Simple("J1", unif01),
                new Simple("J2", unif01),
                new Simple("J3", unif01));

        Xor k = new Xor("K", List.of(
                DAG.sequence("KA",
                        new Simple("KA1", unif01),
                        new Simple("KA2", unif01)),
                DAG.sequence("KB",
                        new Simple("KB1", unif01),
                        new Simple("KB2", unif01))),
                List.of(0.4, 0.6));
        
        Simple n = new Simple("N", unif01);
        
        DAG o = DAG.forkJoin("O", 
                DAG.sequence("YAPBP",
                        new Simple("Y", unif01),
                        DAG.forkJoin("APBP",
                                new Simple("AP", unif01),
                                new Simple("BP", unif01))),
                DAG.sequence("ZCPDP",
                        new Simple("Z", unif01),
                        DAG.forkJoin("CPDP",
                                DAG.sequence("CP", 
                                        new Simple("CP1", unif01),
                                        new Simple("CP2", unif01)),
                                DAG.sequence("DP", 
                                        new Simple("DP1", unif01),
                                        new Simple("DP2", unif01)))));

        o.flatten();  // to remove DAG nesting
        
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
        
        Repeat e = new Repeat("E", 0.1, 
                DAG.sequence("L", new Repeat("M", 0.2, p), n, o));

        DAG main = DAG.empty("MAIN");
        a.addPrecondition(main.begin());
        b.addPrecondition(main.begin());
        c.addPrecondition(main.begin());
        d.addPrecondition(main.begin());
        e.addPrecondition(a, b);
        f.addPrecondition(b);
        g.addPrecondition(c);
        h.addPrecondition(c);
        i.addPrecondition(e, f);
        j.addPrecondition(f, g, h);
        k.addPrecondition(h, d);
        main.end().addPrecondition(i, j, k);
        
        
        System.out.println(main.yamlRecursive());       
        System.out.println("===");
        
        
        TransientSolution<DeterministicEnablingState, RewardRate> before =
                main.simulate("10", "0.1", 3000);           
        
        main.nest(i).replace(new Simple("BAD", unif01));
        System.out.println(main.yamlRecursive());
        
        TransientSolution<DeterministicEnablingState, RewardRate> after =
                main.simulate("10", "0.1", 3000);

        ActivityViewer.plot("MAIN", List.of("Before", "After"), before, after);

//        System.out.println(main.yamlRecursive());

//        DAG uptoI = main.copyRecursive(main.begin(), i, "");        
//        System.out.println(CostEstimator.edgeCount(uptoI.classGraph()));
//
//        DAG uptoJ = main.copyRecursive(main.begin(), j, "");        
//        System.out.println(CostEstimator.edgeCount(uptoJ.classGraph()));
//
//        DAG uptoK = main.copyRecursive(main.begin(), k, "");        
//        System.out.println(CostEstimator.edgeCount(uptoK.classGraph()));

        
        // new TransientSolutionViewer(main.analyze("10", "0.1", "0.1"));
        
    }
}
