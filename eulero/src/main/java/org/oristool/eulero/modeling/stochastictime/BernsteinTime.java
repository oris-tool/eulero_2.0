package org.oristool.eulero.modeling.stochastictime;

import org.oristool.eulero.math.Continuous;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BernsteinTime extends StochasticTime {
    private ArrayList<Double> c;
    private Integer N;

    public BernsteinTime(BigDecimal EFT, BigDecimal LFT, Integer N, ArrayList<Double> c){
        super(EFT, LFT);
        this.c = c;
        this.N = N;
    }

    public ArrayList<Double> getC() {
        return c;
    }


    @Override
    public StochasticTransitionFeature getStochasticTransitionFeature() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public List<StochasticTransitionFeature> getStochasticTransitionFeatures() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public List<BigDecimal> getWeights() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public Continuous time2QueuingEulero() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime computeJobSizeLinear(double resources) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime computeJobSizeInhomogeneousLinear(double resources, double p) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime computeJobSizePiecewiseLinear(double resources, double Rmax) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public double getExpectedValue() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public double getVariance() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public double PDF(double t) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public double CDF(double t) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime clone() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public void randomizeParameters() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

}
