package org.oristool.eulero.graph;

import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;

public class SimulationActivity extends Activity{
    // TODO questa va cancellata, visto che non si parte più da istogrammi

    private HistogramDistribution histogram;
    private Approximator approximator;

    public SimulationActivity(String name, HistogramDistribution histogram, Approximator approximator) {
        super(name);
        this.histogram = histogram;
        this.approximator = approximator;
    }

    public HistogramDistribution getHistogram() {
        return histogram;
    }

    public Approximator getApproximator() {
        return approximator;
    }


    @Override
    public Activity copyRecursive(String suffix) {
        return null;
    }

    @Override
    public int addPetriBlock(PetriNet pn, Place in, Place out, int prio) {
        PetriBlockHelper.petriBlockWithHistogramFeatureFromSetups(this.name(), pn, in, out, prio, histogram);
        return prio + 1;
    }
}
