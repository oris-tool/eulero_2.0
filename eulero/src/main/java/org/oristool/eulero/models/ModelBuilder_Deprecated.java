package org.oristool.eulero.models;

import org.oristool.eulero.graph.Activity;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.IntStream;

public abstract class ModelBuilder_Deprecated {
    private StochasticTransitionFeature feature;
    private Approximator approximator;

    public ModelBuilder_Deprecated(StochasticTransitionFeature feature, Approximator approximator){
        this.feature = feature;
        this.approximator = approximator;
    }

    public StochasticTransitionFeature getFeature() {
        return feature;
    }

    public Approximator getApproximator() {
        return approximator;
    }

    public double[] cutCDF(double[] cdf){
        int startIndex = IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.001)
                .findFirst()
                .orElse(0);

        int endIndex = IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.999)
                .findFirst()
                .orElse(cdf.length - 1);

        return Arrays.copyOfRange(cdf, startIndex, endIndex);
    }

    public int getLowIndex(double[] cdf){

        return IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.001)
                .findFirst()
                .orElse(0);
    }

    public int getUppIndex(double[] cdf){

        return IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.999)
                .findFirst()
                .orElse(cdf.length-1);
    }


    public abstract Activity buildModelForAnalysis_Heuristic1(BigDecimal timeBound, BigDecimal timeTick);
    public abstract Activity buildModelForAnalysis_Heuristic2(BigDecimal timeBound, BigDecimal timeTick);
    public abstract Activity buildModelForAnalysis_Heuristic3(BigDecimal timeBound, BigDecimal timeTick);
    public abstract Activity buildModelForSimulation();



}
