package org.oristool.eulero.math.approximation;

import org.oristool.eulero.math.distribution.continuous.LinearEuleroPieceDistribution;
import org.oristool.eulero.math.distribution.continuous.ShiftedExponentialDistribution;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class SplineBodyEXPTailApproximation extends Approximator {
    private int pieces;

    public SplineBodyEXPTailApproximation(int pieces){
        this.pieces = pieces;
    }

    @Override
    public Map<String, Map<String, BigDecimal>> getApproximationSupports(double[] cdf, double low, double upp, BigDecimal step) {
        BigDecimal tukeysUpperBound = ApproximationHelpers.getQuartileBounds(cdf, low, upp).get("upp");
        int tukeysUpperBoundIndex = ApproximationHelpers.getQuartileBoundsIndices(cdf, low, upp).get("upp").intValue();

        double timeTick = step.doubleValue();
        double[] x = new double[tukeysUpperBoundIndex];
        for(int i = 0; i < tukeysUpperBoundIndex; i++){
            x[i] = low + i * timeTick;
        }

        Map<String, Map<String, BigDecimal>> supports = new HashMap<>();
        int pieceLength = (int) tukeysUpperBoundIndex / pieces;

        for(int i = 0; i < this.pieces; i++){
            Map<String, BigDecimal> support = new HashMap<>();
            support.put("start", BigDecimal.valueOf(x[i * pieceLength]));
            support.put("end", BigDecimal.valueOf(
                    i == this.pieces - 1 ? tukeysUpperBound.setScale(step.scale(), RoundingMode.HALF_DOWN).doubleValue() : x[(i + 1) * pieceLength]
            ));

            supports.put("body_" + i, support);
        }

        Map<String, BigDecimal> tailSupport = new HashMap<>();
        tailSupport.put("start", tukeysUpperBound.setScale(step.scale(), RoundingMode.HALF_DOWN));
        tailSupport.put("end", BigDecimal.valueOf(Double.MAX_VALUE));
        supports.put("tail", tailSupport);

        return supports;
    }

    @Override
    public Map<String, Map<String, BigInteger>> getApproximationSupportIndices(double[] cdf, double low, double upp) {
        int tukeysUpperBoundIndex = ApproximationHelpers.getQuartileBoundsIndices(cdf, low, upp).get("upp").intValue();

        Map<String, Map<String, BigInteger>> supports = new HashMap<>();
        int pieceLength = (int) tukeysUpperBoundIndex / pieces;

        for(int i = 0; i < this.pieces; i++){
            Map<String, BigInteger> supportIndices = new HashMap<>();
            supportIndices.put("start", BigInteger.valueOf(i * pieceLength));
            supportIndices.put("end", BigInteger.valueOf(
                    i == this.pieces - 1 ? tukeysUpperBoundIndex - 1 : (i + 1) * pieceLength
            ));

            supports.put("body_" + i, supportIndices);
        }

        Map<String, BigInteger> tailSupport = new HashMap<>();
        tailSupport.put("start", BigInteger.valueOf(tukeysUpperBoundIndex));
        tailSupport.put("end", BigInteger.valueOf(cdf.length - 1));
        supports.put("tail", tailSupport);

        return supports;
    }

    @Override
    public Map<String, BigDecimal> getApproximationSupportsWeight(double[] cdf, double low, double upp, BigDecimal step){
        Map<String, BigDecimal> weights = new HashMap<>();

        Map<String, Map<String, BigInteger>> supportIndices = getApproximationSupportIndices(cdf, low, upp);

        supportIndices.forEach((name, support) -> {
            if(name.equals("tail")){
                weights.put(name, BigDecimal.valueOf(0.25));
            } else {
                weights.put(name, BigDecimal.valueOf(cdf[support.get("end").intValue()] - (support.get("start").intValue() - 1 < 0 ? 0 : cdf[support.get("start").intValue() - 1])));
            }
        });

        return weights;
    }

    @Override
    public Map<String, ApproximationSupportSetup> getApproximationSupportSetups(double[] cdf, double low, double upp, BigDecimal step) {
        Map<String, ApproximationSupportSetup> setups = new HashMap<>();
        Map<String, Map<String, BigDecimal>> approximationSupports = getApproximationSupports(cdf, low, upp, step);
        Map<String, Map<String, BigInteger>> approximationIndexedSupports = getApproximationSupportIndices(cdf, low, upp);
        Map<String, BigDecimal> weights = getApproximationSupportsWeight(cdf, low, upp, step);
        Map<String, Map<String, BigDecimal>> params = getApproximationParameters(cdf, low, upp, step);

        params.forEach((name, parameters) -> {
            if(name.equals("tail")){
                setups.put(name, new ApproximationSupportSetup(weights.get(name), approximationSupports.get(name), parameters,
                        new ShiftedExponentialDistribution(name, approximationSupports.get(name).get("start"), parameters.get("lambda"))));
            } else {
                setups.put(name, new ApproximationSupportSetup(weights.get(name), approximationSupports.get(name), parameters,
                        new LinearEuleroPieceDistribution(name,
                                approximationSupports.get(name).get("start"),
                                approximationSupports.get(name).get("end"),
                                parameters.get("alpha"),
                                weights.get(name),
                                BigDecimal.valueOf(cdf[Math.max(approximationIndexedSupports.get(name).get("start").intValue() - 1, 0)])
                        )
                ));
            }

        });

        return setups;
    }

    @Override
    public Map<String, Map<String, BigDecimal>> getApproximationParameters(double[] cdf, double low, double upp, BigDecimal step) {
        Map<String, Map<String, BigInteger>> supportIndices = getApproximationSupportIndices(cdf, low, upp);
        Map<String, Map<String, BigDecimal>> supportValues = getApproximationSupports(cdf, low, upp, step);
        Map<String, BigDecimal> supportWeight = getApproximationSupportsWeight(cdf, low, upp, step);

        double timeTick = step.doubleValue();
        double[] x = new double[cdf.length];
        for(int i = 0; i < x.length; i++){
            x[i] = BigDecimal.valueOf(low + i * timeTick).setScale(step.scale(), RoundingMode.HALF_DOWN).doubleValue();
        }

        Map<String, Map<String, BigDecimal>> allParameters = new HashMap<>();

        supportIndices.forEach((name, indices) -> {
            Map<String, BigDecimal> parameters = new HashMap<>();

            if(name.equals("tail")){
                double tailLambda = Double.MAX_VALUE;
                for(int i = indices.get("start").intValue() + 1; i < indices.get("end").intValue(); i++){
                    double cdfValue = ((i != 0 ? cdf[i - 1] : cdf[i]) - (1 - supportWeight.get("tail").doubleValue())) / supportWeight.get("tail").doubleValue();
                    cdfValue = (cdfValue < 0) ? 0 : BigDecimal.valueOf(cdfValue).setScale(3, RoundingMode.HALF_DOWN).doubleValue();
                    //TODO Fixme
                    if(cdfValue<1 && x[i]>= supportValues.get("tail").get("start").doubleValue()) {
                        tailLambda = Math.min(
                                tailLambda,
                                -Math.log(1 - cdfValue) / (x[i] - supportValues.get("tail").get("start").doubleValue())
                        );
                    }
                }
                parameters.put("lambda", BigDecimal.valueOf(tailLambda));
                allParameters.put(name, parameters);

            } else {
                double localArea = 0;
                double localMean = 0;

                for (int j = indices.get("start").intValue(); j <= indices.get("end").intValue(); j++){
                    localArea += cdf[j] * timeTick;
                    localMean += cdf[j] * timeTick * x[j];
                }

                localMean = localMean/localArea;

                double x1 = x[Math.max(indices.get("start").intValue()-1,0)];
                double x2 = x[(indices.get("end").intValue())];
                double f1 = cdf[indices.get("start").intValue()];
                double h = x2 - x1;
                double m = (Math.pow(x2, 3)/6 - Math.pow(x1, 3)/6  - (x1 * x2 * h)/2) / h;
                double q = (Math.pow(x2, 3)/6 - Math.pow(x1, 3)/6  - (x1 * x2 * h)/2) * f1 / h -
                        (Math.pow(x2, 3)/3 + Math.pow(x1, 3)/6  - (x1 * x2 * x2)/2) * (2 * localArea) / Math.pow(h, 2);

                double alpha = (localMean * localArea - q) / m;

                if(alpha < -f1){
                    alpha = -f1;
                }
                if(alpha > 2*localArea/h - f1){
                    alpha = 2*localArea/h - f1;
                }

                parameters.put("alpha", BigDecimal.valueOf(alpha));
                allParameters.put(name, parameters);
            }
        });

        return allParameters;
    }
}
