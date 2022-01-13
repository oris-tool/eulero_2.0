package org.oristool.eulero.models.qest21;

import org.oristool.eulero.graph.Activity;
import org.oristool.eulero.graph.Analytical;
import org.oristool.eulero.graph.DAG;
import org.oristool.eulero.models.ModelBuilder;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TestBuilder extends ModelBuilder {

    public TestBuilder(StochasticTransitionFeature feature) {
        super(feature);
    }

    public TestBuilder(ArrayList<StochasticTransitionFeature> features, ArrayList<BigDecimal> weights) {
        super(features, weights);
    }

    @Override
    public Activity buildModel() {
        ArrayList<StochasticTransitionFeature> features = this.getFeatures();
        ArrayList<BigDecimal> weights = this.getWeights();


        Analytical q_4 = new Analytical("SimDAG_4_Q", features, weights);
        Analytical r_4 = new Analytical("SimDAG_4_R", features, weights);
        Analytical s_4 = new Analytical("SimDAG_4_S", features, weights);
        Analytical u_4 = new Analytical("SimDAG_4_U", features, weights);
        Analytical v_4 = new Analytical("SimDAG_4_V", features, weights);
        Analytical w_4 = new Analytical("SimDAG_4_W", features, weights);

        DAG tu_4 = DAG.forkJoin("SimDAG_4_TU",
                DAG.sequence("SimDAG_4_T",
                        new Analytical("SimDAG_4_T1", features, weights),
                        new Analytical("SimDAG_4_T2", features, weights)
                ), u_4
        );

        DAG wx_4 = DAG.forkJoin("SimDAG_4_WX",
                DAG.sequence("SimDAG_4_X",
                        new Analytical("SimDAG_4_X1", features, weights),
                        new Analytical("SimDAG_4_X2", features, weights)
                ),
                w_4
        );

        DAG simDag_4 = DAG.empty("SimDAG_4");
        q_4.addPrecondition(simDag_4.begin());
        r_4.addPrecondition(simDag_4.begin());
        s_4.addPrecondition(simDag_4.begin());
        tu_4.addPrecondition(q_4, r_4);
        v_4.addPrecondition(r_4);
        wx_4.addPrecondition(s_4, r_4);

        simDag_4.end().addPrecondition(tu_4, v_4, wx_4);
        simDag_4.setEFT(simDag_4.low());
        simDag_4.setLFT(simDag_4.upp());

        return simDag_4;
    }
}
