package org.oristool.eulero.modeling.stochastictime;

import com.google.common.math.BigIntegerMath;
import org.oristool.eulero.math.Continuous;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
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
        throw new RuntimeException("Method not implemented!");
    }

    @Override
    public List<StochasticTransitionFeature> getStochasticTransitionFeatures() {
        List<StochasticTransitionFeature> returnList = new ArrayList<>();
        returnList.add(StochasticTransitionFeature.newErlangInstance(this.k, this.rate1));
        returnList.add(StochasticTransitionFeature.newExponentialInstance(this.rate2));
        return returnList;
    }

    private static List<StochasticTransitionFeature> getErlangTransition(int k, BigDecimal rate) {
        List<StochasticTransitionFeature> returnList = new ArrayList<>();

        for (int i = 0; i < k; i++)
            returnList.add(StochasticTransitionFeature.newExponentialInstance(rate));

        return returnList;
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
        return BigDecimal.valueOf(k).divide(rate1, MathContext.DECIMAL64)
                .add(BigDecimal.ONE.divide(rate2, MathContext.DECIMAL64)).doubleValue();
    }

    @Override
    public double getVariance() {
        return BigDecimal.valueOf(k).divide(rate1.pow(2), MathContext.DECIMAL64)
                .add(BigDecimal.ONE.divide(rate2.pow(2), MathContext.DECIMAL64)).doubleValue();
    }

    @Override
    public double PDF(double t) {
        if (t < 0)
            return 0;
        double r1 = rate1.doubleValue();
        double r2 = rate2.doubleValue();

        if (Math.abs(r1 - r2) < 1e-7) { // Widened threshold slightly for stability
            int totalK = k + 1;
            return Math.pow(r1, totalK) * Math.pow(t, totalK - 1) * Math.exp(-r1 * t)
                    / BigIntegerMath.factorial(totalK - 1).doubleValue();
        }

        double delta = r1 - r2;
        double C = (Math.pow(r1, k) * r2) / Math.pow(delta, k);

        // Compute the sum incrementally without factorials or explicit powers
        double poissonSum = 0;
        double currentTerm = 1; // Represents (\delta * t)^j / j! for j=0
        for (int j = 0; j < k; j++) {
            poissonSum += currentTerm;
            currentTerm *= (delta * t) / (j + 1);
        }

        double term1 = C * Math.exp(-r2 * t);
        double term2 = C * Math.exp(-r1 * t) * poissonSum;

        return term1 - term2;
    }

    @Override
    public double CDF(double t) {
        if (t < 0)
            return 0.0;

        // Define standard precision (e.g., 34 digits, equivalent to IEEE 754
        // Decimal128)
        MathContext mc = new MathContext(34, RoundingMode.HALF_UP);

        // Safely parse rate1 and rate2 to BigDecimal regardless of their original
        // object type
        BigDecimal r1 = rate1 instanceof BigDecimal ? (BigDecimal) rate1 : BigDecimal.valueOf(rate1.doubleValue());
        BigDecimal r2 = rate2 instanceof BigDecimal ? (BigDecimal) rate2 : BigDecimal.valueOf(rate2.doubleValue());
        BigDecimal T = BigDecimal.valueOf(t);

        // Check condition: Math.abs(r1 - r2) < 1e-7
        BigDecimal deltaThreshold = new BigDecimal("1e-7");
        BigDecimal diff = r1.subtract(r2, mc).abs();

        if (diff.compareTo(deltaThreshold) < 0) {
            int totalK = k + 1;
            BigDecimal v = BigDecimal.ZERO;
            BigDecimal currentTerm = BigDecimal.ONE;

            BigDecimal r1T = r1.multiply(T, mc);
            BigDecimal expNegR1T = exp(r1T.negate(), mc);

            for (int n = 0; n < totalK; n++) {
                v = v.add(expNegR1T.multiply(currentTerm, mc), mc);
                currentTerm = currentTerm.multiply(r1T, mc).divide(BigDecimal.valueOf(n + 1), mc);
            }
            // Only cast to double at the very end
            return BigDecimal.ONE.subtract(v, mc).doubleValue();
        }

        // General case
        BigDecimal delta = r1.subtract(r2, mc);
        BigDecimal r1OverDelta = r1.divide(delta, mc);
        BigDecimal coeff1 = r1OverDelta.pow(k, mc);

        BigDecimal r2T = r2.multiply(T, mc);
        BigDecimal expNegR2T = exp(r2T.negate(), mc);
        BigDecimal val1 = coeff1.multiply(BigDecimal.ONE.subtract(expNegR2T, mc), mc);

        BigDecimal val2 = BigDecimal.ZERO;
        BigDecimal deltaOverR1 = delta.divide(r1, mc);
        BigDecimal r2OverR1 = r2.divide(r1, mc);

        BigDecimal currentA = r2OverR1.multiply(r1OverDelta.pow(k, mc), mc);
        BigDecimal poissonSum = BigDecimal.ZERO;
        BigDecimal currentPoissonTerm = BigDecimal.ONE;

        BigDecimal r1T = r1.multiply(T, mc);
        BigDecimal expNegR1T = exp(r1T.negate(), mc);

        for (int j = 0; j < k; j++) {
            poissonSum = poissonSum.add(currentPoissonTerm, mc);
            BigDecimal integralPart = BigDecimal.ONE.subtract(expNegR1T.multiply(poissonSum, mc), mc);
            val2 = val2.add(currentA.multiply(integralPart, mc), mc);

            // Shift terms step-by-step for the next iteration
            currentA = currentA.multiply(deltaOverR1, mc);
            currentPoissonTerm = currentPoissonTerm.multiply(r1T, mc).divide(BigDecimal.valueOf(j + 1), mc);
        }

        // Only cast to double at the very end
        return val1.subtract(val2, mc).doubleValue();
    }

    /**
     * High-precision exponential function (e^x) using Taylor Series.
     * Handles negative exponents cleanly by using 1 / e^(-x).
     */
    private BigDecimal exp(BigDecimal x, MathContext mc) {
        if (x.signum() == 0)
            return BigDecimal.ONE;

        // For negative exponents, compute e^(-x) and invert it to guarantee convergence
        // stability
        if (x.signum() < 0) {
            return BigDecimal.ONE.divide(exp(x.negate(), mc), mc);
        }

        BigDecimal sum = BigDecimal.ONE;
        BigDecimal term = BigDecimal.ONE;

        // Add internal precision buffer to prevent intermediate rounding errors
        MathContext extendedMc = new MathContext(mc.getPrecision() + 6, mc.getRoundingMode());

        for (int i = 1; i < 1000; i++) {
            term = term.multiply(x, extendedMc).divide(BigDecimal.valueOf(i), extendedMc);
            BigDecimal previousSum = sum;
            sum = sum.add(term, extendedMc);

            // Break out early if the terms become negligibly small
            if (sum.compareTo(previousSum) == 0
                    || term.abs().compareTo(new BigDecimal("1e-" + mc.getPrecision())) < 0) {
                break;
            }
        }
        return sum.round(mc);
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
}
