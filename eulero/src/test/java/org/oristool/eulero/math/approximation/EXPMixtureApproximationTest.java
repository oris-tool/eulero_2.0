package org.oristool.eulero.math.approximation;

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
        BigDecimal low = new BigDecimal(1.95088);
        BigDecimal upp = new BigDecimal(21.4594);
        ArrayList<BigDecimal> histogramValues = new ArrayList<>(
                Arrays.asList(BigDecimal.valueOf(0.00223337), BigDecimal.valueOf(0.0635344), BigDecimal.valueOf(0.194387),
                        BigDecimal.valueOf(0.0706512), BigDecimal.valueOf(0.00253338), BigDecimal.valueOf(0.0000666678),
                        BigDecimal.valueOf(0.00321672), BigDecimal.valueOf(0.0284505), BigDecimal.valueOf(0.0976016),
                        BigDecimal.valueOf(0.127585), BigDecimal.valueOf(0.0700178), BigDecimal.valueOf(0.0256171),
                        BigDecimal.valueOf(0.0267171), BigDecimal.valueOf(0.040584), BigDecimal.valueOf(0.0540176),
                        BigDecimal.valueOf(0.059251), BigDecimal.valueOf(0.0528509), BigDecimal.valueOf(0.039234),
                        BigDecimal.valueOf(0.0228504), BigDecimal.valueOf(0.0117002), BigDecimal.valueOf(0.00478341),
                        BigDecimal.valueOf(0.00161669), BigDecimal.valueOf(0.000466674), BigDecimal.valueOf(0.0000333339)));

        HistogramDistribution histogram = new HistogramDistribution("TestHistogram", low, upp, histogramValues);
        EXPMixtureApproximation approximator = new EXPMixtureApproximation();
        ArrayList<Map<String, BigDecimal>> approximationSupports = approximator.getApproximationSupports(histogram);
        ArrayList<Map<String, BigDecimal>> approximationParameters = approximator.getApproximationParameters(histogram, approximationSupports);

        Assertions.assertEquals(0.68905, approximationParameters.get(0).get("lambda").doubleValue(), 0.0001);
        Assertions.assertEquals(0.407221, approximationParameters.get(1).get("lambda").doubleValue(), 0.0001);
        Assertions.assertEquals(0.200454, approximationParameters.get(2).get("lambda").doubleValue(), 0.0001);
    }

    @Test
    void getSupportWeightTest(){
        BigDecimal low = new BigDecimal(1.95088);
        BigDecimal upp = new BigDecimal(21.4594);
        ArrayList<BigDecimal> histogramValues = new ArrayList<>(
                Arrays.asList(BigDecimal.valueOf(0.00223337), BigDecimal.valueOf(0.0635344), BigDecimal.valueOf(0.194387),
                        BigDecimal.valueOf(0.0706512), BigDecimal.valueOf(0.00253338), BigDecimal.valueOf(0.0000666678),
                        BigDecimal.valueOf(0.00321672), BigDecimal.valueOf(0.0284505), BigDecimal.valueOf(0.0976016),
                        BigDecimal.valueOf(0.127585), BigDecimal.valueOf(0.0700178), BigDecimal.valueOf(0.0256171),
                        BigDecimal.valueOf(0.0267171), BigDecimal.valueOf(0.040584), BigDecimal.valueOf(0.0540176),
                        BigDecimal.valueOf(0.059251), BigDecimal.valueOf(0.0528509), BigDecimal.valueOf(0.039234),
                        BigDecimal.valueOf(0.0228504), BigDecimal.valueOf(0.0117002), BigDecimal.valueOf(0.00478341),
                        BigDecimal.valueOf(0.00161669), BigDecimal.valueOf(0.000466674), BigDecimal.valueOf(0.0000333339)));

        HistogramDistribution histogram = new HistogramDistribution("TestHistogram", low, upp, histogramValues);
        EXPMixtureApproximation approximator = new EXPMixtureApproximation();
        ArrayList<Map<String, BigDecimal>> approximationSupports = approximator.getApproximationSupports(histogram);
        ArrayList<BigDecimal> supportWeights = approximator.getApproximationSupportsWeight(histogram, approximationSupports);

        Assertions.assertEquals(0.333339, supportWeights.get(0).doubleValue(), 0.0001);
        Assertions.assertEquals(0.352556, supportWeights.get(1).doubleValue(), 0.0001);
        Assertions.assertEquals(0.314105, supportWeights.get(2).doubleValue(), 0.0001);
        Assertions.assertEquals(1, supportWeights.get(0).doubleValue() + supportWeights.get(1).doubleValue() + supportWeights.get(2).doubleValue(), 0.0001);
    }

    @Test
    void approximationTest(){
        BigDecimal low = new BigDecimal(1.95088);
        BigDecimal upp = new BigDecimal(21.4594);
        ArrayList<BigDecimal> histogramValues = new ArrayList<>(
                Arrays.asList(BigDecimal.valueOf(0.00223337), BigDecimal.valueOf(0.0635344), BigDecimal.valueOf(0.194387),
                        BigDecimal.valueOf(0.0706512), BigDecimal.valueOf(0.00253338), BigDecimal.valueOf(0.0000666678),
                        BigDecimal.valueOf(0.00321672), BigDecimal.valueOf(0.0284505), BigDecimal.valueOf(0.0976016),
                        BigDecimal.valueOf(0.127585), BigDecimal.valueOf(0.0700178), BigDecimal.valueOf(0.0256171),
                        BigDecimal.valueOf(0.0267171), BigDecimal.valueOf(0.040584), BigDecimal.valueOf(0.0540176),
                        BigDecimal.valueOf(0.059251), BigDecimal.valueOf(0.0528509), BigDecimal.valueOf(0.039234),
                        BigDecimal.valueOf(0.0228504), BigDecimal.valueOf(0.0117002), BigDecimal.valueOf(0.00478341),
                        BigDecimal.valueOf(0.00161669), BigDecimal.valueOf(0.000466674), BigDecimal.valueOf(0.0000333339)));

        HistogramDistribution histogram = new HistogramDistribution("TestHistogram", low, upp, histogramValues);
        EXPMixtureApproximation approximator = new EXPMixtureApproximation();
        ArrayList<HistogramApproximator.ApproximationSupportSetup> setups = approximator.getApproximationSupportSetups(histogram);

        ShiftedTruncatedExponentialDistribution exp0 = new ShiftedTruncatedExponentialDistribution("D0", setups.get(0).getSupport().get("start"),
                setups.get(0).getSupport().get("end"), setups.get(0).getParameters().get("lambda"));
        BigDecimal w0 = setups.get(0).getWeight();

        ShiftedTruncatedExponentialDistribution exp1 = new ShiftedTruncatedExponentialDistribution("D1", setups.get(1).getSupport().get("start"),
                setups.get(1).getSupport().get("end"), setups.get(1).getParameters().get("lambda"));
        BigDecimal w1 = setups.get(1).getWeight();

        ShiftedExponentialDistribution exp2 = new ShiftedExponentialDistribution("D2", setups.get(2).getSupport().get("start"),
                setups.get(2).getParameters().get("lambda"));
        BigDecimal w2 = setups.get(2).getWeight();

        double[] domain = new double[(int) Math.round((histogram.getUpp().doubleValue() + 2) /0.1 + 1)];

        for(int i = 0; i < domain.length; i++){
            domain[i] = i * 0.1;
        }

        for (double val: domain) {
            double provalo = w0.doubleValue() * exp0.cumulativeDensityFunction(BigDecimal.valueOf(val)).doubleValue() +
                    w1.doubleValue() * exp1.cumulativeDensityFunction(BigDecimal.valueOf(val)).doubleValue() +
                    w2.doubleValue() * exp2.cumulativeDensityFunction(BigDecimal.valueOf(val)).doubleValue();

            Assertions.assertTrue(w0.doubleValue() * exp0.cumulativeDensityFunction(BigDecimal.valueOf(val)).doubleValue() +
                    w1.doubleValue() * exp1.cumulativeDensityFunction(BigDecimal.valueOf(val)).doubleValue() +
                    w2.doubleValue() * exp2.cumulativeDensityFunction(BigDecimal.valueOf(val)).doubleValue()
                    <= histogram.cumulativeDensityFunction(BigDecimal.valueOf(val)).doubleValue());
        }
    }
}
