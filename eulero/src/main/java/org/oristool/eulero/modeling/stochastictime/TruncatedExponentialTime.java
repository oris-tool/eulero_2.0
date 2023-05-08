package org.oristool.eulero.modeling.stochastictime;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.expression.Expolynomial;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.List;

public class TruncatedExponentialTime extends StochasticTime{
    private BigDecimal rate;

    public TruncatedExponentialTime(double EFT, double LFT, double rate){
        super(BigDecimal.valueOf(EFT), BigDecimal.valueOf(LFT), SIRIOType.EXPO);
        this.rate = BigDecimal.valueOf(rate);
    }

    @Override
    public StochasticTransitionFeature getStochasticTransitionFeature() {
        return getStochasticTransitionFeatures().get(0);
    }

    @Override
    public List<StochasticTransitionFeature> getStochasticTransitionFeatures() {
        double rate = getRate().doubleValue();
        double a = getEFT().doubleValue();
        double b = getLFT().doubleValue();
        double c = rate > 0 ? a : b;


        return List.of(StochasticTransitionFeature.newExpolynomial(
                Math.abs(rate) * Math.exp(rate * c) / (1 - Math.exp(- Math.abs(rate) * (b - a))) + " * Exp[" + (-rate) + " x]",
                new OmegaBigDecimal(getEFT()), new OmegaBigDecimal(getLFT())
        ));
    }

    @Override
    public List<BigDecimal> getWeights() {
        return List.of(BigDecimal.ONE);
    }

    @Override
    public double getExpectedValue() {
        return 0;
    }

    @Override
    public double PDF(double t) {
        double rate = getRate().doubleValue();
        double a = getEFT().doubleValue();
        double b = getLFT().doubleValue();
        double c = rate > 0 ? a : b;


        if(t >= getEFT().doubleValue() || t <= getLFT().doubleValue()){
            return  rate * Math.exp(Math.abs(rate) * c) / (1 - Math.exp(-Math.abs(rate) * (b-a))) * Math.exp(-rate * t);
        }
        return 0;
    }

    @Override
    public double CDF(double t) {
        double rate = getRate().doubleValue();
        double a = getEFT().doubleValue();
        double b = getLFT().doubleValue();
        double c = rate > 0 ? a : b;

        if(t < getEFT().doubleValue()){
            return 0;
        }

        if(t > getLFT().doubleValue()){
            return 1;
        }

        return (Math.abs(rate) * Math.exp(rate * c)) / (rate * (1 - Math.exp(-Math.abs(rate) * (b - a)))) * (Math.exp(-rate * a) - Math.exp(-rate * t));
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public StochasticTime clone() {
        return new TruncatedExponentialTime(this.getEFT().doubleValue(), this.getLFT().doubleValue(), this.rate.doubleValue());
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }


}
