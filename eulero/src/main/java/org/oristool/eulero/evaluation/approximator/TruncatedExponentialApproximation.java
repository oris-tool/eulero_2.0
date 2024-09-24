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
import org.oristool.eulero.modeling.stochastictime.StochasticTime;
import org.oristool.eulero.modeling.stochastictime.TruncatedExponentialTime;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class TruncatedExponentialApproximation extends Approximator{

    public TruncatedExponentialApproximation(){
        super();
    }

    @Override
    public Pair<BigDecimal,StochasticTransitionFeature> getApproximatedStochasticTransitionFeature(double[] cdf, double low, double upp, BigDecimal step) {
        return null;
    }

    @Override
    public ArrayList<Pair<BigDecimal,StochasticTransitionFeature>> getApproximatedStochasticTransitionFeatures(double[] cdf, double low, double upp, BigDecimal step) {
        ArrayList<Pair<BigDecimal,StochasticTransitionFeature>> features = new ArrayList<>();

        if(cdf.length < (int)(upp - low)/step.doubleValue()){
            throw new RuntimeException("cdf has not enough samples with respect to provided support and time step value");
        }

        // Ricorda che la cdf è data da 0 a upp; low si usa se serve sapere il supporto reale.
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

        features.add(Pair.of(
            BigDecimal.ONE,
            StochasticTransitionFeature.newExpolynomial(
                bodyLambda * Math.exp(bodyLambda * delta) / (1 - Math.exp(-bodyLambda * (upp - delta))) + " * Exp[-" + bodyLambda + " x]",
                new OmegaBigDecimal(String.valueOf(delta)),
                new OmegaBigDecimal(String.valueOf(upp))
            )
        ));

        return features;
    }

    @Override
    public StochasticTime getApproximatedStochasticTime(double[] cdf, double low, double upp, BigDecimal step) {
        ArrayList<Pair<BigDecimal,StochasticTransitionFeature>> features = new ArrayList<>();

        if(cdf.length < (int)(upp - low)/step.doubleValue()){
            throw new RuntimeException("cdf has not enough samples with respect to provided support and time step value");
        }

        // Ricorda che la cdf è data da 0 a upp; low si usa se serve sapere il supporto reale.
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

        return new TruncatedExponentialTime(delta, upp, bodyLambda);
    }
}
