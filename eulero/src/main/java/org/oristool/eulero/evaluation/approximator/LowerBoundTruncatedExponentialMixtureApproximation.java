package org.oristool.eulero.evaluation.approximator;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.oristool.eulero.modeling.stochastictime.StochasticTime;
import org.oristool.eulero.modeling.stochastictime.TruncatedExponentialMixtureTime;
import org.oristool.eulero.modeling.stochastictime.TruncatedExponentialTime;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class LowerBoundTruncatedExponentialMixtureApproximation extends Approximator {
    @Override
    public Pair<BigDecimal, StochasticTransitionFeature> getApproximatedStochasticTransitionFeature(double[] cdf, double low, double upp, BigDecimal step) {
        return null;
    }

    @Override
    public ArrayList<Pair<BigDecimal, StochasticTransitionFeature>> getApproximatedStochasticTransitionFeatures(double[] cdf, double low, double upp, BigDecimal step) {
        return null;
    }

    @Override
    public StochasticTime getApproximatedStochasticTime(double[] cdf, double low, double upp, BigDecimal step) {
        ArrayList<TruncatedExponentialTime> truncatedExponentialTimes = new ArrayList<>();
        ArrayList<BigDecimal> pieceWeights = new ArrayList<>();

        double simplificationWindowWidth = 0.5;
        int simplificationWindowWidthInd = (int) (simplificationWindowWidth / step.doubleValue());

        if(cdf.length < (int)(upp - low)/step.doubleValue()){
            throw new RuntimeException("cdf has not enough samples with respect to provided support and time step value");
        }

        for(int i = 0; i < cdf.length - 1; i++){
            cdf[i] = BigDecimal.valueOf(cdf[i]).doubleValue();
        }

        double timeTick = step.doubleValue();

        double[] pdf = new double[cdf.length];
        double[] pdfDerivative = new double[cdf.length];
        double[] x = new double[cdf.length];
        ArrayList<Integer> supportBreakpoints = new ArrayList<>();
        for(int i = 1; i < cdf.length; i++){
            pdf[i] = BigDecimal.valueOf((cdf[i] - cdf[i - 1]) / timeTick).doubleValue();
            x[i] = i * timeTick;
        }

        int start = IntStream.range(0, cdf.length)
                .filter(i ->  cdf[i] >= 0.001)
                .findFirst().orElse(0);
        int end = IntStream.range(0, cdf.length)
                .filter(i ->  cdf[i] >= 0.999)
                .findFirst().orElse(cdf.length);
        supportBreakpoints.add(start);

        for(int i = start; i <= end - 1; i++){
            pdfDerivative[i] = BigDecimal.valueOf((pdf[i] - pdf[i - 1]) / timeTick).doubleValue();

            // CHECK SIGNUM OF PDF and DERIVATIVE to DETERMINE IF FUNCTION GOES FROM DOWNWARD TO UPWARD OR VICEVERSA
            if(Math.signum(pdfDerivative[i - 1]) != 0 && Math.signum(pdfDerivative[i]) != 0 && // case of a flex
                    Math.signum(pdfDerivative[i - 1]) * Math.signum(pdfDerivative[i]) < 0 && // signum changes
                    Math.abs(pdfDerivative[i] - pdfDerivative[i - 1]) <= step.doubleValue() * 10){ // ?

                // TO AVOID TOO MANY BREAKPOINTS AROUND THE SAME PLACE
                if((i - 1) - supportBreakpoints.get(supportBreakpoints.size() - 1) < simplificationWindowWidthInd){
                    supportBreakpoints.remove(supportBreakpoints.size() - 1);
                }
                supportBreakpoints.add(i - 1);
            }

            // NOT REMEMBER; probably the last piece...
            if((Math.signum(pdfDerivative[i - 1]) == 0 || Math.signum(pdfDerivative[i]) == 0)
                    && (Math.signum(pdfDerivative[i - 1]) < 0 || Math.signum(pdfDerivative[i]) < 0) &&
                    Math.abs(pdfDerivative[i] - pdfDerivative[i - 1]) <= step.doubleValue() * 10){

                if((i - 1) - supportBreakpoints.get(supportBreakpoints.size() - 1) < simplificationWindowWidthInd){
                    supportBreakpoints.remove(supportBreakpoints.size() - 1);
                }
                supportBreakpoints.add(i - 1);
            }
        }

        supportBreakpoints.add(end - 1);
        for(int j = 0; j < supportBreakpoints.size() - 1; j++ ){
            double lambda = pdfDerivative[supportBreakpoints.get(j) + 1] >= 0 ? Double.MAX_VALUE : 0;

            // WEIGHT IS THE DIFFERENCE BETWEEN THE MAXIMUM CDF OF A PIECE MINUS THE MAXIMUM OF THE PREDECESSOR PIECE
            double featureWeight = j != 0 ? cdf[supportBreakpoints.get(j + 1)] - cdf[supportBreakpoints.get(j)] : cdf[supportBreakpoints.get(j + 1)];

            int finalJ = j;

            // MAKES ALL PIECES STARTING FROM 0...
            int cutStartingIndex = pdfDerivative[supportBreakpoints.get(j) + 1] >= 0 ?
                    IntStream.range(supportBreakpoints.get(j), supportBreakpoints.get(j + 1))
                            .filter(i ->  cdf[i] - cdf[supportBreakpoints.get(finalJ)] >= 0.001)
                            .findFirst().orElse(supportBreakpoints.get(j)) : supportBreakpoints.get(j);

            int cutEndingIndex = supportBreakpoints.get(j + 1);

            for(int i = cutStartingIndex; i < cutEndingIndex; i++){
                double normalizedCdfValue = (cdf[i] - cdf[supportBreakpoints.get(j)]) / featureWeight ;

                try {
                    // DEPENDING ON THE SIGN, EVALUATE NEGATIVE OR POSITIVE RATE EXPONENTIAL
                    // RULE IN THE PAPER
                    lambda = pdfDerivative[supportBreakpoints.get(j) + 1] >= 0 ?
                            Math.min(lambda, evaluatePositiveExpLambda(x[cutStartingIndex], x[cutEndingIndex], x[i], normalizedCdfValue)) :
                            Math.max(lambda, evaluateNegativeExpLambda(x[cutStartingIndex], x[cutEndingIndex], x[i], normalizedCdfValue));
                } catch (Exception e){
                    System.out.println(e);
                }
            }

            lambda = BigDecimal.valueOf(lambda).setScale(4, RoundingMode.HALF_DOWN).doubleValue();
            lambda = pdfDerivative[supportBreakpoints.get(j) + 1] >= 0 ? -lambda : lambda;

            truncatedExponentialTimes.add(new TruncatedExponentialTime(x[cutStartingIndex], x[cutEndingIndex], lambda));
            pieceWeights.add(BigDecimal.valueOf(featureWeight));
        }

        return new TruncatedExponentialMixtureTime(truncatedExponentialTimes, pieceWeights);
    }

    public double evaluatePositiveExpLambda(double xLow, double xUpp, double x, double cdfValue){
        NewtonRaphsonSolver zeroSolver = new NewtonRaphsonSolver();
        return zeroSolver.solve(10000, new UnivariateDifferentiableFunction() {
            private double delta;
            private double b;
            private double time;
            private double histogram;

            @Override
            public DerivativeStructure value(DerivativeStructure t) throws DimensionMismatchException {
                // t should be our lambda
                DerivativeStructure p1 = t.multiply(delta - b).exp();
                DerivativeStructure p2 = t.multiply(time - b).exp();
                DerivativeStructure p = p1.subtract(p2);
                DerivativeStructure q = t.multiply(delta - b).expm1();

                return (p.divide(q).subtract(histogram));
            }

            @Override
            public double value(double x) {
                // Here x is the lambda of the function
                return (Math.exp(x * (delta - b)) - Math.exp(x * (time - b))) / (Math.exp(x * (delta - b)) - 1) - histogram;
            }

            public UnivariateDifferentiableFunction init(double delta, double b, double time, double histogram) {
                this.delta = delta;
                this.b = b;
                this.time = time;
                this.histogram = histogram;
                return this;
            }
        }.init(xLow, xUpp, x, cdfValue), 0.0001);
    }

    public double evaluateNegativeExpLambda(double xLow, double xUpp, double x, double cdfValue){
        NewtonRaphsonSolver zeroSolver = new NewtonRaphsonSolver();
        return zeroSolver.solve(10000, new UnivariateDifferentiableFunction() {
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
        }.init(xLow, xUpp, x, cdfValue), 0.0001);
    }
}
