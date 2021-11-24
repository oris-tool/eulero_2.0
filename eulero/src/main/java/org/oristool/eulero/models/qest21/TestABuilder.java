package org.oristool.eulero.models.qest21;

import org.oristool.eulero.graph.*;
import org.oristool.eulero.models.ModelBuilder;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.util.List;

public class TestABuilder extends ModelBuilder {

    public TestABuilder(StochasticTransitionFeature feature) {
        super(feature);
    }

    @Override
    public Activity buildModel() {
        StochasticTransitionFeature feature = this.getFeature();

        Analytical q_0 = new Analytical("SimDAG_0_Q", feature);
        Analytical r_0 = new Analytical("SimDAG_0_R", feature);
        Analytical s_0 = new Analytical("SimDAG_0_S", feature);
        Analytical t_0 = new Analytical("SimDAG_0_T", feature);
        Analytical v_0 = new Analytical("SimDAG_0_V", feature);
        DAG simDag_0 = DAG.empty("SimDAG_0");
        q_0.addPrecondition(simDag_0.begin());
        r_0.addPrecondition(simDag_0.begin());
        s_0.addPrecondition(simDag_0.begin());
        t_0.addPrecondition(q_0, r_0);
        v_0.addPrecondition(r_0, s_0);
        simDag_0.end().addPrecondition(t_0, v_0);

        Analytical q_1 = new Analytical("SimDAG_1_Q", feature);
        Analytical r_1 = new Analytical("SimDAG_1_R", feature);
        Analytical s_1 = new Analytical("SimDAG_1_S", feature);
        Analytical t_1 = new Analytical("SimDAG_1_T", feature);
        Analytical u_1 = new Analytical("SimDAG_1_U", feature);
        DAG simDag_1 = DAG.empty("SimDAG_1");
        q_1.addPrecondition(simDag_1.begin());
        r_1.addPrecondition(simDag_1.begin());
        s_1.addPrecondition(simDag_1.begin());
        t_1.addPrecondition(q_1, r_1);
        u_1.addPrecondition(s_1, r_1);
        simDag_1.end().addPrecondition(t_1, u_1);

        Analytical q_2 = new Analytical("SimDAG_2_Q", feature);
        Analytical r_2 = new Analytical("SimDAG_2_R", feature);
        Analytical s_2 = new Analytical("SimDAG_2_S", feature);
        Analytical t_2 = new Analytical("SimDAG_2_T", feature);
        Analytical u_2 = new Analytical("SimDAG_2_U", feature);
        DAG simDag_2 = DAG.empty("SimDAG_2");
        q_2.addPrecondition(simDag_2.begin());
        r_2.addPrecondition(simDag_2.begin());
        s_2.addPrecondition(simDag_2.begin());
        t_2.addPrecondition(q_2, r_2);
        u_2.addPrecondition(s_2, r_2);
        simDag_2.end().addPrecondition(t_2, u_2);

        Analytical q_3 = new Analytical("SimDAG_3_Q", feature);
        Analytical r_3 = new Analytical("SimDAG_3_R", feature);
        Analytical s_3 = new Analytical("SimDAG_3_S", feature);
        Analytical t_3 = new Analytical("SimDAG_3_T", feature);
        Analytical v_3 = new Analytical("SimDAG_3_V", feature);
        DAG simDag_3 = DAG.empty("SimDAG_3");
        q_3.addPrecondition(simDag_3.begin());
        r_3.addPrecondition(simDag_3.begin());
        s_3.addPrecondition(simDag_3.begin());
        t_3.addPrecondition(q_3, r_3);
        v_3.addPrecondition(r_3, s_3);
        simDag_3.end().addPrecondition(t_3, v_3);

        return DAG.forkJoin("Main",
                DAG.forkJoin("A",
                        DAG.sequence("B",
                                new Analytical("C", feature),
                                new Analytical("D", feature),
                                new Analytical("E", feature),
                                new Analytical("F", feature)
                        ),
                        DAG.sequence("G",
                                simDag_0,
                                new Analytical("I", feature),
                                DAG.forkJoin("SimAND",
                                        DAG.sequence("SimAND_A",
                                                new Analytical("SimAND_B", feature),
                                                DAG.forkJoin("SimAND_C",
                                                        new Analytical("SimAND_D", feature),
                                                        new Analytical("SimAND_E", feature)
                                                )
                                        ),
                                        DAG.sequence("SimAND_F",
                                                new Analytical("SimAND_G", feature),
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
                                new Analytical("M", feature),
                                DAG.sequence("N",
                                        new Analytical("O", feature),
                                        new Analytical("P", feature)
                                )
                        ),
                        DAG.forkJoin("Q",
                                DAG.sequence("R",
                                        new Analytical("S", feature),
                                        new Analytical("T", feature)
                                ),
                                new Xor("U", List.of(
                                        new Analytical("V", feature),
                                        new Analytical("W", feature)
                                ), List.of(0.3, 0.7))
                        ),
                        DAG.forkJoin("X",
                                DAG.sequence("Y",
                                        new Analytical("Z", feature),
                                        new Analytical("A'", feature),
                                        new Analytical("B'", feature)
                                ),
                                simDag_3
                        )
                )
        );
    }
}
