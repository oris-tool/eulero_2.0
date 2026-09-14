package org.oristool.eulero.modeling.stochastictime;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.eulero.math.Continuous;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;
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
    public String toString() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public HistogramTime clone() {
        HistogramTime clone = (HistogramTime)super.clone();
        clone.bins = this.bins;
        clone.values = this.values;
        return clone;
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

    @Override
    public StochasticTransitionFeature getStochasticTransitionFeature() {
        double eft = getEFT().doubleValue();
        double w   = (getLFT().doubleValue() - eft) / bins;
        double N   = getNormalizationFactor();

        List<GEN> pieces = new ArrayList<>();
        for (int i = 0; i < bins; i++) {
            double h = (values.get(i) / N) / w;
            double a = eft + i * w;
            double b = eft + (i + 1) * w;
            pieces.add(GEN.newExpolynomial(
                    String.valueOf(h),
                    new OmegaBigDecimal(BigDecimal.valueOf(a)),
                    new OmegaBigDecimal(BigDecimal.valueOf(b))));
        }
        return StochasticTransitionFeature.of(new PartitionedGEN(pieces));
    }

    @Override
    public List<StochasticTransitionFeature> getStochasticTransitionFeatures() {
        return List.of(getStochasticTransitionFeature());       // caso size==1 in buildSTPN
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
        double eft = getEFT().doubleValue();
        double lft = getLFT().doubleValue();
        if(t < eft || t >= lft){
            return 0.0;
        }
        double w = (lft - eft) / bins;
        int interval = (int) ((t - eft) / w);
        return (values.get(interval) / getNormalizationFactor()) / w;
    }

    @Override
    public double CDF(double t) {
        double eft = getEFT().doubleValue();
        double lft = getLFT().doubleValue();
        if(t < eft){
            return 0.0;
        }
        if(t >= lft){
            return 1.0;
        }
        double N = getNormalizationFactor();
        double w = (lft - eft) / bins;
        int interval = (int) ((t - eft) / w);
        double cumulativeBefore = values.subList(0, interval).stream()
                .mapToDouble(x -> x / N).sum();
        double binStart = eft + interval * w;
        double partial = (values.get(interval) / N) * (t - binStart) / w;
        return cumulativeBefore + partial;
    }
    @Override
    public StochasticTimeType getType() {
        return StochasticTimeType.HISTOGRAM;
    }
}
