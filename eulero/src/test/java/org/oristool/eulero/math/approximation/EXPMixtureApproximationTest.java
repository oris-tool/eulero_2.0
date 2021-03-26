package org.oristool.eulero.math.approximation;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oristool.eulero.math.distribution.continuous.ShiftedExponentialDistribution;
import org.oristool.eulero.math.distribution.continuous.ShiftedTruncatedExponentialDistribution;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class EXPMixtureApproximationTest {

    @Test
    void getApproximationParametersTest(){
        NormalDistribution distribution = new NormalDistribution(9, 2.5);
        double low = 0;
        double upp = 20;
        double timeTick = 0.01;
        double[] cdf = new double[(int) ((upp - low) / timeTick)];
        for(int i = 0; i < cdf.length; i++){
            cdf[i] = distribution.cumulativeProbability(low + i * timeTick);
        }

        EXPMixtureApproximation approximator = new EXPMixtureApproximation();
        Map<String, Map<String, BigDecimal>> approximationParameters = approximator.getApproximationParameters(cdf, low, upp);

        double prova = 0;
        // timeTick is multiplied by a factor 3 in the allowed error, due to the two ways discretization step in computin Tukeys Bounds
        Assertions.assertEquals(0.1054, approximationParameters.get("body").get("lambda").doubleValue(), 0.05);
        Assertions.assertEquals(1.20229, approximationParameters.get("tail").get("lambda").doubleValue(), 0.1);

    }

    @Test
    void getApproximationStochasticallyOrderedTest(){
        NormalDistribution distribution = new NormalDistribution(9, 2.5);
        double low = 0;
        double upp = 20;
        double timeTick = 0.01;
        double[] cdf = new double[(int) ((upp - low) / timeTick)];
        for(int i = 0; i < cdf.length; i++){
            cdf[i] = distribution.cumulativeProbability(low + i * timeTick);
        }

        EXPMixtureApproximation approximator = new EXPMixtureApproximation();
        Map<String, Approximator.ApproximationSupportSetup> setups = approximator.getApproximationSupportSetups(cdf, low, upp);

        ShiftedTruncatedExponentialDistribution body = new ShiftedTruncatedExponentialDistribution("body", setups.get("body").getSupport().get("start"),
                setups.get("body").getSupport().get("end"), setups.get("body").getParameters().get("lambda"));
        BigDecimal bodyWeight = setups.get("body").getWeight();

        ShiftedExponentialDistribution tail = new ShiftedExponentialDistribution("D2", setups.get("tail").getSupport().get("start"),
                setups.get("tail").getParameters().get("lambda"));
        BigDecimal tailWeight = setups.get("tail").getWeight();

        for(int i = 0; i < cdf.length; i++){
            Assertions.assertTrue(bodyWeight.doubleValue() * tail.cumulativeDensityFunction(BigDecimal.valueOf(low + i * timeTick)).doubleValue() +
                    tailWeight.doubleValue() * tail.cumulativeDensityFunction(BigDecimal.valueOf(low + i * timeTick)).doubleValue()
                    <= cdf[i]);
        }
    }
}
