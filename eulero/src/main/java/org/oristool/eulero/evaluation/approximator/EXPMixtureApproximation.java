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
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class EXPMixtureApproximation extends Approximator {

    public EXPMixtureApproximation(){
        super();
    }

    @Override
    public Pair<BigDecimal, StochasticTransitionFeature> getApproximatedStochasticTransitionFeature(double[] cdf, double low, double upp, BigDecimal step) {
        if(cdf.length < (upp - low)/step.doubleValue()){
            throw new RuntimeException("cdf has not enough samples with respect to provided support and time step value");
        }
        // Ricorda che la cdf è data da 0 a upp; low si usa se serve sapere il supporto reale.
        ArrayList<GEN> distributionPieces = new ArrayList<>();
        NewtonRaphsonSolver zeroSolver = new NewtonRaphsonSolver();

        int Q3Index = IntStream.range(0, cdf.length)
            .filter(i -> cdf[i] >= 0.75)
            .findFirst()
            .orElse(cdf.length - 1);

        double Q3 = /*low +*/ Q3Index * step.doubleValue();
        double timeTick = step.doubleValue();

        double[] pdf = new double[cdf.length];
        double[] x = new double[cdf.length];
        for(int i = 0; i < cdf.length - 1; i++){
            pdf[i + 1] = (cdf[i+1] - cdf[i]) / timeTick;
            x[i] = /*low +*/ i * timeTick;
        }

        double pdfMax = Arrays.stream(pdf, 0, Q3Index).max().getAsDouble();
        int xMaxIndex = IntStream.range(0, Q3Index)
            .filter(i ->  pdf[i] == pdfMax)
            .findFirst() // first occurrence
            .orElse(-1);
        double xMax = /*low +*/ timeTick * xMaxIndex;
        double cdfMax = cdf[xMaxIndex];

        double delta = (pdfMax * xMax - cdfMax) / pdfMax;

        int deltaIndex = IntStream.range(0, Q3Index)
            .filter(i ->  x[i] >= delta)
            .findFirst() // first occurrence
            .orElse(-1);

        // Body
        double bodyLambda = Double.MAX_VALUE;

        double[] test = new double[cdf.length];
        for(int i = deltaIndex; i < Q3Index; i++){
            double cdfValue = cdf[i] / cdf[Q3Index];
            test[i] = cdfValue;
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

                    public UnivariateDifferentiableFunction init(double delta, double b, double time, double histogram){
                        this.delta = delta;
                        this.b = b;
                        this.time = time;
                        this.histogram = histogram;
                        return this;
                    }
                }.init(delta, Q3, x[i], cdfValue), 0.0001)
            );
        }

        String bodyDensity =
            cdf[Q3Index] * bodyLambda * Math.exp(bodyLambda * delta) / (1 - Math.exp(-bodyLambda * (Q3 - delta))) +
                    " * Exp[-" + bodyLambda + " x]";
        distributionPieces.add(GEN.newExpolynomial(bodyDensity, new OmegaBigDecimal(String.valueOf(delta)), new OmegaBigDecimal(String.valueOf(Q3))));

        //tail
        double tailLambda = Double.MAX_VALUE;
        for(int i = Q3Index ; i < cdf.length; i++){
            double cdfValue = (cdf[i] - cdf[Q3Index]) / (1 - cdf[Q3Index]);

            //Discard bad conditioned values
            if(cdfValue > 0  &&  cdfValue < 1 && /*low +*/ (i * step.doubleValue()) > Q3) {
                tailLambda = Math.min(
                    tailLambda,
                    -Math.log(1 - cdfValue) / (/*low +*/ (i * step.doubleValue()) - Q3)
                );
            }
        }

        String tailDensity = (1 - cdf[Q3Index]) * tailLambda * Math.exp(tailLambda * Q3) + " * Exp[-" + tailLambda + " x]";
        distributionPieces.add(GEN.newExpolynomial(tailDensity, new OmegaBigDecimal(String.valueOf(Q3)), OmegaBigDecimal.POSITIVE_INFINITY));

        return Pair.of(BigDecimal.ONE, StochasticTransitionFeature.of(new PartitionedGEN(distributionPieces)));
    }

    @Override
    public ArrayList<Pair<BigDecimal, StochasticTransitionFeature>> getApproximatedStochasticTransitionFeatures(double[] cdf, double low, double upp, BigDecimal step) {
        ArrayList<Pair<BigDecimal, StochasticTransitionFeature>> features = new ArrayList<>();

        if(cdf.length < (upp - low)/step.doubleValue()){
            throw new RuntimeException("cdf has not enough samples with respect to provided support and time step value");
        }
        // Ricorda che la cdf è data da 0 a upp; low si usa se serve sapere il supporto reale.
        ArrayList<GEN> distributionPieces = new ArrayList<>();
        NewtonRaphsonSolver zeroSolver = new NewtonRaphsonSolver();

        int Q3Index = IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.75)
                .findFirst()
                .orElse(cdf.length - 1);

        double Q3 = /*low +*/ Q3Index * step.doubleValue();
        double timeTick = step.doubleValue();

        double[] pdf = new double[cdf.length];
        double[] x = new double[cdf.length];
        for(int i = 0; i < cdf.length - 1; i++){
            pdf[i + 1] = BigDecimal.valueOf((cdf[i+1] - cdf[i]) / timeTick).setScale(3, RoundingMode.HALF_DOWN).doubleValue();
            x[i] = /*low +*/ i * timeTick;
        }

        double pdfMax = Arrays.stream(pdf, 0, Q3Index).max().getAsDouble();
        int xMaxIndex = IntStream.range(0, Q3Index)
            .filter(i ->  pdf[i] == pdfMax)
            /*.findFirst() // first occurrence
            .orElse(-1);*/
            .reduce((first, second) -> second).orElse(-1);

        if(xMaxIndex == Q3Index - 1){
            //tail
            double tailLambda = Double.MAX_VALUE;
            int index = IntStream.range(Q3Index, cdf.length)
                .filter(t -> cdf[t] >= 0.999)
                .findFirst()
                .orElse(cdf.length);
            for(int i = Q3Index ; i < index; i++){
                //double cdfValue = (cdf[i] - cdf[Q3Index]) / (1 - cdf[Q3Index]);

                //Discard bad conditioned values
                if(cdf[i] > 0  &&  cdf[i] < 1 && /*low +*/ (i * step.doubleValue()) > Q3) {
                    tailLambda = Math.min(
                        tailLambda,
                        -Math.log(1 - cdf[i]) / (/*low +*/ (i * step.doubleValue()) - Q3)
                    );
                }
            }

            features.add(Pair.of(
                    BigDecimal.ONE,
                    StochasticTransitionFeature.newExponentialInstance(BigDecimal.valueOf(tailLambda))
            ));

        } else {
            double xMax = /*low +*/ timeTick * xMaxIndex;
            double cdfMax = cdf[xMaxIndex];

            double delta = (pdfMax * xMax - cdfMax) / pdfMax;

            int deltaIndex = IntStream.range(0, Q3Index)
                .filter(i ->  x[i] >= delta)
                .findFirst() // first occurrence
                .orElse(-1);

            // Body
            double bodyLambda = Double.MAX_VALUE;

            double[] test = new double[cdf.length];
            for(int i = deltaIndex; i < Q3Index; i++){
                double cdfValue = cdf[i] / cdf[Q3Index];
                test[i] = cdfValue;
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

                        public UnivariateDifferentiableFunction init(double delta, double b, double time, double histogram){
                            this.delta = delta;
                            this.b = b;
                            this.time = time;
                            this.histogram = histogram;
                            return this;
                        }
                    }.init(delta, Q3, x[i], cdfValue), 0.0001)
                );
            }

            features.add(Pair.of(
                BigDecimal.valueOf(0.75),
                StochasticTransitionFeature.newExpolynomial(
                    bodyLambda * Math.exp(bodyLambda * delta) / (1 - Math.exp(-bodyLambda * (Q3 - delta))) + " * Exp[-" + bodyLambda + " x]",
                    new OmegaBigDecimal(String.valueOf(delta)),
                    new OmegaBigDecimal(String.valueOf(Q3))
                )
            ));

            //tail
            double tailLambda = Double.MAX_VALUE;
            int index = IntStream.range(Q3Index, cdf.length)
                .filter(t -> cdf[t] >= 0.999)
                .findFirst()
                .orElse(cdf.length);
            for(int i = Q3Index ; i < index; i++){
                double cdfValue = (cdf[i] - cdf[Q3Index]) / (1 - cdf[Q3Index]);

                //Discard bad conditioned values
                if(cdfValue > 0  &&  cdfValue < 1 && /*low +*/ (i * step.doubleValue()) > Q3) {
                    tailLambda = Math.min(
                        tailLambda,
                        -Math.log(1 - cdfValue) / (/*low +*/ (i * step.doubleValue()) - Q3)
                    );
                }
            }

            features.add(Pair.of(
                BigDecimal.valueOf(0.25),
                StochasticTransitionFeature.newExponentialInstance(BigDecimal.valueOf(tailLambda))
            ));
        }

        return features;
    }

    @Override
    public StochasticTime getApproximatedStochasticTime(double[] cdf, double low, double upp, BigDecimal step) {
        return null;
    }
}
