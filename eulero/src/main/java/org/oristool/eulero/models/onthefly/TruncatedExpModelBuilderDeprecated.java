package org.oristool.eulero.models.onthefly;

import org.oristool.eulero.graph.*;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.distribution.continuous.ShiftedTruncatedExponentialDistribution;
import org.oristool.eulero.models.ModelBuilder_Deprecated;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TruncatedExpModelBuilderDeprecated extends ModelBuilder_Deprecated {
    private ArrayList<Map<String, Double>> features;

    public TruncatedExpModelBuilderDeprecated(ArrayList<Map<String, Double>> features, Approximator approximator) {
        super(null, approximator);
        this.features = features;
    }

    /* DAG dependency breaking */
    public Activity buildModelForAnalysis_Heuristic1(BigDecimal timeBound, BigDecimal timeTick) {
        int featuresSize = features.size();
        int counter = 0;
        Approximator approximator = this.getApproximator();

        ArrayList<StochasticTransitionFeature> stochasticTransitionFeatures = new ArrayList();
        for(Map<String, Double> feature: features){
            List<GEN> transition_gens = new ArrayList<>();
            DBMZone transition_d_0 = new DBMZone(Variable.X);
            ShiftedTruncatedExponentialDistribution distribution =
                    new ShiftedTruncatedExponentialDistribution("distribution", BigDecimal.valueOf(feature.get("low")),
                            BigDecimal.valueOf(feature.get("upp")), BigDecimal.valueOf(feature.get("lambda")));

            Expolynomial transition_e_0 = Expolynomial.fromString(distribution.getExpolynomialDensityString());
            //Normalization
            transition_d_0.setCoefficient(Variable.X, Variable.TSTAR, new OmegaBigDecimal(String.valueOf(distribution.getUpp())));
            transition_d_0.setCoefficient(Variable.TSTAR, Variable.X, new OmegaBigDecimal(String.valueOf(-distribution.getLow().doubleValue())));
            GEN transition_gen_0 = new GEN(transition_d_0, transition_e_0);
            transition_gens.add(transition_gen_0);
            PartitionedGEN transition_pFunction = new PartitionedGEN(transition_gens);
            stochasticTransitionFeatures.add(StochasticTransitionFeature.of(transition_pFunction));
        }


        // Computing P, M', N' and K
        Analytical q_0 = new Analytical("Q", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical r_0 = new Analytical("R", stochasticTransitionFeatures.get(counter % features.size()));
        Analytical r_0_bis = new Analytical("RBis", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical s_0 = new Analytical("S", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical v_0 = new Analytical("V", stochasticTransitionFeatures.get(counter++ % features.size()));

        DAG tu_0 = DAG.forkJoin("TU",
                new Analytical("U", stochasticTransitionFeatures.get(counter++ % features.size())),
                DAG.sequence("T",
                        new Analytical("T1", stochasticTransitionFeatures.get(counter++ % features.size())),
                        new Analytical("T2", stochasticTransitionFeatures.get(counter++ % features.size()))
                )
        );

        DAG wx_0 = DAG.forkJoin("WX",
                new Analytical("W", stochasticTransitionFeatures.get(counter++ % features.size())),
                DAG.sequence("X",
                        new Analytical("X1", stochasticTransitionFeatures.get(counter++ % features.size())),
                        new Analytical("X2", stochasticTransitionFeatures.get(counter++ % features.size()))
                )
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

        TransientSolution<DeterministicEnablingState, RewardRate> pUpAnalysis_0 = p_up_0.analyze(p_up_0.upp().toString(), timeTick.toString(), "0.001");
        double[] pUpCdf_0 = new double[pUpAnalysis_0.getSolution().length];
        for(int count = 0; count < pUpAnalysis_0.getSolution().length; count++){
            pUpCdf_0[count] = pUpAnalysis_0.getSolution()[count][0][0];
        }

        TransientSolution<DeterministicEnablingState, RewardRate> pDownAnalysis_0 = p_down_0.analyze(p_down_0.upp().toString(), timeTick.toString(), "0.001");
        double[] pDownCdf_0 = new double[pDownAnalysis_0.getSolution().length];
        for(int count = 0; count < pDownAnalysis_0.getSolution().length; count++){
            pDownCdf_0[count] = pDownAnalysis_0.getSolution()[count][0][0];
        }

        Numerical numericalP = Numerical.and(List.of(
                new Numerical("PUp_numerical", timeTick, 0, pUpCdf_0.length, pUpCdf_0, approximator),
                new Numerical("PDown_numerical", timeTick, 0, pDownCdf_0.length, pDownCdf_0, approximator)
        ));

        Analytical q_1 = new Analytical("Q'", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical r_1 = new Analytical("R'", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical s_1 = new Analytical("S'", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical t_1 = new Analytical("T'", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical u_1 = new Analytical("U'", stochasticTransitionFeatures.get(counter++ % features.size()));

        DAG m_1 = DAG.empty("M'");
        q_1.addPrecondition(m_1.begin());
        r_1.addPrecondition(m_1.begin());
        s_1.addPrecondition(m_1.begin());
        t_1.addPrecondition(q_1, r_1);
        u_1.addPrecondition(s_1, r_1);
        m_1.end().addPrecondition(t_1, u_1);

        TransientSolution<DeterministicEnablingState, RewardRate> m1Analysis = m_1.analyze(m_1.upp().toString(), timeTick.toString(), "0.001");
        m1Analysis.getSolution();
        double[] m1Cdf = new double[m1Analysis.getSolution().length];
        for (int count = 0; count < m1Analysis.getSolution().length; count++) {
            m1Cdf[count] = m1Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM1 = new Numerical("m1", timeTick, 0, m1Cdf.length, m1Cdf, approximator);

        Analytical q_2 = new Analytical("Q''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical r_2 = new Analytical("R''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical s_2 = new Analytical("S''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical t_2 = new Analytical("T''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical u_2 = new Analytical("U''", stochasticTransitionFeatures.get(counter++ % features.size()));

        DAG m_2 = DAG.empty("M''");
        q_2.addPrecondition(m_2.begin());
        r_2.addPrecondition(m_2.begin());
        s_2.addPrecondition(m_2.begin());
        t_2.addPrecondition(q_2, r_2);
        u_2.addPrecondition(s_2, r_2);
        m_2.end().addPrecondition(t_2, u_2);

        TransientSolution<DeterministicEnablingState, RewardRate> m2Analysis = m_2.analyze(m_1.upp().toString(), timeTick.toString(), "0.001");
        m2Analysis.getSolution();
        double[] m2Cdf = new double[m2Analysis.getSolution().length];
        for (int count = 0; count < m2Analysis.getSolution().length; count++) {
            m2Cdf[count] = m2Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM2 = new Numerical("m2", timeTick, 0, m2Cdf.length, m2Cdf, approximator);

        Analytical q_3 = new Analytical("Q'''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical r_3 = new Analytical("R''''", stochasticTransitionFeatures.get(counter % features.size()));
        Analytical r_3_bis = new Analytical("R'''Bis", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical s_3 = new Analytical("S'''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical v_3 = new Analytical("V'''", stochasticTransitionFeatures.get(counter++ % features.size()));

        DAG tu_3 = DAG.forkJoin("TU'''",
                new Analytical("U'''", stochasticTransitionFeatures.get(counter++ % features.size())),
                DAG.sequence("T'''",
                        new Analytical("T1'''", stochasticTransitionFeatures.get(counter++ % features.size())),
                        new Analytical("T2'''", stochasticTransitionFeatures.get(counter++ % features.size()))
                )
        );

        DAG wx_3 = DAG.forkJoin("WX'''",
                new Analytical("W'''", stochasticTransitionFeatures.get(counter++ % features.size())),
                DAG.sequence("X'''",
                        new Analytical("X1'''", stochasticTransitionFeatures.get(counter++ % features.size())),
                        new Analytical("X2'''", stochasticTransitionFeatures.get(counter++ % features.size()))
                )
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

        TransientSolution<DeterministicEnablingState, RewardRate> m3UpAnalysis_0 = m_3_up.analyze(m_3_up.upp().toString(), timeTick.toString(), "0.001");
        double[] m3UpCdf = new double[m3UpAnalysis_0.getSolution().length];
        for(int count = 0; count < m3UpAnalysis_0.getSolution().length; count++){
            m3UpCdf[count] = m3UpAnalysis_0.getSolution()[count][0][0];
        }

        TransientSolution<DeterministicEnablingState, RewardRate> m3DownAnalysis_0 = m_3_down.analyze(m_3_down.upp().toString(), timeTick.toString(), "0.001");
        double[] m3DownCdf = new double[m3DownAnalysis_0.getSolution().length];
        for(int count = 0; count < m3DownAnalysis_0.getSolution().length; count++){
            m3DownCdf[count] = m3DownAnalysis_0.getSolution()[count][0][0];
        }

        // P
        Numerical numericalM3 = Numerical.and(List.of(
                new Numerical("M3Up_Numerical", timeTick, 0, m3UpCdf.length, m3UpCdf),
                new Numerical("M3Down_Numerical", timeTick, 0, m3DownCdf.length, m3DownCdf)
        ));

        Analytical n = new Analytical("N", stochasticTransitionFeatures.get(counter++ % features.size()));

        // Gestisco E
        Numerical oNumerical = Numerical.and(List.of(
                Numerical.seq(List.of(
                        Numerical.truncatedExp("G'", features.get(counter % features.size()).get("lambda"),
                                features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick),
                        Numerical.and(List.of(
                                Numerical.truncatedExp("I'", features.get(counter % features.size()).get("lambda"),
                                        features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick),
                                Numerical.truncatedExp("J'", features.get(counter % features.size()).get("lambda"),
                                        features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick))
                        ))
                ),
                Numerical.seq(List.of(
                        Numerical.truncatedExp("K'", features.get(counter % features.size()).get("lambda"),
                                features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick),
                        Numerical.and(List.of(numericalM1, numericalM2)))
                ))
        );

        Repeat m = new Repeat("M", 0.2, numericalP);

        Numerical o = new Numerical("O", timeTick, oNumerical.min(), oNumerical.max(), oNumerical.getCdf(), approximator);

        Repeat e = new Repeat("E", 0.3,
                DAG.sequence("L", m, n, o)
        );

        TransientSolution<DeterministicEnablingState, RewardRate> eAnalysis = e.analyze("30", timeTick.toString(), "0.001");
        double[] eCdf = new double[eAnalysis.getSolution().length];
        for (int count = 0; count < eAnalysis.getSolution().length; count++) {
            eCdf[count] = eAnalysis.getSolution()[count][0][0];
        }

        Numerical numericalE = new Numerical("e", timeTick, 0, eCdf.length, eCdf, approximator);

        Analytical f = new Analytical("F", stochasticTransitionFeatures.get(counter % features.size()));
        Analytical fBis = new Analytical("FBis", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical j = new Analytical("J", stochasticTransitionFeatures.get(counter++ % features.size()));
        Xor g = new Xor("G",
                List.of(new Analytical("G1", stochasticTransitionFeatures.get(counter++ % features.size())),
                new Analytical("G2", stochasticTransitionFeatures.get(counter++ % features.size()))),
                List.of(0.7, 0.3)
        );
        Xor i = new Xor("I",
                List.of(new Analytical("I1",stochasticTransitionFeatures.get(counter++ % features.size())),
                new Analytical("I2",stochasticTransitionFeatures.get(counter++ % features.size()))),
                List.of(0.1, 0.9)
        );

        // Gestione Main
        DAG main1 = DAG.empty("Main1");
        numericalE.addPrecondition(main1.begin());
        f.addPrecondition(main1.begin());
        i.addPrecondition(numericalE, f);
        main1.end().addPrecondition(i);

        DAG main2 = DAG.empty("Main2");
        g.addPrecondition(main2.begin());
        fBis.addPrecondition(main2.begin());
        numericalM3.addPrecondition(fBis);
        j.addPrecondition(g, fBis);
        main2.end().addPrecondition(numericalM3, j);

        TransientSolution<DeterministicEnablingState, RewardRate> main1Analysis = main1.analyze(main1.upp().toString(), timeTick.toString(), "0.001");
        double[] main1Cdf = new double[main1Analysis.getSolution().length];
        for(int count = 0; count < main1Analysis.getSolution().length; count++){
            main1Cdf[count] = main1Analysis.getSolution()[count][0][0];
        }

        TransientSolution<DeterministicEnablingState, RewardRate> main2Analysis = main2.analyze(main2.upp().toString(), timeTick.toString(), "0.001");
        double[] main2Cdf = new double[main2Analysis.getSolution().length];
        for(int count = 0; count < main2Analysis.getSolution().length; count++){
            main2Cdf[count] = main2Analysis.getSolution()[count][0][0];
        }


        return Numerical.and(List.of(
                new Numerical("numericalM1", timeTick, 0, main1Cdf.length, main1Cdf),
                new Numerical("numericalM2", timeTick, 0, main2Cdf.length, main2Cdf)
        ));
    }

    /* DAG numerical&approximation */
    public Activity buildModelForAnalysis_Heuristic2(BigDecimal timeBound, BigDecimal timeTick) {
        int featuresSize = features.size();
        int counter = 0;
        Approximator approximator = this.getApproximator();

        ArrayList<StochasticTransitionFeature> stochasticTransitionFeatures = new ArrayList();
        for(Map<String, Double> feature: features){
            List<GEN> transition_gens = new ArrayList<>();
            DBMZone transition_d_0 = new DBMZone(Variable.X);
            ShiftedTruncatedExponentialDistribution distribution =
                    new ShiftedTruncatedExponentialDistribution("distribution", BigDecimal.valueOf(feature.get("low")),
                            BigDecimal.valueOf(feature.get("upp")), BigDecimal.valueOf(feature.get("lambda")));

            Expolynomial transition_e_0 = Expolynomial.fromString(distribution.getExpolynomialDensityString());
            //Normalization
            transition_d_0.setCoefficient(Variable.X, Variable.TSTAR, new OmegaBigDecimal(String.valueOf(distribution.getUpp())));
            transition_d_0.setCoefficient(Variable.TSTAR, Variable.X, new OmegaBigDecimal(String.valueOf(-distribution.getLow().doubleValue())));
            GEN transition_gen_0 = new GEN(transition_d_0, transition_e_0);
            transition_gens.add(transition_gen_0);
            PartitionedGEN transition_pFunction = new PartitionedGEN(transition_gens);
            stochasticTransitionFeatures.add(StochasticTransitionFeature.of(transition_pFunction));
        }

        // Computing P, M', N' and K
        Analytical q_0 = new Analytical("Q", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical r_0 = new Analytical("R", stochasticTransitionFeatures.get(counter % features.size()));
        Analytical r_0_bis = new Analytical("RBis", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical s_0 = new Analytical("S", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical v_0 = new Analytical("V", stochasticTransitionFeatures.get(counter++ % features.size()));

        Numerical tu_0 = Numerical.and(List.of(
                Numerical.truncatedExp("T", features.get(counter % features.size()).get("lambda"),
                        features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick),
                Numerical.seq(List.of(
                        Numerical.truncatedExp("U1", features.get(counter % features.size()).get("lambda"),
                                features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick),
                        Numerical.truncatedExp("U2", features.get(counter % features.size()).get("lambda"),
                                features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick)
                ))
        ));

        Numerical wx_0 = Numerical.and(List.of(
                Numerical.truncatedExp("W", features.get(counter % features.size()).get("lambda"),
                        features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick),
                Numerical.seq(List.of(
                        Numerical.truncatedExp("X1", features.get(counter % features.size()).get("lambda"),
                                features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick),
                        Numerical.truncatedExp("X2", features.get(counter % features.size()).get("lambda"),
                                features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick)
                ))
        ));

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

        TransientSolution<DeterministicEnablingState, RewardRate> pUpAnalysis_0 = p_up_0.analyze(p_up_0.upp().add(BigDecimal.valueOf(2)).toString(), timeTick.toString(), "0.001");
        double[] pUpCdf_0 = new double[pUpAnalysis_0.getSolution().length];
        for(int count = 0; count < pUpAnalysis_0.getSolution().length; count++){
            pUpCdf_0[count] = pUpAnalysis_0.getSolution()[count][0][0];
        }

        TransientSolution<DeterministicEnablingState, RewardRate> pDownAnalysis_0 = p_down_0.analyze(p_down_0.upp().add(BigDecimal.valueOf(2)).toString(), timeTick.toString(), "0.001");
        double[] pDownCdf_0 = new double[pDownAnalysis_0.getSolution().length];
        for(int count = 0; count < pDownAnalysis_0.getSolution().length; count++){
            pDownCdf_0[count] = pDownAnalysis_0.getSolution()[count][0][0];
        }

        Numerical numericalP = Numerical.and(List.of(
                new Numerical("PUp_numerical", timeTick, 0, pUpCdf_0.length, pUpCdf_0, approximator),
                new Numerical("PDown_numerical", timeTick, 0, pDownCdf_0.length, pDownCdf_0, approximator)
        ));

        Analytical q_1 = new Analytical("Q'", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical r_1 = new Analytical("R'", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical s_1 = new Analytical("S'", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical t_1 = new Analytical("T'", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical u_1 = new Analytical("U'", stochasticTransitionFeatures.get(counter++ % features.size()));

        DAG m_1 = DAG.empty("M'");
        q_1.addPrecondition(m_1.begin());
        r_1.addPrecondition(m_1.begin());
        s_1.addPrecondition(m_1.begin());
        t_1.addPrecondition(q_1, r_1);
        u_1.addPrecondition(s_1, r_1);
        m_1.end().addPrecondition(t_1, u_1);

        TransientSolution<DeterministicEnablingState, RewardRate> m1Analysis = m_1.analyze(m_1.upp().toString(), timeTick.toString(), "0.001");
        m1Analysis.getSolution();
        double[] m1Cdf = new double[m1Analysis.getSolution().length];
        for (int count = 0; count < m1Analysis.getSolution().length; count++) {
            m1Cdf[count] = m1Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM1 = new Numerical("m1", timeTick, 0, m1Cdf.length, m1Cdf, approximator);

        Analytical q_2 = new Analytical("Q''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical r_2 = new Analytical("R''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical s_2 = new Analytical("S''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical t_2 = new Analytical("T''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical u_2 = new Analytical("U''", stochasticTransitionFeatures.get(counter++ % features.size()));

        DAG m_2 = DAG.empty("M''");
        q_2.addPrecondition(m_2.begin());
        r_2.addPrecondition(m_2.begin());
        s_2.addPrecondition(m_2.begin());
        t_2.addPrecondition(q_2, r_2);
        u_2.addPrecondition(s_2, r_2);
        m_2.end().addPrecondition(t_2, u_2);

        TransientSolution<DeterministicEnablingState, RewardRate> m2Analysis = m_2.analyze(m_2.upp().toString(), timeTick.toString(), "0.001");
        m2Analysis.getSolution();
        double[] m2Cdf = new double[m2Analysis.getSolution().length];
        for (int count = 0; count < m2Analysis.getSolution().length; count++) {
            m2Cdf[count] = m2Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM2 = new Numerical("m2", timeTick, 0, m2Cdf.length, m2Cdf, approximator);

        Analytical q_3 = new Analytical("Q'''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical r_3 = new Analytical("R''''", stochasticTransitionFeatures.get(counter % features.size()));
        Analytical r_3_bis = new Analytical("R'''Bis", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical s_3 = new Analytical("S'''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical v_3 = new Analytical("V'''", stochasticTransitionFeatures.get(counter++ % features.size()));

        Numerical tu_3 = Numerical.and(List.of(
                Numerical.truncatedExp("U'''", features.get(counter % features.size()).get("lambda"),
                        features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick),
                Numerical.seq(List.of(
                        Numerical.truncatedExp("T1'''", features.get(counter % features.size()).get("lambda"),
                                features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick),
                        Numerical.truncatedExp("T2'''", features.get(counter % features.size()).get("lambda"),
                                features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick)
                ))
        ));

        Numerical wx_3 = Numerical.and(List.of(
                Numerical.truncatedExp("W'''", features.get(counter % features.size()).get("lambda"),
                        features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick),
                Numerical.seq(List.of(
                        Numerical.truncatedExp("X1'''", features.get(counter % features.size()).get("lambda"),
                                features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick),
                        Numerical.truncatedExp("X2'''", features.get(counter % features.size()).get("lambda"),
                                features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick)
                ))
        ));

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

        TransientSolution<DeterministicEnablingState, RewardRate> m3UpAnalysis_0 = m_3_up.analyze(m_3_up.upp().add(BigDecimal.valueOf(2)).toString(), timeTick.toString(), "0.001");
        double[] m3UpCdf = new double[m3UpAnalysis_0.getSolution().length];
        for(int count = 0; count < m3UpAnalysis_0.getSolution().length; count++){
            m3UpCdf[count] = m3UpAnalysis_0.getSolution()[count][0][0];
        }

        TransientSolution<DeterministicEnablingState, RewardRate> m3DownAnalysis_0 = m_3_down.analyze(m_3_down.upp().add(BigDecimal.valueOf(2)).toString(), timeTick.toString(), "0.001");
        double[] m3DownCdf = new double[m3DownAnalysis_0.getSolution().length];
        for(int count = 0; count < m3DownAnalysis_0.getSolution().length; count++){
            m3DownCdf[count] = m3DownAnalysis_0.getSolution()[count][0][0];
        }

        // M3
        Numerical numericalM3 = Numerical.and(List.of(
                new Numerical("M3Up_Numerical", timeTick, 0, m3UpCdf.length, m3UpCdf),
                new Numerical("M3Down_Numerical", timeTick, 0, m3DownCdf.length, m3DownCdf)
        ));

        Analytical n = new Analytical("N", stochasticTransitionFeatures.get(counter++ % features.size()));

        // Gestisco E
        Numerical oNumerical = Numerical.and(List.of(
                Numerical.seq(List.of(
                        Numerical.truncatedExp("G'", features.get(counter % features.size()).get("lambda"),
                                features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick),
                        Numerical.and(List.of(
                                Numerical.truncatedExp("I'", features.get(counter % features.size()).get("lambda"),
                                        features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick),
                                Numerical.truncatedExp("J'", features.get(counter % features.size()).get("lambda"),
                                        features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick)
                        ))
                )),
                Numerical.seq(List.of(
                        Numerical.truncatedExp("K'", features.get(counter % features.size()).get("lambda"),
                                features.get(counter % features.size()).get("low"), features.get(counter++ % features.size()).get("upp"), timeTick),
                        Numerical.and(List.of(numericalM1, numericalM2)))
                ))
        );

        Repeat m = new Repeat("M", 0.2, numericalP);
        Numerical o = new Numerical("O", timeTick, oNumerical.min(), oNumerical.max(), oNumerical.getCdf(), approximator);

        Repeat e = new Repeat("E", 0.3,
                DAG.sequence("L", m, n, o)
        );

        TransientSolution<DeterministicEnablingState, RewardRate> eAnalysis = e.analyze("33.5", timeTick.toString(), "0.001");
        double[] eCdf = new double[eAnalysis.getSolution().length];
        for (int count = 0; count < eAnalysis.getSolution().length; count++) {
            eCdf[count] = eAnalysis.getSolution()[count][0][0];
        }

        Numerical numericalE = new Numerical("e", timeTick, 0, eCdf.length, eCdf, approximator);

        Analytical f = new Analytical("F", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical j = new Analytical("J", stochasticTransitionFeatures.get(counter++ % features.size()));
        Xor g = new Xor("G",
                List.of(new Analytical("G1", stochasticTransitionFeatures.get(counter++ % features.size())),
                        new Analytical("G2", stochasticTransitionFeatures.get(counter++ % features.size()))),
                List.of(0.7, 0.3)
        );
        Xor i = new Xor("I",
                List.of(new Analytical("I1", stochasticTransitionFeatures.get(counter++ % features.size())), new Analytical("I2", stochasticTransitionFeatures.get(counter++ % features.size()))),
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

    /* DAG dependency breaking - no numerical */
    public Activity buildModelForAnalysis_Heuristic3(BigDecimal timeBound, BigDecimal timeTick) {
        int featuresSize = features.size();
        int counter = 0;
        Approximator approximator = this.getApproximator();

        ArrayList<StochasticTransitionFeature> stochasticTransitionFeatures = new ArrayList();
        for(Map<String, Double> feature: features){
            List<GEN> transition_gens = new ArrayList<>();
            DBMZone transition_d_0 = new DBMZone(Variable.X);
            ShiftedTruncatedExponentialDistribution distribution =
                    new ShiftedTruncatedExponentialDistribution("distribution", BigDecimal.valueOf(feature.get("low")),
                            BigDecimal.valueOf(feature.get("upp")), BigDecimal.valueOf(feature.get("lambda")));

            Expolynomial transition_e_0 = Expolynomial.fromString(distribution.getExpolynomialDensityString());
            //Normalization
            transition_d_0.setCoefficient(Variable.X, Variable.TSTAR, new OmegaBigDecimal(String.valueOf(distribution.getUpp())));
            transition_d_0.setCoefficient(Variable.TSTAR, Variable.X, new OmegaBigDecimal(String.valueOf(-distribution.getLow().doubleValue())));
            GEN transition_gen_0 = new GEN(transition_d_0, transition_e_0);
            transition_gens.add(transition_gen_0);
            PartitionedGEN transition_pFunction = new PartitionedGEN(transition_gens);
            stochasticTransitionFeatures.add(StochasticTransitionFeature.of(transition_pFunction));
        }


        // Computing P, M', N' and K
        Analytical q_0 = new Analytical("Q", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical r_0 = new Analytical("R", stochasticTransitionFeatures.get(counter % features.size()));
        Analytical r_0_bis = new Analytical("RBis", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical s_0 = new Analytical("S", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical v_0 = new Analytical("V", stochasticTransitionFeatures.get(counter++ % features.size()));

        DAG tu_0 = DAG.forkJoin("TU",
                new Analytical("U", stochasticTransitionFeatures.get(counter++ % features.size())),
                DAG.sequence("T",
                        new Analytical("T1", stochasticTransitionFeatures.get(counter++ % features.size())),
                        new Analytical("T2", stochasticTransitionFeatures.get(counter++ % features.size()))
                )
        );

        DAG wx_0 = DAG.forkJoin("WX",
                new Analytical("W", stochasticTransitionFeatures.get(counter++ % features.size())),
                DAG.sequence("X",
                        new Analytical("X1", stochasticTransitionFeatures.get(counter++ % features.size())),
                        new Analytical("X2", stochasticTransitionFeatures.get(counter++ % features.size()))
                )
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

        TransientSolution<DeterministicEnablingState, RewardRate> pUpAnalysis_0 = p_up_0.analyze(p_up_0.upp().add(BigDecimal.valueOf(2)).toString(), timeTick.toString(), "0.001");
        double[] pUpCdf_0 = new double[pUpAnalysis_0.getSolution().length];
        for(int count = 0; count < pUpAnalysis_0.getSolution().length; count++){
            pUpCdf_0[count] = pUpAnalysis_0.getSolution()[count][0][0];
        }

        TransientSolution<DeterministicEnablingState, RewardRate> pDownAnalysis_0 = p_down_0.analyze(p_down_0.upp().add(BigDecimal.valueOf(2)).toString(), timeTick.toString(), "0.001");
        double[] pDownCdf_0 = new double[pDownAnalysis_0.getSolution().length];
        for(int count = 0; count < pDownAnalysis_0.getSolution().length; count++){
            pDownCdf_0[count] = pDownAnalysis_0.getSolution()[count][0][0];
        }

        Numerical numericalP = Numerical.and(List.of(
                new Numerical("PUp_numerical", timeTick, 0, pUpCdf_0.length, pUpCdf_0, approximator),
                new Numerical("PDown_numerical", timeTick, 0, pDownCdf_0.length, pDownCdf_0, approximator)
        ));

        Analytical q_1 = new Analytical("Q'", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical r_1 = new Analytical("R'", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical s_1 = new Analytical("S'", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical t_1 = new Analytical("T'", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical u_1 = new Analytical("U'", stochasticTransitionFeatures.get(counter++ % features.size()));

        DAG m_1 = DAG.empty("M'");
        q_1.addPrecondition(m_1.begin());
        r_1.addPrecondition(m_1.begin());
        s_1.addPrecondition(m_1.begin());
        t_1.addPrecondition(q_1, r_1);
        u_1.addPrecondition(s_1, r_1);
        m_1.end().addPrecondition(t_1, u_1);

        TransientSolution<DeterministicEnablingState, RewardRate> m1Analysis = m_1.analyze(m_1.upp().toString(), timeTick.toString(), "0.001");
        m1Analysis.getSolution();
        double[] m1Cdf = new double[m1Analysis.getSolution().length];
        for (int count = 0; count < m1Analysis.getSolution().length; count++) {
            m1Cdf[count] = m1Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM1 = new Numerical("m1", timeTick, 0, m1Cdf.length, m1Cdf, approximator);

        Analytical q_2 = new Analytical("Q''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical r_2 = new Analytical("R''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical s_2 = new Analytical("S''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical t_2 = new Analytical("T''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical u_2 = new Analytical("U''", stochasticTransitionFeatures.get(counter++ % features.size()));

        DAG m_2 = DAG.empty("M''");
        q_2.addPrecondition(m_2.begin());
        r_2.addPrecondition(m_2.begin());
        s_2.addPrecondition(m_2.begin());
        t_2.addPrecondition(q_2, r_2);
        u_2.addPrecondition(s_2, r_2);
        m_2.end().addPrecondition(t_2, u_2);

        TransientSolution<DeterministicEnablingState, RewardRate> m2Analysis = m_2.analyze(m_2.upp().toString(), timeTick.toString(), "0.001");
        m2Analysis.getSolution();
        double[] m2Cdf = new double[m2Analysis.getSolution().length];
        for (int count = 0; count < m2Analysis.getSolution().length; count++) {
            m2Cdf[count] = m2Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM2 = new Numerical("m2", timeTick, 0, m2Cdf.length, m2Cdf, approximator);

        Analytical q_3 = new Analytical("Q'''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical r_3 = new Analytical("R''''", stochasticTransitionFeatures.get(counter % features.size()));
        Analytical r_3_bis = new Analytical("R'''Bis", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical s_3 = new Analytical("S'''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical v_3 = new Analytical("V'''", stochasticTransitionFeatures.get(counter++ % features.size()));

        DAG tu_3 = DAG.forkJoin("TU'''",
                new Analytical("U'''", stochasticTransitionFeatures.get(counter++ % features.size())),
                DAG.sequence("T'''",
                        new Analytical("T1'''", stochasticTransitionFeatures.get(counter++ % features.size())),
                        new Analytical("T2'''", stochasticTransitionFeatures.get(counter++ % features.size()))
                )
        );

        DAG wx_3 = DAG.forkJoin("WX'''",
                new Analytical("W'''", stochasticTransitionFeatures.get(counter++ % features.size())),
                DAG.sequence("X'''",
                        new Analytical("X1'''", stochasticTransitionFeatures.get(counter++ % features.size())),
                        new Analytical("X2'''", stochasticTransitionFeatures.get(counter++ % features.size()))
                )
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

        TransientSolution<DeterministicEnablingState, RewardRate> m3UpAnalysis_0 = m_3_up.analyze(m_3_up.upp().add(BigDecimal.valueOf(2)).toString(), timeTick.toString(), "0.001");
        double[] m3UpCdf = new double[m3UpAnalysis_0.getSolution().length];
        for(int count = 0; count < m3UpAnalysis_0.getSolution().length; count++){
            m3UpCdf[count] = m3UpAnalysis_0.getSolution()[count][0][0];
        }

        TransientSolution<DeterministicEnablingState, RewardRate> m3DownAnalysis_0 = m_3_down.analyze(m_3_down.upp().add(BigDecimal.valueOf(2)).toString(), timeTick.toString(), "0.001");
        double[] m3DownCdf = new double[m3DownAnalysis_0.getSolution().length];
        for(int count = 0; count < m3DownAnalysis_0.getSolution().length; count++){
            m3DownCdf[count] = m3DownAnalysis_0.getSolution()[count][0][0];
        }

        // P
        Numerical numericalM3 = Numerical.and(List.of(
                new Numerical("M3Up_Numerical", timeTick, 0, m3UpCdf.length, m3UpCdf),
                new Numerical("M3Down_Numerical", timeTick, 0, m3DownCdf.length, m3DownCdf)
        ));

        Analytical n = new Analytical("N", stochasticTransitionFeatures.get(counter++ % features.size()));

        // Gestisco E
        DAG o1 = DAG.sequence("O1",
                new Analytical("G'", stochasticTransitionFeatures.get(counter++ % features.size())),
                DAG.forkJoin("I'1J'",
                        new Analytical("I'", stochasticTransitionFeatures.get(counter++ % features.size())),
                        new Analytical("J'", stochasticTransitionFeatures.get(counter++ % features.size()))
                )
        );

        TransientSolution<DeterministicEnablingState, RewardRate> o1Analysis = o1.analyze(o1.upp().add(BigDecimal.ONE).toString(), timeTick.toString(), "0.001");
        double[] o1Cdf = new double[o1Analysis.getSolution().length];
        for(int count = 0; count < o1Analysis.getSolution().length; count++){
            o1Cdf[count] = o1Analysis.getSolution()[count][0][0];
        }

        Numerical o1Numerical = new Numerical("O1_Numerical", timeTick, 0, o1Cdf.length, o1Cdf, approximator);

        DAG o2 = DAG.sequence("O2",
                new Analytical("K", stochasticTransitionFeatures.get(counter++ % features.size())),
                DAG.forkJoin("M1M2", numericalM1, numericalM2)
        );

        TransientSolution<DeterministicEnablingState, RewardRate> o2Analysis = o2.analyze(o2.upp().add(BigDecimal.ONE).toString(), timeTick.toString(), "0.001");
        double[] o2Cdf = new double[o2Analysis.getSolution().length];
        for(int count = 0; count < o2Analysis.getSolution().length; count++){
            o2Cdf[count] = o2Analysis.getSolution()[count][0][0];
        }

        Numerical o2Numerical = new Numerical("O2_Numerical", timeTick, 0, o2Cdf.length, o2Cdf, approximator);

        DAG o = DAG.forkJoin("O", o1Numerical, o2Numerical);

        TransientSolution<DeterministicEnablingState, RewardRate> oAnalysis = o.analyze(o.upp().toString(), timeTick.toString(), "0.001");
        double[] oCdf = new double[oAnalysis.getSolution().length];
        for(int count = 0; count < oAnalysis.getSolution().length; count++){
            oCdf[count] = oAnalysis.getSolution()[count][0][0];
        }

        Numerical oNumerical = new Numerical("O_Numerical", timeTick, 0, oCdf.length, oCdf, approximator);
        Repeat m = new Repeat("M", 0.2, numericalP);

        Repeat e = new Repeat("E", 0.3,
                DAG.sequence("L", m, n, oNumerical)
        );

        TransientSolution<DeterministicEnablingState, RewardRate> eAnalysis = e.analyze("33.5", timeTick.toString(), "0.001");
        double[] eCdf = new double[eAnalysis.getSolution().length];
        for(int count = 0; count < eAnalysis.getSolution().length; count++){
            eCdf[count] = eAnalysis.getSolution()[count][0][0];
        }

        Numerical numericalE = new Numerical("e", timeTick, 0, eCdf.length, eCdf, approximator);

        Analytical f = new Analytical("F", stochasticTransitionFeatures.get(counter % features.size()));
        Analytical fBis = new Analytical("FBis", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical j = new Analytical("J", stochasticTransitionFeatures.get(counter++ % features.size()));
        Xor g = new Xor("G",
                List.of(new Analytical("G1", stochasticTransitionFeatures.get(counter++ % features.size())),
                        new Analytical("G2", stochasticTransitionFeatures.get(counter++ % features.size()))),
                List.of(0.7, 0.3)
        );
        Xor i = new Xor("I",
                List.of(new Analytical("I1", stochasticTransitionFeatures.get(counter++ % features.size())),
                        new Analytical("I2", stochasticTransitionFeatures.get(counter++ % features.size()))),
                List.of(0.1, 0.9)
        );

        // Gestione Main
        DAG main1 = DAG.empty("Main1");
        numericalE.addPrecondition(main1.begin());
        f.addPrecondition(main1.begin());
        i.addPrecondition(numericalE, f);
        main1.end().addPrecondition(i);

        DAG main2 = DAG.empty("Main2");
        g.addPrecondition(main2.begin());
        fBis.addPrecondition(main2.begin());
        numericalM3.addPrecondition(fBis);
        j.addPrecondition(g, fBis);
        main2.end().addPrecondition(numericalM3, j);

        TransientSolution<DeterministicEnablingState, RewardRate> main1Analysis = main1.analyze(main1.upp().add(BigDecimal.valueOf(2)).toString(), timeTick.toString(), "0.001");
        double[] main1Cdf = new double[main1Analysis.getSolution().length];
        for(int count = 0; count < main1Analysis.getSolution().length; count++){
            main1Cdf[count] = main1Analysis.getSolution()[count][0][0];
        }

        TransientSolution<DeterministicEnablingState, RewardRate> main2Analysis = main2.analyze(main2.upp().add(BigDecimal.valueOf(2)).toString(), timeTick.toString(), "0.001");
        double[] main2Cdf = new double[main2Analysis.getSolution().length];
        for(int count = 0; count < main2Analysis.getSolution().length; count++){
            main2Cdf[count] = main2Analysis.getSolution()[count][0][0];
        }

        Numerical numMain1 = new Numerical("numericalM1", timeTick, 0, main1Cdf.length, main1Cdf);
        Numerical numMain2 = new Numerical("numericalM2", timeTick, 0, main2Cdf.length, main2Cdf);

        DAG main = DAG.forkJoin("MAIN", numMain1, numMain2);

        return main;
    }

    /* DAG numerical&approximation - no numerical */
    public Activity buildModelForAnalysis_Heuristic4(BigDecimal timeBound, BigDecimal timeTick) {
        int featuresSize = features.size();
        int counter = 0;
        Approximator approximator = this.getApproximator();

        // Computing P, M', N' and K
        Analytical q_0 = new Analytical("Q", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical r_0 = new Analytical("R", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical s_0 = new Analytical("S", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical u_0 = new Analytical("U", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical v_0 = new Analytical("V", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical w_0 = new Analytical("W", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));

        DAG tu_0 = DAG.forkJoin("TU",
                DAG.sequence("T",
                        new Analytical("T1", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp")))),
                        new Analytical("T2", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))))
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
                        new Analytical("X1", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp")))),
                        new Analytical("X2", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))))
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

        Analytical q_1 = new Analytical("Q'", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical r_1 = new Analytical("R'", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical s_1 = new Analytical("S'", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical t_1 = new Analytical("T'", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical u_1 = new Analytical("U'", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));

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
        for (int count = 0; count < m1Analysis.getSolution().length; count++) {
            m1Cdf[count] = m1Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM1 = new Numerical("m1", timeTick, 0, m1Cdf.length + 1, m1Cdf, approximator);

        Analytical q_2 = new Analytical("Q''", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical r_2 = new Analytical("R''", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical s_2 = new Analytical("S''", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical t_2 = new Analytical("T''", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical u_2 = new Analytical("U''", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));

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
        for (int count = 0; count < m2Analysis.getSolution().length; count++) {
            m2Cdf[count] = m2Analysis.getSolution()[count][0][0];
        }

        Numerical numericalM2 = new Numerical("m2", timeTick, 0, m2Cdf.length + 1, m2Cdf, approximator);

        Analytical q_3 = new Analytical("Q'''", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical r_3 = new Analytical("R''''", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical s_3 = new Analytical("S'''", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical u_3 = new Analytical("U'''", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical v_3 = new Analytical("V'''", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical w_3 = new Analytical("W'''", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));

        DAG tu_3 = DAG.forkJoin("TU'''",
                DAG.sequence("T'''",
                        new Analytical("T1'''", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp")))),
                        new Analytical("T2'''", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))))
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
                        new Analytical("X1'''", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp")))),
                        new Analytical("X2'''", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))))
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
                new Analytical("G'", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp")))),
                DAG.forkJoin("I'1J'",
                        new Analytical("I'", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp")))),
                        new Analytical("J'", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))))
                )
        );

        TransientSolution<DeterministicEnablingState, RewardRate> o1Analysis = o1.analyze("2", timeTick.toString(), "0.001");
        double[] o1Cdf = new double[o1Analysis.getSolution().length];
        for(int count = 0; count < o1Analysis.getSolution().length; count++){
            o1Cdf[count] = o1Analysis.getSolution()[count][0][0];
        }

        Numerical o1Numerical = new Numerical("O1_Numerical", timeTick, 0, o1Cdf.length, o1Cdf, approximator);

        DAG o2 = DAG.sequence("O2",
                new Analytical("K", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp")))),
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
        Analytical n = new Analytical("N", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));

        Repeat e = new Repeat("E", 0.3,
                DAG.sequence("L", m, n, oNumerical)
        );

        TransientSolution<DeterministicEnablingState, RewardRate> eAnalysis = e.analyze(timeBound.toString(), timeTick.toString(), "0.001");
        double[] eCdf = new double[eAnalysis.getSolution().length];
        for(int count = 0; count < eAnalysis.getSolution().length; count++){
            eCdf[count] = eAnalysis.getSolution()[count][0][0];
        }

        Numerical numericalE = new Numerical("e", timeTick, 0,  timeBound.divide(timeTick).intValue(), eCdf, approximator);

        Analytical f = new Analytical("F", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Analytical j = new Analytical("J", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))));
        Xor g = new Xor("G",
                List.of(new Analytical("G1", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp")))), new Analytical("G2", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))))),
                List.of(0.7, 0.3)
        );
        Xor i = new Xor("I",
                List.of(new Analytical("I1", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp")))), new Analytical("I2", StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(features.get((counter) % featuresSize).get("low")), BigDecimal.valueOf(features.get((counter++) % featuresSize).get("upp"))))),
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
        int featuresSize = features.size();
        int counter = 0;

        ArrayList<StochasticTransitionFeature> stochasticTransitionFeatures = new ArrayList();
        for(Map<String, Double> feature: features){
            List<GEN> transition_gens = new ArrayList<>();
            DBMZone transition_d_0 = new DBMZone(Variable.X);
            ShiftedTruncatedExponentialDistribution distribution =
                    new ShiftedTruncatedExponentialDistribution("distribution", BigDecimal.valueOf(feature.get("low")),
                            BigDecimal.valueOf(feature.get("upp")), BigDecimal.valueOf(feature.get("lambda")));

            Expolynomial transition_e_0 = Expolynomial.fromString(distribution.getExpolynomialDensityString());
            //Normalization
            transition_d_0.setCoefficient(Variable.X, Variable.TSTAR, new OmegaBigDecimal(String.valueOf(distribution.getUpp())));
            transition_d_0.setCoefficient(Variable.TSTAR, Variable.X, new OmegaBigDecimal(String.valueOf(-distribution.getLow().doubleValue())));
            GEN transition_gen_0 = new GEN(transition_d_0, transition_e_0);
            transition_gens.add(transition_gen_0);
            PartitionedGEN transition_pFunction = new PartitionedGEN(transition_gens);
            stochasticTransitionFeatures.add(StochasticTransitionFeature.of(transition_pFunction));
        }


        // Computing M, M', N' and K
        Analytical q_0 = new Analytical("Q", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical r_0 = new Analytical("R", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical s_0 = new Analytical("S", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical v_0 = new Analytical("V", stochasticTransitionFeatures.get(counter++ % features.size()));

        DAG tu_0 = DAG.forkJoin("TU",
                new Analytical("U", stochasticTransitionFeatures.get(counter++ % features.size())),
                DAG.sequence("T",
                        new Analytical("T1", stochasticTransitionFeatures.get(counter++ % features.size())),
                        new Analytical("T2", stochasticTransitionFeatures.get(counter++ % features.size()))
                )
        );
        DAG wx_0 = DAG.forkJoin("WX",
                new Analytical("W", stochasticTransitionFeatures.get(counter++ % features.size())),
                DAG.sequence("X",
                        new Analytical("X1", stochasticTransitionFeatures.get(counter++ % features.size())),
                        new Analytical("X2", stochasticTransitionFeatures.get(counter++ % features.size()))
                )
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

        Analytical q_1 = new Analytical("Q'", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical r_1 = new Analytical("R'", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical s_1 = new Analytical("S'", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical t_1 = new Analytical("T'", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical u_1 = new Analytical("U'", stochasticTransitionFeatures.get(counter++ % features.size()));

        DAG m_1 = DAG.empty("M'");
        q_1.addPrecondition(m_1.begin());
        r_1.addPrecondition(m_1.begin());
        s_1.addPrecondition(m_1.begin());
        t_1.addPrecondition(q_1, r_1);
        u_1.addPrecondition(s_1, r_1);
        m_1.end().addPrecondition(t_1, u_1);

        Analytical q_2 = new Analytical("Q''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical r_2 = new Analytical("R''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical s_2 = new Analytical("S''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical t_2 = new Analytical("T''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical u_2 = new Analytical("U''", stochasticTransitionFeatures.get(counter++ % features.size()));

        DAG m_2 = DAG.empty("M''");
        q_2.addPrecondition(m_2.begin());
        r_2.addPrecondition(m_2.begin());
        s_2.addPrecondition(m_2.begin());
        t_2.addPrecondition(q_2, r_2);
        u_2.addPrecondition(s_2, r_2);
        m_2.end().addPrecondition(t_2, u_2);

        Analytical q_3 = new Analytical("Q'''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical r_3 = new Analytical("R'''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical s_3 = new Analytical("S'''", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical v_3 = new Analytical("V'''", stochasticTransitionFeatures.get(counter++ % features.size()));

        DAG tu_3 = DAG.forkJoin("TU'''",
                new Analytical("U'''", stochasticTransitionFeatures.get(counter++ % features.size())),
                DAG.sequence("T3'''",
                        new Analytical("T1'''", stochasticTransitionFeatures.get(counter++ % features.size())),
                        new Analytical("T2'''", stochasticTransitionFeatures.get(counter++ % features.size()))
                )
        );

        DAG wx_3 = DAG.forkJoin("WX'''",
                new Analytical("W'''", stochasticTransitionFeatures.get(counter++ % features.size())),
                DAG.sequence("X'''",
                        new Analytical("X1'''", stochasticTransitionFeatures.get(counter++ % features.size())),
                        new Analytical("X2'''", stochasticTransitionFeatures.get(counter++ % features.size()))
                )
        );

        DAG m_3 = DAG.empty("M'''");
        q_3.addPrecondition(m_3.begin());
        r_3.addPrecondition(m_3.begin());
        s_3.addPrecondition(m_3.begin());
        tu_3.addPrecondition(q_3, r_3);
        v_3.addPrecondition(r_3);
        wx_3.addPrecondition(r_3, s_3);
        m_3.end().addPrecondition(tu_3, v_3, wx_3);

        Repeat e = new Repeat("E", 0.3,
                DAG.sequence("L",
                        m, new Analytical("N", stochasticTransitionFeatures.get(counter++ % features.size())),
                        DAG.forkJoin("O",
                                DAG.sequence("E'",
                                        new Analytical("G'", stochasticTransitionFeatures.get(counter++ % features.size())),
                                        DAG.forkJoin("H'",
                                                new Analytical("I'", stochasticTransitionFeatures.get(counter++ % features.size())),
                                                new Analytical("J'", stochasticTransitionFeatures.get(counter++ % features.size()))
                                        )),
                                DAG.sequence("F'",
                                        new Analytical("K'", stochasticTransitionFeatures.get(counter++ % features.size())),
                                        DAG.forkJoin("L'", m_1, m_2)
                                )
                        )
                )
        );

        Analytical f = new Analytical("F", stochasticTransitionFeatures.get(counter++ % features.size()));
        Analytical j = new Analytical("J", stochasticTransitionFeatures.get(counter++ % features.size()));
        Xor g = new Xor("G", List.of(
                new Analytical("G1", stochasticTransitionFeatures.get(counter++ % features.size())),
                new Analytical("G2", stochasticTransitionFeatures.get(counter++ % features.size()))),
                List.of(0.7, 0.3)
        );
        Xor i = new Xor("I", List.of(
                new Analytical("I1", stochasticTransitionFeatures.get(counter++ % features.size())),
                new Analytical("I2", stochasticTransitionFeatures.get(counter++ % features.size()))),
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
