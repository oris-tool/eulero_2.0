package org.oristool.eulero.modelgenerator.qest21;

import org.oristool.eulero.graph.Activity;
import org.oristool.eulero.graph.Analytical;
import org.oristool.eulero.graph.DAG;
import org.oristool.eulero.graph.Xor;
import org.oristool.eulero.modelgenerator.ModelBuilder;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TestBBuilder extends ModelBuilder {
    public TestBBuilder(StochasticTransitionFeature feature) {
        super(feature);
    }

    public TestBBuilder(ArrayList<StochasticTransitionFeature> features, ArrayList<BigDecimal> weights) {
        super(features, weights);
    }

    @Override
    public Activity buildModel() {
        ArrayList<StochasticTransitionFeature> features = this.getFeatures();
        ArrayList<BigDecimal> weights = this.getWeights();


        Analytical q_0 = new Analytical("SimDAG_0_Q", features, weights);
        Analytical r_0 = new Analytical("SimDAG_0_R", features, weights);
        Analytical s_0 = new Analytical("SimDAG_0_S", features, weights);
        Analytical u_0 = new Analytical("SimDAG_0_U", features, weights);
        Analytical v_0 = new Analytical("SimDAG_0_V", features, weights);
        Analytical w_0 = new Analytical("SimDAG_0_W", features, weights);

        DAG tu_0 = DAG.forkJoin("SimDAG_0_TU",
                DAG.sequence("SimDAG_0_T",
                        new Analytical("SimDAG_0_T1", features, weights),
                        new Analytical("SimDAG_0_T2", features, weights)
                ), u_0
        );

        DAG wx_0 = DAG.forkJoin("SimDAG_0_WX",
                DAG.sequence("SimDAG_0_X",
                        new Analytical("SimDAG_0_X1", features, weights),
                        new Analytical("SimDAG_0_X2", features, weights)
                ),
                w_0
        );

        DAG simDag_0 = DAG.empty("SimDAG_0");
        q_0.addPrecondition(simDag_0.begin());
        r_0.addPrecondition(simDag_0.begin());
        s_0.addPrecondition(simDag_0.begin());
        tu_0.addPrecondition(q_0, r_0);
        v_0.addPrecondition(r_0);
        wx_0.addPrecondition(s_0, r_0);

        simDag_0.end().addPrecondition(tu_0, v_0, wx_0);
        simDag_0.setEFT(simDag_0.low());
        simDag_0.setLFT(simDag_0.upp());

        Analytical q_1 = new Analytical("SimDAG_1_Q", features, weights);
        Analytical r_1 = new Analytical("SimDAG_1_R", features, weights);
        Analytical s_1 = new Analytical("SimDAG_1_S", features, weights);
        Analytical u_1 = new Analytical("SimDAG_1_U", features, weights);
        Analytical t_1 = new Analytical("SimDAG_1_T", features, weights);

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
        Analytical u_2 = new Analytical("SimDAG_2_U", features, weights);
        Analytical t_2 = new Analytical("SimDAG_2_T", features, weights);

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
        Analytical u_3 = new Analytical("SimDAG_3_U", features, weights);
        Analytical v_3 = new Analytical("SimDAG_3_V", features, weights);
        Analytical w_3 = new Analytical("SimDAG_3_W", features, weights);

        DAG tu_3 = DAG.forkJoin("SimDAG_3_TU",
                DAG.sequence("SimDAG_3_T",
                        new Analytical("SimDAG_3_T1", features, weights),
                        new Analytical("SimDAG_3_T2", features, weights)
                ), u_3
        );

        DAG wx_3 = DAG.forkJoin("SimDAG_3_WX",
                DAG.sequence("SimDAG_3_X",
                        new Analytical("SimDAG_3_X1", features, weights),
                        new Analytical("SimDAG_3_X2", features, weights)
                ),
                w_3
        );

        DAG simDag_3 = DAG.empty("SimDAG_3");
        q_3.addPrecondition(simDag_3.begin());
        r_3.addPrecondition(simDag_3.begin());
        s_3.addPrecondition(simDag_3.begin());
        tu_3.addPrecondition(q_3, r_3);
        v_3.addPrecondition(r_3);
        wx_3.addPrecondition(s_3, r_3);

        simDag_3.end().addPrecondition(tu_3, v_3, wx_3);
        simDag_3.setEFT(simDag_3.low());
        simDag_3.setLFT(simDag_3.upp());

        return DAG.forkJoin("Main",
                DAG.forkJoin("A",
                        DAG.sequence("B",
                                new Analytical("C", features, weights),
                                new Analytical("D", features, weights),
                                new Analytical("E", features, weights),
                                new Analytical("F", features, weights)
                        ),
                        DAG.sequence("G",
                                simDag_0,
                                new Analytical("I", features, weights),
                                DAG.forkJoin("SimAND",
                                        DAG.sequence("SimAND_A",
                                                new Analytical("SimAND_B", features, weights),
                                                DAG.forkJoin("SimAND_C",
                                                        new Analytical("SimAND_D", features, weights),
                                                        new Analytical("SimAND_E", features, weights)
                                                )
                                        ),
                                        DAG.sequence("SimAND_F",
                                                new Analytical("SimAND_G", features, weights),
                                                DAG.forkJoin("SimAND_H",
                                                        simDag_1,
                                                        simDag_2
                                                )
                                        )
                                )
                        )
                ),
                DAG.sequence("K",
                        DAG.forkJoin("L",
                                new Analytical("M", features, weights),
                                DAG.sequence("N",
                                        new Analytical("O", features, weights),
                                        new Analytical("P", features, weights)
                                )
                        ),
                        DAG.forkJoin("Q",
                                DAG.sequence("R",
                                        new Analytical("S", features, weights),
                                        new Analytical("T", features, weights)
                                ),
                                new Xor("U", List.of(
                                        new Analytical("V", features, weights),
                                        new Analytical("W", features, weights)
                                ), List.of(0.3, 0.7))
                        ),
                        DAG.forkJoin("X",
                                DAG.sequence("Y",
                                        new Analytical("Z", features, weights),
                                        new Analytical("A'", features, weights),
                                        new Analytical("B'", features, weights)
                                ),
                                simDag_3
                        )
                )
        );
    }

    @Override
    public String builderName() {
        return "B";
    }
}
