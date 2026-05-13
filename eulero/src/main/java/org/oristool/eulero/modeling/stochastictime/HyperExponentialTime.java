package org.oristool.eulero.modeling.stochastictime;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.List;

import org.oristool.eulero.evaluation.approximator.TruncatedExponentialMixtureApproximation;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.evaluation.heuristics.SDFHeuristicsVisitor;
import org.oristool.eulero.math.Continuous;
import org.oristool.eulero.modeling.Simple;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

public class HyperExponentialTime extends StochasticTime {

    private BigDecimal rate1;
    private BigDecimal rate2;
    private BigDecimal prob1;
    private BigDecimal prob2;

    public HyperExponentialTime(BigDecimal rate1, BigDecimal rate2, BigDecimal prob1) {
        super(BigDecimal.ZERO, BigDecimal.valueOf(Double.MAX_VALUE));
        this.rate1 = rate1;
        this.rate2 = rate2;
        this.prob1 = prob1;
        this.prob2 = BigDecimal.ONE.subtract(prob1);
    }

    @Override
    public StochasticTransitionFeature getStochasticTransitionFeature() {
        return StochasticTransitionFeature.newHyperExp(
            List.of(prob1, prob2),
            List.of(rate1, rate2));
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
        return prob1.divide(rate1, MathContext.DECIMAL64).doubleValue()
            + prob2.divide(rate2, MathContext.DECIMAL64).doubleValue();
    }

    @Override
    public double getVariance() {
        BigDecimal secondMoment = BigDecimal.valueOf(2).multiply(prob1).divide(rate1.pow(2), MathContext.DECIMAL64)
            .add(BigDecimal.valueOf(2).multiply(prob2).divide(rate2.pow(2), MathContext.DECIMAL64));
        double quadraticMean = Math.pow(getExpectedValue(), 2);
        return secondMoment.doubleValue() - quadraticMean;
    }

    @Override
    public double PDF(double t) {
        return prob1.doubleValue() * rate1.doubleValue() * Math.exp(-rate1.doubleValue()*t)
            + prob2.doubleValue() * rate2.doubleValue() * Math.exp(-rate2.doubleValue()*t);
    }

    @Override
    public double CDF(double t) {
        BigDecimal term1 = prob1.multiply(BigDecimal.valueOf(Math.exp(-rate1.doubleValue() * t)));
        BigDecimal term2 = prob2.multiply(BigDecimal.valueOf(Math.exp(-rate2.doubleValue() * t)));
        BigDecimal value = term1.add(term2);
        return BigDecimal.ONE.subtract(value).doubleValue();
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toString'");
    }

    @Override
    public StochasticTime clone() {
        return new HyperExponentialTime(rate1, rate2, prob1);
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

    public BigDecimal getProb1() {
        return prob1;
    }

    public BigDecimal getProb2() {
        return prob2;
    }

    @Override
    public StochasticTimeType getType() {
        return StochasticTimeType.HYPER_EXPONENTIAL;
    }

    public static void main(String[] args) {
        Simple task = new Simple("null", new HyperExponentialTime(new BigDecimal(2), new BigDecimal(1), new BigDecimal(0.7)));
        AnalysisHeuristicsVisitor analyzer = new SDFHeuristicsVisitor(
            BigInteger.valueOf(2),
            BigInteger.valueOf(5),
            new TruncatedExponentialMixtureApproximation());
        double[] cdf = task.analyze(new BigDecimal(40), new BigDecimal(0.1), analyzer);
        try (PrintWriter writer = new PrintWriter(new FileWriter("hyper_mia.csv"))) {
            writer.println("time,cdf");
            for (int i = 0; i < cdf.length; i++) {
                double time = i * 0.1;
                writer.println(time + "," + cdf[i]);
            }
            System.out.println("CDF saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
