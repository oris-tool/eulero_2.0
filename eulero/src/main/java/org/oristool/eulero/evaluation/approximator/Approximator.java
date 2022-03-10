package org.oristool.eulero.evaluation.approximator;

import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;

import java.util.ArrayList;

public abstract class Approximator {
    private ArrayList<BigDecimal> stochasticTransitionFeatureWeights;

    public Approximator(){
        stochasticTransitionFeatureWeights = new ArrayList<>();
    };

    public abstract StochasticTransitionFeature getApproximatedStochasticTransitionFeature(double[] cdf, double low, double upp, BigDecimal step);

    public abstract ArrayList<StochasticTransitionFeature> getApproximatedStochasticTransitionFeatures(double[] cdf, double low, double upp, BigDecimal step);

    public ArrayList<BigDecimal> stochasticTransitionFeatureWeights() {
        return stochasticTransitionFeatureWeights;
    }


}
