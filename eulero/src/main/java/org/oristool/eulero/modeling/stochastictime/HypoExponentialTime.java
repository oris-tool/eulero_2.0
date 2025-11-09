package org.oristool.eulero.modeling.stochastictime;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

import org.oristool.eulero.math.Continuous;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

public class HypoExponentialTime extends StochasticTime {

    private BigDecimal rate1;
    private BigDecimal rate2;

    public HypoExponentialTime(BigDecimal rate1, BigDecimal rate2) {
        super(BigDecimal.ZERO, BigDecimal.valueOf(Double.MAX_VALUE));
        this.rate1 = rate1;
        this.rate2 = rate2;
    }

    @Override
    public StochasticTransitionFeature getStochasticTransitionFeature() {
        return StochasticTransitionFeature.newHypoExp(rate1, rate2);
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
        throw new UnsupportedOperationException("Unimplemented method 'time2QueuingEulero'");
    }

    @Override
    public StochasticTime computeJobSizeLinear(double resources) {
        throw new UnsupportedOperationException("Unimplemented method 'computeJobSizeLinear'");
    }

    @Override
    public StochasticTime computeJobSizeInhomogeneousLinear(double resources, double p) {
        throw new UnsupportedOperationException(
                "Unimplemented method 'computeJobSizeInhomogeneousLinear'");
    }

    @Override
    public StochasticTime computeJobSizePiecewiseLinear(double resources, double Rmax) {
        throw new UnsupportedOperationException(
                "Unimplemented method 'computeJobSizePiecewiseLinear'");
    }

    @Override
    public double getExpectedValue() {
        return BigDecimal.ONE.divide(rate1, MathContext.DECIMAL64).doubleValue()
                + BigDecimal.ONE.divide(rate2, MathContext.DECIMAL64).doubleValue();
    }

    @Override
    public double getVariance() {
        return 1/Math.pow(rate1.doubleValue(), 2) + 1/Math.pow(rate2.doubleValue(), 2);
    }

    @Override
    public double PDF(double t) {
        return (rate1.doubleValue() * rate2.doubleValue())
                / (rate2.doubleValue() - rate1.doubleValue())
                * (Math.exp(-rate1.doubleValue() * t));
    }

    @Override
    public double CDF(double t) {
        double diff = rate2.subtract(rate1).doubleValue();
        return 1.0 - (rate2.doubleValue() / diff) * Math.exp(-rate1.doubleValue() * t)
                + (rate1.doubleValue() / diff) * Math.exp(-rate2.doubleValue() * t);
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toString'");
    }

    @Override
    public StochasticTime clone() {
        return new HypoExponentialTime(rate1, rate2);
    }

    @Override
    public void randomizeParameters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'randomizeParameters'");
    }

    public BigDecimal getRate1() {
        return rate1;
    }

    public BigDecimal getRate2() {
        return rate2;
    }


}
