package org.oristool.eulero.math.distribution.discrete;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

public class HistogramDistributionTest {

    @Test
    public void testXValues(){
        ArrayList<BigDecimal> histogramValues = new ArrayList<>(Arrays.asList(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.4), BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.1)));
        BigDecimal low = new BigDecimal(2);
        BigDecimal upp = new BigDecimal(5);

        HistogramDistribution d = new HistogramDistribution("TestName", low, upp, histogramValues);

        Assertions.assertEquals(BigInteger.valueOf(5), d.getBinsNumber());
    }

    @Test
    public void testPMF(){
        ArrayList<BigDecimal> histogramValues = new ArrayList<>(Arrays.asList(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.4), BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.1)));
        BigDecimal low = new BigDecimal(2);
        BigDecimal upp = new BigDecimal(5);

        HistogramDistribution d = new HistogramDistribution("TestName", low, upp, histogramValues);

        Assertions.assertEquals(BigDecimal.valueOf(0.2), d.probabilityMassFunction(BigInteger.valueOf(3)));
    }

    @Test
    public void testPDF(){
        ArrayList<BigDecimal> histogramValues = new ArrayList<>(Arrays.asList(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.4), BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.1)));
        BigDecimal low = new BigDecimal(2);
        BigDecimal upp = new BigDecimal(5);

        HistogramDistribution d = new HistogramDistribution("TestName", low, upp, histogramValues);

        Assertions.assertEquals(BigDecimal.valueOf(0.2 * 5 / 3), d.probabilityMassFunction(BigInteger.valueOf(3), DiscreteHelpers.HistogramType.PDF));
    }


}
