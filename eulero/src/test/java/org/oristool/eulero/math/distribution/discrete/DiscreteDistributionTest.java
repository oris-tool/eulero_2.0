package org.oristool.eulero.math.distribution.discrete;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

public class DiscreteDistributionTest {
    @Test
    public void testCDF(){
        ArrayList<BigDecimal> histogramValues = new ArrayList<>(Arrays.asList(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.4), BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.1)));
        BigDecimal low = new BigDecimal(2);
        BigDecimal upp = new BigDecimal(5);

        HistogramDistribution d = new HistogramDistribution("TestName", low, upp, histogramValues);

        Assertions.assertEquals(1, d.cumulativeDensityFunction(BigDecimal.valueOf(5.2)).doubleValue(), 0.0001);
        Assertions.assertEquals(0.3, d.cumulativeDensityFunction(BigDecimal.valueOf(2.7)).doubleValue(), 0.0001);
    }
}
