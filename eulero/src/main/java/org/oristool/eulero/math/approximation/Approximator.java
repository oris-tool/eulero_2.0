package org.oristool.eulero.math.approximation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public abstract class Approximator {
    public Approximator(){ };

    public Map<String, Map<String, BigDecimal>> getApproximationSupports(double[] cdf, double low, double upp) {
        BigDecimal tukeysUpperBound = ApproximationHelpers.getTukeysBounds(cdf, low, upp).get("upp");
        int tukeysUpperBoundIndex = ApproximationHelpers.getTukeysBoundsIndices(cdf, low, upp).get("upp").intValue();
        double timeTick = (upp - low) / (cdf.length - 1);

        double[] pdf = new double[tukeysUpperBoundIndex];
        double[] x = new double[tukeysUpperBoundIndex];
        for(int i = 0; i < tukeysUpperBoundIndex; i++){
            pdf[i] = (cdf[i+1] - cdf[i]) / timeTick;
            x[i] = low + i * timeTick;
        }

        double pdfMax = Arrays.stream(pdf, 0, tukeysUpperBoundIndex).max().getAsDouble();
        int xMaxIndex = IntStream.range(0, tukeysUpperBoundIndex)
                .filter(i ->  pdf[i] == pdfMax)
                .findFirst() // first occurrence
                .orElse(-1);
        double xMax = low + timeTick * (xMaxIndex + 1);
        double cdfMax = cdf[xMaxIndex + 1];

        double delta = Arrays.stream(x)
                .filter(value -> value >= (pdfMax * xMax - cdfMax) / pdfMax)
                .findFirst()
                .orElse(-1);

        Map<String, BigDecimal> bodySupport = new HashMap<>();
        bodySupport.put("start", BigDecimal.valueOf(delta));
        bodySupport.put("end", tukeysUpperBound);

        Map<String, BigDecimal> tailSupport = new HashMap<>();
        tailSupport.put("start", tukeysUpperBound);
        tailSupport.put("end", BigDecimal.valueOf(Double.MAX_VALUE));

        return Map.ofEntries(Map.entry("body", bodySupport), Map.entry("tail", tailSupport));
    }

    public Map<String, Map<String, BigInteger>> getApproximationSupportIndices(double[] cdf, double low, double upp) {
        int tukeysUpperBoundIndex = ApproximationHelpers.getTukeysBoundsIndices(cdf, low, upp).get("upp").intValue();
        double timeTick = (upp - low) / (cdf.length - 1);

        double[] pdf = new double[tukeysUpperBoundIndex];
        double[] x = new double[tukeysUpperBoundIndex];
        for(int i = 0; i < tukeysUpperBoundIndex; i++){
            pdf[i] = (cdf[i+1] - cdf[i]) / timeTick;
            x[i] = low + i * timeTick;
        }

        double pdfMax = Arrays.stream(pdf, 0, tukeysUpperBoundIndex).max().getAsDouble();
        int xMaxIndex = IntStream.range(0, tukeysUpperBoundIndex)
                .filter(i ->  pdf[i] == pdfMax)
                .findFirst() // first occurrence
                .orElse(-1);
        double xMax = low + timeTick * xMaxIndex;
        double cdfMax = cdf[xMaxIndex];

        double delta = (pdfMax * xMax - cdfMax) / pdfMax;

        int deltaIndex = IntStream.range(0, tukeysUpperBoundIndex)
                .filter(i ->  x[i] >= delta)
                .findFirst() // first occurrence
                .orElse(-1);

        Map<String, BigInteger> bodySupport = new HashMap<>();
        bodySupport.put("start", BigInteger.valueOf(deltaIndex));
        bodySupport.put("end", BigInteger.valueOf(tukeysUpperBoundIndex));

        Map<String, BigInteger> tailSupport = new HashMap<>();
        tailSupport.put("start", BigInteger.valueOf(tukeysUpperBoundIndex));
        tailSupport.put("end", BigInteger.valueOf(cdf.length - 1));

        return Map.ofEntries(Map.entry("body", bodySupport), Map.entry("tail", tailSupport));
    }

    public Map<String, BigDecimal> getApproximationSupportsWeight(double[] cdf, double low, double upp){
        int tukeysUpperBoundIndex = ApproximationHelpers.getTukeysBoundsIndices(cdf, low, upp).get("upp").intValue();

        return Map.ofEntries(
                Map.entry("body", BigDecimal.valueOf(cdf[tukeysUpperBoundIndex - 1])),
                Map.entry("tail", BigDecimal.valueOf(1 - cdf[tukeysUpperBoundIndex - 1]))
        );
    }

    public Map<String, ApproximationSupportSetup> getApproximationSupportSetups(double[] cdf, double low, double upp){
        Map<String, Map<String, BigDecimal>> approximationSupports = getApproximationSupports(cdf, low, upp);
        Map<String, BigDecimal> weights = getApproximationSupportsWeight(cdf, low, upp);
        Map<String, Map<String, BigDecimal>> params = getApproximationParameters(cdf, low, upp);

        return Map.ofEntries(
                Map.entry("body", new ApproximationSupportSetup(weights.get("body"), approximationSupports.get("body"), params.get("body"))),
                Map.entry("tail", new ApproximationSupportSetup(weights.get("tail"), approximationSupports.get("tail"), params.get("tail")))
        );
    }

    public abstract Map<String, Map<String, BigDecimal>> getApproximationParameters(double[] cdf, double low, double upp);

    public static class ApproximationSupportSetup {
        private final BigDecimal weight;
        private final Map<String, BigDecimal> support;
        private final Map<String, BigDecimal> parameters;

        public ApproximationSupportSetup(BigDecimal weight, Map<String, BigDecimal> support, Map<String, BigDecimal> parameters){
            this.weight = weight;
            this.support = support;
            this.parameters = parameters;
        }

        public BigDecimal getWeight() {
            return weight;
        }

        public Map<String, BigDecimal> getSupport() {
            return support;
        }

        public Map<String, BigDecimal> getParameters() {
            return parameters;
        }
    }
}
