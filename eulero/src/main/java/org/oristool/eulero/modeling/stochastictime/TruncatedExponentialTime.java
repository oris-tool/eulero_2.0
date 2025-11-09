package org.oristool.eulero.modeling.stochastictime;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.eulero.math.Continuous;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@XmlRootElement(name = "TruncatedExp")
public class TruncatedExponentialTime extends StochasticTime {
    private BigDecimal rate;

    public TruncatedExponentialTime() {
    }

    public TruncatedExponentialTime(double EFT, double LFT, double rate) {
        super(BigDecimal.valueOf(EFT), BigDecimal.valueOf(LFT));
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
                Math.abs(rate) * Math.exp(rate * c) / (1 - Math.exp(-Math.abs(rate) * (b - a)))
                        + " * Exp[" + (-rate) + " x]",
                new OmegaBigDecimal(getEFT()), new OmegaBigDecimal(getLFT())));
    }

    @Override
    public List<BigDecimal> getWeights() {
        return List.of(BigDecimal.ONE);
    }

    @Override
    public Continuous time2QueuingEulero() {
        return Continuous.truncatedExp(getRate().doubleValue(), getEFT().doubleValue(),
                getLFT().doubleValue());
    }

    @Override
    public StochasticTime computeJobSizeLinear(double resources) {
        return new TruncatedExponentialTime(this.getEFT().doubleValue() * resources,
                this.getLFT().doubleValue() * resources, this.rate.doubleValue() / resources);
    }

    @Override
    public StochasticTime computeJobSizeInhomogeneousLinear(double resources, double p) {
        double gamma = 1 / (p / resources + 1 - p);
        return new TruncatedExponentialTime(this.getEFT().doubleValue() * gamma,
                this.getLFT().doubleValue() * gamma, this.rate.doubleValue() / gamma);
    }

    @Override
    public StochasticTime computeJobSizePiecewiseLinear(double resources, double Rmax) {
        return computeJobSizeLinear(Math.min(resources, Rmax));
    }

    @Override
    public double getExpectedValue() {
        double rate = getRate().doubleValue();
        double a = getEFT().doubleValue();
        double b = getLFT().doubleValue();

        return a + (a - b) / (Math.exp((b - a) * rate) - 1) + 1 / rate;
    }

    @Override
    public double getVariance() {
        double rate = getRate().doubleValue(); 
        double a = getEFT().doubleValue(); 
        double b = getLFT().doubleValue(); 

        if (a == b) { // deterministic
            return 0.0;
        }

        if (rate == 0.0) { // uniform
            return Math.pow(b - a, 2) / 12.0;
        }

        double L = b - a; 
        double rateL = rate * L; 

        double varTerm1 = 1.0 / (rate * rate);
        double exp_kL = Math.exp(rateL);
        double denominator_kL = Math.expm1(rateL);
        double varTerm2_numerator = (L * L) * exp_kL;
        double varTerm2_denominator = denominator_kL * denominator_kL;
        double varTerm2 = varTerm2_numerator / varTerm2_denominator;
        return varTerm1 - varTerm2;
    }

    @Override
    public double PDF(double t) {
        double rate = getRate().doubleValue();
        double a = getEFT().doubleValue();
        double b = getLFT().doubleValue();
        double c = rate > 0 ? a : b;

        if (t >= getEFT().doubleValue() && t <= getLFT().doubleValue()) {
            return rate * Math.exp(Math.abs(rate) * c) / (1 - Math.exp(-Math.abs(rate) * (b - a)))
                    * Math.exp(-rate * t);
        }

        return 0;
    }

    @Override
    public double CDF(double t) {
        double rate = getRate().doubleValue();
        double a = getEFT().doubleValue();
        double b = getLFT().doubleValue();
        double c = rate > 0 ? a : b;

        if (t < getEFT().doubleValue()) {
            return 0;
        }

        if (t > getLFT().doubleValue()) {
            return 1;
        }

        return (Math.abs(rate) * Math.exp(rate * c))
                / (rate * (1 - Math.exp(-Math.abs(rate) * (b - a))))
                * (Math.exp(-rate * a) - Math.exp(-rate * t));
    }

    @Override
    public String toString() {
        return "[EFT: " + this.getEFT() + ", LFT: " + this.getLFT() + ", lambda: "
                + this.getRate().toString() + "]";
    }

    @Override
    public StochasticTime clone() {
        return new TruncatedExponentialTime(this.getEFT().doubleValue(),
                this.getLFT().doubleValue(), this.rate.doubleValue());
    }

    @Override
    public void randomizeParameters() {
        double a = new Random().nextDouble();
        double b = a + new Random().nextDouble() * 5;
        double rate = new Random().nextDouble() * 3;
        this.setEFT(BigDecimal.valueOf((a)));
        this.setLFT(BigDecimal.valueOf((b)));
        this.setRate(BigDecimal.valueOf(rate));
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }


}
