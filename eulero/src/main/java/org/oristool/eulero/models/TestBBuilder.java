package org.oristool.eulero.models;

import org.oristool.eulero.MainHelper;
import org.oristool.eulero.graph.*;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.List;

public class TestBBuilder extends ModelBuilder{
    public TestBBuilder(StochasticTransitionFeature feature, Approximator approximator) {
        super(feature, approximator);
    }

    public Activity buildModelForAnalysis_Heuristic1(BigDecimal timeBound, BigDecimal timeTick) {
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
        DAG wx_0 = DAG.forkJoin("WX",
            DAG.sequence("X",
                new Analytical("X1", feature),
                new Analytical("X2", feature)
            ),
            w_0
        );

        DAG p = DAG.empty("P");
        q_0.addPrecondition(p.begin());
        r_0.addPrecondition(p.begin());
        s_0.addPrecondition(p.begin());
        tu_0.addPrecondition(q_0, r_0);
        v_0.addPrecondition(r_0);
        wx_0.addPrecondition(r_0, s_0);
        p.end().addPrecondition(tu_0, v_0, wx_0);

        DAG tu_nested_0 = p.nest(tu_0);
        TransientSolution<DeterministicEnablingState, RewardRate> tuAnalysis_0 = tu_nested_0.analyze("3", timeTick.toString(), "0.001");
        double[] tuCdf_0 = new double[tuAnalysis_0.getSolution().length];
        for(int count = 0; count < tuAnalysis_0.getSolution().length; count++){
            tuCdf_0[count] = tuAnalysis_0.getSolution()[count][0][0];
        }

        DAG wx_nested_0 = p.nest(wx_0);
        TransientSolution<DeterministicEnablingState, RewardRate> wxAnalysis_0 = wx_nested_0.analyze("3", timeTick.toString(), "0.001");
        double[] wxCdf_0 = new double[wxAnalysis_0.getSolution().length];
        for(int count = 0; count < wxAnalysis_0.getSolution().length; count++){
            wxCdf_0[count] = wxAnalysis_0.getSolution()[count][0][0];
        }

        DAG v_nested_0 = p.nest(v_0);
        TransientSolution<DeterministicEnablingState, RewardRate> vAnalysis_0 = v_nested_0.analyze("3", timeTick.toString(), "0.001");
        double[] vCdf_0 = new double[vAnalysis_0.getSolution().length];
        for(int count = 0; count < vAnalysis_0.getSolution().length; count++){
            vCdf_0[count] = vAnalysis_0.getSolution()[count][0][0];
        }

        Numerical numericalP = Numerical.and(List.of(
            new Numerical("WX_Numerical", timeTick, 0, wxCdf_0.length, wxCdf_0),
            new Numerical("TU_Numerical", timeTick, 0, tuCdf_0.length, tuCdf_0),
            new Numerical("V_Numerical", timeTick, 0, vCdf_0.length, vCdf_0)
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
        Analytical r_3 = new Analytical("R'''", feature);
        Analytical s_3 = new Analytical("S'''", feature);
        Analytical u_3 = new Analytical("U'''", feature);
        Analytical v_3 = new Analytical("V'''", feature);
        Analytical w_3 = new Analytical("W'''", feature);

        DAG tu_3 = DAG.forkJoin("TU'''",
            DAG.sequence("T3'''",
                new Analytical("T1'''", feature),
                new Analytical("T2'''", feature)),
            u_3
        );

        DAG wx_3 = DAG.forkJoin("WX'''",
            DAG.sequence("X'''",
                new Analytical("X1'''", feature),
                new Analytical("X2'''", feature)),
            w_3
        );

        DAG m_3 = DAG.empty("M'''");
        q_3.addPrecondition(m_3.begin());
        r_3.addPrecondition(m_3.begin());
        s_3.addPrecondition(m_3.begin());
        tu_3.addPrecondition(q_3, r_3);
        v_3.addPrecondition(r_3);
        wx_3.addPrecondition(r_3, s_3);
        m_3.end().addPrecondition(tu_3, v_3, wx_3);

        DAG tu_nested_3 = m_3.nest(tu_3);
        TransientSolution<DeterministicEnablingState, RewardRate> tuAnalysis_3 = tu_nested_3.analyze("3", timeTick.toString(), "0.001");
        double[] tuCdf_3 = new double[tuAnalysis_3.getSolution().length];
        for(int count = 0; count < tuAnalysis_3.getSolution().length; count++){
            tuCdf_3[count] = tuAnalysis_3.getSolution()[count][0][0];
        }

        DAG wx_nested_3 = m_3.nest(wx_3);
        TransientSolution<DeterministicEnablingState, RewardRate> wxAnalysis_3 = wx_nested_3.analyze("3", timeTick.toString(), "0.001");
        double[] wxCdf_3 = new double[wxAnalysis_3.getSolution().length];
        for(int count = 0; count < wxAnalysis_3.getSolution().length; count++){
            wxCdf_3[count] = wxAnalysis_3.getSolution()[count][0][0];
        }

        DAG v_nested_3 = m_3.nest(v_3);
        TransientSolution<DeterministicEnablingState, RewardRate> vAnalysis_3 = v_nested_3.analyze("3", timeTick.toString(), "0.001");
        double[] vCdf_3 = new double[vAnalysis_3.getSolution().length];
        for(int count = 0; count < vAnalysis_3.getSolution().length; count++){
            vCdf_3[count] = vAnalysis_3.getSolution()[count][0][0];
        }

        Numerical numericalM3 = Numerical.and(List.of(
                new Numerical("WX_Numerical_3", timeTick, 0, wxCdf_3.length, wxCdf_3),
                new Numerical("TU_Numerical_3", timeTick, 0, tuCdf_3.length, tuCdf_3),
                new Numerical("V_Numerical_3", timeTick, 0, vCdf_3.length, vCdf_3)
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

    @Override
    public Activity buildModelForAnalysis_Heuristic2(BigDecimal timeBound, BigDecimal timeTick) {
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
        DAG wx_0 = DAG.forkJoin("WX",
                DAG.sequence("X",
                        new Analytical("X1", feature),
                        new Analytical("X2", feature)
                ),
                w_0
        );

        DAG p = DAG.empty("P");
        q_0.addPrecondition(p.begin());
        r_0.addPrecondition(p.begin());
        s_0.addPrecondition(p.begin());
        tu_0.addPrecondition(q_0, r_0);
        v_0.addPrecondition(r_0);
        wx_0.addPrecondition(r_0, s_0);
        p.end().addPrecondition(tu_0, v_0, wx_0);

        DAG tu_nested_0 = p.nest(tu_0);
        TransientSolution<DeterministicEnablingState, RewardRate> tuAnalysis_0 = tu_nested_0.analyze("3", timeTick.toString(), "0.001");
        double[] tuCdf_0 = new double[tuAnalysis_0.getSolution().length];
        for(int count = 0; count < tuAnalysis_0.getSolution().length; count++){
            tuCdf_0[count] = tuAnalysis_0.getSolution()[count][0][0];
        }
        tu_nested_0.replace(new Numerical("TU0_numerical", timeTick, 0, tuCdf_0.length, tuCdf_0, approximator));

        DAG wx_nested_0 = p.nest(wx_0);
        TransientSolution<DeterministicEnablingState, RewardRate> wxAnalysis_0 = wx_nested_0.analyze("3", timeTick.toString(), "0.001");
        double[] wxCdf_0 = new double[wxAnalysis_0.getSolution().length];
        for(int count = 0; count < wxAnalysis_0.getSolution().length; count++){
            wxCdf_0[count] = wxAnalysis_0.getSolution()[count][0][0];
        }
        wx_nested_0.replace(new Numerical("WX0_numerical", timeTick, 0, wxCdf_0.length, wxCdf_0, approximator));

        DAG v_nested_0 = p.nest(v_0);
        TransientSolution<DeterministicEnablingState, RewardRate> vAnalysis_0 = v_nested_0.analyze("3", timeTick.toString(), "0.001");
        double[] vCdf_0 = new double[vAnalysis_0.getSolution().length];
        for(int count = 0; count < vAnalysis_0.getSolution().length; count++){
            vCdf_0[count] = vAnalysis_0.getSolution()[count][0][0];
        }
        v_nested_0.replace(new Numerical("V0_numerical", timeTick, 0, vCdf_0.length, vCdf_0, approximator));

        // P
        TransientSolution<DeterministicEnablingState, RewardRate> pAnalysis = p.analyze("3", timeTick.toString(), "0.001");
        double[] pCdf = new double[pAnalysis.getSolution().length];
        for(int count = 0; count < pAnalysis.getSolution().length; count++){
            pCdf[count] = pAnalysis.getSolution()[count][0][0];
        }

        Numerical numericalP = new Numerical("P_Numerical", timeTick, 0, pCdf.length, pCdf, approximator);

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
        Analytical r_3 = new Analytical("R'''", feature);
        Analytical s_3 = new Analytical("S'''", feature);
        Analytical u_3 = new Analytical("U'''", feature);
        Analytical v_3 = new Analytical("V'''", feature);
        Analytical w_3 = new Analytical("W'''", feature);

        DAG tu_3 = DAG.forkJoin("TU'''",
                DAG.sequence("T3'''",
                        new Analytical("T1'''", feature),
                        new Analytical("T2'''", feature)),
                u_3
        );

        DAG wx_3 = DAG.forkJoin("WX'''",
                DAG.sequence("X'''",
                        new Analytical("X1'''", feature),
                        new Analytical("X2'''", feature)),
                w_3
        );

        DAG m_3 = DAG.empty("M'''");
        q_3.addPrecondition(m_3.begin());
        r_3.addPrecondition(m_3.begin());
        s_3.addPrecondition(m_3.begin());
        tu_3.addPrecondition(q_3, r_3);
        v_3.addPrecondition(r_3);
        wx_3.addPrecondition(r_3, s_3);
        m_3.end().addPrecondition(tu_3, v_3, wx_3);

        DAG tu_nested_3 = m_3.nest(tu_3);
        TransientSolution<DeterministicEnablingState, RewardRate> tuAnalysis_3 = tu_nested_3.analyze("3", timeTick.toString(), "0.001");
        double[] tuCdf_3 = new double[tuAnalysis_3.getSolution().length];
        for(int count = 0; count < tuAnalysis_3.getSolution().length; count++){
            tuCdf_3[count] = tuAnalysis_3.getSolution()[count][0][0];
        }
        tu_nested_3.replace(new Numerical("TU_Numerical_3", timeTick, 0, tuCdf_3.length, tuCdf_3));

        DAG wx_nested_3 = m_3.nest(wx_3);
        TransientSolution<DeterministicEnablingState, RewardRate> wxAnalysis_3 = wx_nested_3.analyze("3", timeTick.toString(), "0.001");
        double[] wxCdf_3 = new double[wxAnalysis_3.getSolution().length];
        for(int count = 0; count < wxAnalysis_3.getSolution().length; count++){
            wxCdf_3[count] = wxAnalysis_3.getSolution()[count][0][0];
        }
        wx_nested_3.replace(new Numerical("WX_Numerical_3", timeTick, 0, wxCdf_3.length, wxCdf_3));

        DAG v_nested_3 = m_3.nest(v_3);
        TransientSolution<DeterministicEnablingState, RewardRate> vAnalysis_3 = v_nested_3.analyze("3", timeTick.toString(), "0.001");
        double[] vCdf_3 = new double[vAnalysis_3.getSolution().length];
        for(int count = 0; count < vAnalysis_3.getSolution().length; count++){
            vCdf_3[count] = vAnalysis_3.getSolution()[count][0][0];
        }
        v_nested_3.replace(new Numerical("V_Numerical_3", timeTick, 0, tuCdf_0.length, tuCdf_0));


        TransientSolution<DeterministicEnablingState, RewardRate> m3Analysis = m_3.analyze("3", timeTick.toString(), "0.001");
        double[] m3Cdf = new double[m3Analysis.getSolution().length];
        for(int count = 0; count < m3Analysis.getSolution().length; count++){
            m3Cdf[count] = m3Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM3 = new Numerical("Numerical_M3", timeTick, 0, m3Cdf.length, m3Cdf);

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
