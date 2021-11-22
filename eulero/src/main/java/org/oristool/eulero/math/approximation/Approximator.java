package org.oristool.eulero.math.approximation;

import org.oristool.eulero.math.distribution.continuous.ContinuousDistribution;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.util.ArrayList;
import java.util.Map;

public abstract class Approximator {
    private ArrayList<BigDecimal> stochasticTransitionFeatureWeights;

    public Approximator(){
        stochasticTransitionFeatureWeights = new ArrayList<>();
    };

    public abstract Map<String, Map<String, BigDecimal>> getApproximationSupports(double[] cdf, double low, double upp, BigDecimal step);

    public abstract Map<String, Map<String, BigInteger>> getApproximationSupportIndices(double[] cdf, double low, double upp);

    public abstract Map<String, BigDecimal> getApproximationSupportsWeight(double[] cdf, double low, double upp, BigDecimal step);

    public abstract Map<String, ApproximationSupportSetup> getApproximationSupportSetups(double[] cdf, double low, double upp, BigDecimal step);

    public abstract Map<String, Map<String, BigDecimal>> getApproximationParameters(double[] cdf, double low, double upp, BigDecimal step);

    public abstract StochasticTransitionFeature getApproximatedStochasticTransitionFeature(double[] cdf, double low, double upp, BigDecimal step);

    public abstract ArrayList<StochasticTransitionFeature> getApproximatedStochasticTransitionFeatures(double[] cdf, double low, double upp, BigDecimal step);

    public ArrayList<BigDecimal> stochasticTransitionFeatureWeights() {
        return stochasticTransitionFeatureWeights;
    }

    public static class ApproximationSupportSetup {
        private final BigDecimal weight;
        private final Map<String, BigDecimal> support;
        private final Map<String, BigDecimal> parameters;
        private final ContinuousDistribution distribution;

        public ApproximationSupportSetup(BigDecimal weight, Map<String, BigDecimal> support, Map<String, BigDecimal> parameters, ContinuousDistribution distribution){
            this.weight = weight;
            this.support = support;
            this.parameters = parameters;
            this.distribution = distribution;
        }

        public BigDecimal getWeight() {
            return weight;
        }

        public Map<String, BigDecimal> getSupport() {
            return support;
        }

        public Map<String, BigDecimal> getParameters() {
            return parameters;
        }

        public ContinuousDistribution getDistribution() {
            return distribution;
        }
    }
}
