package org.oristool.eulero.graph;

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
import org.oristool.models.stpn.trees.EmpiricalTransitionFeature;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PetriBlockHelper {

    public static void petriBlockFromSetups(String blockName, PetriNet pn, Place in, Place out, int prio, ArrayList<ApproximationSupportSetup> setups){
        for (ApproximationSupportSetup setup: setups) {
            Place p = pn.addPlace("p" + blockName);

            Transition t_imm = pn.addTransition(blockName + "_switch" + setups.indexOf(setup) );
            t_imm.addFeature(new Priority(prio));
            t_imm.addFeature(StochasticTransitionFeature
                    .newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(setup.getWeight().doubleValue())));
            pn.addPrecondition(in, t_imm);
            pn.addPostcondition(t_imm, p);

            // Case for Truncated EXP
            if(setup.getParameters().containsKey("lambda") && setup.getSupport().get("end").doubleValue() < Double.MAX_VALUE){
                Transition t = pn.addTransition(blockName + "_" + setups.indexOf(setup));
                ShiftedTruncatedExponentialDistribution distribution = new ShiftedTruncatedExponentialDistribution(blockName, setup.getSupport().get("start"), setup.getSupport().get("end"), setup.getParameters().get("lambda"));

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

            // Case for ShiftedExp
            if(setup.getParameters().containsKey("lambda") && setup.getSupport().get("end").doubleValue() == Double.MAX_VALUE){
                Transition tDet = pn.addTransition(blockName + "_" + setups.indexOf(setup) + "_DET");
                tDet.addFeature(new Priority(prio));
                tDet.addFeature(StochasticTransitionFeature.newDeterministicInstance(setup.getSupport().get("start")));

                Place pDet = pn.addPlace("p" + blockName + "_post_DET");

                Transition t = pn.addTransition(blockName + "_" + setups.indexOf(setup));
                t.addFeature(new Priority(prio));
                t.addFeature(StochasticTransitionFeature.newExponentialInstance(setup.getParameters().get("lambda")));

                pn.addPrecondition(p, tDet);
                pn.addPostcondition(tDet, pDet);
                pn.addPrecondition(pDet, t);
                pn.addPostcondition(t, out);
            }
        }
    }

    public static void petriBlockWithHistogramFeatureFromSetups(String blockName, PetriNet pn, Place in, Place out, int prio, HistogramDistribution histogram) {
        Transition t = pn.addTransition(blockName);
        t.addFeature(EmpiricalTransitionFeature.newInstance(histogram.getCDFHistogramValues(), histogram.getLow(), histogram.getUpp()));
        t.addFeature(StochasticTransitionFeature.newUniformInstance(histogram.getLow(), histogram.getUpp()));
        pn.addPrecondition(in, t);
        pn.addPostcondition(t, out);
    }
}
