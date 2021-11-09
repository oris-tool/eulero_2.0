package org.oristool.eulero.stpnblocks;

import org.oristool.eulero.graph.Activity;
import org.oristool.eulero.graph.Numerical;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class SEQBlock extends CompositeBlock{
    public SEQBlock(String name, List<STPNBlock> children) {
        super(name, children);
    }

    @Override
    public boolean isWellNested() {
        boolean isWellNested = true;
        for (STPNBlock block: this.children()) {
            isWellNested = isWellNested && block.isWellNested();
        }
        return isWellNested;
    }

    @Override
    public double[] getNumericalCDF(BigDecimal step) {
        Numerical s1 = activities.get(0);
        if (activities.size() == 1)
            return new Numerical(s1.name(), s1.step, s1.min, s1.max, s1.cdf.clone());

        String name = "sum(" + activities.stream().map(Activity::name).collect(Collectors.joining(",")) + ")";
        BigDecimal step = s1.step();

        for (int i = 1; i < children().size(); i++) {
            Numerical s2 = children().get(i).;
            if (s2.step().compareTo(step) != 0)
                throw new IllegalArgumentException("Steps should be the same");

            int min = s1.min() + s2.min();
            int max = s1.max() + s2.max();

            double[] cdf = new double[max-min-1];
            for (int x = 0; x < cdf.length; x++) {
                // P(T <= t) = sum_{s1_min < u <= min(t,s1_max)} P(s1 = u)*P(s2 <= t-u)
                int t = x+min+1;
                int lower = s1.min()+1;
                int upper = Math.min(s1.max(), t);
                upper = Math.min(upper, t-s2.min());  // t-u+1 >= s2_min+1  =>  u <= t-s2_min

                for (int u = lower; u <= upper; u++)
                    cdf[x] += (s1.CDF(u)-s1.CDF(u-1)) * (s2.CDF(t-u+1)+s2.CDF(t-u))/2;
            }

            s1 = new Numerical(name, step, min, max, cdf);
        }

        return s1;
        return new double[0];
    }

    @Override
    public int parseToPetriNet(PetriNet pn, Place in, Place out, int prio) {
        for (STPNBlock block: this.children()) {
            block.
            isWellNested = isWellNested && block.isWellNested();
        }
        Transition t = pn.addTransition(this.name());
        t.addFeature(new Priority(prio));
        StochasticTransitionFeature stochasticTransitionFeature = StochasticTransitionFeature.newExpolynomial(distribution.getExpolynomialDensityString(), new OmegaBigDecimal(this.EFT()), new OmegaBigDecimal(this.LFT()));
        t.addFeature(stochasticTransitionFeature);
        pn.addPrecondition(in, t);
        pn.addPostcondition(t, out);
        return prio + 1;



        return 0;
    }
}
