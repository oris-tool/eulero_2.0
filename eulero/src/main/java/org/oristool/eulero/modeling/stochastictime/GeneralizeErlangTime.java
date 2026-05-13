package org.oristool.eulero.modeling.stochastictime;

import com.google.common.math.BigIntegerMath;

import org.oristool.eulero.evaluation.approximator.TruncatedExponentialMixtureApproximation;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.evaluation.heuristics.SDFHeuristicsVisitor;
import org.oristool.eulero.math.Continuous;
import org.oristool.eulero.modeling.Simple;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.List;

public class GeneralizeErlangTime extends StochasticTime {

    private int k;
    private BigDecimal rate1;
    private BigDecimal rate2;

    public GeneralizeErlangTime(int k, BigDecimal rate1, BigDecimal rate2) {
        super(BigDecimal.ZERO, BigDecimal.valueOf(Double.MAX_VALUE));
        this.k = k;
        this.rate1 = rate1;
        this.rate2 = rate2;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public BigDecimal getRate1() {
        return rate1;
    }

    public void setRate1(BigDecimal rate1) {
        this.rate1 = rate1;
    }

    public BigDecimal getRate2() {
        return rate2;
    }

    public void setRate2(BigDecimal rate2) {
        this.rate2 = rate2;
    }

    @Override
    public StochasticTransitionFeature getStochasticTransitionFeature() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public List<StochasticTransitionFeature> getStochasticTransitionFeatures() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public List<BigDecimal> getWeights() {
        return List.of(BigDecimal.ONE);
    }

    @Override
    public Continuous time2QueuingEulero() {
        throw new UnsupportedOperationException("Method not implemented yet");
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
    public double getExpectedValue() {
        return BigDecimal.valueOf(k).divide(rate1, MathContext.DECIMAL64).add(BigDecimal.ONE.divide(rate2, MathContext.DECIMAL64)).doubleValue();
    }

    @Override
    public double getVariance() {
        return BigDecimal.valueOf(k).divide(rate1.pow(2), MathContext.DECIMAL64).add(BigDecimal.ONE.divide(rate2.pow(2), MathContext.DECIMAL64)).doubleValue();
    }

    @Override
    public double PDF(double t) {
        if (t < 0) return 0;
        double r1 = rate1.doubleValue();
        double r2 = rate2.doubleValue();

        if (Math.abs(r1 - r2) < 1e-9) {
            // Treat as Erlang(k+1, r1)
            int totalK = k + 1;
            return Math.pow(r1, totalK) * Math.pow(t, totalK - 1) * Math.exp(-r1 * t) / BigIntegerMath.factorial(totalK - 1).doubleValue();
        }

        double delta = r1 - r2;
        double term1 = (Math.pow(r1, k) * r2 / Math.pow(delta, k)) * Math.exp(-r2 * t);

        double term2 = 0;
        for (int j = 0; j < k; j++) {
            term2 += (Math.pow(t, j) / (BigIntegerMath.factorial(j).doubleValue() * Math.pow(delta, k - j)));
        }
        term2 *= (Math.pow(r1, k) * r2 * Math.exp(-r1 * t));

        return term1 - term2;
    }

    @Override
    public double CDF(double t) {
        if (t < 0) return 0;
        double r1 = rate1.doubleValue();
        double r2 = rate2.doubleValue();

        if (Math.abs(r1 - r2) < 1e-9) {
            // Treat as Erlang(k+1, r1)
            int totalK = k + 1;
            double v = 0;
            for(int n = 0; n < totalK; n++){
                v += Math.exp(-r1 * t) * Math.pow(r1 * t, n) / BigIntegerMath.factorial(n).doubleValue();
            }
            return 1 - v;
        }

        double delta = r1 - r2;

        // Term 1 Integral
        // A * (1 - e^(-r2 * t)) / r2
        // A = r1^k * r2 / delta^k
        // Integral = (r1^k / delta^k) * (1 - e^(-r2 * t))
        double coeff1 = Math.pow(r1, k) / Math.pow(delta, k);
        double val1 = coeff1 * (1 - Math.exp(-r2 * t));

        // Term 2 Integral (Sum)
        double val2 = 0;
        // Constants for term 2: B * Sum( t^j / (j! * delta^(k-j)) * e^(-r1*t) )
        // B = r1^k * r2
        // We integrate B * t^j * e^(-r1*t) / (j! * delta^(k-j))
        // Int(t^j * e^(-r1*t)) = j! / r1^(j+1) * (1 - e^(-r1*t) * Sum_{m=0}^j (r1*t)^m / m!)

        double B = Math.pow(r1, k) * r2;

        for (int j = 0; j < k; j++) {
            double termCoeff = B / (BigIntegerMath.factorial(j).doubleValue() * Math.pow(delta, k - j));
            double integralPart = (BigIntegerMath.factorial(j).doubleValue() / Math.pow(r1, j + 1));
            double poissonSum = 0;
            for (int m = 0; m <= j; m++) {
                poissonSum += Math.pow(r1 * t, m) / BigIntegerMath.factorial(m).doubleValue();
            }
            integralPart *= (1 - Math.exp(-r1 * t) * poissonSum);
            val2 += termCoeff * integralPart;
        }

        return val1 - val2;
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime clone() {
        return new GeneralizeErlangTime(k, rate1, rate2);
    }

    @Override
    public void randomizeParameters() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTimeType getType() {
        return StochasticTimeType.GENERALIZED_ERLANG;
    }

    public static void main(String[] args) {
        Simple task = new Simple("null", new GeneralizeErlangTime(2, new BigDecimal(1) , new BigDecimal(1)));
        AnalysisHeuristicsVisitor analyzer = new SDFHeuristicsVisitor(
            BigInteger.valueOf(2),
            BigInteger.valueOf(5),
            new TruncatedExponentialMixtureApproximation());
        double[] cdf = task.analyze(new BigDecimal(20), new BigDecimal(0.1), analyzer);
        try (PrintWriter writer = new PrintWriter(new FileWriter("generalized_erlang_mia.csv"))) {
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
