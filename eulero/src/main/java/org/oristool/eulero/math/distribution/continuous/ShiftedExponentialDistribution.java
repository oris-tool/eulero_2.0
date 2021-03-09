package org.oristool.eulero.math.distribution.continuous;

import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.integration.UnivariateIntegrator;

import java.math.BigDecimal;

public class ShiftedExponentialDistribution extends ContinuousDistribution{
    private BigDecimal lambda;

    public ShiftedExponentialDistribution(String name, BigDecimal low, BigDecimal lambda, UnivariateIntegrator integrator) {
        super(name, low, BigDecimal.valueOf(Double.MAX_VALUE), integrator);
        this.lambda = lambda;
    }

    public ShiftedExponentialDistribution(String name, BigDecimal low, BigDecimal lambda) {
        this(name, low, lambda, new SimpsonIntegrator());
    }


    @Override
    public BigDecimal probabilityDensityFunction(BigDecimal t) {
        if(t.compareTo(getLow()) == -1 || t.compareTo(getUpp()) == 1){
            return BigDecimal.valueOf(0);
        }

        return BigDecimal.valueOf(lambda.doubleValue() * Math.exp(- lambda.doubleValue() * (t.doubleValue() - getLow().doubleValue())));
    }

    @Override
    public String getExpolynomialDensityString() {
        double multiplicationCoefficient = (this.lambda.doubleValue() * Math.exp(this.lambda.doubleValue() * getLow().doubleValue()));
        return multiplicationCoefficient + " * Exp[-" + this.lambda + " x]";
    }

    @Override
    public BigDecimal getNormalizationFactor() {
        return BigDecimal.valueOf(1);
    }
}
