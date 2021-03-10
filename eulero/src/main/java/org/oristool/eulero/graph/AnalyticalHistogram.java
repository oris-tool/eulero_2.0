package org.oristool.eulero.graph;

import org.oristool.eulero.math.approximation.HistogramApproximator;
import org.oristool.eulero.math.approximation.HistogramApproximator.ApproximationSupportSetup;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;

import java.util.ArrayList;

public class AnalyticalHistogram extends Activity{

    private HistogramDistribution histogram;
    private HistogramApproximator approximator;


    public AnalyticalHistogram(String name, HistogramDistribution histogram, HistogramApproximator approximator) {
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
    public int addPetriBlock(PetriNet pn, Place in, Place out, int prio) {
        ArrayList<ApproximationSupportSetup> setups = approximator.getApproximationSupportSetups(histogram);

        PetriBlockHelper.petriBlockFromSetups(this.name(), pn, in, out, prio, setups);

        return prio + 1;
    }

    // TODO yamlData()
}
