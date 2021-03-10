package org.oristool.eulero.math.distribution.discrete;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;

/* Histogram must be already normalized */
public class HistogramDistribution extends DiscreteDistribution {
    private BigDecimal low;
    private BigDecimal upp;
    private ArrayList<BigDecimal> histogramValues;
    private BigInteger binsNumber;


    public HistogramDistribution(String name, BigDecimal low, BigDecimal upp, ArrayList<BigDecimal> histogramValues) {
        super(name);
        this.low = low;
        this.upp = upp;
        this.histogramValues = roundingUpValues(histogramValues, 6);
        this.binsNumber = BigInteger.valueOf(histogramValues.size());
        computeXValues();
    }

    public BigDecimal probabilityMassFunction(BigInteger n, DiscreteHelpers.HistogramType histogramType) {
        switch(histogramType){
            case PDF:
                double pmfValue = histogramValues.get(n.intValue()).doubleValue() * binsNumber.doubleValue() / (upp.doubleValue() - low.doubleValue());
                return  BigDecimal.valueOf(pmfValue);

            default:
                return histogramValues.get(n.intValue());
        }
    }

    public BigDecimal probabilityMassFunction(BigInteger n) {
        return probabilityMassFunction(n, DiscreteHelpers.HistogramType.RELATIVE_COUNT);
    }

    public BigInteger getBinsNumber() {
        return binsNumber;
    }

    public ArrayList<BigDecimal> getHistogramValues(DiscreteHelpers.HistogramType histogramType) {
        switch(histogramType){
            case PDF:
                ArrayList<BigDecimal> PDFHistogramValues = new ArrayList<>();

                for (BigDecimal value: histogramValues) {
                    PDFHistogramValues.add(BigDecimal.valueOf(value.doubleValue() * binsNumber.doubleValue() / (upp.doubleValue() - low.doubleValue())));
                }
                return PDFHistogramValues;

            default:
                return histogramValues;
        }
    }

    public ArrayList<BigDecimal> getHistogramValues() {
        return getHistogramValues(DiscreteHelpers.HistogramType.RELATIVE_COUNT);
    }

    public ArrayList<BigDecimal> getCDFHistogramValues() {
        ArrayList<BigDecimal> cdfHistogramValues = new ArrayList<>();
        double histogramValuesSum = 0;

        for(int i = 0; i < binsNumber.intValue(); i++){
            histogramValuesSum += histogramValues.get(i).doubleValue();
        }

        for(int i = 0; i < binsNumber.intValue(); i++){
            double partialSum = 0;

            for(int j = 0; j <=i; j++){
                partialSum += histogramValues.get(j).doubleValue();
            }

            cdfHistogramValues.add(BigDecimal.valueOf(partialSum / histogramValuesSum));
        }

        return cdfHistogramValues;
    }

    public BigDecimal getUpp() {
        return upp;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void computeXValues() {
        for (int i = 0; i < histogramValues.size(); i++) {
            double xValue = low.doubleValue() + i * (upp.doubleValue() - low.doubleValue()) / binsNumber.doubleValue();
            getXValues().add(BigDecimal.valueOf(xValue));
        }
    }

    public ArrayList<BigDecimal> roundingUpValues(ArrayList<BigDecimal> values, int roundingScale){
        ArrayList<BigDecimal> roundedValues = new ArrayList<>();

        for(BigDecimal value: values){
            roundedValues.add(value.setScale(roundingScale, RoundingMode.HALF_UP));
        }

        return roundedValues;
    }
}