package org.oristool.eulero.math.approximation;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.checkerframework.checker.units.qual.A;
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

public class EXPMixtureApproximation extends Approximator {

    public EXPMixtureApproximation(){
        super();
    }

    @Override
    public Map<String, Map<String, BigDecimal>> getApproximationSupports(double[] cdf, double low, double upp, BigDecimal step) {
        //BigDecimal tukeysUpperBound = ApproximationHelpers.getTukeysBounds(cdf, low, upp).get("upp");
        BigDecimal tukeysUpperBound = ApproximationHelpers.getQuartileBounds(cdf, low, upp).get("upp");
        int tukeysUpperBoundIndex = ApproximationHelpers.getTukeysBoundsIndices(cdf, low, upp).get("upp").intValue();

        double timeTick = step.doubleValue();
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
        double xMax = low + timeTick * (xMaxIndex + 1);
        double cdfMax = cdf[xMaxIndex + 1];

        double delta = Arrays.stream(x)
                .filter(value -> value >= (pdfMax * xMax - cdfMax) / pdfMax)
                .findFirst()
                .orElse(-1);

        Map<String, BigDecimal> bodySupport = new HashMap<>();
        bodySupport.put("start", BigDecimal.valueOf(delta).setScale(step.scale(), RoundingMode.HALF_DOWN));
        bodySupport.put("end", tukeysUpperBound.setScale(step.scale(), RoundingMode.HALF_DOWN));

        Map<String, BigDecimal> tailSupport = new HashMap<>();
        tailSupport.put("start", tukeysUpperBound.setScale(step.scale(), RoundingMode.HALF_DOWN));
        tailSupport.put("end", BigDecimal.valueOf(Double.MAX_VALUE));

        return Map.ofEntries(Map.entry("body", bodySupport), Map.entry("tail", tailSupport));
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
        int tukeysUpperBoundIndex = ApproximationHelpers.getQuartileBoundsIndices(cdf, low, upp).get("upp").intValue();

        return Map.ofEntries(
                Map.entry("body", BigDecimal.valueOf(cdf[tukeysUpperBoundIndex - 1])),
                Map.entry("tail", BigDecimal.valueOf(1 - cdf[tukeysUpperBoundIndex - 1]))
        );
    }

    @Override
    public Map<String, ApproximationSupportSetup> getApproximationSupportSetups(double[] cdf, double low, double upp, BigDecimal step){
        Map<String, Map<String, BigDecimal>> approximationSupports = getApproximationSupports(cdf, low, upp, step);
        Map<String, BigDecimal> weights = getApproximationSupportsWeight(cdf, low, upp, step);
        Map<String, Map<String, BigDecimal>> params = getApproximationParameters(cdf, low, upp, step);

        return Map.ofEntries(
                Map.entry("body", new ApproximationSupportSetup(weights.get("body"), approximationSupports.get("body"), params.get("body"),
                        new ShiftedTruncatedExponentialDistribution("body", approximationSupports.get("body").get("start"), approximationSupports.get("body").get("end"), params.get("body").get("lambda")))),
                Map.entry("tail", new ApproximationSupportSetup(weights.get("tail"), approximationSupports.get("tail"), params.get("tail"),
                        new ShiftedExponentialDistribution("tail", approximationSupports.get("tail").get("start"), params.get("tail").get("lambda"))))
        );
    }

    @Override
    public Map<String, Map<String, BigDecimal>> getApproximationParameters(double[] cdf, double low, double upp, BigDecimal step) {
        Map<String, Map<String, BigInteger>> supportIndices = getApproximationSupportIndices(cdf, low, upp);
        Map<String, Map<String, BigDecimal>> supportValues = getApproximationSupports(cdf, low, upp, step);
        Map<String, BigDecimal> supportWeight = getApproximationSupportsWeight(cdf, low, upp, step);
        NewtonRaphsonSolver zeroSolver = new NewtonRaphsonSolver();

        double timeTick = step.doubleValue();
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

        bodyParameters.put("lambda", BigDecimal.valueOf(Math.max(bodyLambda, 0.5)));
        bodyParameters.put("delta", supportValues.get("body").get("start"));


        // Handling Tail
        Map<String, BigDecimal> tailParameters = new HashMap<>();
        double tailLambda = Double.MAX_VALUE;

        for(int i = supportIndices.get("tail").get("start").intValue() + 1; i < supportIndices.get("tail").get("end").intValue(); i++){
            double cdfValue = ((i != 0 ? cdf[i - 1] : cdf[i]) - (1 - supportWeight.get("tail").doubleValue())) / supportWeight.get("tail").doubleValue();
            cdfValue = BigDecimal.valueOf(cdfValue).setScale(3, RoundingMode.HALF_DOWN).doubleValue();


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

    @Override
    public StochasticTransitionFeature getApproximatedStochasticTransitionFeature(double[] cdf, double low, double upp, BigDecimal step) {
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

        return StochasticTransitionFeature.of(new PartitionedGEN(distributionPieces));
    }

    @Override
    public ArrayList<StochasticTransitionFeature> getApproximatedStochasticTransitionFeatures(double[] cdf, double low, double upp, BigDecimal step) {
        ArrayList<StochasticTransitionFeature> features = new ArrayList<>();

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

            features.add(StochasticTransitionFeature.newExponentialInstance(BigDecimal.valueOf(tailLambda)));

            stochasticTransitionFeatureWeights().add(BigDecimal.valueOf(1.));
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

            features.add(StochasticTransitionFeature.newExpolynomial(
                    bodyLambda * Math.exp(bodyLambda * delta) / (1 - Math.exp(-bodyLambda * (Q3 - delta))) + " * Exp[-" + bodyLambda + " x]",
                    new OmegaBigDecimal(String.valueOf(delta)),
                    new OmegaBigDecimal(String.valueOf(Q3))
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

            features.add(StochasticTransitionFeature.newExponentialInstance(BigDecimal.valueOf(tailLambda)));

            stochasticTransitionFeatureWeights().add(BigDecimal.valueOf(0.75));
            stochasticTransitionFeatureWeights().add(BigDecimal.valueOf(0.25));
        }

        return features;
    }
}
