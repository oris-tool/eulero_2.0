package org.oristool.eulero.modeling.stochastictime;

import org.oristool.eulero.math.Continuous;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UniformTime extends StochasticTime {
    public UniformTime(){}

    public UniformTime(BigDecimal EFT, BigDecimal LFT) {
        super(EFT, LFT);
    }

    public UniformTime(double EFT, double LFT) {
        super(BigDecimal.valueOf(EFT), BigDecimal.valueOf(LFT));
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
    public Continuous time2QueuingEulero() {
        return Continuous.unif(this.getEFT().doubleValue(), this.getLFT().doubleValue());
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

    @Override
    public void randomizeParameters() {
        double a = (new Random()).nextDouble();
        double b = a + new Random().nextDouble() * (5 - a);
        this.setEFT(BigDecimal.valueOf(a));
        this.setLFT(BigDecimal.valueOf(b));
    }
}
