package org.oristool.eulero.math.approximation;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.GEN;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.IntStream;

public class SplineTruncatedEXP extends Approximator{
    private int bodyPieces;

    public SplineTruncatedEXP(int bodyPieces){
        this.bodyPieces = bodyPieces;
    }

    @Override
    public Map<String, Map<String, BigDecimal>> getApproximationSupports(double[] cdf, double low, double upp, BigDecimal step) {
        return null;
    }

    @Override
    public Map<String, Map<String, BigInteger>> getApproximationSupportIndices(double[] cdf, double low, double upp) {
        return null;
    }

    @Override
    public Map<String, BigDecimal> getApproximationSupportsWeight(double[] cdf, double low, double upp, BigDecimal step) {
        return null;
    }

    @Override
    public Map<String, ApproximationSupportSetup> getApproximationSupportSetups(double[] cdf, double low, double upp, BigDecimal step) {
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
        // Clean weights vector
        stochasticTransitionFeatureWeights().clear();
        ArrayList<StochasticTransitionFeature> features = new ArrayList<>();

        // Ricorda che la cdf è data da 0 a upp; low si usa se serve sapere il supporto reale.
        if(cdf.length < (upp - low)/step.doubleValue()){
            throw new RuntimeException("cdf has enough samples with respect to provided support and time step value");
        }

        double timeTick = step.doubleValue();
        NewtonRaphsonSolver zeroSolver = new NewtonRaphsonSolver();

        double[] pdf = new double[cdf.length];
        double[] x = new double[cdf.length];

        for(int i = 0; i < pdf.length - 1; i++){
            pdf[i + 1] = BigDecimal.valueOf((cdf[i+1] - cdf[i]) / timeTick).setScale(6, RoundingMode.HALF_DOWN).doubleValue();
            //pdf[i] = (i != pdf.length - 1 ? (cdf[i+1] - cdf[i]) : 0) / step.doubleValue() ;
            x[i + 1] = /*low +*/ i * timeTick;
        }

        double pdfMax = Arrays.stream(pdf, 0, pdf.length - 1).max().getAsDouble();
        int xMaxIndex = IntStream.range(0, pdf.length - 1)
                .filter(i ->  pdf[i] == pdfMax)
                //.reduce((first, second) -> second)
                .findFirst().orElse(-1);

        double xMax = x[xMaxIndex];
        double cdfMax = cdf[xMaxIndex];

        double delta = (pdfMax * xMax - cdfMax) / pdfMax;
        int deltaIndex = IntStream.range(0, pdf.length)
                .filter(i ->  x[i] >= delta)
                .findFirst() // first occurrence
                .orElse(-1);

        int bodyPieceWidth = (int) ((upp - delta) / step.doubleValue() / (double) bodyPieces);

        for(int i = 0; i < bodyPieces; i++){
            int bodyPieceStartingIndex = deltaIndex + i * bodyPieceWidth;
            int bodyPieceEndingIndex = (i != bodyPieces - 1) ? deltaIndex + (i + 1) * bodyPieceWidth : cdf.length - 1;

            double bodyLambda = Double.MAX_VALUE;

            double[] test = new double[cdf.length];
            for(int j = bodyPieceStartingIndex; j < bodyPieceEndingIndex; j++){
                double cdfValue;
                if(i == 0){
                    cdfValue = cdf[j] / cdf[bodyPieceEndingIndex];
                } else {
                    cdfValue = (cdf[j] - cdf[bodyPieceStartingIndex]) / (cdf[bodyPieceEndingIndex] - cdf[bodyPieceStartingIndex]);
                }
                test[j] = cdfValue;

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
                            }.init(BigDecimal.valueOf(bodyPieceStartingIndex * timeTick).setScale(step.scale(), RoundingMode.HALF_DOWN).doubleValue(),
                                    BigDecimal.valueOf(bodyPieceEndingIndex * timeTick).setScale(step.scale(), RoundingMode.HALF_DOWN).doubleValue(),
                                    BigDecimal.valueOf(j * timeTick).setScale(step.scale(), RoundingMode.HALF_DOWN).doubleValue(),
                                    cdfValue), 0.0000001)
                    );
                } catch(Exception e){
                    System.out.println("Eccezione: il valore utilizzato era " + cdfValue);
                }

            }

            bodyLambda = BigDecimal.valueOf(bodyLambda).setScale(4, RoundingMode.HALF_DOWN).doubleValue();
            features.add(StochasticTransitionFeature.newExpolynomial(
                    bodyLambda * Math.exp(bodyLambda * bodyPieceStartingIndex * timeTick) / (1 - Math.exp(-bodyLambda * (bodyPieceEndingIndex * timeTick - bodyPieceStartingIndex * timeTick))) + " * Exp[-" + bodyLambda + " x]",
                    new OmegaBigDecimal(String.valueOf(bodyPieceStartingIndex * timeTick)),
                    new OmegaBigDecimal(String.valueOf(bodyPieceEndingIndex * timeTick))
            ));
            stochasticTransitionFeatureWeights().add(BigDecimal.valueOf(cdf[bodyPieceEndingIndex] - cdf[bodyPieceStartingIndex]));

        }
        return features;
    }
}
