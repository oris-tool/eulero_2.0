package org.oristool.eulero.models;

import org.oristool.eulero.graph.*;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.List;

public class TestEBuilder extends ModelBuilder {
    public TestEBuilder(StochasticTransitionFeature feature, Approximator approximator) {
        super(feature, approximator);
    }

    public Activity buildModelForAnalysis_Heuristic1(BigDecimal timeBound, BigDecimal timeTick) {
        StochasticTransitionFeature feature = this.getFeature();
        Approximator approximator = this.getApproximator();

        // Computing P, M', N' and K
        Analytical q_0 = new Analytical("Q", feature);
        Analytical r_0 = new Analytical("R", feature);
        Analytical s_0 = new Analytical("S", feature);
        Analytical t_0 = new Analytical("T", feature);
        Analytical u_0 = new Analytical("U", feature);
        Analytical v_0 = new Analytical("V", feature);

        DAG p = DAG.empty("P");
        q_0.addPrecondition(p.begin());
        r_0.addPrecondition(p.begin());
        s_0.addPrecondition(p.begin());
        t_0.addPrecondition(q_0, r_0);
        u_0.addPrecondition(r_0);
        v_0.addPrecondition(r_0, s_0);
        p.end().addPrecondition(t_0, u_0, v_0);

        TransientSolution<DeterministicEnablingState, RewardRate> pAnalysis = p.analyze("3", timeTick.toString(), "0.001");
        double[] pCdf = new double[pAnalysis.getSolution().length];
        for(int count = 0; count < pAnalysis.getSolution().length; count++){
            pCdf[count] = pAnalysis.getSolution()[count][0][0];
        }

        Numerical numericalP = new Numerical("p", timeTick, 0, pCdf.length, pCdf, approximator);

        Analytical q_1 = new Analytical("Q'", feature);
        Analytical r_1 = new Analytical("R'", feature);
        Analytical s_1 = new Analytical("S'", feature);
        Analytical t_1 = new Analytical("T'", feature);
        Analytical u_1 = new Analytical("U'", feature);

        DAG m_1 = DAG.empty("M'");
        q_1.addPrecondition(m_1.begin());
        r_1.addPrecondition(m_1.begin());
        s_1.addPrecondition(m_1.begin());
        t_1.addPrecondition(q_1, r_1);
        u_1.addPrecondition(s_1, r_1);
        m_1.end().addPrecondition(t_1, u_1);

        TransientSolution<DeterministicEnablingState, RewardRate> m1Analysis = m_1.analyze("3", timeTick.toString(), "0.001");
        m1Analysis.getSolution();
        double[] m1Cdf = new double[m1Analysis.getSolution().length];
        for(int count = 0; count < m1Analysis.getSolution().length; count++){
            m1Cdf[count] = m1Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM1 = new Numerical("m1", timeTick, 0, m1Cdf.length, m1Cdf, approximator);

        Analytical q_2 = new Analytical("Q''", feature);
        Analytical r_2 = new Analytical("R''", feature);
        Analytical s_2 = new Analytical("S''", feature);
        Analytical t_2 = new Analytical("T''", feature);
        Analytical u_2 = new Analytical("U''", feature);

        DAG m_2 = DAG.empty("M''");
        q_2.addPrecondition(m_2.begin());
        r_2.addPrecondition(m_2.begin());
        s_2.addPrecondition(m_2.begin());
        t_2.addPrecondition(q_2, r_2);
        u_2.addPrecondition(s_2, r_2);
        m_2.end().addPrecondition(t_2, u_2);

        TransientSolution<DeterministicEnablingState, RewardRate> m2Analysis = m_2.analyze("3", timeTick.toString(), "0.001");
        m2Analysis.getSolution();
        double[] m2Cdf = new double[m2Analysis.getSolution().length];
        for(int count = 0; count < m2Analysis.getSolution().length; count++){
            m2Cdf[count] = m2Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM2 = new Numerical("m2", timeTick, 0, m2Cdf.length, m2Cdf, approximator);

        Analytical q_3 = new Analytical("Q'''", feature);
        Analytical r_3 = new Analytical("R'''", feature);
        Analytical s_3 = new Analytical("S'''", feature);
        Analytical t_3 = new Analytical("T'''", feature);
        Analytical u_3 = new Analytical("U'''", feature);
        Analytical v_3 = new Analytical("V'''", feature);

        DAG m_3 = DAG.empty("M'''");
        q_3.addPrecondition(m_3.begin());
        r_3.addPrecondition(m_3.begin());
        s_3.addPrecondition(m_3.begin());
        t_3.addPrecondition(q_3, r_3);
        u_3.addPrecondition(r_3);
        v_3.addPrecondition(r_3, s_3);
        m_3.end().addPrecondition(t_3, u_3, v_3);

        TransientSolution<DeterministicEnablingState, RewardRate> m3Analysis = m_3.analyze("3", timeTick.toString(), "0.001");
        m3Analysis.getSolution();
        double[] m3Cdf = new double[m3Analysis.getSolution().length];
        for(int count = 0; count < m3Analysis.getSolution().length; count++){
            m3Cdf[count] = m3Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM3 = new Numerical("m3", timeTick, 0,  m3Cdf.length, m3Cdf, approximator);

        // Gestisco E
        Numerical oNumerical = Numerical.and(List.of(
            Numerical.seq(List.of(
                Numerical.uniform("G'", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                Numerical.and(List.of(
                    Numerical.uniform("I'", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                    Numerical.uniform("J'", BigDecimal.ZERO, BigDecimal.ONE, timeTick))
                ))
            ),
            Numerical.seq(List.of(
                Numerical.uniform("K'", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                Numerical.and(List.of(numericalM1, numericalM2)))
            ))
        );

        Repeat m = new Repeat("M", 0.2, numericalP);
        Analytical n = new Analytical("N", feature);
        Numerical o = new Numerical("O", timeTick, oNumerical.min(), oNumerical.max(), oNumerical.getCdf(), approximator);

        Repeat e = new Repeat("E", 0.3,
            DAG.sequence("L", m, n, o)
        );

        TransientSolution<DeterministicEnablingState, RewardRate> eAnalysis = e.analyze(timeBound.toString(), timeTick.toString(), "0.001");
        double[] eCdf = new double[eAnalysis.getSolution().length];
        for(int count = 0; count < eAnalysis.getSolution().length; count++){
            eCdf[count] = eAnalysis.getSolution()[count][0][0];
        }

        Numerical numericalE = new Numerical("e", timeTick, 0,  timeBound.divide(timeTick).intValue(), eCdf, approximator);

        Analytical f = new Analytical("F", feature);
        Analytical j = new Analytical("J", feature);
        Xor g = new Xor("G",
            List.of(new Analytical("G1", feature), new Analytical("G2", feature)),
            List.of(0.7, 0.3)
        );
        Xor i = new Xor("I",
                List.of(new Analytical("I1", feature), new Analytical("I2", feature)),
                List.of(0.1, 0.9)
        );

        // Gestione Main
        DAG main = DAG.empty("Main");
        numericalE.addPrecondition(main.begin());
        f.addPrecondition(main.begin());
        g.addPrecondition(main.begin());
        i.addPrecondition(numericalE, f);
        numericalM3.addPrecondition(f);
        j.addPrecondition(g, f);
        main.end().addPrecondition(i, numericalM3, j);

        return main;
    }

    public Activity buildModelForAnalysis_Heuristic2(BigDecimal timeBound, BigDecimal timeTick) {
        StochasticTransitionFeature feature = this.getFeature();
        Approximator approximator = this.getApproximator();

        // Computing P, M', N' and K
        Analytical q_0 = new Analytical("Q", feature);
        Analytical r_0 = new Analytical("R", feature);
        Analytical s_0 = new Analytical("S", feature);
        Analytical t_0 = new Analytical("T", feature);
        Analytical u_0 = new Analytical("U", feature);
        Analytical v_0 = new Analytical("V", feature);

        DAG p = DAG.empty("P");
        q_0.addPrecondition(p.begin());
        r_0.addPrecondition(p.begin());
        s_0.addPrecondition(p.begin());
        t_0.addPrecondition(q_0, r_0);
        u_0.addPrecondition(r_0);
        v_0.addPrecondition(r_0, s_0);
        p.end().addPrecondition(t_0, u_0, v_0);

        TransientSolution<DeterministicEnablingState, RewardRate> pAnalysis = p.analyze("3", timeTick.toString(), "0.001");
        double[] pCdf = new double[pAnalysis.getSolution().length];
        for(int count = 0; count < pAnalysis.getSolution().length; count++){
            pCdf[count] = pAnalysis.getSolution()[count][0][0];
        }

        Numerical numericalP = new Numerical("p", timeTick, 0, pCdf.length, pCdf, approximator);

        Analytical q_1 = new Analytical("Q'", feature);
        Analytical r_1 = new Analytical("R'", feature);
        Analytical s_1 = new Analytical("S'", feature);
        Analytical t_1 = new Analytical("T'", feature);
        Analytical u_1 = new Analytical("U'", feature);

        DAG m_1 = DAG.empty("M'");
        q_1.addPrecondition(m_1.begin());
        r_1.addPrecondition(m_1.begin());
        s_1.addPrecondition(m_1.begin());
        t_1.addPrecondition(q_1, r_1);
        u_1.addPrecondition(s_1, r_1);
        m_1.end().addPrecondition(t_1, u_1);

        TransientSolution<DeterministicEnablingState, RewardRate> m1Analysis = m_1.analyze("3", timeTick.toString(), "0.001");
        m1Analysis.getSolution();
        double[] m1Cdf = new double[m1Analysis.getSolution().length];
        for(int count = 0; count < m1Analysis.getSolution().length; count++){
            m1Cdf[count] = m1Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM1 = new Numerical("m1", timeTick, 0, m1Cdf.length, m1Cdf, approximator);

        Analytical q_2 = new Analytical("Q''", feature);
        Analytical r_2 = new Analytical("R''", feature);
        Analytical s_2 = new Analytical("S''", feature);
        Analytical t_2 = new Analytical("T''", feature);
        Analytical u_2 = new Analytical("U''", feature);

        DAG m_2 = DAG.empty("M''");
        q_2.addPrecondition(m_2.begin());
        r_2.addPrecondition(m_2.begin());
        s_2.addPrecondition(m_2.begin());
        t_2.addPrecondition(q_2, r_2);
        u_2.addPrecondition(s_2, r_2);
        m_2.end().addPrecondition(t_2, u_2);

        TransientSolution<DeterministicEnablingState, RewardRate> m2Analysis = m_2.analyze("3", timeTick.toString(), "0.001");
        m2Analysis.getSolution();
        double[] m2Cdf = new double[m2Analysis.getSolution().length];
        for(int count = 0; count < m2Analysis.getSolution().length; count++){
            m2Cdf[count] = m2Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM2 = new Numerical("m2", timeTick, 0, m2Cdf.length, m2Cdf, approximator);

        Analytical q_3 = new Analytical("Q'''", feature);
        Analytical r_3 = new Analytical("R'''", feature);
        Analytical s_3 = new Analytical("S'''", feature);
        Analytical t_3 = new Analytical("T'''", feature);
        Analytical u_3 = new Analytical("U'''", feature);
        Analytical v_3 = new Analytical("V'''", feature);

        DAG m_3 = DAG.empty("M'''");
        q_3.addPrecondition(m_3.begin());
        r_3.addPrecondition(m_3.begin());
        s_3.addPrecondition(m_3.begin());
        t_3.addPrecondition(q_3, r_3);
        u_3.addPrecondition(r_3);
        v_3.addPrecondition(r_3, s_3);
        m_3.end().addPrecondition(t_3, u_3, v_3);

        TransientSolution<DeterministicEnablingState, RewardRate> m3Analysis = m_3.analyze("3", timeTick.toString(), "0.001");
        m3Analysis.getSolution();
        double[] m3Cdf = new double[m3Analysis.getSolution().length];
        for(int count = 0; count < m3Analysis.getSolution().length; count++){
            m3Cdf[count] = m3Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM3 = new Numerical("m3", timeTick, 0,  m3Cdf.length, m3Cdf, approximator);

        // Gestisco E
        DAG o2 = DAG.sequence("O2",
                new Analytical("K", feature),
                DAG.forkJoin("M1M2", numericalM1, numericalM2)
        );
        TransientSolution<DeterministicEnablingState, RewardRate> o2Analysis = o2.analyze("4", timeTick.toString(), "0.001");
        double[] o2Cdf = new double[o2Analysis.getSolution().length];
        for(int count = 0; count < o2Analysis.getSolution().length; count++){
            o2Cdf[count] = o2Analysis.getSolution()[count][0][0];
        }

        Numerical o2Numerical = new Numerical("O2_Numerical", timeTick, 0, o2Cdf.length, o2Cdf, approximator);

        DAG o = DAG.forkJoin("O",
                DAG.sequence("O1",
                        new Analytical("G'", feature),
                        DAG.forkJoin("I'1J'",
                                new Analytical("I'", feature),
                                new Analytical("J'", feature)
                        )
                ), o2Numerical

        );

        TransientSolution<DeterministicEnablingState, RewardRate> oAnalysis = o.analyze(timeBound.toString(), timeTick.toString(), "0.001");
        double[] oCdf = new double[oAnalysis.getSolution().length];
        for(int count = 0; count < oAnalysis.getSolution().length; count++){
            oCdf[count] = oAnalysis.getSolution()[count][0][0];
        }

        Numerical oNumerical = new Numerical("O_Numerical", timeTick, 0, oCdf.length, oCdf, approximator);
        Repeat m = new Repeat("M", 0.2, numericalP);
        Analytical n = new Analytical("N", feature);

        Repeat e = new Repeat("E", 0.3,
                DAG.sequence("L", m, n, oNumerical)
        );

        TransientSolution<DeterministicEnablingState, RewardRate> eAnalysis = e.analyze(timeBound.toString(), timeTick.toString(), "0.001");
        double[] eCdf = new double[eAnalysis.getSolution().length];
        for(int count = 0; count < eAnalysis.getSolution().length; count++){
            eCdf[count] = eAnalysis.getSolution()[count][0][0];
        }

        Numerical numericalE = new Numerical("e", timeTick, 0,  timeBound.divide(timeTick).intValue(), eCdf, approximator);

        Analytical f = new Analytical("F", feature);
        Analytical j = new Analytical("J", feature);
        Xor g = new Xor("G",
                List.of(new Analytical("G1", feature), new Analytical("G2", feature)),
                List.of(0.7, 0.3)
        );
        Xor i = new Xor("I",
                List.of(new Analytical("I1", feature), new Analytical("I2", feature)),
                List.of(0.1, 0.9)
        );

        // Gestione Main
        DAG main = DAG.empty("Main");
        numericalE.addPrecondition(main.begin());
        f.addPrecondition(main.begin());
        g.addPrecondition(main.begin());
        i.addPrecondition(numericalE, f);
        numericalM3.addPrecondition(f);
        j.addPrecondition(g, f);
        main.end().addPrecondition(i, numericalM3, j);

        return main;
    }

    public Activity buildModelForSimulation() {
        StochasticTransitionFeature feature = this.getFeature();

        // Computing M, M', N' and K
        Analytical q_0 = new Analytical("Q", feature);
        Analytical r_0 = new Analytical("R", feature);
        Analytical s_0 = new Analytical("S", feature);
        Analytical t_0 = new Analytical("T", feature);
        Analytical u_0 = new Analytical("U", feature);
        Analytical v_0 = new Analytical("V", feature);

        DAG p = DAG.empty("P");
        q_0.addPrecondition(p.begin());
        r_0.addPrecondition(p.begin());
        s_0.addPrecondition(p.begin());
        t_0.addPrecondition(q_0, r_0);
        u_0.addPrecondition(r_0);
        v_0.addPrecondition(r_0, s_0);
        p.end().addPrecondition(t_0, u_0, v_0);

        Repeat m = new Repeat("M", 0.2, p);

        Analytical q_1 = new Analytical("Q'", feature);
        Analytical r_1 = new Analytical("R'", feature);
        Analytical s_1 = new Analytical("S'", feature);
        Analytical t_1 = new Analytical("T'", feature);
        Analytical u_1 = new Analytical("U'", feature);

        DAG m_1 = DAG.empty("M'");
        q_1.addPrecondition(m_1.begin());
        r_1.addPrecondition(m_1.begin());
        s_1.addPrecondition(m_1.begin());
        t_1.addPrecondition(q_1, r_1);
        u_1.addPrecondition(s_1, r_1);
        m_1.end().addPrecondition(t_1, u_1);

        Analytical q_2 = new Analytical("Q''", feature);
        Analytical r_2 = new Analytical("R''", feature);
        Analytical s_2 = new Analytical("S''", feature);
        Analytical t_2 = new Analytical("T''", feature);
        Analytical u_2 = new Analytical("U''", feature);

        DAG m_2 = DAG.empty("M''");
        q_2.addPrecondition(m_2.begin());
        r_2.addPrecondition(m_2.begin());
        s_2.addPrecondition(m_2.begin());
        t_2.addPrecondition(q_2, r_2);
        u_2.addPrecondition(s_2, r_2);
        m_2.end().addPrecondition(t_2, u_2);

        Analytical q_3 = new Analytical("Q'''", feature);
        Analytical r_3 = new Analytical("R'''", feature);
        Analytical s_3 = new Analytical("S'''", feature);
        Analytical t_3 = new Analytical("T'''", feature);
        Analytical u_3 = new Analytical("U'''", feature);
        Analytical v_3 = new Analytical("V'''", feature);

        DAG m_3 = DAG.empty("M'''");
        q_3.addPrecondition(m_3.begin());
        r_3.addPrecondition(m_3.begin());
        s_3.addPrecondition(m_3.begin());
        t_3.addPrecondition(q_3, r_3);
        u_3.addPrecondition(r_3);
        v_3.addPrecondition(r_3, s_3);
        m_3.end().addPrecondition(t_3, u_3, v_3);

        Repeat e = new Repeat("E", 0.15,
            DAG.sequence("L",
                m, new Analytical("N", feature),
                DAG.forkJoin("O",
                    DAG.sequence("E'",
                        new Analytical("G'", feature),
                    DAG.forkJoin("H'",
                        new Analytical("I'", feature),
                        new Analytical("J'", feature)
                    )),
                    DAG.sequence("F'",
                        new Analytical("K'", feature),
                        DAG.forkJoin("L'", m_1, m_2)
                    )
                )
            )
        );

        Analytical f = new Analytical("F", feature);
        Analytical j = new Analytical("J", feature);
        Xor g = new Xor("G",
                List.of(new Analytical("G1", feature), new Analytical("G2", feature)),
                List.of(0.7, 0.3)
        );
        Xor i = new Xor("I",
                List.of(new Analytical("I1", feature), new Analytical("I2", feature)),
                List.of(0.1, 0.9)
        );

        DAG main = DAG.empty("Main");
        e.addPrecondition(main.begin());
        f.addPrecondition(main.begin());
        g.addPrecondition(main.begin());
        i.addPrecondition(e, f);
        m_3.addPrecondition(f);
        j.addPrecondition(g, f);
        main.end().addPrecondition(i, m_3, j);

        return main;
    }
}
