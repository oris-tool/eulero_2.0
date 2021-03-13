package org.oristool.eulero.graph;

import org.oristool.eulero.math.approximation.HistogramApproximator;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;

public class SimulationActivity extends Activity{

    private HistogramDistribution histogram;
    private HistogramApproximator approximator;

    public SimulationActivity(String name, HistogramDistribution histogram, HistogramApproximator approximator) {
        super(name);
        this.histogram = histogram;
        this.approximator = approximator;
    }

    public HistogramDistribution getHistogram() {
        return histogram;
    }

    public HistogramApproximator getApproximator() {
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
