package org.oristool.eulero.math.approximation;

import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;
import org.oristool.eulero.math.distribution.continuous.ShiftedExponentialDistribution;
import org.oristool.eulero.math.distribution.continuous.ShiftedTruncatedExponentialDistribution;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class EXPMixtureApproximation extends Approximator {

    public EXPMixtureApproximation(){
    }

    @Override
    public Map<String, Map<String, BigDecimal>> getApproximationSupports(double[] cdf, double low, double upp, BigDecimal step) {
        //BigDecimal tukeysUpperBound = ApproximationHelpers.getTukeysBounds(cdf, low, upp).get("upp");
        BigDecimal tukeysUpperBound = ApproximationHelpers.getQuartileBounds(cdf, low, upp).get("upp");
        int tukeysUpperBoundIndex = ApproximationHelpers.getTukeysBoundsIndices(cdf, low, upp).get("upp").intValue();

        double timeTick = step.doubleValue();
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
        bodySupport.put("start", BigDecimal.valueOf(delta).setScale(step.scale(), RoundingMode.HALF_DOWN));
        bodySupport.put("end", tukeysUpperBound.setScale(step.scale(), RoundingMode.HALF_DOWN));

        Map<String, BigDecimal> tailSupport = new HashMap<>();
        tailSupport.put("start", tukeysUpperBound.setScale(step.scale(), RoundingMode.HALF_DOWN));
        tailSupport.put("end", BigDecimal.valueOf(Double.MAX_VALUE));

        return Map.ofEntries(Map.entry("body", bodySupport), Map.entry("tail", tailSupport));
    }

    @Override
    public Map<String, Map<String, BigInteger>> getApproximationSupportIndices(double[] cdf, double low, double upp) {
        int tukeysUpperBoundIndex = ApproximationHelpers.getQuartileBoundsIndices(cdf, low, upp).get("upp").intValue();
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

    @Override
    public Map<String, BigDecimal> getApproximationSupportsWeight(double[] cdf, double low, double upp, BigDecimal step){
        int tukeysUpperBoundIndex = ApproximationHelpers.getQuartileBoundsIndices(cdf, low, upp).get("upp").intValue();

        return Map.ofEntries(
                Map.entry("body", BigDecimal.valueOf(cdf[tukeysUpperBoundIndex - 1])),
                Map.entry("tail", BigDecimal.valueOf(1 - cdf[tukeysUpperBoundIndex - 1]))
        );
    }

    @Override
    public Map<String, ApproximationSupportSetup> getApproximationSupportSetups(double[] cdf, double low, double upp, BigDecimal step){
        Map<String, Map<String, BigDecimal>> approximationSupports = getApproximationSupports(cdf, low, upp, step);
        Map<String, BigDecimal> weights = getApproximationSupportsWeight(cdf, low, upp, step);
        Map<String, Map<String, BigDecimal>> params = getApproximationParameters(cdf, low, upp, step);

        return Map.ofEntries(
                Map.entry("body", new ApproximationSupportSetup(weights.get("body"), approximationSupports.get("body"), params.get("body"),
                        new ShiftedTruncatedExponentialDistribution("body", approximationSupports.get("body").get("start"), approximationSupports.get("body").get("end"), params.get("body").get("lambda")))),
                Map.entry("tail", new ApproximationSupportSetup(weights.get("tail"), approximationSupports.get("tail"), params.get("tail"),
                        new ShiftedExponentialDistribution("tail", approximationSupports.get("tail").get("start"), params.get("tail").get("lambda"))))
        );
    }

    @Override
    public Map<String, Map<String, BigDecimal>> getApproximationParameters(double[] cdf, double low, double upp, BigDecimal step) {
        Map<String, Map<String, BigInteger>> supportIndices = getApproximationSupportIndices(cdf, low, upp);
        Map<String, Map<String, BigDecimal>> supportValues = getApproximationSupports(cdf, low, upp, step);
        Map<String, BigDecimal> supportWeight = getApproximationSupportsWeight(cdf, low, upp, step);
        NewtonRaphsonSolver zeroSolver = new NewtonRaphsonSolver();

        double timeTick = step.doubleValue();
        double[] x = new double[cdf.length];
        for(int i = 0; i < x.length; i++){
            x[i] = low + i * timeTick;
        }

        // Handling Body
        Map<String, BigDecimal> bodyParameters = new HashMap<>();

        double bodyLambda = Double.MAX_VALUE;

        for(int i = supportIndices.get("body").get("start").intValue() + 1; i < supportIndices.get("body").get("end").intValue(); i++){
            //Serve davvero i != 0???
            double cdfValue = ((i != 0 ? cdf[i - 1] : cdf[i])) / supportWeight.get("body").doubleValue();

            UnivariateDifferentiableFunction function =
                    new ApproximationHelpers.CumulativeTruncatedExp(supportValues.get("body").get("start").doubleValue(), supportValues.get("body").get("end").doubleValue(), x[i], cdfValue);

            bodyLambda = Math.min(
                    bodyLambda,
                    zeroSolver.solve(10000, function, 0.0001)
            );
        }

        bodyParameters.put("lambda", BigDecimal.valueOf(Math.max(bodyLambda, 0.5)));
        bodyParameters.put("delta", supportValues.get("body").get("start"));


        // Handling Tail
        Map<String, BigDecimal> tailParameters = new HashMap<>();
        double tailLambda = Double.MAX_VALUE;

        for(int i = supportIndices.get("tail").get("start").intValue() + 1; i < supportIndices.get("tail").get("end").intValue(); i++){
            double cdfValue = ((i != 0 ? cdf[i - 1] : cdf[i]) - (1 - supportWeight.get("tail").doubleValue())) / supportWeight.get("tail").doubleValue();
            cdfValue = BigDecimal.valueOf(cdfValue).setScale(3, RoundingMode.HALF_DOWN).doubleValue();


            tailLambda = Math.min(
                    tailLambda,
                    -Math.log(1 - cdfValue) / (x[i] - supportValues.get("tail").get("start").doubleValue())
                );
        }

        tailParameters.put("lambda", BigDecimal.valueOf(tailLambda));

        return Map.ofEntries(
                Map.entry("body", bodyParameters),
                Map.entry("tail", tailParameters)
        );
    }

    /*public Map<String, Map<String, BigDecimal>> getApproximationParameters(double[] cdf, double low, double upp){
        ArrayList<Map<String, BigDecimal>> lambdas = new ArrayList<>();
        NewtonRaphsonSolver zeroSolver = new NewtonRaphsonSolver();

        Map<String, Map<String, BigDecimal>> supports = getApproximationSupports(cdf, low, upp);

                ApproximationHelpers.getTukeysBoundsIndices(cdf, low, upp).get("low");

        // Body
        double bodyStart = supports.get("body").get("start").doubleValue();
        double bodyEnd = supports.get("body").get("end").doubleValue();

        // Tail
        double tailStart = supports.get("tail").get("start").doubleValue();
        double tailEnd = supports.get("tail").get("end").doubleValue();
        double tailLambda = Double.MAX_VALUE;

        for(int j = supportsIndices.get(i).get("start").intValue(); j <= end.; j++){

            double cdfValue = ((j != 0 ? histogram.getCDFHistogramValues().get(j - 1).doubleValue()
                    : histogram.getCDFHistogramValues().get(j).doubleValue() ) - subtractionFactor) / normalizationFactor;
            double xValue = histogram.getXValues().get(j).doubleValue();

            // handling last support with Exponential
            tailLambda = Math.min(tailLambda, -Math.log(1 - cdfValue) / (xValue - start));
        }

        for (int i = 0; i < approximationSupports.size(); i++) {
            double start = approximationSupports.get(i).get("start").doubleValue();
            double end = approximationSupports.get(i).get("end").doubleValue();


            double subtractionFactor = i != 0 ? histogram.getCDFHistogramValues().get(supportsIndices.get(i - 1).get("end").intValue() - 1).doubleValue() : 0;

            double normalizationFactor = i != 0 ?
                    (i != approximationSupports.size() - 1 ? histogram.getCDFHistogramValues().get(supportsIndices.get(i).get("end").intValue() - 1).doubleValue() : 1 ) -
                            histogram.getCDFHistogramValues().get(supportsIndices.get(i - 1).get("end").intValue() - 1).doubleValue()
                    : histogram.getCDFHistogramValues().get(supportsIndices.get(i).get("end").intValue() - 1).doubleValue();

            double computedLambda = Double.MAX_VALUE;

            for(int j = supportsIndices.get(i).get("start").intValue(); j <= supportsIndices.get(i).get("end").intValue(); j++){

                double cdfValue = ((j != 0 ? histogram.getCDFHistogramValues().get(j - 1).doubleValue()
                        : histogram.getCDFHistogramValues().get(j).doubleValue() ) - subtractionFactor) / normalizationFactor;
                double xValue = histogram.getXValues().get(j).doubleValue();

                if(i != approximationSupports.size() - 1){
                    UnivariateDifferentiableFunction function = new ApproximationHelpers.CumulativeTruncatedExp(start, end, xValue, cdfValue);
                    computedLambda = Math.min(computedLambda, zeroSolver.solve(10000, function, 0.0001));
                } else {
                    // handling last support with Exponential
                    computedLambda = Math.min(computedLambda, -Math.log(1 - cdfValue) / (xValue - start));
                }
            }

            Map<String, BigDecimal> parameterMap = new HashMap<>();
            parameterMap.put("lambda", BigDecimal.valueOf(computedLambda));
            lambdas.add(parameterMap);
        }

        return Map.ofEntries(
                Map.entry("body", ),
                Map.entry("tail", ),
        );
    }*/
}
