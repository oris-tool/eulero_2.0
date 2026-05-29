package org.oristool.eulero.modeling.stochastictime;

import java.math.BigDecimal;
import java.util.List;

import org.oristool.eulero.math.Continuous;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

public class ShiftedExponentialTime extends StochasticTime {

    private final DeterministicTime deterministicVariable;
    private final ExponentialTime exponentialVariable;

    public ShiftedExponentialTime(BigDecimal deterministicValue, BigDecimal rate) {
        super(deterministicValue, BigDecimal.valueOf(Double.MAX_VALUE));
        this.deterministicVariable = new DeterministicTime(deterministicValue);
        this.exponentialVariable = new ExponentialTime(rate);
    }

    @Override
    public StochasticTransitionFeature getStochasticTransitionFeature() {
        throw new UnsupportedOperationException("Unimplemented method 'getStochasticTransitionFeature'");
    }

    @Override
    public List<StochasticTransitionFeature> getStochasticTransitionFeatures() {
        return List.of(
                StochasticTransitionFeature.newDeterministicInstance(this.deterministicVariable.getValue()),
                StochasticTransitionFeature.newExponentialInstance(this.exponentialVariable.getRate())
            );
    }

    @Override
    public List<BigDecimal> getWeights() {
        return List.of(BigDecimal.ONE);
    }

    @Override
    public StochasticTimeType getType() {
        return StochasticTimeType.SHIFTED_EXPONENTIAL;
    }

    @Override
    public Continuous time2QueuingEulero() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'time2QueuingEulero'");
    }

    @Override
    public StochasticTime computeJobSizeLinear(double resources) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'computeJobSizeLinear'");
    }

    @Override
    public StochasticTime computeJobSizeInhomogeneousLinear(double resources, double p) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'computeJobSizeInhomogeneousLinear'");
    }

    @Override
    public StochasticTime computeJobSizePiecewiseLinear(double resources, double Rmax) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'computeJobSizePiecewiseLinear'");
    }

    @Override
    public double getExpectedValue() {
        return this.deterministicVariable.getExpectedValue() + this.exponentialVariable.getExpectedValue();
    }

    @Override
    public double getVariance() {
        return this.exponentialVariable.getVariance();
    }

    @Override
    public double PDF(double t) {
        if (t >= this.deterministicVariable.getValue().doubleValue())
            return this.exponentialVariable.PDF(t - this.deterministicVariable.getValue().doubleValue());
        return 0;
    }

    @Override
    public double CDF(double t) {
        if (t >= this.deterministicVariable.getValue().doubleValue())
            return this.exponentialVariable.CDF(t - this.deterministicVariable.getValue().doubleValue());
        return 0;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toString'");
    }

    @Override
    public StochasticTime clone() {
        return new ShiftedExponentialTime(this.deterministicVariable.getValue(), this.exponentialVariable.getRate());
    }

    @Override
    public void randomizeParameters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'randomizeParameters'");
    }

    public BigDecimal getRate() {
        return this.exponentialVariable.getRate();
    }

    public BigDecimal getDeterministicValue() {
        return this.deterministicVariable.getValue();
    }
}
