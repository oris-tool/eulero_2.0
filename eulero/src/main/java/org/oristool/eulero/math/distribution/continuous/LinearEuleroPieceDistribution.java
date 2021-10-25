package org.oristool.eulero.math.distribution.continuous;

import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.integration.UnivariateIntegrator;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LinearEuleroPieceDistribution extends ContinuousDistribution{
    private BigDecimal alpha;
    private BigDecimal P;
    private BigDecimal f1;

    public LinearEuleroPieceDistribution(String name, BigDecimal low, BigDecimal upp, UnivariateIntegrator integrator, BigDecimal alpha, BigDecimal P, BigDecimal f1) {
        super(name, low, upp, integrator);
        this.alpha = alpha;
        this.P = P;
        this.f1 = f1;
    }

    public LinearEuleroPieceDistribution(String name, BigDecimal low, BigDecimal upp, BigDecimal alpha, BigDecimal P, BigDecimal f1) {
        this(name, low, upp, new SimpsonIntegrator(), alpha, P, f1);
    }

    @Override
    public BigDecimal probabilityDensityFunction(BigDecimal t) {
        double h = getUpp().doubleValue() - getLow().doubleValue();
        double c1 = (f1.doubleValue() + alpha.doubleValue()) / h;
        double c2 = (2 * P.doubleValue() / h - f1.doubleValue() - alpha.doubleValue()) / h;
        return BigDecimal.valueOf(c1 * (getUpp().doubleValue() - t.doubleValue()) + c2 * ( t.doubleValue() - getLow().doubleValue()));
    }

    @Override
    public String getExpolynomialDensityString() {
        double h = getUpp().add(getLow().negate()).setScale(getUpp().scale(), RoundingMode.HALF_DOWN).doubleValue();
        double c1 = (f1.doubleValue() + alpha.doubleValue()) / h;
        double c2 = (2 * P.doubleValue() / h - f1.doubleValue() - alpha.doubleValue()) / h;

        return (c1*getUpp().doubleValue() - c2*getLow().doubleValue()) + " + " + (c2 - c1) + "*x^1";
    }

    @Override
    public BigDecimal getNormalizationFactor() {
        return BigDecimal.valueOf(1.0 / this.P.doubleValue());
    }
}
