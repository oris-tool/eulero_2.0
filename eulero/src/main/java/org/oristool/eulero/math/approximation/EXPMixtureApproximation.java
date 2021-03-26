package org.oristool.eulero.math.approximation;

import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EXPMixtureApproximation extends Approximator {

    public EXPMixtureApproximation(){
    }

    @Override
    public Map<String, Map<String, BigDecimal>> getApproximationParameters(double[] cdf, double low, double upp) {
        Map<String, Map<String, BigInteger>> supportIndices = getApproximationSupportIndices(cdf, low, upp);
        Map<String, Map<String, BigDecimal>> supportValues = getApproximationSupports(cdf, low, upp);
        Map<String, BigDecimal> supportWeight = getApproximationSupportsWeight(cdf, low, upp);
        NewtonRaphsonSolver zeroSolver = new NewtonRaphsonSolver();

        double timeTick = (upp - low) / cdf.length;
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

        bodyParameters.put("lambda", BigDecimal.valueOf(bodyLambda));
        bodyParameters.put("delta", supportValues.get("body").get("start"));


        // Handling Tail
        Map<String, BigDecimal> tailParameters = new HashMap<>();
        double tailLambda = Double.MAX_VALUE;

        for(int i = supportIndices.get("tail").get("start").intValue() + 1; i < supportIndices.get("tail").get("end").intValue(); i++){
            //Serve davvero i != 0???
            double cdfValue = ((i != 0 ? cdf[i - 1] : cdf[i]) - (1 - supportWeight.get("tail").doubleValue())) / supportWeight.get("tail").doubleValue();

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
