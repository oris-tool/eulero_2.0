package org.oristool.eulero.math.distribution.continuous;

import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.integration.UnivariateIntegrator;

import java.math.BigDecimal;

public class ShiftedTruncatedExponentialDistribution extends ContinuousDistribution{
    private BigDecimal lambda;

    public ShiftedTruncatedExponentialDistribution(String name, BigDecimal low, BigDecimal upp, BigDecimal lambda, UnivariateIntegrator integrator) {
        super(name, low, upp, integrator);
        this.lambda = lambda;
    }

    public ShiftedTruncatedExponentialDistribution(String name, BigDecimal low, BigDecimal upp, BigDecimal lambda) {
        this(name, low, upp, lambda, new SimpsonIntegrator());
    }

    public BigDecimal probabilityDensityFunction(BigDecimal t) {
        if(t.compareTo(getLow()) == -1 || t.compareTo(getUpp()) == 1){
            return BigDecimal.valueOf(0);
        }

        return BigDecimal.valueOf(lambda.doubleValue() * Math.exp(-lambda.doubleValue() * (t.doubleValue() - getLow().doubleValue()))
                / (1 - Math.exp(-lambda.doubleValue() * (getUpp().doubleValue() - getLow().doubleValue()))));
    }
}
