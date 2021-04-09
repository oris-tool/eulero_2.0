package org.oristool.eulero.math.approximation;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.IntStream;

public class ApproximationHelpers {
    public static class CumulativeTruncatedExp implements UnivariateDifferentiableFunction {
        private double delta;
        private double b;
        private double time;
        private double histogram;


        public CumulativeTruncatedExp(double delta, double b, double time, double histogram){
            this.delta = delta;
            this.b = b;
            this.time = time;
            this.histogram = histogram;
        }

        public double value(double x) {
            // Here x is the lambda of the function
            return (1 - Math.exp(-x * (time - delta))) / (1 - Math.exp(-x * (b - delta))) - histogram;
        }

        public DerivativeStructure value(DerivativeStructure t) throws DimensionMismatchException {
            // t should be our lambda
            DerivativeStructure p = t.multiply(delta - time).expm1();
            DerivativeStructure q = t.multiply(delta - b).expm1();

            return p.divide(q).subtract(histogram);
        }
    }

    public static Map<String, BigDecimal> getTukeysBounds(double[] cdf, double low, double upp){
        double timeTick = (upp - low) / (cdf.length - 1);
        double[] x = new double[cdf.length];
        for(int i = 0; i < x.length; i++){
            x[i] = low + i * timeTick;
        }

        double Q1 = low + timeTick * IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.25)
                .findFirst().orElse(0);

        double Q3 = low + timeTick * IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.75)
                .findFirst().orElse(cdf.length - 1);

        double IQR = Q3 - Q1;

        double lowerBound = Arrays.stream(x)
                .filter(val -> val <= Q1 - 1.5 * IQR)
                .max()
                .orElse(x[0]);

        double upperBound = Arrays.stream(x)
                .filter(val -> val >=  Q3 + 1.5 * IQR)
                .min()
                .orElse(x[x.length - 1]);

        return Map.ofEntries(
                    Map.entry("low", BigDecimal.valueOf(lowerBound)),
                    Map.entry("upp", BigDecimal.valueOf(upperBound))
            );
    }

    public static Map<String, BigInteger> getTukeysBoundsIndices(double[] cdf, double low, double upp){
        double timeTick = (upp - low) / (cdf.length - 1);
        double[] x = new double[cdf.length];
        for(int i = 0; i < x.length; i++){
            x[i] = low + i * timeTick;
        }

        double Q1 = low + timeTick * IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.25)
                .findFirst().orElse(0);

        double Q3 = low + timeTick * IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.75)
                .findFirst().orElse(cdf.length - 1);

        double IQR = Q3 - Q1;

        int lowerBoundIndex = IntStream.range(0, cdf.length)
                .filter(i -> x[i] <= Q1 - 1.5 * IQR )
                .max()
                .orElse(0);

        int upperBoundIndex = IntStream.range(0, cdf.length)
                .filter(i -> x[i] >= Q3 + 1.5 * IQR)
                .min()
                .orElse(cdf.length - 1);

        return Map.ofEntries(
                Map.entry("low", BigInteger.valueOf(lowerBoundIndex)),
                Map.entry("upp", BigInteger.valueOf(upperBoundIndex))
        );
    }

    public static Map<String, BigDecimal> getQuartileBounds(double[] cdf, double low, double upp){
        double timeTick = (upp - low) / (cdf.length - 1);
        double[] x = new double[cdf.length];
        for(int i = 0; i < x.length; i++){
            x[i] = low + i * timeTick;
        }

        double Q1 = low + timeTick * IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.25)
                .findFirst().orElse(0);

        double Q3 = low + timeTick * IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.75)
                .findFirst().orElse(cdf.length - 1);

        return Map.ofEntries(
                Map.entry("low", BigDecimal.valueOf(Q1)),
                Map.entry("upp", BigDecimal.valueOf(Q3))
        );
    }

    public static Map<String, BigInteger> getQuartileBoundsIndices(double[] cdf, double low, double upp){
        double timeTick = (upp - low) / (cdf.length - 1);
        double[] x = new double[cdf.length];
        for(int i = 0; i < x.length; i++){
            x[i] = low + i * timeTick;
        }

        double Q1 = low + timeTick * IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.25)
                .findFirst().orElse(0);

        double Q3 = low + timeTick * IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.75)
                .findFirst().orElse(cdf.length - 1);

        int Q1Index = IntStream.range(0, cdf.length)
                .filter(i -> x[i] <= Q1)
                .max()
                .orElse(0);

        int Q3Index = IntStream.range(0, cdf.length)
                .filter(i -> x[i] >= Q3)
                .min()
                .orElse(cdf.length - 1);

        return Map.ofEntries(
                Map.entry("low", BigInteger.valueOf(Q1Index)),
                Map.entry("upp", BigInteger.valueOf(Q3Index))
        );
    }
}
