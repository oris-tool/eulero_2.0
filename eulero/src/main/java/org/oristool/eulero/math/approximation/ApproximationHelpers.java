package org.oristool.eulero.math.approximation;

import org.apache.commons.math3.analysis.FunctionUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;

public class ApproximationHelpers {
    // TODO - TBD: is it ok to use such a class??

    public static class CumulativeTruncatedExp implements UnivariateDifferentiableFunction {
        private double delta;
        private double b;
        private double time;
        private double histogram;


        public CumulativeTruncatedExp(double delta, double b, double time, double histogram){
            this.delta = delta;
            this.b = b;
            this.time = time;
            this.histogram = histogram;
        }

        public double value(double x) {
            // Here x is the lambda of the function
            return (1 - Math.exp(-x * (time - delta))) / (1 - Math.exp(-x * (b - delta))) - histogram;
        }

        public UnivariateFunction derivative() {
            return FunctionUtils.toDifferentiableUnivariateFunction(this).derivative();
        }

        public DerivativeStructure value(DerivativeStructure t) throws DimensionMismatchException {
            // t should be our lambda
            DerivativeStructure p = t.multiply(delta - time).expm1();
            DerivativeStructure q = t.multiply(delta - b).expm1();

            return p.divide(q).subtract(histogram);
        }
    }
}
