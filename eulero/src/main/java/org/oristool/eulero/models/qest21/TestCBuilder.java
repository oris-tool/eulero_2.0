package org.oristool.eulero.models.qest21;

import org.oristool.eulero.graph.*;
import org.oristool.eulero.models.ModelBuilder;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TestCBuilder extends ModelBuilder {
    public TestCBuilder(StochasticTransitionFeature feature) {
        super(feature);
    }

    public TestCBuilder(ArrayList<StochasticTransitionFeature> features, ArrayList<BigDecimal> weights) {
        super(features, weights);
    }

    @Override
    public Activity buildModel() {
        ArrayList<StochasticTransitionFeature> features = this.getFeatures();
        ArrayList<BigDecimal> weights = this.getWeights();


        // Computing M, M', N' and K
        Analytical q_0 = new Analytical("SimDAG_0_Q", features, weights);
        Analytical r_0 = new Analytical("SimDAG_0_R", features, weights);
        Analytical s_0 = new Analytical("SimDAG_0_S", features, weights);
        Analytical t_0 = new Analytical("SimDAG_0_T", features, weights);
        Analytical v_0 = new Analytical("SimDAG_0_V", features, weights);

        DAG simDag_0 = DAG.empty("SimDAG_0");
        q_0.addPrecondition(simDag_0.begin());
        r_0.addPrecondition(simDag_0.begin());
        s_0.addPrecondition(simDag_0.begin());
        t_0.addPrecondition(q_0, r_0);
        v_0.addPrecondition(r_0, s_0);
        simDag_0.end().addPrecondition(t_0, v_0);
        simDag_0.setEFT(simDag_0.low());
        simDag_0.setLFT(simDag_0.upp());

        Repeat bRepetition = new Repeat("NR_B", 0.2, simDag_0);

        Analytical q_1 = new Analytical("SimDAG_1_Q", features, weights);
        Analytical r_1 = new Analytical("SimDAG_1_R", features, weights);
        Analytical s_1 = new Analytical("SimDAG_1_S", features, weights);
        Analytical t_1 = new Analytical("SimDAG_1_T", features, weights);
        Analytical u_1 = new Analytical("SimDAG_1_U", features, weights);

        DAG simDag_1 = DAG.empty("SimDAG_1");
        q_1.addPrecondition(simDag_1.begin());
        r_1.addPrecondition(simDag_1.begin());
        s_1.addPrecondition(simDag_1.begin());
        t_1.addPrecondition(q_1, r_1);
        u_1.addPrecondition(s_1, r_1);
        simDag_1.end().addPrecondition(t_1, u_1);
        simDag_1.setEFT(simDag_1.low());
        simDag_1.setLFT(simDag_1.upp());

        Analytical q_2 = new Analytical("SimDAG_2_Q", features, weights);
        Analytical r_2 = new Analytical("SimDAG_2_R", features, weights);
        Analytical s_2 = new Analytical("SimDAG_2_S", features, weights);
        Analytical t_2 = new Analytical("SimDAG_2_T", features, weights);
        Analytical u_2 = new Analytical("SimDAG_2_U", features, weights);

        DAG simDag_2 = DAG.empty("SimDAG_2");
        q_2.addPrecondition(simDag_2.begin());
        r_2.addPrecondition(simDag_2.begin());
        s_2.addPrecondition(simDag_2.begin());
        t_2.addPrecondition(q_2, r_2);
        u_2.addPrecondition(s_2, r_2);
        simDag_2.end().addPrecondition(t_2, u_2);
        simDag_2.setEFT(simDag_2.low());
        simDag_2.setLFT(simDag_2.upp());

        Analytical q_3 = new Analytical("SimDAG_3_Q", features, weights);
        Analytical r_3 = new Analytical("SimDAG_3_R", features, weights);
        Analytical s_3 = new Analytical("SimDAG_3_S", features, weights);
        Analytical t_3 = new Analytical("SimDAG_3_T", features, weights);
        Analytical v_3 = new Analytical("SimDAG_3_V", features, weights);

        DAG simDag_3 = DAG.empty("SimDAG_3");
        q_3.addPrecondition(simDag_3.begin());
        r_3.addPrecondition(simDag_3.begin());
        s_3.addPrecondition(simDag_3.begin());
        t_3.addPrecondition(q_3, r_3);
        v_3.addPrecondition(r_3, s_3);
        simDag_3.end().addPrecondition(t_3, v_3);
        simDag_3.setEFT(simDag_3.low());
        simDag_3.setLFT(simDag_3.upp());

        Analytical q_4 = new Analytical("SimDAG_4_Q", features, weights);
        Analytical r_4 = new Analytical("SimDAG_4_R", features, weights);
        Analytical s_4 = new Analytical("SimDAG_4_S", features, weights);
        Analytical t_4 = new Analytical("SimDAG_4_T", features, weights);
        Analytical v_4 = new Analytical("SimDAG_4_V", features, weights);

        DAG simDag_4 = DAG.empty("SimDAG_4");
        q_4.addPrecondition(simDag_4.begin());
        r_4.addPrecondition(simDag_4.begin());
        s_4.addPrecondition(simDag_4.begin());
        t_4.addPrecondition(q_4, r_4);
        v_4.addPrecondition(r_4, s_4);
        simDag_4.end().addPrecondition(t_4, v_4);
        simDag_4.setEFT(simDag_4.low());
        simDag_4.setLFT(simDag_4.upp());


        /*Repeat nestedRepetition = new Repeat("NestedRepetition", 0.15,
                DAG.sequence("NR_A",
                        bRepetition,
                        new Analytical("NR_AD", feature),
                        DAG.forkJoin("NR_ComAND",
                                DAG.sequence("NR_ComAND_A",
                                        new Analytical("NR_ComAND_B", feature),
                                        DAG.forkJoin("NR_ComAND_C",
                                                new Analytical("NR_ComAND_D", feature),
                                                new Analytical("NR_ComAND_E", feature)
                                        )
                                ),
                                DAG.sequence("NR_ComAND_F",
                                        new Analytical("NR_ComAND_G", feature),
                                        DAG.forkJoin("NR_ComAND_H", simDag_1, simDag_2)
                                )
                        )
                )
        );*/

        DAG main = DAG.forkJoin("MAIN",
                DAG.forkJoin("A",
                        DAG.sequence("B",
                                new Analytical("C", features, weights),
                                new Analytical("D", features, weights),
                                new Analytical("E", features, weights),
                                new Analytical("F", features, weights)
                        ),
                        /*nestedRepetition*/ simDag_4
                ),
                DAG.sequence("H",
                        DAG.forkJoin("I",
                                new Analytical("J", features, weights),
                                DAG.sequence("K",
                                        new Analytical("L", features, weights),
                                        new Analytical("M", features, weights)
                                )
                        ),
                        DAG.forkJoin("N",
                                DAG.sequence("O",
                                        new Analytical("P", features, weights),
                                        new Analytical("Q'", features, weights)
                                ),
                                new Xor("R",
                                        List.of(
                                                new Analytical("S", features, weights),
                                                new Analytical("T", features, weights)
                                        ),
                                        List.of(0.3, 0.7)
                                )
                        ),
                        DAG.forkJoin("U",
                                DAG.sequence("V",
                                        new Analytical("W", features, weights),
                                        new Analytical("X", features, weights),
                                        new Analytical("Y", features, weights)
                                ), simDag_3
                        )
                )
        );

        return main;
    }
}
