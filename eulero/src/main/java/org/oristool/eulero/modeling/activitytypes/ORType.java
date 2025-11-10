package org.oristool.eulero.modeling.activitytypes;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.Composite;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.models.tpn.ConcurrencyTransitionFeature;
import org.oristool.models.tpn.TimedTransitionFeature;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement(name = "or-type")
public class ORType extends ActivityType {
    //TODO: ci serve davvero? Perchè poi un tema è: come si rende probabilistico un qualcosa che non è well-nested?

    @XmlElementWrapper(name = "probs")
    @XmlElement(name = "prob", required = true)
    private final List<Double> probs;

    private Activity wrapper;

    public ORType(ArrayList<Activity> children, List<Double> probs) {
        super(children);
        this.probs = probs;
    }

    public ORType(ArrayList<Activity> children){
        super(children);
        this.probs = new ArrayList<>();
    }

    public List<Double> probs() {
        return probs;
    }

    @Override
    public void initActivity(Composite activity, Activity... children) {
        if (List.of(children).size() != probs.size())
            throw new IllegalArgumentException("Each alternative must have one probability");
        activity.setActivities(new ArrayList<>(List.of(children)));
        activity.setMin(Arrays.stream(children).reduce((a, b)-> a.low().compareTo(b.low()) != 1 ? a : b).get().low());
        activity.setMax(Arrays.stream(children).reduce((a, b)-> a.upp().compareTo(b.upp()) != -1 ? a : b).get().upp());
        activity.setEnumType(ActivityEnumType.OR);
    }

    @Override
    public void buildTPN(PetriNet pn, Place in, Place out, int prio) {
        // input/output places of alternative activities
        Transition split = pn.addTransition(this.getActivity().name() + "_split");
        Transition join = pn.addTransition(this.getActivity().name() + "_join");
        pn.addPrecondition(in, split);
        pn.addPostcondition(join, out);

        for (int i = 0; i < getActivity().activities().size(); i++) {
            // Immedatie Transitions setting probability of occurring and missing
            Transition actOccursIMM = pn.addTransition(getActivity().name() + "_occurs");
            actOccursIMM.addFeature(new Priority(prio));
            actOccursIMM.addFeature(StochasticTransitionFeature
                    .newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(this.probs.get(i))));
            actOccursIMM.addFeature(new TimedTransitionFeature("0", "0"));

            Transition actMissedIMM = pn.addTransition(getActivity().name() + "_missed");
            actMissedIMM.addFeature(new Priority(prio));
            actMissedIMM.addFeature(StochasticTransitionFeature
                    .newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(1. - this.probs.get(i))));
            actMissedIMM.addFeature(new TimedTransitionFeature("0", "0"));

            // Effective transition, subjected tto occuring probability
            Transition effectiveTranstition = pn.addTransition(getActivity().activities().get(i).name() + "_timed");
            effectiveTranstition.addFeature(StochasticTransitionFeature.newUniformInstance(getActivity().activities().get(i).min(), getActivity().activities().get(i).max()));
            effectiveTranstition.addFeature(new TimedTransitionFeature(getActivity().activities().get(i).min().toString(), getActivity().activities().get(i).max().toString()));
            effectiveTranstition.addFeature(new ConcurrencyTransitionFeature(getActivity().activities().get(i).C()));



            Place act_in = pn.addPlace("p" + getActivity().name());
            Place act_occuring_in = pn.addPlace("p" + getActivity().name() + "_occurs");
            Place act_out = pn.addPlace("p" + getActivity().name() + "_end" + i);

            pn.addPrecondition(act_in, actMissedIMM);
            pn.addPrecondition(act_in, actOccursIMM);
            pn.addPostcondition(actOccursIMM, act_occuring_in);
            pn.addPrecondition(act_occuring_in, effectiveTranstition);
            pn.addPostcondition(effectiveTranstition, act_out);
            pn.addPostcondition(actMissedIMM, act_out);

            pn.addPostcondition(split, act_in);
            pn.addPrecondition(act_out, join);
        }
    }

    @Override
    public int buildSTPN(PetriNet pn, Place in, Place out, int prio) {
// input/output places of alternative activities
        Transition split = pn.addTransition(this.getActivity().name() + "_split");
        Transition join = pn.addTransition(this.getActivity().name() + "_join");
        pn.addPrecondition(in, split);
        pn.addPostcondition(join, out);

        split.addFeature(StochasticTransitionFeature
                .newDeterministicInstance(BigDecimal.ZERO));
        split.addFeature(new Priority(prio++));

        join.addFeature(StochasticTransitionFeature
                .newDeterministicInstance(BigDecimal.ZERO));
        join.addFeature(new Priority(prio++));

        for (int i = 0; i < getActivity().activities().size(); i++) {
            // Immediate Transitions setting probability of occurring and missing
            Transition actOccursIMM = pn.addTransition(getActivity().name() + "_occurs");
            actOccursIMM.addFeature(new Priority(prio));
            actOccursIMM.addFeature(StochasticTransitionFeature
                    .newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(this.probs.get(i))));
            actOccursIMM.addFeature(new TimedTransitionFeature("0", "0"));

            Transition actMissedIMM = pn.addTransition(getActivity().name() + "_missed");
            actMissedIMM.addFeature(new Priority(prio));
            actMissedIMM.addFeature(StochasticTransitionFeature
                    .newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(1. - this.probs.get(i))));
            actMissedIMM.addFeature(new TimedTransitionFeature("0", "0"));


            Place act_in = pn.addPlace("p" + getActivity().name());
            Place act_occuring_in = pn.addPlace("p" + getActivity().name() + "_occurs");
            Place act_out = pn.addPlace("p" + getActivity().name() + "_end" + i);

            // Effective transition, subjected tto occuring probability
            getActivity().activities().get(i).buildSTPN(pn, act_occuring_in, act_out, prio++);

            pn.addPrecondition(act_in, actMissedIMM);
            pn.addPrecondition(act_in, actOccursIMM);
            pn.addPostcondition(actOccursIMM, act_occuring_in);
            pn.addPostcondition(actMissedIMM, act_out);

            pn.addPostcondition(split, act_in);
            pn.addPrecondition(act_out, join);
        }

        return prio;
    }

    @Override
    public BigInteger computeQ(Composite activity, boolean getSimplified) {
        int maximumS = 0;
        for(Activity act: activity.activities()){
            maximumS = Math.max(maximumS, act.Q().intValue());
        }

        return getSimplified ? BigInteger.ONE : BigInteger.valueOf(maximumS);
    }

    @Override
    public Activity copyRecursive(String suffix) {
        return null;
    }

    @Override
    public double[] analyze(BigDecimal timeLimit, BigDecimal timeTick, AnalysisHeuristicsVisitor visitor) {
        return visitor.analyze(this, timeLimit, timeTick);
    }

    @Override
    public BigDecimal upp() {
        return this.getActivity().max();
    }

    @Override
    public BigDecimal low() {
        return this.getActivity().min();
    }

    @Override
    public double getFairTimeLimit() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public ActivityType clone() {
        ArrayList<Activity> clonedActivities = getChildren().stream().map(Activity::clone).collect(Collectors.toCollection(ArrayList::new));
        // TODO le probs() me le copia per davvero o sono dannato?
        return new ORType(clonedActivities, probs());
    }

}
