package org.oristool.eulero.modeling.stochastictime;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "Uniform")
public class UniformTime extends StochasticTime {
    public UniformTime(){}

    public UniformTime(BigDecimal EFT, BigDecimal LFT) {
        super(EFT, LFT, SIRIOType.UNI);
    }

    public UniformTime(double EFT, double LFT) {
        super(BigDecimal.valueOf(EFT), BigDecimal.valueOf(LFT), SIRIOType.UNI);
    }

    public StochasticTransitionFeature getStochasticTransitionFeature(){
        return StochasticTransitionFeature.newUniformInstance(getEFT(), getLFT());
    }

    @Override
    public List<StochasticTransitionFeature> getStochasticTransitionFeatures() {
        return new ArrayList<>(List.of(getStochasticTransitionFeature()));
    }

    @Override
    public List<BigDecimal> getWeights() {
        return List.of(BigDecimal.ONE);
    }

    @Override
    public double getExpectedValue() {
        return getEFT().add(getLFT()).doubleValue()/2.;
    }

    @Override
    public double PDF(double t) {
        if(t < getEFT().doubleValue() || t > getLFT().doubleValue()){
            return 0.0;
        }
        return 1 / (getLFT().doubleValue() - getEFT().doubleValue());
    }

    @Override
    public double CDF(double t) {
        if(t < getEFT().doubleValue()){
            return 0.0;
        }

        if(t > getLFT().doubleValue()){
            return 1.0;
        }

        return (t - getEFT().doubleValue()) / (getLFT().doubleValue() - getEFT().doubleValue());
    }

    @Override
    public String toString() {
        return "[" + getEFT() + ", " + getLFT() + " ]";
    }

    @Override
    public StochasticTime clone() {
        return new UniformTime(this.getEFT(), this.getLFT());
    }
}
