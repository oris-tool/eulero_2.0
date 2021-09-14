package org.oristool.eulero.math.approximation;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

public class ApproximatorTest {
    @Test
    void getApproximationSupportTest(){
        NormalDistribution distribution = new NormalDistribution(9, 2.5);
        double low = 0;
        double upp = 20;
        double timeTick = 0.01;
        double[] cdf = new double[(int) ((upp - low) / timeTick)];
        for(int i = 0; i < cdf.length; i++){
            cdf[i] = distribution.cumulativeProbability(low + i * timeTick);
        }

        EXPMixtureApproximation approximator = new EXPMixtureApproximation();
        Map<String, Map<String, BigDecimal>> approximationSupports = approximator.getApproximationSupports(cdf, low, upp, BigDecimal.valueOf(timeTick));

        // timeTick is multiplied by a factor 3 in the allowed error, due to the two ways discretization step in computin Tukeys Bounds
        Assertions.assertEquals(5.866, approximationSupports.get("body").get("start").doubleValue(), 2 * timeTick);
        Assertions.assertEquals(15.744, approximationSupports.get("body").get("end").doubleValue(), 2 * timeTick);

        Assertions.assertEquals(15.744, approximationSupports.get("tail").get("start").doubleValue(), 2 * timeTick);
        Assertions.assertEquals(Double.MAX_VALUE, approximationSupports.get("tail").get("end").doubleValue(), 0.001);
    }

    @Test
    void getApproximationWeightTest(){
        NormalDistribution distribution = new NormalDistribution(9, 2.5);
        double low = 0;
        double upp = 20;
        double timeTick = 0.01;
        double[] cdf = new double[(int) ((upp - low) / timeTick)];
        for(int i = 0; i < cdf.length; i++){
            cdf[i] = distribution.cumulativeProbability(low + i * timeTick);
        }

        EXPMixtureApproximation approximator = new EXPMixtureApproximation();
         Map<String, BigDecimal> approximationWeights = approximator.getApproximationSupportsWeight(cdf, low, upp, BigDecimal.valueOf(timeTick));

        // timeTick is multiplied by a factor 3 in the allowed error, due to the two ways discretization step in computin Tukeys Bounds
        Assertions.assertEquals(0.996, approximationWeights.get("body").doubleValue(), 2 * timeTick);
        Assertions.assertEquals(1 - 0.996, approximationWeights.get("tail").doubleValue(), 2 * timeTick);
    }
}