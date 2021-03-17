package org.oristool.eulero.math.approximation;

import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EXPMixtureApproximation extends HistogramApproximator{

    public EXPMixtureApproximation(BigInteger neighbourhoodHalfSize){
        super(neighbourhoodHalfSize);
    }

    public EXPMixtureApproximation(){
        this(BigInteger.valueOf(20));
    }

    public ArrayList<Map<String, BigDecimal>> getApproximationParameters(HistogramDistribution histogram, ArrayList<Map<String, BigDecimal>> approximationSupports){
        ArrayList<Map<String, BigDecimal>> lambdas = new ArrayList<>();
        NewtonRaphsonSolver zeroSolver = new NewtonRaphsonSolver();

        ArrayList<Map<String, BigInteger>> supportsIndices = getApproximationSupportsBoundingIndices(histogram, approximationSupports);

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

        return lambdas;
    }
}
