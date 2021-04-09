package org.oristool.eulero.models;

import org.oristool.eulero.graph.Activity;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;

public abstract class ModelBuilder {
    private StochasticTransitionFeature feature;
    private Approximator approximator;

    public ModelBuilder(StochasticTransitionFeature feature, Approximator approximator){
        this.feature = feature;
        this.approximator = approximator;
    }

    public StochasticTransitionFeature getFeature() {
        return feature;
    }

    public Approximator getApproximator() {
        return approximator;
    }

    public abstract Activity buildModelForAnalysis_Heuristic1(BigDecimal timeBound, BigDecimal timeTick);
    public abstract Activity buildModelForAnalysis_Heuristic2(BigDecimal timeBound, BigDecimal timeTick);
    public abstract Activity buildModelForSimulation();
}
