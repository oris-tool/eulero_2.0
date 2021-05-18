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

public class TestBBuilder extends ModelBuilder {
    public TestBBuilder(StochasticTransitionFeature feature, Approximator approximator) {
        super(feature, approximator);
    }


    /* DAG dependency breaking */
    public Activity buildModelForAnalysis_Heuristic1(BigDecimal timeBound, BigDecimal timeTick) {
        StochasticTransitionFeature feature = this.getFeature();
        Approximator approximator = this.getApproximator();

        // Computing P, M', N' and K
        Analytical q_0 = new Analytical("Q", feature);
        Analytical r_0 = new Analytical("R", feature);
        Analytical r_0_bis = new Analytical("RBis", feature);
        Analytical s_0 = new Analytical("S", feature);
        Analytical u_0 = new Analytical("U", feature);
        Analytical v_0 = new Analytical("V", feature);
        Analytical w_0 = new Analytical("W", feature);

        DAG tu_0 = DAG.forkJoin("TU",
                DAG.sequence("T",
                        new Analytical("T1", feature),
                        new Analytical("T2", feature)
                ), u_0
        );

        DAG wx_0 = DAG.forkJoin("WX",
                DAG.sequence("X",
                        new Analytical("X1", feature),
                        new Analytical("X2", feature)
                ),
                w_0
        );

        DAG p_up_0 = DAG.empty("P_UP");
        q_0.addPrecondition(p_up_0.begin());
        r_0.addPrecondition(p_up_0.begin());
        tu_0.addPrecondition(q_0, r_0);
        v_0.addPrecondition(r_0);
        p_up_0.end().addPrecondition(tu_0, v_0);

        DAG p_down_0 = DAG.empty("P_Down");
        r_0_bis.addPrecondition(p_down_0.begin());
        s_0.addPrecondition(p_down_0.begin());
        wx_0.addPrecondition(s_0, r_0_bis);
        p_down_0.end().addPrecondition(wx_0);

        TransientSolution<DeterministicEnablingState, RewardRate> pUpAnalysis_0 = p_up_0.analyze("3", timeTick.toString(), "0.001");
        double[] pUpCdf_0 = new double[pUpAnalysis_0.getSolution().length];
        for(int count = 0; count < pUpAnalysis_0.getSolution().length; count++){
            pUpCdf_0[count] = pUpAnalysis_0.getSolution()[count][0][0];
        }

        TransientSolution<DeterministicEnablingState, RewardRate> pDownAnalysis_0 = p_down_0.analyze("3", timeTick.toString(), "0.001");
        double[] pDownCdf_0 = new double[pDownAnalysis_0.getSolution().length];
        for(int count = 0; count < pDownAnalysis_0.getSolution().length; count++){
            pDownCdf_0[count] = pDownAnalysis_0.getSolution()[count][0][0];
        }

        Numerical numericalP = Numerical.and(List.of(
            new Numerical("PUp_numerical", timeTick, 0, pUpCdf_0.length, pUpCdf_0, approximator),
            new Numerical("PDown_numerical", timeTick, 0, pDownCdf_0.length, pDownCdf_0, approximator)
        ));

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
        double[] m2Cdf = new double[m2Analysis.getSolution().length];
        for(int count = 0; count < m2Analysis.getSolution().length; count++){
            m2Cdf[count] = m2Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM2 = new Numerical("m2", timeTick, 0,  m2Cdf.length, m2Cdf, approximator);

        Analytical q_3 = new Analytical("Q'''", feature);
        Analytical r_3 = new Analytical("R''''", feature);
        Analytical r_3_bis = new Analytical("R'''Bis", feature);
        Analytical s_3 = new Analytical("S'''", feature);
        Analytical u_3 = new Analytical("U'''", feature);
        Analytical v_3 = new Analytical("V'''", feature);
        Analytical w_3 = new Analytical("W'''", feature);

        DAG tu_3 = DAG.forkJoin("TU'''",
                DAG.sequence("T'''",
                        new Analytical("T1'''", feature),
                        new Analytical("T2'''", feature)
                ), u_3
        );

        DAG wx_3 = DAG.forkJoin("WX'''",
                DAG.sequence("X'''",
                        new Analytical("X1'''", feature),
                        new Analytical("X2'''", feature)
                ),
                w_3
        );

        DAG m_3_up = DAG.empty("M3_UP");
        q_3.addPrecondition(m_3_up.begin());
        r_3.addPrecondition(m_3_up.begin());
        tu_3.addPrecondition(q_3, r_3);
        v_3.addPrecondition(r_3);
        m_3_up.end().addPrecondition(tu_3, v_3);

        DAG m_3_down = DAG.empty("M3_Down");
        r_3_bis.addPrecondition(m_3_down.begin());
        s_3.addPrecondition(m_3_down.begin());
        wx_3.addPrecondition(s_3, r_3_bis);
        m_3_down.end().addPrecondition(wx_3);

        TransientSolution<DeterministicEnablingState, RewardRate> m3UpAnalysis_0 = m_3_up.analyze("3", timeTick.toString(), "0.001");
        double[] m3UpCdf = new double[m3UpAnalysis_0.getSolution().length];
        for(int count = 0; count < m3UpAnalysis_0.getSolution().length; count++){
            m3UpCdf[count] = m3UpAnalysis_0.getSolution()[count][0][0];
        }

        TransientSolution<DeterministicEnablingState, RewardRate> m3DownAnalysis_0 = m_3_down.analyze("3", timeTick.toString(), "0.001");
        double[] m3DownCdf = new double[m3DownAnalysis_0.getSolution().length];
        for(int count = 0; count < m3DownAnalysis_0.getSolution().length; count++){
            m3DownCdf[count] = m3DownAnalysis_0.getSolution()[count][0][0];
        }

        // P
        Numerical numericalM3 = Numerical.and(List.of(
                new Numerical("M3Up_Numerical", timeTick, 0, m3UpCdf.length, m3UpCdf),
                new Numerical("M3Down_Numerical", timeTick, 0, m3DownCdf.length, m3DownCdf)
        ));

        Numerical main = Numerical.and(List.of(
            Numerical.and(List.of(
                Numerical.seq(List.of(
                    Numerical.uniform("A", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                    Numerical.uniform("B", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                    Numerical.uniform("C", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                    Numerical.uniform("D", BigDecimal.ZERO, BigDecimal.ONE, timeTick))
                ),
                Numerical.seq(List.of(
                    numericalP, Numerical.uniform("N", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
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

        Numerical o = Numerical.seq(List.of(
                numericalP, Numerical.uniform("N", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
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
                                Numerical.and(List.of(numericalM1, numericalM2))
                        ))
                ))
        ));

        return main;
    }

    /* DAG numerical&approximation */
    public Activity buildModelForAnalysis_Heuristic2(BigDecimal timeBound, BigDecimal timeTick) {
        StochasticTransitionFeature feature = this.getFeature();
        Approximator approximator = this.getApproximator();

        // Computing P, M', N' and K
        Analytical q_0 = new Analytical("Q", feature);
        Analytical r_0 = new Analytical("R", feature);
        Analytical s_0 = new Analytical("S", feature);
        Analytical v_0 = new Analytical("V", feature);

        Numerical tu_0 = Numerical.and(List.of(
                Numerical.uniform("U", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                Numerical.seq(List.of(
                        Numerical.uniform("T1", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                        Numerical.uniform("T2", BigDecimal.ZERO, BigDecimal.ONE, timeTick)
                ))
        ));

        Numerical wx_0 = Numerical.and(List.of(
                Numerical.uniform("W", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                Numerical.seq(List.of(
                        Numerical.uniform("X1", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                        Numerical.uniform("X2", BigDecimal.ZERO, BigDecimal.ONE, timeTick)
                ))
        ));

        DAG p = DAG.empty("P");
        q_0.addPrecondition(p.begin());
        r_0.addPrecondition(p.begin());
        s_0.addPrecondition(p.begin());
        tu_0.addPrecondition(q_0, r_0);
        wx_0.addPrecondition(s_0, r_0);
        v_0.addPrecondition(r_0);
        p.end().addPrecondition(tu_0, v_0, wx_0);

        TransientSolution<DeterministicEnablingState, RewardRate> pAnalysis = p.analyze("3", timeTick.toString(), "0.001");
        double[] pCDF = new double[pAnalysis.getSolution().length];
        for(int count = 0; count < pAnalysis.getSolution().length; count++){
            pCDF[count] = pAnalysis.getSolution()[count][0][0];
        }

        Numerical numericalP = new Numerical("PUp_numerical", timeTick, 0, pCDF.length, pCDF, approximator);

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
        double[] m2Cdf = new double[m2Analysis.getSolution().length];
        for(int count = 0; count < m2Analysis.getSolution().length; count++){
            m2Cdf[count] = m2Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM2 = new Numerical("m2", timeTick, 0,  m2Cdf.length, m2Cdf, approximator);

        Analytical q_3 = new Analytical("Q'''", feature);
        Analytical r_3 = new Analytical("R''''", feature);
        Analytical s_3 = new Analytical("S'''", feature);
        Analytical v_3 = new Analytical("V'''", feature);

        Numerical tu_3 = Numerical.and(List.of(
                Numerical.uniform("U'''", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                Numerical.seq(List.of(
                        Numerical.uniform("T1'''", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                        Numerical.uniform("T2'''", BigDecimal.ZERO, BigDecimal.ONE, timeTick)
                ))
        ));

        Numerical wx_3 = Numerical.and(List.of(
                Numerical.uniform("W'''", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                Numerical.seq(List.of(
                        Numerical.uniform("X1'''", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                        Numerical.uniform("X2'''", BigDecimal.ZERO, BigDecimal.ONE, timeTick)
                ))
        ));

        DAG m_3 = DAG.empty("M'''");
        q_3.addPrecondition(m_3.begin());
        r_3.addPrecondition(m_3.begin());
        s_3.addPrecondition(m_3.begin());
        tu_3.addPrecondition(q_3, r_3);
        wx_3.addPrecondition(s_3, r_3);
        v_3.addPrecondition(r_3);
        m_3.end().addPrecondition(tu_3, v_3, wx_3);

        TransientSolution<DeterministicEnablingState, RewardRate> m3Analysis = m_3.analyze("3", timeTick.toString(), "0.001");
        double[] m3Cdf = new double[m3Analysis.getSolution().length];
        for(int count = 0; count < m3Analysis.getSolution().length; count++){
            m3Cdf[count] = m3Analysis.getSolution()[count][0][0];
        }

        // M3
        Numerical numericalM3 = new Numerical("M3Numerical", timeTick, 0, m3Cdf.length, m3Cdf);

        Numerical main = Numerical.and(List.of(
                Numerical.and(List.of(
                        Numerical.seq(List.of(
                                Numerical.uniform("A", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                                Numerical.uniform("B", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                                Numerical.uniform("C", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                                Numerical.uniform("D", BigDecimal.ZERO, BigDecimal.ONE, timeTick))
                        ),
                        Numerical.seq(List.of(
                                numericalP, Numerical.uniform("N", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
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

    /* DAG dependency breaking - no numerical */
    public Activity buildModelForAnalysis_Heuristic3(BigDecimal timeBound, BigDecimal timeTick) {
        StochasticTransitionFeature feature = this.getFeature();
        Approximator approximator = this.getApproximator();

        // Computing P, M', N' and K
        Analytical q_0 = new Analytical("Q", feature);
        Analytical r_0 = new Analytical("R", feature);
        Analytical r_0_bis = new Analytical("RBis", feature);
        Analytical s_0 = new Analytical("S", feature);
        Analytical u_0 = new Analytical("U", feature);
        Analytical v_0 = new Analytical("V", feature);
        Analytical w_0 = new Analytical("W", feature);

        DAG tu_0 = DAG.forkJoin("TU",
                DAG.sequence("T",
                        new Analytical("T1", feature),
                        new Analytical("T2", feature)
                ), u_0
        );

        DAG wx_0 = DAG.forkJoin("WX",
                DAG.sequence("X",
                        new Analytical("X1", feature),
                        new Analytical("X2", feature)
                ),
                w_0
        );

        DAG p_up_0 = DAG.empty("P_UP");
        q_0.addPrecondition(p_up_0.begin());
        r_0.addPrecondition(p_up_0.begin());
        tu_0.addPrecondition(q_0, r_0);
        v_0.addPrecondition(r_0);
        p_up_0.end().addPrecondition(tu_0, v_0);

        DAG p_down_0 = DAG.empty("P_Down");
        r_0_bis.addPrecondition(p_down_0.begin());
        s_0.addPrecondition(p_down_0.begin());
        wx_0.addPrecondition(s_0, r_0_bis);
        p_down_0.end().addPrecondition(wx_0);

        TransientSolution<DeterministicEnablingState, RewardRate> pUpAnalysis_0 = p_up_0.analyze("3", timeTick.toString(), "0.001");
        double[] pUpCdf_0 = new double[pUpAnalysis_0.getSolution().length];
        for(int count = 0; count < pUpAnalysis_0.getSolution().length; count++){
            pUpCdf_0[count] = pUpAnalysis_0.getSolution()[count][0][0];
        }

        TransientSolution<DeterministicEnablingState, RewardRate> pDownAnalysis_0 = p_down_0.analyze("3", timeTick.toString(), "0.001");
        double[] pDownCdf_0 = new double[pDownAnalysis_0.getSolution().length];
        for(int count = 0; count < pDownAnalysis_0.getSolution().length; count++){
            pDownCdf_0[count] = pDownAnalysis_0.getSolution()[count][0][0];
        }

        Numerical numericalP = Numerical.and(List.of(
                new Numerical("PUp_numerical", timeTick, 0, pUpCdf_0.length, pUpCdf_0, approximator),
                new Numerical("PDown_numerical", timeTick, 0, pDownCdf_0.length, pDownCdf_0, approximator)
        ));

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
        double[] m2Cdf = new double[m2Analysis.getSolution().length];
        for(int count = 0; count < m2Analysis.getSolution().length; count++){
            m2Cdf[count] = m2Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM2 = new Numerical("m2", timeTick, 0,  m2Cdf.length, m2Cdf, approximator);

        Analytical q_3 = new Analytical("Q'''", feature);
        Analytical r_3 = new Analytical("R''''", feature);
        Analytical r_3_bis = new Analytical("R'''Bis", feature);
        Analytical s_3 = new Analytical("S'''", feature);
        Analytical u_3 = new Analytical("U'''", feature);
        Analytical v_3 = new Analytical("V'''", feature);
        Analytical w_3 = new Analytical("W'''", feature);

        DAG tu_3 = DAG.forkJoin("TU'''",
                DAG.sequence("T'''",
                        new Analytical("T1'''", feature),
                        new Analytical("T2'''", feature)
                ), u_3
        );

        DAG wx_3 = DAG.forkJoin("WX'''",
                DAG.sequence("X'''",
                        new Analytical("X1'''", feature),
                        new Analytical("X2'''", feature)
                ),
                w_3
        );

        DAG m_3_up = DAG.empty("M3_UP");
        q_3.addPrecondition(m_3_up.begin());
        r_3.addPrecondition(m_3_up.begin());
        tu_3.addPrecondition(q_3, r_3);
        v_3.addPrecondition(r_3);
        m_3_up.end().addPrecondition(tu_3, v_3);

        DAG m_3_down = DAG.empty("M3_Down");
        r_3_bis.addPrecondition(m_3_down.begin());
        s_3.addPrecondition(m_3_down.begin());
        wx_3.addPrecondition(s_3, r_3_bis);
        m_3_down.end().addPrecondition(wx_3);

        TransientSolution<DeterministicEnablingState, RewardRate> m3UpAnalysis_0 = m_3_up.analyze("3", timeTick.toString(), "0.001");
        double[] m3UpCdf = new double[m3UpAnalysis_0.getSolution().length];
        for(int count = 0; count < m3UpAnalysis_0.getSolution().length; count++){
            m3UpCdf[count] = m3UpAnalysis_0.getSolution()[count][0][0];
        }

        TransientSolution<DeterministicEnablingState, RewardRate> m3DownAnalysis_0 = m_3_down.analyze("3", timeTick.toString(), "0.001");
        double[] m3DownCdf = new double[m3DownAnalysis_0.getSolution().length];
        for(int count = 0; count < m3DownAnalysis_0.getSolution().length; count++){
            m3DownCdf[count] = m3DownAnalysis_0.getSolution()[count][0][0];
        }

        // P
        Numerical numericalM3 = Numerical.and(List.of(
                new Numerical("M3Up_Numerical", timeTick, 0, m3UpCdf.length, m3UpCdf),
                new Numerical("M3Down_Numerical", timeTick, 0, m3DownCdf.length, m3DownCdf)
        ));

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
                        numericalP,
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
        StochasticTransitionFeature feature = this.getFeature();
        Approximator approximator = this.getApproximator();

        // Computing P, M', N' and K
        Analytical q_0 = new Analytical("Q", feature);
        Analytical r_0 = new Analytical("R", feature);
        Analytical s_0 = new Analytical("S", feature);
        Analytical u_0 = new Analytical("U", feature);
        Analytical v_0 = new Analytical("V", feature);
        Analytical w_0 = new Analytical("W", feature);

        DAG tu_0 = DAG.forkJoin("TU",
                DAG.sequence("T",
                        new Analytical("T1", feature),
                        new Analytical("T2", feature)
                ), u_0
        );

        TransientSolution<DeterministicEnablingState, RewardRate> tu0Analysis_0 = tu_0.analyze("3", timeTick.toString(), "0.001");
        double[] tu0Cdf = new double[tu0Analysis_0.getSolution().length];
        for(int count = 0; count < tu0Analysis_0.getSolution().length; count++){
            tu0Cdf[count] = tu0Analysis_0.getSolution()[count][0][0];
        }
        Numerical tu0Numerical = new Numerical("TU0Numerical", timeTick, 0, tu0Cdf.length, tu0Cdf, approximator);

        DAG wx_0 = DAG.forkJoin("WX",
                DAG.sequence("X",
                        new Analytical("X1", feature),
                        new Analytical("X2", feature)
                ),
                w_0
        );
        TransientSolution<DeterministicEnablingState, RewardRate> wx0Analysis_0 = wx_0.analyze("3", timeTick.toString(), "0.001");
        double[] twx0Cdf = new double[wx0Analysis_0.getSolution().length];
        for(int count = 0; count < wx0Analysis_0.getSolution().length; count++){
            twx0Cdf[count] = wx0Analysis_0.getSolution()[count][0][0];
        }
        Numerical wx0Numerical = new Numerical("WX0Numerical", timeTick, 0, twx0Cdf.length, twx0Cdf, approximator);

        DAG p = DAG.empty("P");
        q_0.addPrecondition(p.begin());
        r_0.addPrecondition(p.begin());
        s_0.addPrecondition(p.begin());
        tu0Numerical.addPrecondition(q_0, r_0);
        wx0Numerical.addPrecondition(s_0, r_0);
        v_0.addPrecondition(r_0);
        p.end().addPrecondition(tu0Numerical, v_0, wx0Numerical);

        TransientSolution<DeterministicEnablingState, RewardRate> pAnalysis = p.analyze("3", timeTick.toString(), "0.001");
        double[] pCDF = new double[pAnalysis.getSolution().length];
        for(int count = 0; count < pAnalysis.getSolution().length; count++){
            pCDF[count] = pAnalysis.getSolution()[count][0][0];
        }

        Numerical numericalP = new Numerical("P_numerical", timeTick, 0, pCDF.length, pCDF, approximator);

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
        double[] m2Cdf = new double[m2Analysis.getSolution().length];
        for(int count = 0; count < m2Analysis.getSolution().length; count++){
            m2Cdf[count] = m2Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM2 = new Numerical("m2", timeTick, 0,  m2Cdf.length, m2Cdf, approximator);

        Analytical q_3 = new Analytical("Q'''", feature);
        Analytical r_3 = new Analytical("R''''", feature);
        Analytical s_3 = new Analytical("S'''", feature);
        Analytical u_3 = new Analytical("U'''", feature);
        Analytical v_3 = new Analytical("V'''", feature);
        Analytical w_3 = new Analytical("W'''", feature);

        DAG tu_3 = DAG.forkJoin("TU'''",
                DAG.sequence("T'''",
                        new Analytical("T1'''", feature),
                        new Analytical("T2'''", feature)
                ), u_3
        );

        TransientSolution<DeterministicEnablingState, RewardRate> tu3Analysis = tu_3.analyze("3", timeTick.toString(), "0.001");
        double[] tu3Cdf_0 = new double[tu3Analysis.getSolution().length];
        for(int count = 0; count < tu3Analysis.getSolution().length; count++){
            tu3Cdf_0[count] = tu3Analysis.getSolution()[count][0][0];
        }

        Numerical tu3Numerical = new Numerical("TU3Numerical", timeTick, 0, tu3Cdf_0.length, tu3Cdf_0, approximator);

        DAG wx_3 = DAG.forkJoin("WX'''",
            DAG.sequence("X'''",
                new Analytical("X1'''", feature),
                new Analytical("X2'''", feature)
            ),
            w_3
        );

        TransientSolution<DeterministicEnablingState, RewardRate> wx3Analysis = wx_3.analyze("3", timeTick.toString(), "0.001");
        double[] wx3Cdf_0 = new double[wx3Analysis.getSolution().length];
        for(int count = 0; count < wx3Analysis.getSolution().length; count++){
            wx3Cdf_0[count] = wx3Analysis.getSolution()[count][0][0];
        }

        Numerical wx3Numerical = new Numerical("WX3Numerical", timeTick, 0, wx3Cdf_0.length, wx3Cdf_0, approximator);

        DAG m_3 = DAG.empty("M'''");
        q_3.addPrecondition(m_3.begin());
        r_3.addPrecondition(m_3.begin());
        s_3.addPrecondition(m_3.begin());
        tu3Numerical.addPrecondition(q_3, r_3);
        wx3Numerical.addPrecondition(s_3, r_3);
        v_3.addPrecondition(r_3);
        m_3.end().addPrecondition(tu3Numerical, v_3, wx3Numerical);

        TransientSolution<DeterministicEnablingState, RewardRate> m3Analysis = m_3.analyze("3", timeTick.toString(), "0.001");
        double[] m3Cdf = new double[m3Analysis.getSolution().length];
        for(int count = 0; count < m3Analysis.getSolution().length; count++){
            m3Cdf[count] = m3Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM3 = new Numerical("M3_Numerical", timeTick, 0, m3Cdf.length, m3Cdf, approximator);

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
                numericalP,
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



    public Activity buildModelForSimulation() {
        StochasticTransitionFeature feature = this.getFeature();

        // Computing P, M', N' and K
        Analytical q_0 = new Analytical("Q", feature);
        Analytical r_0 = new Analytical("R", feature);
        Analytical s_0 = new Analytical("S", feature);
        Analytical u_0 = new Analytical("U", feature);
        Analytical v_0 = new Analytical("V", feature);
        Analytical w_0 = new Analytical("W", feature);

        DAG tu_0 = DAG.forkJoin("TU",
                DAG.sequence("T",
                        new Analytical("T1", feature),
                        new Analytical("T2", feature)
                ), u_0
        );

        DAG wx_0 = DAG.forkJoin("WX",
                DAG.sequence("X",
                        new Analytical("X1", feature),
                        new Analytical("X2", feature)
                ), w_0
        );

        DAG p = DAG.empty("P");
        q_0.addPrecondition(p.begin());
        r_0.addPrecondition(p.begin());
        s_0.addPrecondition(p.begin());
        tu_0.addPrecondition(q_0, r_0);
        v_0.addPrecondition(r_0);
        wx_0.addPrecondition(r_0, s_0);
        p.end().addPrecondition(tu_0, v_0, wx_0);

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
        Analytical u_3 = new Analytical("U'''", feature);
        Analytical v_3 = new Analytical("V'''", feature);
        Analytical w_3 = new Analytical("W'''", feature);

        DAG tu_3 = DAG.forkJoin("TU'''",
            DAG.sequence("T3'''",
                new Analytical("T1'''", feature),
                new Analytical("T2'''", feature)
            ), u_3
        );
        DAG wx_3 = DAG.forkJoin("WX'''",
            DAG.sequence("X'''",
                new Analytical("X1'''", feature),
                new Analytical("X2'''", feature)
            ), w_3
        );

        DAG m_3 = DAG.empty("M'''");
        q_3.addPrecondition(m_3.begin());
        r_3.addPrecondition(m_3.begin());
        s_3.addPrecondition(m_3.begin());
        tu_3.addPrecondition(q_3, r_3);
        v_3.addPrecondition(r_3);
        wx_3.addPrecondition(r_3, s_3);
        m_3.end().addPrecondition(tu_3, v_3, wx_3);

        DAG main = DAG.forkJoin("MAIN",
            DAG.forkJoin("M1",
                DAG.sequence("M1A",
                    new Analytical("A", feature),
                    new Analytical("B", feature),
                    new Analytical("C", feature),
                    new Analytical("D", feature)
                ),
                DAG.sequence("E",
                    p, new Analytical("N", feature),
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
