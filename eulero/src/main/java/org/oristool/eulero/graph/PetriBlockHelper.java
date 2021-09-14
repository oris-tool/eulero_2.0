package org.oristool.eulero.graph;

import org.oristool.eulero.math.approximation.Approximator.ApproximationSupportSetup;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.EmpiricalTransitionFeature;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PetriBlockHelper {

    public static void petriBlockFromSetups(String blockName, PetriNet pn, Place in, Place out, int prio, Map<String, ApproximationSupportSetup> setups, GENRepresentation rep){
        // handling body
        if(rep.equals(GENRepresentation.XOR)){
            XORRepresentation(blockName, pn, in, out, prio, setups);
        } else {
            PiecewiseRepresentation(blockName, pn, in, out, prio, setups);
        }
    }

    public static void XORRepresentation(String blockName, PetriNet pn, Place in, Place out, int prio, Map<String, ApproximationSupportSetup> setups){
        setups.forEach((name, setup) -> {
            if(name.equals("tail")){
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
            } else {
                // If... distinguere piecewise con Xor
                Place pBody = pn.addPlace("p" + blockName + "_" + name + "_switch");

                Transition tImmBody = pn.addTransition(blockName + "_" + name + "_switch");
                try{
                    tImmBody.addFeature(new Priority(prio));
                } catch (Exception e){

                }

                tImmBody.addFeature(StochasticTransitionFeature
                        .newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(setup.getWeight().doubleValue())));
                pn.addPrecondition(in, tImmBody);
                pn.addPostcondition(tImmBody, pBody);

                Transition tBody = pn.addTransition(blockName + "_" + name);
                tBody.addFeature(new Priority(prio));

                DBMZone transition_d_0 = new DBMZone(Variable.X);
                Expolynomial transition_e_0 = Expolynomial.fromString(setup.getDistribution().getExpolynomialDensityString());
                transition_e_0.multiply(setup.getDistribution().getNormalizationFactor());
                transition_d_0.setCoefficient(Variable.X, Variable.TSTAR, new OmegaBigDecimal(String.valueOf(setup.getDistribution().getUpp().doubleValue())));
                transition_d_0.setCoefficient(Variable.TSTAR, Variable.X, new OmegaBigDecimal(String.valueOf(-setup.getDistribution().getLow().doubleValue())));
                GEN transition_gen_0 = new GEN(transition_d_0, transition_e_0);
                tBody.addFeature(StochasticTransitionFeature.of(transition_gen_0));

                pn.addPrecondition(pBody, tBody);
                pn.addPostcondition(tBody, out);
            }
        });
    }

    public static void PiecewiseRepresentation(String blockName, PetriNet pn, Place in, Place out, int prio, Map<String, ApproximationSupportSetup> setups){
        Transition t = pn.addTransition(blockName);
        t.addFeature(new Priority(prio));
        List<GEN> tBody_gens = new ArrayList<>();

        setups.forEach((name, setup) -> {
            if(name.equals("tail")){
                // handling tail
                DBMZone transition_d_0 = new DBMZone(Variable.X);
                Expolynomial transition_e_0 = Expolynomial.fromString(setup.getDistribution().getExpolynomialDensityString());
                transition_e_0.multiply(BigDecimal.valueOf(setup.getWeight().doubleValue()));
                transition_d_0.setCoefficient(Variable.X, Variable.TSTAR, OmegaBigDecimal.POSITIVE_INFINITY);
                transition_d_0.setCoefficient(Variable.TSTAR, Variable.X, new OmegaBigDecimal(String.valueOf(-setup.getDistribution().getLow().doubleValue())));
                GEN transition_gen_0 = new GEN(transition_d_0, transition_e_0);
                tBody_gens.add(transition_gen_0);
            } else {
                DBMZone transition_d_0 = new DBMZone(Variable.X);
                Expolynomial transition_e_0 = Expolynomial.fromString(setup.getDistribution().getExpolynomialDensityString());
                transition_d_0.setCoefficient(Variable.X, Variable.TSTAR, new OmegaBigDecimal(String.valueOf(setup.getDistribution().getUpp().doubleValue())));
                transition_d_0.setCoefficient(Variable.TSTAR, Variable.X, new OmegaBigDecimal(String.valueOf(-setup.getDistribution().getLow().doubleValue())));
                GEN transition_gen_0 = new GEN(transition_d_0, transition_e_0);
                tBody_gens.add(transition_gen_0);
            }
        });

        PartitionedGEN tBody_pFunction = new PartitionedGEN(tBody_gens);
        t.addFeature(StochasticTransitionFeature.of(tBody_pFunction));
        pn.addPrecondition(in, t);
        pn.addPostcondition(t, out);;
    }

    public static void petriBlockWithHistogramFeatureFromSetups(String blockName, PetriNet pn, Place in, Place out, int prio, HistogramDistribution histogram) {
        Transition t = pn.addTransition(blockName);
        t.addFeature(EmpiricalTransitionFeature.newInstance(histogram.getCDFHistogramValues(), histogram.getLow(), histogram.getUpp()));
        t.addFeature(StochasticTransitionFeature.newUniformInstance(histogram.getLow(), histogram.getUpp()));
        pn.addPrecondition(in, t);
        pn.addPostcondition(t, out);
    }

    public enum GENRepresentation {
        PIECEWISE,
        XOR
    }
}
