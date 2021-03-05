package org.oristool.eulero.math.approximation;

import org.oristool.eulero.math.distribution.discrete.DiscreteHelpers;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class HistogramApproximator {
    private BigInteger neighbourhoodHalfSize;

    public HistogramApproximator(BigInteger neighbourhoodHalfSize){
        this.neighbourhoodHalfSize = neighbourhoodHalfSize;
    };

    public HistogramApproximator(){
        this(BigInteger.valueOf(3));
    };

    public ArrayList<BigInteger> getMaximaIndices(HistogramDistribution histogram){
        // Note that this is a naive solution
        ArrayList<BigInteger> maximaIndex = new ArrayList<>();
        ArrayList<BigDecimal> histogramValues =  histogram.getHistogramValues(DiscreteHelpers.HistogramType.PDF);

        for (BigDecimal element: histogramValues) {
            boolean acceptValue = true;
            int startingComparisonIndex = histogramValues.indexOf(element) - neighbourhoodHalfSize.intValue();
            int endingComparisonIndex = histogramValues.indexOf(element) + neighbourhoodHalfSize.intValue();

            for(int i = Math.max(0, startingComparisonIndex); i < Math.min(histogramValues.size(), endingComparisonIndex); i++) {
                if(element.compareTo(histogramValues.get(i)) == -1){
                    acceptValue = false;
                }
            }

            if (acceptValue){
                maximaIndex.add(BigInteger.valueOf(histogramValues.indexOf(element)));
            }
        }
        return maximaIndex;
    }

    public ArrayList<BigDecimal> getMaxima(HistogramDistribution histogram){
        // Note that this is a naive solution
        ArrayList<BigDecimal> maxima = new ArrayList<>();
        ArrayList<BigDecimal> histogramValues =  histogram.getHistogramValues(DiscreteHelpers.HistogramType.PDF);

        for (BigDecimal element: histogramValues) {
            boolean acceptValue = true;
            int startingComparisonIndex = histogramValues.indexOf(element) - neighbourhoodHalfSize.intValue();
            int endingComparisonIndex = histogramValues.indexOf(element) + neighbourhoodHalfSize.intValue();

            for(int i = Math.max(0, startingComparisonIndex); i < Math.min(histogramValues.size(), endingComparisonIndex); i++) {
                if(element.compareTo(histogramValues.get(i)) == -1){
                    acceptValue = false;
                }
            }

            if (acceptValue){
                maxima.add(element);
            }
        }
        return maxima;
    }

    public ArrayList<BigInteger> getMinimaIndices(HistogramDistribution histogram){
        // Note that this is a naive solution
        ArrayList<BigInteger> minimaIndex = new ArrayList<>();
        ArrayList<BigDecimal> histogramValues =  histogram.getHistogramValues(DiscreteHelpers.HistogramType.PDF);

        for (BigDecimal element: histogramValues) {
            boolean acceptValue = true;
            int startingComparisonIndex = histogramValues.indexOf(element) - neighbourhoodHalfSize.intValue();
            int endingComparisonIndex = histogramValues.indexOf(element) + neighbourhoodHalfSize.intValue();

            for(int i = Math.max(0, startingComparisonIndex); i < Math.min(histogramValues.size(), endingComparisonIndex); i++) {
                if(element.compareTo(histogramValues.get(i)) == 1){
                    acceptValue = false;
                }
            }

            if (acceptValue){
                minimaIndex.add(BigInteger.valueOf(histogramValues.indexOf(element)));
            }
        }
        return minimaIndex;
    }

    public ArrayList<BigDecimal> getMinima(HistogramDistribution histogram){
        // Note that this is a naive solution
        // TODO TBD - should we drop the first/last values? since they are at the end of the support
        ArrayList<BigDecimal> minima = new ArrayList<>();
        ArrayList<BigDecimal> histogramValues =  histogram.getHistogramValues(DiscreteHelpers.HistogramType.PDF);

        for (BigDecimal element: histogramValues) {
            boolean acceptValue = true;
            int startingComparisonIndex = histogramValues.indexOf(element) - neighbourhoodHalfSize.intValue();
            int endingComparisonIndex = histogramValues.indexOf(element) + neighbourhoodHalfSize.intValue();

            for(int i = Math.max(0, startingComparisonIndex); i < Math.min(histogramValues.size(), endingComparisonIndex); i++) {
                if(element.compareTo(histogramValues.get(i)) == 1){
                    acceptValue = false;
                }
            }

            if (acceptValue){
                minima.add(element);
            }
        }
        return minima;
    }

    public ArrayList<Map<String, BigDecimal>> getApproximationSupports(HistogramDistribution histogram) {
        ArrayList<Map<String, BigDecimal>> approximationSupports = new ArrayList<>();

        // Getting relevant values to perform subSupport index calculation
        ArrayList<BigInteger> histogramMaximaIndices = this.getMaximaIndices(histogram);
        ArrayList<BigDecimal> histogramMaxima = this.getMaxima(histogram);

        ArrayList<BigDecimal> histogramMaximaAbscissa = new ArrayList<>();
        for(BigInteger index: histogramMaximaIndices){
            histogramMaximaAbscissa.add(histogram.getXValues().get(index.intValue()));
        }

        ArrayList<BigDecimal> histogramMaximaCDF = new ArrayList<>();
        for(BigInteger index: histogramMaximaIndices){
            histogramMaximaCDF.add(histogram.cumulativeDensityFunction(histogram.getXValues().get(index.intValue())));
        }

        ArrayList<BigInteger> histogramMinimaIndices = this.getMinimaIndices(histogram);

        ArrayList<BigDecimal> histogramMinimaCDF = new ArrayList<>();
        for(BigInteger index: histogramMinimaIndices){
            histogramMinimaCDF.add(histogram.cumulativeDensityFunction(histogram.getXValues().get(index.intValue())));
        }

        // Performing Calculation
        for(int i = 0; i < histogramMaxima.size() - 1; i++){
            BigDecimal start = BigDecimal.valueOf(
                    (
                            histogramMaxima.get(i).doubleValue() * histogramMaximaAbscissa.get(i).doubleValue()
                                    -
                                    histogramMaximaCDF.get(i).doubleValue() + histogramMinimaCDF.get(i).doubleValue()
                    ) / histogramMaxima.get(i).doubleValue());

            BigDecimal end = BigDecimal.valueOf(
                    (
                            histogramMaxima.get(i + 1).doubleValue() * histogramMaximaAbscissa.get(i + 1).doubleValue()
                                    -
                                    histogramMaximaCDF.get(i + 1).doubleValue() + histogramMinimaCDF.get(i).doubleValue()
                    ) / histogramMaxima.get(i + 1).doubleValue());

            Map<String, BigDecimal> supportValues = new HashMap<>();
            supportValues.put("start", start);
            supportValues.put("end", end);

            approximationSupports.add(supportValues);

        }

        // computing last support which will have Infinite as support
        BigDecimal start = BigDecimal.valueOf(
                (
                        histogramMaxima.get(histogramMaxima.size() - 1).doubleValue() * histogramMaximaAbscissa.get(histogramMaxima.size() - 1).doubleValue()
                                -
                                histogramMaximaCDF.get(histogramMaxima.size() - 1).doubleValue() + histogramMinimaCDF.get(histogramMaxima.size() - 1).doubleValue()
                ) / histogramMaxima.get(histogramMaxima.size() - 1).doubleValue());

        Map<String, BigDecimal> supportValues = new HashMap<>();
        supportValues.put("start", start);
        supportValues.put("end", new BigDecimal(Double.MAX_VALUE));
        approximationSupports.add(supportValues);

        //Checking consistency between i-th b value and (i+1)-th value
        for(int i = 0; i < approximationSupports.size() - 1; i++){
            if(approximationSupports.get(i).get("end").doubleValue() <= approximationSupports.get(i).get("start").doubleValue()){
                approximationSupports.get(i).replace("end", approximationSupports.get(i + 1).get("start"));
            }
        }

        return approximationSupports;
    }

    public ArrayList<Map<String, BigInteger>> getApproximationSupportsBoundingIndices(HistogramDistribution histogram, ArrayList<Map<String, BigDecimal>> approximationSupports){
        ArrayList<Map<String, BigInteger>> indices = new ArrayList<>();

        for (int i = 0; i < approximationSupports.size(); i++) {
            double start = approximationSupports.get(i).get("start").doubleValue();
            double end = approximationSupports.get(i).get("end").doubleValue();

            int supportStartingIndex = histogram.getXValues().indexOf(histogram.getXValues().stream().filter(value -> value.doubleValue() >= start ).findFirst().get());
            int supportEndingIndex = i != approximationSupports.size() - 1 ?
                    histogram.getXValues().indexOf(histogram.getXValues().stream().filter(value -> value.doubleValue() > end ).findFirst().get()) - 1
                    : histogram.getXValues().size() - 1;

            Map<String, BigInteger> index = new HashMap<>();
            index.put("start", BigInteger.valueOf(supportStartingIndex));
            index.put("end", BigInteger.valueOf(supportEndingIndex));

            indices.add(index);
        }

        return indices;
    }

    public ArrayList<BigDecimal> getApproximationSupportsWeight(HistogramDistribution histogram, ArrayList<Map<String, BigDecimal>> approximationSupports){
        ArrayList<BigDecimal> supportWeights = new ArrayList<>();
        ArrayList<Map<String, BigInteger>> supportsIndices = getApproximationSupportsBoundingIndices(histogram, approximationSupports);

        for (int i = 0; i < approximationSupports.size(); i++) {
            double weight = i != 0 ?
                    (i != approximationSupports.size() - 1 ? histogram.getCDFHistogramValues().get(supportsIndices.get(i).get("end").intValue() - 1).doubleValue() : 1 ) -
                            histogram.getCDFHistogramValues().get(supportsIndices.get(i - 1).get("end").intValue() - 1).doubleValue()
                    : histogram.getCDFHistogramValues().get(supportsIndices.get(i).get("end").intValue() - 1).doubleValue();

            supportWeights.add(BigDecimal.valueOf(weight));
        }

        return  supportWeights;
    }

    public ArrayList<ApproximationSupportSetup> getApproximationSupportSetups(HistogramDistribution histogram){
        ArrayList<ApproximationSupportSetup> setups = new ArrayList<>();
        ArrayList<Map<String, BigDecimal>> approximationSupports = getApproximationSupports(histogram);
        ArrayList<BigDecimal> weights = getApproximationSupportsWeight(histogram, approximationSupports);
        ArrayList<Map<String, BigDecimal>> params = getApproximationParameters(histogram, approximationSupports);

        for(int i = 0; i < approximationSupports.size(); i++){
            ApproximationSupportSetup setup = new ApproximationSupportSetup(weights.get(i), approximationSupports.get(i), params.get(i));
            setups.add(setup);
        }

        return setups;
    }

    public abstract ArrayList<Map<String, BigDecimal>> getApproximationParameters(HistogramDistribution histogram, ArrayList<Map<String, BigDecimal>> approximationSupports);

    public class ApproximationSupportSetup {
        private BigDecimal weight;
        private Map<String, BigDecimal> support;
        private Map<String, BigDecimal> parameters;

        public ApproximationSupportSetup(BigDecimal weight, Map<String, BigDecimal> support, Map<String, BigDecimal> parameters){
            this.weight = weight;
            this.support = support;
            this.parameters = parameters;
        }

        public BigDecimal getWeight() {
            return weight;
        }

        public Map<String, BigDecimal> getSupport() {
            return support;
        }

        public Map<String, BigDecimal> getParameters() {
            return parameters;
        }
    }
}
