package org.oristool.eulero.models;

import org.oristool.eulero.graph.Activity;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.util.Arrays;
import java.util.stream.IntStream;

public abstract class ModelBuilder {
    private StochasticTransitionFeature feature;

    public ModelBuilder(StochasticTransitionFeature feature){
        this.feature = feature;
    }

    public ModelBuilder() {

    }

    public StochasticTransitionFeature getFeature() {
        return feature;
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

    public abstract Activity buildModel();

}
