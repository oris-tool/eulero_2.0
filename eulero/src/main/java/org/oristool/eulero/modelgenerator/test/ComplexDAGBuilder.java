package org.oristool.eulero.modelgenerator.test;

import org.oristool.eulero.graph.Activity;
import org.oristool.eulero.graph.Analytical;
import org.oristool.eulero.graph.DAG;
import org.oristool.eulero.modelgenerator.ModelBuilder;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.ArrayList;

public class ComplexDAGBuilder extends ModelBuilder {
    public ComplexDAGBuilder(StochasticTransitionFeature feature) {
        super(feature);
    }

    public ComplexDAGBuilder(ArrayList<StochasticTransitionFeature> features, ArrayList<BigDecimal> weights) {
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

        return simDag_0;
    }
}
