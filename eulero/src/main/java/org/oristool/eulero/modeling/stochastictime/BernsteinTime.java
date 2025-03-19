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
        return null;
    }

    @Override
    public List<StochasticTransitionFeature> getStochasticTransitionFeatures() {
        return null;
    }

    @Override
    public List<BigDecimal> getWeights() {
        return null;
    }

    @Override
    public Continuous time2QueuingEulero() {
        return null;
    }

    @Override
    public StochasticTime computeJobSizeLinear(double resources) {
        return null;
    }

    @Override
    public StochasticTime computeJobSizeInhomogeneousLinear(double resources, double p) {
        return null;
    }

    @Override
    public StochasticTime computeJobSizePiecewiseLinear(double resources, double Rmax) {
        return null;
    }

    @Override
    public double getExpectedValue() {
        return 0;
    }

    @Override
    public double PDF(double t) {
        return 0;
    }

    @Override
    public double CDF(double t) {
        return 0;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public StochasticTime clone() {
        return null;
    }

    @Override
    public void randomizeParameters() {
    }
}
