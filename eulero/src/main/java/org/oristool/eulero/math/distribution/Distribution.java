package org.oristool.eulero.math.distribution;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public abstract class Distribution {
    private String name;
    private BigDecimal low;
    private BigDecimal upp;

    public Distribution(String name, BigDecimal low, BigDecimal upp) {
        this.name = name;
        this.low = low;
        this.upp = upp;
    }

    public String getName(){
        return name;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getUpp() {
        return upp;
    }

    public double[] getCDFasArray(BigDecimal step) {
        int min = low.divide(step, MathContext.DECIMAL128)
                .setScale(0, RoundingMode.HALF_UP).intValue();

        int max = upp.divide(step, MathContext.DECIMAL128)
                .setScale(0, RoundingMode.HALF_UP).intValue();

        double[] cdf = new double[max - min - 1];

        for(int i = 0; i < cdf.length; i++) {
            BigDecimal evaluationPoint = BigDecimal.valueOf(min + i).multiply(step);
            cdf[i] = cumulativeDensityFunction(evaluationPoint).doubleValue();
        }

        return cdf;
    }

    public abstract BigDecimal getMean();
    public abstract BigDecimal getVariance();
    public abstract BigDecimal cumulativeDensityFunction(BigDecimal t);
}
