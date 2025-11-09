package org.oristool.eulero.modeling.stochastictime;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.eulero.math.Continuous;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "Histogram")
public class HistogramTime extends StochasticTime {
    private Integer bins;
    @XmlElementWrapper(name = "values")
    @XmlElement(name = "value", required = true)
    private ArrayList<Double> values;

    public HistogramTime(){}
    public HistogramTime(Double EFT, Double LFT, Integer bins, ArrayList<Double> values){
        super(BigDecimal.valueOf(EFT), BigDecimal.valueOf(LFT));
        this.bins = bins;
        this.values = values;
    }
    @Override
    public StochasticTransitionFeature getStochasticTransitionFeature() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime clone() {
        return new HistogramTime(this.getEFT().doubleValue(), this.getLFT().doubleValue(), this.bins, this.values);
    }

    @Override
    public void randomizeParameters() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public Integer bins() {
        return bins;
    }

    public ArrayList<Double> values() {
        return values;
    }

    public double getNormalizationFactor(){
        double normalize = 0;
        for(Double v: values){
            normalize += v;
        }

        return normalize;
    }

    public List<StochasticTransitionFeature> getStochasticTransitionFeatures() {
        ArrayList<StochasticTransitionFeature> features = new ArrayList<>();
        for(int i = 0; i < bins; i++){
            features.add(StochasticTransitionFeature.newDeterministicInstance(BigDecimal.valueOf(getEFT().doubleValue() + i * (getLFT().doubleValue() - getEFT().doubleValue()) / (double) bins)));
        }
        return features;
    }
    public List<BigDecimal> getWeights() {
        ArrayList<BigDecimal> weights = new ArrayList<>();
        Double normalizationFactor = getNormalizationFactor();
        for(int i = 0; i < bins; i++){
            weights.add(BigDecimal.valueOf(values.get(i) / normalizationFactor));
        }
        return weights;
    }

    @Override
    public Continuous time2QueuingEulero() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime computeJobSizeLinear(double resources) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime computeJobSizeInhomogeneousLinear(double resources, double p) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime computeJobSizePiecewiseLinear(double resources, double Rmax) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public double getExpectedValue() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public double getVariance() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public double PDF(double t) {
        if(t < getEFT().doubleValue() || t >= getLFT().doubleValue()){
            return 0.0;
        }
        int interval = (int) ((t - getEFT().doubleValue()) / ((getLFT().doubleValue() - getEFT().doubleValue()) / bins));
        return values.get(interval) * (getLFT().doubleValue() - getEFT().doubleValue()) / bins;
    }

    @Override
    public double CDF(double t) {
        if(t < getEFT().doubleValue()){
            return 0.0;
        }

        if(t > getLFT().doubleValue()){
            return 1.0;
        }
        int interval = (int) ((t - getEFT().doubleValue())/ ((getLFT().doubleValue() - getEFT().doubleValue()) / bins));
        return values.subList(0, interval).stream().mapToDouble(x -> x / getNormalizationFactor()).sum();
    }
}
