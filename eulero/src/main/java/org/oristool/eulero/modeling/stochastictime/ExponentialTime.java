package org.oristool.eulero.modeling.stochastictime;

import org.oristool.eulero.math.Continuous;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.List;

public class ExponentialTime extends StochasticTime {
    private BigDecimal rate;

    public ExponentialTime(){}
    public ExponentialTime(BigDecimal rate) {
        super(BigDecimal.ZERO, BigDecimal.valueOf(Double.MAX_VALUE));
        this.rate = rate;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    @Override
    public StochasticTransitionFeature getStochasticTransitionFeature() {
        return StochasticTransitionFeature.newExponentialInstance(getRate().toString());
    }

    @Override
    public List<StochasticTransitionFeature> getStochasticTransitionFeatures() {
        return List.of(getStochasticTransitionFeature());
    }

    @Override
    public List<BigDecimal> getWeights() {
        return List.of(BigDecimal.ONE);
    }

    @Override
    public Continuous time2QueuingEulero() {
        return Continuous.exp(this.rate.doubleValue());
    }

    @Override
    public StochasticTime computeJobSizeLinear(double resources) {
        return null;
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
        return 1 / getRate().doubleValue();
    }

    @Override
    public double getVariance() {
        return 1/ Math.pow(rate.doubleValue(),2);
    }

    @Override
    public double PDF(double t) {
        return rate.doubleValue() * Math.exp(-rate.doubleValue() * t);
    }

    @Override
    public double CDF(double t) {
        return 1 - Math.exp(-rate.doubleValue() * t);
    }


    @Override
    public String toString() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime clone() {
        return new ExponentialTime(this.rate);
    }

    @Override
    public void randomizeParameters() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }


}
