package org.oristool.eulero.math.approximation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class HistogramApproximatorTest {

    @Test
    void getMinimaTest(){
        BigDecimal low = new BigDecimal(2.03687);
        BigDecimal upp = new BigDecimal(21.3148);
        ArrayList<BigDecimal> histogramValues = new ArrayList<>(
                Arrays.asList(BigDecimal.valueOf(0.0216004), BigDecimal.valueOf(0.250038), BigDecimal.valueOf(0.061551),
                        BigDecimal.valueOf(0.000300005), BigDecimal.valueOf(0.0110669), BigDecimal.valueOf(0.118402),
                        BigDecimal.valueOf(0.170136), BigDecimal.valueOf(0.0505008), BigDecimal.valueOf(0.0436341),
                        BigDecimal.valueOf(0.0741512), BigDecimal.valueOf(0.0890015), BigDecimal.valueOf(0.0654678),
                        BigDecimal.valueOf(0.0321672), BigDecimal.valueOf(0.00938349), BigDecimal.valueOf(0.00235004),
                        BigDecimal.valueOf(0.000250004)));

        HistogramDistribution histogram = new HistogramDistribution("TestHistogram", low, upp, histogramValues);
        HistogramApproximator analyzer = new EXPMixtureApproximation();

        Assertions.assertEquals(0.000249, analyzer.getMinima(histogram, analyzer.getMaximaIndices(histogram)).get(1).doubleValue(), 0.0001);
        Assertions.assertEquals(0.036215, analyzer.getMinima(histogram, analyzer.getMaximaIndices(histogram)).get(2).doubleValue(), 0.0001);
        Assertions.assertEquals(3, analyzer.getMinimaIndices(histogram, analyzer.getMaximaIndices(histogram)).get(1).intValue());
        Assertions.assertEquals(8, analyzer.getMinimaIndices(histogram, analyzer.getMaximaIndices(histogram)).get(2).intValue());
    }

    @Test
    void getMaximaTest(){
        BigDecimal low = new BigDecimal(2.03687);
        BigDecimal upp = new BigDecimal(21.3148);
        ArrayList<BigDecimal> histogramValues = new ArrayList<>(
                Arrays.asList(BigDecimal.valueOf(0.0216004), BigDecimal.valueOf(0.250038), BigDecimal.valueOf(0.061551),
                        BigDecimal.valueOf(0.000300005), BigDecimal.valueOf(0.0110669), BigDecimal.valueOf(0.118402),
                        BigDecimal.valueOf(0.170136), BigDecimal.valueOf(0.0505008), BigDecimal.valueOf(0.0436341),
                        BigDecimal.valueOf(0.0741512), BigDecimal.valueOf(0.0890015), BigDecimal.valueOf(0.0654678),
                        BigDecimal.valueOf(0.0321672), BigDecimal.valueOf(0.00938349), BigDecimal.valueOf(0.00235004),
                        BigDecimal.valueOf(0.000250004)));

        HistogramDistribution histogram = new HistogramDistribution("TestHistogram", low, upp, histogramValues);
        HistogramApproximator analyzer = new EXPMixtureApproximation(BigInteger.valueOf(3));

        Assertions.assertEquals(0.207522, analyzer.getMaxima(histogram).get(0).doubleValue(), 0.0001);
        Assertions.assertEquals(0.141207, analyzer.getMaxima(histogram).get(1).doubleValue(), 0.0001);
        Assertions.assertEquals(0.073868, analyzer.getMaxima(histogram).get(2).doubleValue(), 0.0001);
        Assertions.assertEquals(1, analyzer.getMaximaIndices(histogram).get(0).intValue());
        Assertions.assertEquals(6, analyzer.getMaximaIndices(histogram).get(1).intValue());
        Assertions.assertEquals(10, analyzer.getMaximaIndices(histogram).get(2).intValue());
    }

    @Test
    void getApproximationSupportTest(){
        BigDecimal low = new BigDecimal(2.03687);
        BigDecimal upp = new BigDecimal(21.3148);
        ArrayList<BigDecimal> histogramValues = new ArrayList<>(
                Arrays.asList(BigDecimal.valueOf(0.0216004), BigDecimal.valueOf(0.250038), BigDecimal.valueOf(0.061551),
                        BigDecimal.valueOf(0.000300005), BigDecimal.valueOf(0.0110669), BigDecimal.valueOf(0.118402),
                        BigDecimal.valueOf(0.170136), BigDecimal.valueOf(0.0505008), BigDecimal.valueOf(0.0436341),
                        BigDecimal.valueOf(0.0741512), BigDecimal.valueOf(0.0890015), BigDecimal.valueOf(0.0654678),
                        BigDecimal.valueOf(0.0321672), BigDecimal.valueOf(0.00938349), BigDecimal.valueOf(0.00235004),
                        BigDecimal.valueOf(0.000250004)));

        HistogramDistribution histogram = new HistogramDistribution("TestHistogram", low, upp, histogramValues);
        EXPMixtureApproximation approximator = new EXPMixtureApproximation();
        ArrayList<Map<String, BigDecimal>> approximationSupports = approximator.getApproximationSupports(histogram);

        Assertions.assertEquals(3.13765, approximationSupports.get(0).get("start").doubleValue(), 0.0001);
        Assertions.assertEquals(5.98751, approximationSupports.get(0).get("end").doubleValue(), 0.0001);

        Assertions.assertEquals(8.34709, approximationSupports.get(1).get("start").doubleValue(), 0.0001);
        Assertions.assertEquals(12.491, approximationSupports.get(1).get("end").doubleValue(), 0.0001);

        Assertions.assertEquals(12.491, approximationSupports.get(2).get("start").doubleValue(), 0.0001);
        // Test POSITIVE_INFINITY on the last support b value.
    }
}