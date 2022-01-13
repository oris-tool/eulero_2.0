package org.oristool.eulero.models;

import org.checkerframework.checker.units.qual.A;
import org.oristool.eulero.graph.Activity;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public abstract class ModelBuilder {
    private ArrayList<StochasticTransitionFeature> features = new ArrayList<>();
    private ArrayList<BigDecimal> weights = new ArrayList<>();

    public ModelBuilder(StochasticTransitionFeature feature){
        this.features.add(feature);
        this.weights.add(BigDecimal.ONE);
    }

    public ModelBuilder(ArrayList<StochasticTransitionFeature> features, ArrayList<BigDecimal> weights){
        this.features = features;
        this.weights = weights;
    }

    public ModelBuilder() {

    }

    public ArrayList<StochasticTransitionFeature> getFeatures() {
        return features;
    }

    public ArrayList<BigDecimal> getWeights() {
        return weights;
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
