package org.oristool.eulero.modeling.stochastictime;

import jakarta.xml.bind.annotation.*;
import org.apache.commons.lang3.ArrayUtils;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class StochasticTime implements Cloneable{
    @XmlElements({
            @XmlElement(name = "type", required = true)
    })
    private SIRIOType type;
    private BigDecimal EFT;
    private BigDecimal LFT;

    public StochasticTime(){}

    public StochasticTime(BigDecimal EFT, BigDecimal LFT, SIRIOType type){
        this.EFT = EFT;
        this.LFT = LFT;
        this.type = type;
    }

    public SIRIOType getType() {
        return type;
    }

    public abstract StochasticTransitionFeature getStochasticTransitionFeature();
    public abstract List<StochasticTransitionFeature> getStochasticTransitionFeatures();
    public abstract List<BigDecimal> getWeights();

    public BigDecimal getEFT() {
        return EFT;
    }

    public void setEFT(BigDecimal EFT) {
        this.EFT = EFT;
    }

    public BigDecimal getLFT() {
        return LFT;
    }

    public void setLFT(BigDecimal LFT) {
        this.LFT = LFT;
    }

    public double[] getNumericalPDF(double step, double limit){
        int counter = 0;
        ArrayList<Double> pdf = new ArrayList<>();
        while(counter * step <= limit){
            pdf.add(PDF(step * (double) counter));
            counter++;
        }
        return ArrayUtils.toPrimitive(pdf.toArray(new Double[0]));
    }

    public double[] getNumericalCDF(double step, double limit){
        int counter = 0;
        ArrayList<Double> cdf = new ArrayList<>();
        while(BigDecimal.valueOf(counter * step).setScale(BigDecimal.valueOf(step).scale(), RoundingMode.HALF_DOWN).doubleValue() <= limit){
            cdf.add(CDF(step * (double) counter));
            counter++;
        }
        return ArrayUtils.toPrimitive(cdf.toArray(new Double[0]));
    }

    public double[] getSpecularNumericalCDF(double step, double limit){
        double[] cdf = getNumericalCDF(step, limit);
        double[] specularCDF = new double[cdf.length];
        for(int i = 0; i < cdf.length; i++){
            specularCDF[i] = 1 - cdf[i];
        }

        return specularCDF;
    }

    public abstract double getExpectedValue();

    public abstract double PDF(double t);
    public abstract double CDF(double t);

    public abstract String toString();

    @Override
    public abstract StochasticTime clone();
}
