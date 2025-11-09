package org.oristool.eulero.modeling.stochastictime;

import com.google.common.math.BigIntegerMath;
import org.oristool.eulero.math.Continuous;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

public class ErlangTime extends StochasticTime{
    private int k;
    private double rate;

    public ErlangTime(int k, double rate){
        super(BigDecimal.ZERO, BigDecimal.valueOf(Double.MAX_VALUE));
        this.k = k;
        this.rate = rate;
    }

    public ErlangTime() {
    }

    @Override
    public StochasticTransitionFeature getStochasticTransitionFeature() {
        return StochasticTransitionFeature.newErlangInstance(k,new BigDecimal(rate));
    }

    @Override
    public List<StochasticTransitionFeature> getStochasticTransitionFeatures() {
        return List.of(StochasticTransitionFeature.newErlangInstance(k, new BigDecimal(rate)));
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
    public double getVariance() {
        return k/Math.pow(rate, 2);
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

    //TODO Qui ci sono problemi numerici perchè se sbaglio a scelgiere lo step sono finito; in realtà la cosa vale su PDF
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
    public Continuous time2QueuingEulero() {
        return Continuous.erlang(this.getK(), this.getRate());
    }

    @Override
    public StochasticTime computeJobSizeLinear(double resources) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime computeJobSizeInhomogeneousLinear(double resources, double p) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime computeJobSizePiecewiseLinear(double resources, double Rmax) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime clone() {
        return new ErlangTime(this.k, this.rate);
    }

    @Override
    public void randomizeParameters() {
        this.setRate(0.01 + (new Random().nextDouble() * 10));
        this.setK(1 + (new Random().nextInt(4)));
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getRate() {
        return rate;
    }

    public void setK(int k) {
        this.k = k;
    }

    public int getK() {
        return k;
    }
}
