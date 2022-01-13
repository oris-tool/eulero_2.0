package org.oristool.eulero.math.approximation;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.oristool.eulero.math.distribution.continuous.ShiftedExponentialDistribution;
import org.oristool.eulero.math.distribution.continuous.ShiftedTruncatedExponentialDistribution;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class TruncatedExponentialApproximation extends Approximator{

    public TruncatedExponentialApproximation(){
        super();
    }

    @Override
    public Map<String, Map<String, BigDecimal>> getApproximationSupports(double[] cdf, double low, double upp, BigDecimal step) {
        return null;
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
        return null;
    }

    @Override
    public Map<String, Approximator.ApproximationSupportSetup> getApproximationSupportSetups(double[] cdf, double low, double upp, BigDecimal step){
        return null;
    }

    @Override
    public Map<String, Map<String, BigDecimal>> getApproximationParameters(double[] cdf, double low, double upp, BigDecimal step) {
        return null;
    }

    @Override
    public StochasticTransitionFeature getApproximatedStochasticTransitionFeature(double[] cdf, double low, double upp, BigDecimal step) {
        return null;
    }

    @Override
    public ArrayList<StochasticTransitionFeature> getApproximatedStochasticTransitionFeatures(double[] cdf, double low, double upp, BigDecimal step) {
        stochasticTransitionFeatureWeights().clear();
        ArrayList<StochasticTransitionFeature> features = new ArrayList<>();

        if(cdf.length < (upp - low)/step.doubleValue()){
            throw new RuntimeException("cdf has not enough samples with respect to provided support and time step value");
        }
        // Ricorda che la cdf è data da 0 a upp; low si usa se serve sapere il supporto reale.
        ArrayList<GEN> distributionPieces = new ArrayList<>();
        NewtonRaphsonSolver zeroSolver = new NewtonRaphsonSolver();

        for(int i = 0; i < cdf.length - 1; i++){
            cdf[i] = BigDecimal.valueOf(cdf[i]).doubleValue();
        }
        double timeTick = step.doubleValue();

        double[] pdf = new double[cdf.length];
        double[] x = new double[cdf.length];
        for(int i = 0; i < cdf.length - 1; i++){
            pdf[i + 1] = BigDecimal.valueOf((cdf[i+1] - cdf[i]) / timeTick).setScale(3, RoundingMode.HALF_DOWN).doubleValue();
            x[i] = /*low +*/ i * timeTick;
        }

        double pdfMax = Arrays.stream(pdf, 0, pdf.length).max().getAsDouble();
        int xMaxIndex = IntStream.range(0, pdf.length)
                .filter(i ->  pdf[i] == pdfMax)
                .findFirst() // first occurrence
                .orElse(-1);
                //.reduce((first, second) -> second).orElse(-1);


        double xMax = /*low +*/ timeTick * xMaxIndex;
        double cdfMax = cdf[xMaxIndex];

        double delta = BigDecimal.valueOf((pdfMax * xMax - cdfMax) / pdfMax).doubleValue();

        int deltaIndex = IntStream.range(0, pdf.length)
                .filter(i ->  x[i] >= delta)
                .findFirst() // first occurrence
                .orElse(-1);

        // Body
        double bodyLambda = Double.MAX_VALUE;

        for(int i = deltaIndex; i < pdf.length; i++){
            //if(cdf[i] > 0  &&  cdf[i] < 1){
                try {
                    bodyLambda = Math.min(
                            bodyLambda,
                            zeroSolver.solve(10000, new UnivariateDifferentiableFunction() {
                                private double delta;
                                private double b;
                                private double time;
                                private double histogram;

                                @Override
                                public DerivativeStructure value(DerivativeStructure t) throws DimensionMismatchException {
                                    // t should be our lambda
                                    DerivativeStructure p = t.multiply(delta - time).expm1();
                                    DerivativeStructure q = t.multiply(delta - b).expm1();

                                    return p.divide(q).subtract(histogram);
                                }

                                @Override
                                public double value(double x) {
                                    // Here x is the lambda of the function
                                    return (1 - Math.exp(-x * (time - delta))) / (1 - Math.exp(-x * (b - delta))) - histogram;
                                }

                                public UnivariateDifferentiableFunction init(double delta, double b, double time, double histogram) {
                                    this.delta = delta;
                                    this.b = b;
                                    this.time = time;
                                    this.histogram = histogram;
                                    return this;
                                }
                            }.init(delta, upp, x[i], cdf[i]), 0.0001)
                    );
                } catch (Exception e){
                    System.out.println("Eccezione...");
                }
            //}
        }

        bodyLambda = BigDecimal.valueOf(bodyLambda).setScale(3, RoundingMode.HALF_UP).doubleValue();

        features.add(StochasticTransitionFeature.newExpolynomial(
                bodyLambda * Math.exp(bodyLambda * delta) / (1 - Math.exp(-bodyLambda * (upp - delta))) + " * Exp[-" + bodyLambda + " x]",
                new OmegaBigDecimal(String.valueOf(delta)),
                new OmegaBigDecimal(String.valueOf(upp))
        ));

        stochasticTransitionFeatureWeights().add(BigDecimal.valueOf(1.0));
        return features;
    }
}
