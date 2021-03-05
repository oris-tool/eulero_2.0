package org.oristool.eulero.math.distribution;

import java.math.BigDecimal;

public abstract class Distribution {
    private String name;

    public Distribution(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public abstract BigDecimal getMean();
    public abstract BigDecimal getVariance();
    public abstract BigDecimal cumulativeDensityFunction(BigDecimal t);
}
