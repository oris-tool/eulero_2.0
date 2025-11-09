package org.oristool.eulero.modeling.stochastictime;

import org.oristool.eulero.math.Continuous;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.List;

public class DeterministicTime extends StochasticTime{
    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    private BigDecimal value;

    public DeterministicTime(){
        super(BigDecimal.ZERO, BigDecimal.ZERO);
        this.value = BigDecimal.ZERO;
    }

    public DeterministicTime(BigDecimal value){
        super(value, value);
        this.value = value;
    }

    @Override
    public StochasticTransitionFeature getStochasticTransitionFeature() {
        return StochasticTransitionFeature.newDeterministicInstance(value);
    }

    @Override
    public List<StochasticTransitionFeature> getStochasticTransitionFeatures() {
        return List.of(StochasticTransitionFeature.newDeterministicInstance(value));
    }

    @Override
    public List<BigDecimal> getWeights() {
        return List.of(BigDecimal.ONE);
    }

    @Override
    public Continuous time2QueuingEulero() {
        return Continuous.det(this.value.doubleValue());
    }

    @Override
    public StochasticTime computeJobSizeLinear(double resources) {
        return new DeterministicTime(this.value.multiply(BigDecimal.valueOf(resources)));
    }

    @Override
    public StochasticTime computeJobSizeInhomogeneousLinear(double resources, double p) {
        double gamma = 1/( p/resources + 1 - p);
        return new DeterministicTime(this.value.multiply(BigDecimal.valueOf(gamma)));
    }

    @Override
    public StochasticTime computeJobSizePiecewiseLinear(double resources, double Rmax) {
        return new DeterministicTime(this.value.multiply(BigDecimal.valueOf(Math.min(Rmax, resources))));
    }

    @Override
    public double getExpectedValue() {
        return this.value.doubleValue();
    }

    @Override
    public double getVariance() {
        return 0;
    }

    @Override
    public double PDF(double t) {
        if(t == value.doubleValue()){
            return Double.MAX_VALUE;
        }
        return 0;
    }

    @Override
    public double CDF(double t) {
        if(t > value.doubleValue()){
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime clone() {
        return new DeterministicTime(this.value);
    }

    @Override
    public void randomizeParameters() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

}
