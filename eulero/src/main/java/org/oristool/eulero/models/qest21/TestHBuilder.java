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

public class TestHBuilder extends ModelBuilder {
    public TestHBuilder(StochasticTransitionFeature feature, Approximator approximator) {
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


        // Gestione Main
        DAG i = DAG.sequence("DAG1",
                DAG.forkJoin("DAG1_EF",
                        numericalE,
                        new Analytical("DAG1_F", feature)
                ),
                DAG.forkJoin("DAG1_I",
                        new Analytical("IA", feature),
                        DAG.sequence("IB", new Analytical("IB1", feature), new Analytical("IB2", feature))
                )
        );

        DAG k = DAG.sequence("DAG2", numericalM3, new Analytical("DAG2_F", feature));

        DAG j = DAG.sequence("DAG3",
                DAG.forkJoin("DAG3_EF",
                        new Xor("DAG3_G", List.of(
                                new Analytical("DAG3_X", feature),
                                new Analytical("DAG3_Y", feature)
                        ), List.of(0.7, 0.3)),
                        new Analytical("DAG3_F", feature)
                ),
                DAG.forkJoin("DAG3_J",
                        new Analytical("JA", feature),
                        DAG.sequence("JB", new Analytical("JB1", feature), new Analytical("JB2", feature))
                )
        );

        TransientSolution<DeterministicEnablingState, RewardRate> jAnalysis = j.analyze("10", timeTick.toString(), "0.001");
        double[] jCDF = new double[jAnalysis.getSolution().length];
        for(int count = 0; count < jAnalysis.getSolution().length; count++){
            jCDF[count] = jAnalysis.getSolution()[count][0][0];
        }

        TransientSolution<DeterministicEnablingState, RewardRate> iAnalysis = i.analyze("10", timeTick.toString(), "0.001");
        double[] iCDF = new double[iAnalysis.getSolution().length];
        for(int count = 0; count < iAnalysis.getSolution().length; count++){
            iCDF[count] = iAnalysis.getSolution()[count][0][0];
        }

        TransientSolution<DeterministicEnablingState, RewardRate> kAnalysis = k.analyze("10", timeTick.toString(), "0.001");
        double[] kCDF = new double[kAnalysis.getSolution().length];
        for(int count = 0; count < kAnalysis.getSolution().length; count++){
            kCDF[count] = kAnalysis.getSolution()[count][0][0];
        }

        Numerical main = Numerical.and(List.of(
                new Numerical("I_Numerical", timeTick, 0, iCDF.length, iCDF),
                new Numerical("K_Numerical", timeTick, 0, kCDF.length, kCDF),
                new Numerical("J_Numerical", timeTick, 0, jCDF.length, jCDF)
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

        // Gestione Main
        Analytical r_main = new Analytical("R_main", feature);

        Numerical s_main = Numerical.xor(List.of(0.7, 0.3), List.of(
                Numerical.uniform("S_main_1", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                Numerical.uniform("S_main_2", BigDecimal.ZERO, BigDecimal.ONE, timeTick)
        ));

        Numerical t_main = Numerical.and(List.of(
                Numerical.uniform("T_main", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                Numerical.seq(List.of(
                        Numerical.uniform("T_main_1", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                        Numerical.uniform("T_main_2", BigDecimal.ZERO, BigDecimal.ONE, timeTick)
                ))
        ));

        Numerical v_main = Numerical.and(List.of(
                Numerical.uniform("V_main", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                Numerical.seq(List.of(
                        Numerical.uniform("V_main_1", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                        Numerical.uniform("V_main_2", BigDecimal.ZERO, BigDecimal.ONE, timeTick)
                ))
        ));


        DAG main = DAG.empty("MAIN");
        numericalE.addPrecondition(main.begin());
        r_main.addPrecondition(main.begin());
        s_main.addPrecondition(main.begin());
        t_main.addPrecondition(numericalE, r_main);
        numericalM3.addPrecondition(r_main);
        v_main.addPrecondition(s_main, r_main);
        main.end().addPrecondition(t_main, v_main, numericalM3);

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

        // Gestisco E
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
                new Analytical("Kisthis", feature),
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


        // Gestione Main
        DAG i = DAG.sequence("DAG1",
                DAG.forkJoin("DAG1_EF",
                        numericalE,
                        new Analytical("DAG1_F", feature)
                ),
                DAG.forkJoin("DAG1_I",
                        new Analytical("IA", feature),
                        DAG.sequence("IB", new Analytical("IB1", feature), new Analytical("IB2", feature))
                )
        );

        DAG k = DAG.sequence("DAG2", numericalM3, new Analytical("DAG2_F", feature));

        DAG j = DAG.sequence("DAG3",
                DAG.forkJoin("DAG3_EF",
                        new Xor("DAG3_G", List.of(
                                new Analytical("DAG3_X", feature),
                                new Analytical("DAG3_Y", feature)
                        ), List.of(0.7, 0.3)),
                        new Analytical("DAG3_F", feature)
                ),
                DAG.forkJoin("DAG3_J",
                        new Analytical("JA", feature),
                        DAG.sequence("JB", new Analytical("JB1", feature), new Analytical("JB2", feature))
                )
        );

        TransientSolution<DeterministicEnablingState, RewardRate> jAnalysis = j.analyze("10", timeTick.toString(), "0.001");
        double[] jCDF = new double[jAnalysis.getSolution().length];
        for(int count = 0; count < jAnalysis.getSolution().length; count++){
            jCDF[count] = jAnalysis.getSolution()[count][0][0];
        }

        TransientSolution<DeterministicEnablingState, RewardRate> iAnalysis = i.analyze("10", timeTick.toString(), "0.001");
        double[] iCDF = new double[iAnalysis.getSolution().length];
        for(int count = 0; count < iAnalysis.getSolution().length; count++){
            iCDF[count] = iAnalysis.getSolution()[count][0][0];
        }

        TransientSolution<DeterministicEnablingState, RewardRate> kAnalysis = k.analyze("10", timeTick.toString(), "0.001");
        double[] kCDF = new double[kAnalysis.getSolution().length];
        for(int count = 0; count < kAnalysis.getSolution().length; count++){
            kCDF[count] = kAnalysis.getSolution()[count][0][0];
        }

        DAG main = DAG.forkJoin("Main",
                new Numerical("I_Numerical", timeTick, 0, iCDF.length, iCDF),
                new Numerical("K_Numerical", timeTick, 0, kCDF.length, kCDF),
                new Numerical("J_Numerical", timeTick, 0, jCDF.length, jCDF)
        );

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

        // Gestisco E
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


        // Gestione Main
        Analytical f = new Analytical("F", feature);

        Xor g = new Xor("G", List.of(
                new Analytical("X", feature),
                new Analytical("Y", feature)
        ), List.of(0.7, 0.3));
        TransientSolution<DeterministicEnablingState, RewardRate> gAnalysis = g.analyze("1", timeTick.toString(), "0.001");
        double[] gCdf = new double[gAnalysis.getSolution().length];
        for(int count = 0; count < gAnalysis.getSolution().length; count++){
            gCdf[count] = gAnalysis.getSolution()[count][0][0];
        }
        Numerical numericalG = new Numerical("NumericalG", timeTick, 0, gCdf.length, gCdf, approximator);

        DAG i = DAG.forkJoin("I",
                new Analytical("IA", feature),
                DAG.sequence("IB", new Analytical("IB1", feature), new Analytical("IB2", feature))
        );
        TransientSolution<DeterministicEnablingState, RewardRate> iAnalysis = i.analyze(timeBound.toString(), timeTick.toString(), "0.001");
        double[] iCdf = new double[iAnalysis.getSolution().length];
        for(int count = 0; count < iAnalysis.getSolution().length; count++){
            iCdf[count] = iAnalysis.getSolution()[count][0][0];
        }
        Numerical numericalI = new Numerical("NumericalI", timeTick, 0,  timeBound.divide(timeTick).intValue(), iCdf, approximator);


        DAG j = DAG.forkJoin("J",
                new Analytical("JA", feature),
                DAG.sequence("JB", new Analytical("JB1", feature), new Analytical("JB2", feature))
        );
        TransientSolution<DeterministicEnablingState, RewardRate> jAnalysis = j.analyze(timeBound.toString(), timeTick.toString(), "0.001");
        double[] jCdf = new double[jAnalysis.getSolution().length];
        for(int count = 0; count < jAnalysis.getSolution().length; count++){
            jCdf[count] = jAnalysis.getSolution()[count][0][0];
        }
        Numerical numericalJ = new Numerical("JNumerical", timeTick, 0,  timeBound.divide(timeTick).intValue(), jCdf, approximator);

        DAG main = DAG.empty("main");
        numericalE.addPrecondition(main.begin());
        f.addPrecondition(main.begin());
        numericalG.addPrecondition(main.begin());
        numericalI.addPrecondition(numericalE, f);
        numericalJ.addPrecondition(numericalG, f);
        numericalM3.addPrecondition(f);
        main.end().addPrecondition(numericalI, numericalJ, numericalM3);

        /*DAG nestedJ = main.nest(j);
        TransientSolution<DeterministicEnablingState, RewardRate> jAnalysis = nestedJ.analyze("10", timeTick.toString(), "0.001");
        double[] jCDF = new double[jAnalysis.getSolution().length];
        for(int count = 0; count < jAnalysis.getSolution().length; count++){
            jCDF[count] = jAnalysis.getSolution()[count][0][0];
        }
        nestedJ.replace(new Numerical("J_numerical", timeTick, 0, jCDF.length, jCDF));

        DAG nestedI = main.nest(i);
        TransientSolution<DeterministicEnablingState, RewardRate> iAnalysis = nestedI.analyze("10", timeTick.toString(), "0.001");
        double[] iCDF = new double[iAnalysis.getSolution().length];
        for(int count = 0; count < iAnalysis.getSolution().length; count++){
            iCDF[count] = iAnalysis.getSolution()[count][0][0];
        }
        nestedI.replace(new Numerical("I_numerical", timeTick, 0, iCDF.length, iCDF));

        DAG nestedK = main.nest(numericalM3);
        TransientSolution<DeterministicEnablingState, RewardRate> kAnalysis = nestedK.analyze("10", timeTick.toString(), "0.001");
        double[] kCDF = new double[kAnalysis.getSolution().length];
        for(int count = 0; count < kAnalysis.getSolution().length; count++){
            kCDF[count] = kAnalysis.getSolution()[count][0][0];
        }
        nestedK.replace(new Numerical("K_numerical", timeTick, 0, kCDF.length, kCDF));*/

        return main;
    }

    public Activity buildModelForSimulation() {
        StochasticTransitionFeature feature = this.getFeature();

        // Computing M, M', N' and K
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

        Repeat e = new Repeat("E", 0.15,
            DAG.sequence("L",
                m, new Analytical("N", feature),
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
        );

        Analytical f = new Analytical("F", feature);

        DAG j = DAG.forkJoin("J",
            new Analytical("JA", feature),
            DAG.sequence("JB", new Analytical("JB1", feature), new Analytical("JB2", feature))
        );

        Xor g = new Xor("G",
            List.of(new Analytical("G1", feature), new Analytical("G2", feature)),
            List.of(0.7, 0.3)
        );

        DAG i = DAG.forkJoin("I",
            new Analytical("IA", feature),
            DAG.sequence("IB", new Analytical("IB1", feature), new Analytical("IB2", feature))
        );

        DAG k = DAG.sequence("KB",m_3, new Analytical("KB2", feature));

        DAG main = DAG.empty("Main");
        e.addPrecondition(main.begin());
        f.addPrecondition(main.begin());
        g.addPrecondition(main.begin());
        i.addPrecondition(e, f);
        k.addPrecondition(f);
        j.addPrecondition(g, f);
        main.end().addPrecondition(i, k, j);

        return main;
    }
}
