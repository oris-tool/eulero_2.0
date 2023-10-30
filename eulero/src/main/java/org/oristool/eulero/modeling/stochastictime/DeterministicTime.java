package org.oristool.eulero.modeling.stochastictime;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.List;

@XmlRootElement(name = "Deterministic")
public class DeterministicTime extends StochasticTime{
    private BigDecimal value;

    public DeterministicTime(){
        super(BigDecimal.ZERO, BigDecimal.ZERO, SIRIOType.IMM);
        this.value = BigDecimal.ZERO;
    }

    public DeterministicTime(BigDecimal value){
        super(value, value, SIRIOType.DET);
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
    public double getExpectedValue() {
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
        return null;
    }

    @Override
    public StochasticTime clone() {
        return new DeterministicTime(this.value);
    }
}
