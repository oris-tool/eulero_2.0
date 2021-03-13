package org.oristool.eulero.graph;

import org.oristool.eulero.math.approximation.HistogramApproximator;
import org.oristool.eulero.math.approximation.HistogramApproximator.ApproximationSupportSetup;
import org.oristool.eulero.math.distribution.continuous.ShiftedTruncatedExponentialDistribution;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.GEN;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

import java.math.BigDecimal;
import java.util.ArrayList;

public class AnalyticalHistogram extends Activity{

    private HistogramDistribution histogram;
    private HistogramApproximator approximator;


    public AnalyticalHistogram(String name, HistogramDistribution histogram, HistogramApproximator approximator) {
        super(name);
        this.histogram = histogram;
        this.approximator = approximator;
    }

    @Override
    public AnalyticalHistogram copyRecursive(String suffix) {
        return new AnalyticalHistogram(this.name() + suffix, 
                this.getHistogram(), this.getApproximator());
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

        for (ApproximationSupportSetup setup: setups) {
            Place p = pn.addPlace("p" + this.name());

            Transition t_imm = pn.addTransition(this.name() + "_switch" + setups.indexOf(setup) );
            t_imm.addFeature(new Priority(prio));
            t_imm.addFeature(StochasticTransitionFeature
                    .newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(setup.getWeight().doubleValue())));
            pn.addPrecondition(in, t_imm);
            pn.addPostcondition(t_imm, p);

            if(setup.getParameters().containsKey("lambda") && setup.getSupport().get("end").doubleValue() < Double.MAX_VALUE){
                Transition t = pn.addTransition(this.name() + "_" + setups.indexOf(setup));
                ShiftedTruncatedExponentialDistribution distribution = new ShiftedTruncatedExponentialDistribution(this.name(), setup.getSupport().get("start"), setup.getSupport().get("end"), setup.getParameters().get("lambda"));

                t.addFeature(new Priority(prio));

                DBMZone transition_d_0 = new DBMZone(Variable.X);
                Expolynomial transition_e_0 = Expolynomial.fromString(distribution.getExpolynomialDensityString());
                transition_e_0.multiply(distribution.getNormalizationFactor());
                transition_d_0.setCoefficient(Variable.X, Variable.TSTAR, new OmegaBigDecimal(String.valueOf(distribution.getUpp().doubleValue())));
                transition_d_0.setCoefficient(Variable.TSTAR, Variable.X, new OmegaBigDecimal(String.valueOf(-distribution.getLow().doubleValue())));
                GEN transition_gen_0 = new GEN(transition_d_0, transition_e_0);
                t.addFeature(StochasticTransitionFeature.of(transition_gen_0));

                pn.addPrecondition(p, t);
                pn.addPostcondition(t, out);
            }

            if(setup.getParameters().containsKey("lambda") && setup.getSupport().get("end").doubleValue() == Double.MAX_VALUE){
                Transition tDet = pn.addTransition(this.name() + "_" + setups.indexOf(setup) + "_DET");
                tDet.addFeature(new Priority(prio));
                tDet.addFeature(StochasticTransitionFeature.newDeterministicInstance(setup.getSupport().get("start")));

                Place pDet = pn.addPlace("p" + this.name() + "_post_DET");

                Transition t = pn.addTransition(this.name() + "_" + setups.indexOf(setup));
                t.addFeature(new Priority(prio));
                t.addFeature(StochasticTransitionFeature.newExponentialInstance(setup.getParameters().get("lambda")));

                pn.addPrecondition(p, tDet);
                pn.addPostcondition(tDet, pDet);
                pn.addPrecondition(pDet, t);
                pn.addPostcondition(t, out);
            }
        }

        return prio + 1;
    }

    // TODO yamlData()
}
