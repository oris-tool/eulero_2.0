package org.oristool.eulero.math.distribution.continuous;

import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.integration.UnivariateIntegrator;

import java.math.BigDecimal;

public class LinearEuleroPieceDistribution extends ContinuousDistribution{
    private BigDecimal alpha;
    private BigDecimal P;
    private BigDecimal f;

    public LinearEuleroPieceDistribution(String name, BigDecimal low, BigDecimal upp, UnivariateIntegrator integrator, BigDecimal alpha, BigDecimal P, BigDecimal f) {
        super(name, low, upp, integrator);
        this.alpha = alpha;
        this.P = P;
        this.f = f;
    }

    public LinearEuleroPieceDistribution(String name, BigDecimal low, BigDecimal upp, BigDecimal alpha, BigDecimal P, BigDecimal f) {
        this(name, low, upp, new SimpsonIntegrator(), alpha, P, f);
    }

    @Override
    public BigDecimal probabilityDensityFunction(BigDecimal t) {
        double h = getUpp().doubleValue() - getLow().doubleValue();
        double c1 = (f.doubleValue() + alpha.doubleValue()) / h;
        double c2 = (2 * P.doubleValue() / h - f.doubleValue() - alpha.doubleValue()) / h;
        return BigDecimal.valueOf(c1 * (t.doubleValue() - getUpp().doubleValue()) + c2 * (getLow().doubleValue() - t.doubleValue()));
    }

    @Override
    public String getExpolynomialDensityString() {
        double h = getUpp().doubleValue() - getLow().doubleValue();
        double c1 = (f.doubleValue() + alpha.doubleValue()) / h;
        double c2 = (2 * P.doubleValue() / h - f.doubleValue() - alpha.doubleValue()) / h;

        return (c1*getUpp().doubleValue() - c2*getLow().doubleValue()) + " + " + (c2 - c1) + "*x^1";
    }

    @Override
    public BigDecimal getNormalizationFactor() {
        return BigDecimal.valueOf(1.0 / this.P.doubleValue());
    }
}
