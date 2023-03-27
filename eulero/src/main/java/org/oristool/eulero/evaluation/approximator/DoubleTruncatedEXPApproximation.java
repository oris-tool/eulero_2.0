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
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class DoubleTruncatedEXPApproximation extends Approximator{
    @Override
    public Pair<BigDecimal, StochasticTransitionFeature> getApproximatedStochasticTransitionFeature(double[] cdf, double low, double upp, BigDecimal step) {
        return null;
    }

    @Override
    public ArrayList<Pair<BigDecimal, StochasticTransitionFeature>> getApproximatedStochasticTransitionFeatures(double[] cdf, double low, double upp, BigDecimal step) {
        ArrayList<Pair<BigDecimal, StochasticTransitionFeature>> features = new ArrayList<>();

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
            x[i + 1] = BigDecimal.valueOf((i + 1) * timeTick).setScale(4, RoundingMode.HALF_DOWN).doubleValue();/*low +*/;
        }

        double pdfMax = Arrays.stream(pdf, 0, pdf.length).max().getAsDouble();
        int xMaxIndex = IntStream.range(0, pdf.length)
                .filter(i ->  pdf[i] == pdfMax)
                .reduce((first, second) -> second).orElse(-1);

        double xMax = x[xMaxIndex];
        double cdfMax = cdf[xMaxIndex];

        int starter = IntStream.range(0, xMaxIndex)
                .filter(i ->  cdf[i] >= 0.01)
                .findFirst().orElse(0);

        if(xMaxIndex > 1){
            double bodyLambda = 0;
            double[] test1 = new double[cdf.length];
            for(int i = starter; i < xMaxIndex; i++) {

                double cdfValue = cdf[i] / cdf[xMaxIndex];
                test1[i] = cdfValue;

                try {
                    bodyLambda = Math.max(
                            bodyLambda,
                            zeroSolver.solve(10000, new UnivariateDifferentiableFunction() {
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
                            }.init(x[starter], x[xMaxIndex], x[i], cdfValue), 0.0001)
                    );
                } catch (Exception e) {
                }
            }

            bodyLambda = BigDecimal.valueOf(bodyLambda).setScale(4, RoundingMode.HALF_DOWN).doubleValue();

            DBMZone bodyDomain = new DBMZone(Variable.X);
            bodyDomain.setCoefficient(Variable.X, Variable.TSTAR, new OmegaBigDecimal(String.valueOf(x[xMaxIndex])));
            bodyDomain.setCoefficient(Variable.TSTAR, Variable.X, new OmegaBigDecimal(String.valueOf(-x[starter])));

            Expolynomial density = Expolynomial.fromString(bodyLambda * Math.exp(-bodyLambda * x[xMaxIndex]) / (1 - Math.exp(-bodyLambda * (x[xMaxIndex] - x[starter]))) + " * Exp[" + bodyLambda + " x]");
            GEN gen = new GEN(bodyDomain, density);
            StochasticTransitionFeature feature = StochasticTransitionFeature.of(
                    new PartitionedGEN(List.of(gen)));

            features.add(Pair.of(
                BigDecimal.valueOf(cdf[xMaxIndex]).setScale(4,RoundingMode.HALF_DOWN),
                feature
            ));
        }

        if(xMaxIndex < cdf.length - 1){
            double tailLambda = Double.MAX_VALUE;
            double[] test2 = new double[cdf.length];
            for(int i = xMaxIndex; i < cdf.length; i++){
                double cdfValue = (cdf[i] - cdf[xMaxIndex]) / (cdf[cdf.length - 1] - cdf[xMaxIndex]);
                test2[i] = cdfValue;
                try {
                    tailLambda = Math.min(
                        tailLambda,
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
                        }.init(x[xMaxIndex], upp, x[i], cdfValue), 0.0001)
                    );
                } catch (Exception e){
                }
            }
            tailLambda = BigDecimal.valueOf(tailLambda).setScale(4, RoundingMode.HALF_DOWN).doubleValue();

            features.add(Pair.of(
                BigDecimal.valueOf(cdf[cdf.length-1] - cdf[xMaxIndex]).setScale(4,RoundingMode.HALF_DOWN),
                StochasticTransitionFeature.newExpolynomial(
                    tailLambda * Math.exp(tailLambda * x[xMaxIndex]) / (1 - Math.exp(-tailLambda * (upp - x[xMaxIndex]))) + " * Exp[-" + tailLambda + " x]",
                    new OmegaBigDecimal(String.valueOf(x[xMaxIndex])),
                    new OmegaBigDecimal(String.valueOf(upp))
                )
            ));
        }

        return features;
    }

    @Override
    public StochasticTime getApproximatedStochasticTime(double[] cdf, double low, double upp, BigDecimal step) {
        return null;
    }
}
