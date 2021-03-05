package org.oristool.eulero.math.distribution.discrete;

import org.oristool.eulero.math.distribution.Distribution;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

public abstract class DiscreteDistribution extends Distribution {
    private ArrayList<BigDecimal> xValues;

    public DiscreteDistribution(String name) {
        super(name);
        xValues = new ArrayList<>();
    }

    public BigDecimal getMean(){
        //TODO
        return null;
    }

    public BigDecimal getVariance() {
        //TODO
        return null;
    }

    public BigDecimal cumulativeDensityFunction(BigDecimal t) {
        double resultValue = 0;

        for (BigDecimal xValue: xValues) {
           if(xValue.compareTo(t) == -1){
               resultValue += probabilityMassFunction(BigInteger.valueOf(xValues.indexOf(xValue))).doubleValue();
           }
        }

        return BigDecimal.valueOf(resultValue);
    }

    public ArrayList<BigDecimal> getXValues(){
        return xValues;
    };

    public abstract BigDecimal probabilityMassFunction(BigInteger n);
    public abstract void computeXValues();
}
