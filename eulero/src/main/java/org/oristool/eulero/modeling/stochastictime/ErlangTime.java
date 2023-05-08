package org.oristool.eulero.modeling.stochastictime;

import com.google.common.math.BigIntegerMath;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

public class ErlangTime extends StochasticTime{
    private final int k;
    private double rate;

    public ErlangTime(int k, double rate){
        super(BigDecimal.ZERO, BigDecimal.valueOf(Double.MAX_VALUE), SIRIOType.EXPO);
        this.k = k;
        this.rate = rate;
    }
    @Override
    public StochasticTransitionFeature getStochasticTransitionFeature() {
        return StochasticTransitionFeature.newErlangInstance(k, String.valueOf(rate));
    }

    @Override
    public List<StochasticTransitionFeature> getStochasticTransitionFeatures() {
        return List.of(StochasticTransitionFeature.newErlangInstance(k, String.valueOf(rate)));
    }

    @Override
    public List<BigDecimal> getWeights() {
        return List.of(BigDecimal.ONE);
    }

    @Override
    public double getExpectedValue() {
        return k/rate;
    }

    @Override
    public double PDF(double t) {
        if(t < 0.){
            return 0.0;
        }

        return Math.pow(rate, k) * Math.pow(t, k - 1) * Math.exp(-rate * t) / BigIntegerMath.factorial(k - 1).doubleValue();
    }

    @Override
    public double CDF(double t) {
        if(t < 0.){
            return 0.0;
        }

        double v = 0;
        for(int n = 0; n < k - 1; n++){
            v += Math.exp(-rate * t) * Math.pow(rate * t, n) / BigIntegerMath.factorial(n).doubleValue();
        }

        return 1 - v;//IntStream.range(0, k - 1).mapToDouble(x -> Math.exp(-rate * t) * Math.pow(rate * t, x) / BigIntegerMath.factorial(x).doubleValue()).sum() ;
    }

    @Override
    public double[] getNumericalCDF(double step, double limit) {
        double[] pdf = getNumericalPDF(step, limit);
        double[] cdf = new double[pdf.length];
        double accumulatore = 0.;
        for(int i = 0; i < cdf.length; i++){
            accumulatore += pdf[i] * step;
            cdf[i] = accumulatore;
        }

        return cdf;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public StochasticTime clone() {
        return new ErlangTime(this.k, this.rate);
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getRate() {
        return rate;
    }

    public int getK() {
        return k;
    }
}
