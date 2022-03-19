package org.oristool.eulero.evaluation.approximator;

import org.apache.commons.lang3.tuple.Pair;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;

import java.util.ArrayList;

public abstract class Approximator {

    public Approximator(){};

    public abstract Pair<BigDecimal,StochasticTransitionFeature> getApproximatedStochasticTransitionFeature(double[] cdf, double low, double upp, BigDecimal step);

    public abstract ArrayList<Pair<BigDecimal,StochasticTransitionFeature>> getApproximatedStochasticTransitionFeatures(double[] cdf, double low, double upp, BigDecimal step);
}
