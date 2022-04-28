/* This program is called EULERO.
 * Copyright (C) 2022 The EULERO Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.oristool.eulero.evaluation.approximator;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.IntStream;

public class TruncatedExponentialMixtureApproximation extends Approximator{
    @Override
    public Pair<BigDecimal, StochasticTransitionFeature> getApproximatedStochasticTransitionFeature(double[] cdf, double low, double upp, BigDecimal step) {
        return null;
    }

    @Override
    public ArrayList<Pair<BigDecimal, StochasticTransitionFeature>> getApproximatedStochasticTransitionFeatures(double[] cdf, double low, double upp, BigDecimal step) {
        //ActivityViewer.CompareResults("", false, "", List.of("Test"), new EvaluationResult("", cdf, 0, cdf.length, step.doubleValue(), 1));
        ArrayList<Pair<BigDecimal, StochasticTransitionFeature>> features = new ArrayList<>();
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

            if(Math.signum(pdfDerivative[i - 1]) != 0 && Math.signum(pdfDerivative[i - 1]) != 0 &&
                    Math.signum(pdfDerivative[i - 1]) * Math.signum(pdfDerivative[i]) < 0 &&
                    Math.abs(pdfDerivative[i] - pdfDerivative[i - 1]) <= step.doubleValue() * 10){

                if((i - 1) - supportBreakpoints.get(supportBreakpoints.size() - 1) < simplificationWindowWidthInd){
                    supportBreakpoints.remove(supportBreakpoints.size() - 1);
                }
                supportBreakpoints.add(i - 1);
            }
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
            double lambda = pdfDerivative[supportBreakpoints.get(j) + 1] >= 0 ? 0 : Double.MAX_VALUE;
            double featureWeight = j != 0 ? cdf[supportBreakpoints.get(j + 1)] - cdf[supportBreakpoints.get(j)] : cdf[supportBreakpoints.get(j + 1)];

            int finalJ = j;
            int cutStartingIndex = pdfDerivative[supportBreakpoints.get(j) + 1] >= 0 ?
                    IntStream.range(supportBreakpoints.get(j), supportBreakpoints.get(j + 1))
                        .filter(i ->  cdf[i] - cdf[supportBreakpoints.get(finalJ)] >= 0.001)
                        .findFirst().orElse(supportBreakpoints.get(j)) : supportBreakpoints.get(j);
            int cutEndingIndex = supportBreakpoints.get(j + 1);


            for(int i = cutStartingIndex; i < cutEndingIndex; i++){
                double normalizedCdfValue = (cdf[i] - cdf[supportBreakpoints.get(j)]) / featureWeight ;

                try {
                    lambda = pdfDerivative[supportBreakpoints.get(j) + 1] >= 0 ?
                            Math.max(lambda, evaluatePositiveExpLambda(x[cutStartingIndex], x[cutEndingIndex], x[i], normalizedCdfValue)) :
                            Math.min(lambda, evaluateNegativeExpLambda(x[cutStartingIndex], x[cutEndingIndex], x[i], normalizedCdfValue));
                } catch (Exception e){
                        System.out.println(e);
                }
            }

            /*if(Math.abs(lambda) == 0){
                features.remove(j - 1);
                stochasticTransitionFeatureWeights().remove(j - 1);
                supportBreakpoints.remove(j);
                j-=2;
            } else {*/

                lambda = BigDecimal.valueOf(lambda).setScale(4, RoundingMode.HALF_DOWN).doubleValue();
                lambda = pdfDerivative[supportBreakpoints.get(j) + 1] >= 0 ? -lambda : lambda;
                double b = lambda > 0 ? x[cutStartingIndex] : x[cutEndingIndex];
                DBMZone bodyDomain = new DBMZone(Variable.X);
                bodyDomain.setCoefficient(Variable.X, Variable.TSTAR, new OmegaBigDecimal(String.valueOf(x[cutEndingIndex])));
                bodyDomain.setCoefficient(Variable.TSTAR, Variable.X, new OmegaBigDecimal(String.valueOf(-x[cutStartingIndex])));

                Expolynomial density = Expolynomial.fromString(Math.abs(lambda) * Math.exp(lambda * b) / (1 - Math.exp(- Math.abs(lambda) * (x[cutEndingIndex] - x[cutStartingIndex]))) + " * Exp[" + (-lambda) + " x]");
                GEN gen = new GEN(bodyDomain, density);
                StochasticTransitionFeature feature = StochasticTransitionFeature.of(
                        new PartitionedGEN(List.of(gen)));

                features.add(Pair.of(BigDecimal.valueOf(featureWeight), feature));
            //}
        }

        return features;
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
