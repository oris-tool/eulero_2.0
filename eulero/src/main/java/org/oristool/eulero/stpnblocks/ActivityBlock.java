package org.oristool.eulero.stpnblocks;

import org.oristool.eulero.math.distribution.continuous.ContinuousDistribution;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.*;

import java.math.BigDecimal;

public class ActivityBlock extends STPNBlock{
    private ContinuousDistribution distribution;

    public ActivityBlock(String name, ContinuousDistribution distribution) {
        super(name);
        setEFT(distribution.getLow());
        setLFT(distribution.getUpp());
        setC(BigDecimal.ONE);
        setR(BigDecimal.ONE);

        this.distribution = distribution;
    }

    @Override
    public boolean isWellNested() {
        return true;
    }

    @Override
    public double[] getNumericalCDF(BigDecimal step) {
        return distribution.getCDFasArray(step);
    }

    @Override
    public int parseToPetriNet(PetriNet pn, Place in, Place out, int prio) {
        Transition t = pn.addTransition(this.name());
        t.addFeature(new Priority(prio));
        StochasticTransitionFeature stochasticTransitionFeature = StochasticTransitionFeature.newExpolynomial(distribution.getExpolynomialDensityString(), new OmegaBigDecimal(this.EFT()), new OmegaBigDecimal(this.LFT()));
        t.addFeature(stochasticTransitionFeature);
        pn.addPrecondition(in, t);
        pn.addPostcondition(t, out);
        return prio + 1;
    }
}
