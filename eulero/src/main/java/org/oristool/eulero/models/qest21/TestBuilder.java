package org.oristool.eulero.models.qest21;

import org.oristool.eulero.graph.Activity;
import org.oristool.eulero.graph.Analytical;
import org.oristool.eulero.graph.DAG;
import org.oristool.eulero.models.ModelBuilder;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

public class TestBuilder  extends ModelBuilder {

    public TestBuilder(StochasticTransitionFeature feature) {
        super(feature);
    }

    @Override
    public Activity buildModel() {
        StochasticTransitionFeature feature = this.getFeature();

        Analytical q_0 = new Analytical("SimDAG_0_Q", feature);
        Analytical r_0 = new Analytical("SimDAG_0_R", feature);
        Analytical s_0 = new Analytical("SimDAG_0_S", feature);
        Analytical u_0 = new Analytical("SimDAG_0_U", feature);
        Analytical v_0 = new Analytical("SimDAG_0_V", feature);
        Analytical w_0 = new Analytical("SimDAG_0_W", feature);

        DAG tu_0 = DAG.forkJoin("SimDAG_0_TU",
                DAG.sequence("SimDAG_0_T",
                        new Analytical("SimDAG_0_T1", feature),
                        new Analytical("SimDAG_0_T2", feature)
                ), u_0
        );

        DAG wx_0 = DAG.forkJoin("SimDAG_0_WX",
                DAG.sequence("SimDAG_0_X",
                        new Analytical("SimDAG_0_X1", feature),
                        new Analytical("SimDAG_0_X2", feature)
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
        return simDag_0;
    }
}
