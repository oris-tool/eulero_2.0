package org.oristool.eulero.models.qest21;

import org.oristool.eulero.graph.*;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.models.ModelBuilder;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.List;

public class TestABuilder extends ModelBuilder {
    public TestABuilder(StochasticTransitionFeature feature, Approximator approximator){
        super(feature, approximator);
    }

    /* DAG dependency breaking */
    public Activity buildModelForAnalysis_Heuristic1(BigDecimal timeBound, BigDecimal timeTick) {
        StochasticTransitionFeature feature = this.getFeature();
        Approximator approximator = this.getApproximator();

        // Computing M, M', N' and K
        Analytical q_0 = new Analytical("Q", feature);
        Analytical r_0 = new Analytical("R", feature);
        Analytical s_0 = new Analytical("S", feature);
        Analytical t_0 = new Analytical("T", feature);
        Analytical v_0 = new Analytical("V", feature);

        DAG m_0 = DAG.empty("M");
        q_0.addPrecondition(m_0.begin());
        r_0.addPrecondition(m_0.begin());
        s_0.addPrecondition(m_0.begin());
        t_0.addPrecondition(q_0, r_0);
        v_0.addPrecondition(r_0, s_0);
        m_0.end().addPrecondition(t_0, v_0);

        TransientSolution<DeterministicEnablingState, RewardRate> m0Analysis = m_0.analyze("3", timeTick.toString(), "0.001");
        m0Analysis.getSolution();
        double[] m0Cdf = new double[m0Analysis.getSolution().length];
        for(int count = 0; count < m0Analysis.getSolution().length; count++){
            m0Cdf[count] = m0Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM0 = new Numerical("m0", timeTick, 0, m0Cdf.length + 1, m0Cdf, approximator);

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

        Numerical numericalM1 = new Numerical("m1", timeTick, 0,  m1Cdf.length + 1, m1Cdf, approximator);

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

        Numerical numericalM2 = new Numerical("m2", timeTick, 0,  m2Cdf.length + 1, m2Cdf, approximator);

        Analytical q_3 = new Analytical("Q'''", feature);
        Analytical r_3 = new Analytical("R'''", feature);
        Analytical s_3 = new Analytical("S'''", feature);
        Analytical t_3 = new Analytical("T'''", feature);
        Analytical v_3 = new Analytical("V'''", feature);

        DAG m_3 = DAG.empty("M'''");
        q_3.addPrecondition(m_3.begin());
        r_3.addPrecondition(m_3.begin());
        s_3.addPrecondition(m_3.begin());
        t_3.addPrecondition(q_3, r_3);
        v_3.addPrecondition(r_3, s_3);
        m_3.end().addPrecondition(t_3, v_3);

        TransientSolution<DeterministicEnablingState, RewardRate> m3Analysis = m_3.analyze("3", timeTick.toString(), "0.001");
        m3Analysis.getSolution();
        double[] m3Cdf = new double[m3Analysis.getSolution().length];
        for(int count = 0; count < m3Analysis.getSolution().length; count++){
            m3Cdf[count] = m3Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM3 = new Numerical("m3", timeTick, 0,  m3Cdf.length + 1, m3Cdf, approximator);

        Numerical main = Numerical.and(List.of(
            Numerical.and(List.of(
                Numerical.seq(List.of(
                    Numerical.uniform("A", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                    Numerical.uniform("B", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                    Numerical.uniform("C", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                    Numerical.uniform("D", BigDecimal.ZERO, BigDecimal.ONE, timeTick))
                ),
                Numerical.seq(List.of(
                    numericalM0, Numerical.uniform("N", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                    Numerical.and(List.of(
                        Numerical.seq(List.of(
                            Numerical.uniform("G'", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                            Numerical.and(List.of(
                                Numerical.uniform("I'", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                                Numerical.uniform("J'", BigDecimal.ZERO, BigDecimal.ONE, timeTick))
                            ))
                        ),
                        Numerical.seq(List.of(
                            Numerical.uniform("G'", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                            Numerical.and(List.of(numericalM1, numericalM2)))
                        ))
                    ))
                ))
            ),
            Numerical.seq(List.of(
                Numerical.and(List.of(
                    Numerical.uniform("F", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                    Numerical.seq(List.of(
                        Numerical.uniform("X", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                        Numerical.uniform("Y", BigDecimal.ZERO, BigDecimal.ONE, timeTick)
                    ))
                )),
                Numerical.and(List.of(
                    Numerical.seq(List.of(
                        Numerical.uniform("Z", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                        Numerical.uniform("A'", BigDecimal.ZERO, BigDecimal.ONE, timeTick)
                    )),
                    Numerical.xor(List.of(0.3, 0.7), List.of(
                        Numerical.uniform("B'", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                        Numerical.uniform("C'", BigDecimal.ZERO, BigDecimal.ONE, timeTick)
                    ))
                )),
                Numerical.and(List.of(
                    Numerical.seq(List.of(
                        Numerical.uniform("Y'", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                        Numerical.uniform("Z'", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                        Numerical.uniform("A''", BigDecimal.ZERO, BigDecimal.ONE, timeTick))
                    ), numericalM3)
                ))
            ))
        );

        return main;
    }

    /* DAG numerical&approximation */
    public Activity buildModelForAnalysis_Heuristic2(BigDecimal timeBound, BigDecimal timeTick) {
        return this.buildModelForAnalysis_Heuristic1(timeBound, timeTick);
    }

    /* DAG dependency breaking - no numerical */
    public Activity buildModelForAnalysis_Heuristic3(BigDecimal timeBound, BigDecimal timeTick) {
        StochasticTransitionFeature feature = this.getFeature();
        Approximator approximator = this.getApproximator();

        // Computing M, M', N' and K
        Analytical q_0 = new Analytical("Q", feature);
        Analytical r_0 = new Analytical("R", feature);
        Analytical s_0 = new Analytical("S", feature);
        Analytical t_0 = new Analytical("T", feature);
        Analytical v_0 = new Analytical("V", feature);

        DAG m_0 = DAG.empty("M");
        q_0.addPrecondition(m_0.begin());
        r_0.addPrecondition(m_0.begin());
        s_0.addPrecondition(m_0.begin());
        t_0.addPrecondition(q_0, r_0);
        v_0.addPrecondition(r_0, s_0);
        m_0.end().addPrecondition(t_0, v_0);

        TransientSolution<DeterministicEnablingState, RewardRate> m0Analysis = m_0.analyze("3", timeTick.toString(), "0.001");
        m0Analysis.getSolution();
        double[] m0Cdf = new double[m0Analysis.getSolution().length];
        for(int count = 0; count < m0Analysis.getSolution().length; count++){
            m0Cdf[count] = m0Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM0 = new Numerical("m0", timeTick, 0, m0Cdf.length, m0Cdf, approximator);

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

        Numerical numericalM1 = new Numerical("m1", timeTick, 0,  m1Cdf.length, m1Cdf, approximator);

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

        Numerical numericalM2 = new Numerical("m2", timeTick, 0,  m2Cdf.length, m2Cdf, approximator);

        Analytical q_3 = new Analytical("Q'''", feature);
        Analytical r_3 = new Analytical("R'''", feature);
        Analytical s_3 = new Analytical("S'''", feature);
        Analytical t_3 = new Analytical("T'''", feature);
        Analytical v_3 = new Analytical("V'''", feature);

        DAG m_3 = DAG.empty("M'''");
        q_3.addPrecondition(m_3.begin());
        r_3.addPrecondition(m_3.begin());
        s_3.addPrecondition(m_3.begin());
        t_3.addPrecondition(q_3, r_3);
        v_3.addPrecondition(r_3, s_3);
        m_3.end().addPrecondition(t_3, v_3);

        TransientSolution<DeterministicEnablingState, RewardRate> m3Analysis = m_3.analyze("3", timeTick.toString(), "0.001");
        m3Analysis.getSolution();
        double[] m3Cdf = new double[m3Analysis.getSolution().length];
        for(int count = 0; count < m3Analysis.getSolution().length; count++){
            m3Cdf[count] = m3Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM3 = new Numerical("m3", timeTick, 0,  m3Cdf.length, m3Cdf, approximator);

        DAG main_right = DAG.sequence("M2",
                DAG.forkJoin("M2A",
                        new Analytical("F", feature),
                        DAG.sequence("G",
                                new Analytical("X", feature),
                                new Analytical("Y", feature)
                        )
                ),
                DAG.forkJoin("M2B",
                        new Xor("I" , List.of(
                                new Analytical("B'", feature),
                                new Analytical("C'", feature)
                        ), List.of(0.3, 0.7)),
                        DAG.sequence("H",
                                new Analytical("Z", feature),
                                new Analytical("A'", feature)
                        )
                ),
                DAG.forkJoin("M2C",
                        numericalM3,
                        DAG.sequence("J",
                                new Analytical("Y'", feature),
                                new Analytical("Z'", feature),
                                new Analytical("A''", feature)
                        )
                )
        );

        TransientSolution<DeterministicEnablingState, RewardRate> mainRightAnalysis = main_right.analyze(timeBound.toString(), timeTick.toString(), "0.001");
        double[] mainRightCdf = new double[mainRightAnalysis.getSolution().length];
        for(int count = 0; count < mainRightAnalysis.getSolution().length; count++){
            mainRightCdf[count] = mainRightAnalysis.getSolution()[count][0][0];
        }

        Numerical mainRightNumerical = new Numerical("MainRightNumerical", timeTick, 0, mainRightCdf.length, mainRightCdf, approximator);

        // Left part
        DAG o1 = DAG.sequence("O1",
                new Analytical("G'", feature),
                DAG.forkJoin("I'1J'",
                        new Analytical("I'", feature),
                        new Analytical("J'", feature)
                )
        );

        TransientSolution<DeterministicEnablingState, RewardRate> o1Analysis = o1.analyze("2", timeTick.toString(), "0.001");
        double[] o1Cdf = new double[o1Analysis.getSolution().length];
        for(int count = 0; count < o1Analysis.getSolution().length; count++){
            o1Cdf[count] = o1Analysis.getSolution()[count][0][0];
        }

        Numerical o1Numerical = new Numerical("O1_Numerical", timeTick, 0, o1Cdf.length, o1Cdf, approximator);

        DAG o2 = DAG.sequence("O2",
                new Analytical("K", feature),
                DAG.forkJoin("M1M2", numericalM1, numericalM2)
        );

        TransientSolution<DeterministicEnablingState, RewardRate> o2Analysis = o2.analyze("3", timeTick.toString(), "0.001");
        double[] o2Cdf = new double[o2Analysis.getSolution().length];
        for(int count = 0; count < o2Analysis.getSolution().length; count++){
            o2Cdf[count] = o2Analysis.getSolution()[count][0][0];
        }

        Numerical o2Numerical = new Numerical("O2_Numerical", timeTick, 0, o2Cdf.length, o2Cdf, approximator);

        DAG o = DAG.forkJoin("O", o1Numerical, o2Numerical);

        TransientSolution<DeterministicEnablingState, RewardRate> oAnalysis = o.analyze("3", timeTick.toString(), "0.001");
        double[] oCdf = new double[oAnalysis.getSolution().length];
        for(int count = 0; count < oAnalysis.getSolution().length; count++){
            oCdf[count] = oAnalysis.getSolution()[count][0][0];
        }

        Numerical oNumerical = new Numerical("O_Numerical", timeTick, 0, oCdf.length, oCdf, approximator);

        DAG main_left = DAG.forkJoin("M1",
                DAG.sequence("M1A",
                        new Analytical("A", feature),
                        new Analytical("B", feature),
                        new Analytical("C", feature),
                        new Analytical("D", feature)
                ),
                DAG.sequence("E",
                        numericalM0,
                        new Analytical("N", feature),
                        oNumerical
                )
        );

        TransientSolution<DeterministicEnablingState, RewardRate> mainLeftAnalysis = main_left.analyze(timeBound.toString(), timeTick.toString(), "0.001");
        double[] mainLeftCdf = new double[mainLeftAnalysis.getSolution().length];
        for(int count = 0; count < mainLeftAnalysis.getSolution().length; count++){
            mainLeftCdf[count] = mainLeftAnalysis.getSolution()[count][0][0];
        }

        Numerical mainLeftNumerical = new Numerical("MainLeftNumerical", timeTick, 0, mainLeftCdf.length, mainLeftCdf, approximator);

        DAG main = DAG.forkJoin("MAIN", mainLeftNumerical, mainRightNumerical);

        return main;
    }

    /* DAG numerical&approximation - no numerical */
    public Activity buildModelForAnalysis_Heuristic4(BigDecimal timeBound, BigDecimal timeTick) {
        return buildModelForAnalysis_Heuristic3(timeBound, timeTick);
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

        DAG m_0 = DAG.empty("M");
        q_0.addPrecondition(m_0.begin());
        r_0.addPrecondition(m_0.begin());
        s_0.addPrecondition(m_0.begin());
        t_0.addPrecondition(q_0, r_0);
        u_0.addPrecondition(r_0);
        v_0.addPrecondition(r_0, s_0);
        m_0.end().addPrecondition(t_0, u_0, v_0);

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

        DAG main = DAG.forkJoin("MAIN",
            DAG.forkJoin("M1",
                DAG.sequence("M1A",
                    new Analytical("A", feature),
                    new Analytical("B", feature),
                    new Analytical("C", feature),
                    new Analytical("D", feature)
                ),
                DAG.sequence("E",
                    m_0, new Analytical("N", feature),
                    DAG.forkJoin("O",
                        DAG.sequence("E'",
                            new Analytical("G'", feature),
                            DAG.forkJoin("H'",
                                new Analytical("I'", feature),
                                new Analytical("J'", feature)
                            )
                        ),
                        DAG.sequence("F'",
                            new Analytical("K'", feature),
                            DAG.forkJoin("L'", m_1, m_2)
                        )
                    )
                )
            ),
            DAG.sequence("M2",
                DAG.forkJoin("M2A",
                    new Analytical("F", feature),
                    DAG.sequence("G",
                        new Analytical("X", feature),
                        new Analytical("Y", feature)
                    )
                ),
                DAG.forkJoin("M2B",
                    DAG.sequence("H",
                        new Analytical("Z", feature),
                        new Analytical("A'", feature)
                    ),
                    new Xor("I",
                        List.of(
                            new Analytical("B'", feature),
                            new Analytical("C'", feature)
                        ),
                        List.of(0.3, 0.7)
                    )
                ),
                DAG.forkJoin("M2C",
                    DAG.sequence("J",
                        new Analytical("Y'", feature),
                        new Analytical("Z'", feature),
                        new Analytical("A''", feature)
                    ),
                    m_3
                )
            )
        );

        return main;
    }
}
