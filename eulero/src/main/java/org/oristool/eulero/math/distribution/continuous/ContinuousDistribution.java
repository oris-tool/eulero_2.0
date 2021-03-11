package org.oristool.eulero.math.distribution.continuous;

import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.integration.UnivariateIntegrator;
import org.oristool.eulero.math.distribution.Distribution;

import java.math.BigDecimal;

public abstract class ContinuousDistribution extends Distribution {
    private final UnivariateIntegrator integrator;

    public ContinuousDistribution(String name, BigDecimal low, BigDecimal upp, UnivariateIntegrator integrator) {
        super(name, low, upp);
        this.integrator = integrator;
    }

    public ContinuousDistribution(String name, BigDecimal low, BigDecimal upp) {
        this(name, low, upp, new SimpsonIntegrator());
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
        if(t.compareTo(getLow()) == -1){
            return BigDecimal.valueOf(0);
        }

        if(t.compareTo(getUpp()) == 1){
            return BigDecimal.valueOf(1);
        }

        double cdfValue = integrator.integrate(1000, x -> probabilityDensityFunction(BigDecimal.valueOf(x)).doubleValue(), getLow().doubleValue(), t.doubleValue());
        return BigDecimal.valueOf(cdfValue);
    }

    public abstract BigDecimal probabilityDensityFunction(BigDecimal t);

    public abstract String getExpolynomialDensityString();

    public abstract BigDecimal getNormalizationFactor();
}
