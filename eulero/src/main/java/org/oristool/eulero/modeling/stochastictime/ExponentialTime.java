package org.oristool.eulero.modeling.stochastictime;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.List;

@XmlRootElement(name = "Exponential")
public class ExponentialTime extends StochasticTime {
    private BigDecimal rate;

    public ExponentialTime(){}
    public ExponentialTime(BigDecimal rate) {
        super(BigDecimal.ZERO, BigDecimal.valueOf(Double.MAX_VALUE), SIRIOType.EXP);
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
        return null;
    }

    @Override
    public List<BigDecimal> getWeights() {
        return null;
    }

    @Override
    public double getExpectedValue() {
        return 1 / getRate().doubleValue();
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
        return null;
    }


}
