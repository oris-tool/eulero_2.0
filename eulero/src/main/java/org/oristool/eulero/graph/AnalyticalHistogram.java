package org.oristool.eulero.graph;

import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.approximation.Approximator.ApproximationSupportSetup;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;

import java.util.ArrayList;
import java.util.Map;

public class AnalyticalHistogram extends Activity{
    // TODO anche questo è deprecated, perchè non si parte più dagli istogrammi

    private double[] cdf;
    private double upp;
    private double low;
    private Approximator approximator;


    public AnalyticalHistogram(String name, double[] cdf, double low, double upp, Approximator approximator) {
        super(name);
        this.cdf = cdf;
        this.low = low;
        this.upp = upp;
        this.approximator = approximator;
    }

    public double[] getCdf() {
        return cdf;
    }

    public double getLow() {
        return low;
    }

    public double getUpp() {
        return upp;
    }

    public Approximator getApproximator() {
        return approximator;
    }

    @Override
    public AnalyticalHistogram copyRecursive(String suffix) {
        return new AnalyticalHistogram(this.name() + suffix, 
                this.getCdf(), this.getLow(), this.getUpp(), this.getApproximator());
    }

    @Override
    public int addPetriBlock(PetriNet pn, Place in, Place out, int prio) {
        Map<String, ApproximationSupportSetup> setups = approximator.getApproximationSupportSetups(cdf, low, upp);

        PetriBlockHelper.petriBlockFromSetups(this.name(), pn, in, out, prio, setups);

        return prio + 1;
    }

    // TODO yamlData()
}
