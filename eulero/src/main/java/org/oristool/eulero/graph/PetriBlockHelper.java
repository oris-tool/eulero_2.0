package org.oristool.eulero.graph;

import org.oristool.eulero.math.approximation.Approximator.ApproximationSupportSetup;
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
import java.util.Map;

public class PetriBlockHelper {

    public static void petriBlockFromSetups(String blockName, PetriNet pn, Place in, Place out, int prio, Map<String, ApproximationSupportSetup> setups){
        // handling body
        ApproximationSupportSetup bodySetup = setups.get("body");
        Place pBody = pn.addPlace("p" + blockName + "_body_switch");

        Transition tImmBody = pn.addTransition(blockName + "_body_switch");
        tImmBody.addFeature(new Priority(prio));
        tImmBody.addFeature(StochasticTransitionFeature
                .newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(bodySetup.getWeight().doubleValue())));
        pn.addPrecondition(in, tImmBody);
        pn.addPostcondition(tImmBody, pBody);

        Transition tBody = pn.addTransition(blockName + "_body");
        ShiftedTruncatedExponentialDistribution bodyDistribution = new ShiftedTruncatedExponentialDistribution(blockName,
                bodySetup.getSupport().get("start"), bodySetup.getSupport().get("end"), bodySetup.getParameters().get("lambda"));

        tBody.addFeature(new Priority(prio));

        DBMZone transition_d_0 = new DBMZone(Variable.X);
        Expolynomial transition_e_0 = Expolynomial.fromString(bodyDistribution.getExpolynomialDensityString());
        transition_e_0.multiply(bodyDistribution.getNormalizationFactor());
        transition_d_0.setCoefficient(Variable.X, Variable.TSTAR, new OmegaBigDecimal(String.valueOf(bodyDistribution.getUpp().doubleValue())));
        transition_d_0.setCoefficient(Variable.TSTAR, Variable.X, new OmegaBigDecimal(String.valueOf(-bodyDistribution.getLow().doubleValue())));
        GEN transition_gen_0 = new GEN(transition_d_0, transition_e_0);
        tBody.addFeature(StochasticTransitionFeature.of(transition_gen_0));

        pn.addPrecondition(pBody, tBody);
        pn.addPostcondition(tBody, out);


        // handling tail
        ApproximationSupportSetup tailSetup = setups.get("tail");
        Place pTail = pn.addPlace("p" + blockName + "_tail_switch");

        Transition tImmTail = pn.addTransition(blockName + "_tail_switch");
        tImmTail.addFeature(new Priority(prio));
        tImmTail.addFeature(StochasticTransitionFeature
                .newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(tailSetup.getWeight().doubleValue())));
        pn.addPrecondition(in, tImmTail);
        pn.addPostcondition(tImmTail, pTail);

        Transition tDet = pn.addTransition(blockName + "_tail_DET");
        tDet.addFeature(new Priority(prio));
        tDet.addFeature(StochasticTransitionFeature.newDeterministicInstance(tailSetup.getSupport().get("start")));

        Place pDet = pn.addPlace("p" + blockName + "_tail_post_DET");

        Transition tTail = pn.addTransition(blockName + "_tail");
        tTail.addFeature(new Priority(prio));
        tTail.addFeature(StochasticTransitionFeature.newExponentialInstance(tailSetup.getParameters().get("lambda")));

        pn.addPrecondition(pTail, tDet);
        pn.addPostcondition(tDet, pDet);
        pn.addPrecondition(pDet, tTail);
        pn.addPostcondition(tTail, out);
    }

    public static void petriBlockWithHistogramFeatureFromSetups(String blockName, PetriNet pn, Place in, Place out, int prio, HistogramDistribution histogram) {
        Transition t = pn.addTransition(blockName);
        t.addFeature(EmpiricalTransitionFeature.newInstance(histogram.getCDFHistogramValues(), histogram.getLow(), histogram.getUpp()));
        t.addFeature(StochasticTransitionFeature.newUniformInstance(histogram.getLow(), histogram.getUpp()));
        pn.addPrecondition(in, t);
        pn.addPostcondition(t, out);
    }
}
