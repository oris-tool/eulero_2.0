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

@XmlRootElement(name = "xor-type")
public class XORType extends ActivityType {
    @XmlElementWrapper(name = "probs")
    @XmlElement(name = "prob", required = true)
    private final List<Double> probs;

    public XORType(ArrayList<Activity> children, List<Double> probs){
        super(children);
        this.probs = probs;
    }

    public XORType(){
        super(new ArrayList<>());
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
        activity.setEnumType(ActivityEnumType.XOR);
    }

    @Override
    public void buildTPN(PetriNet pn, Place in, Place out, int prio) {
        // input/output places of alternative activities
        List<Place> act_ins = new ArrayList<>();
        List<Place> act_outs = new ArrayList<>();

        for (int i = 0; i < getActivity().activities().size(); i++) {
            // Setting immediate transition for XOR branches
            // In pratica lui mi crea le IMM, le appende a in, e tiene traccia di quese per fare qualcosa

            Transition branch = pn.addTransition(getActivity().name() + "_case" + i);
            // same priority for all branches to create conflict
            branch.addFeature(new Priority(prio));
            branch.addFeature(StochasticTransitionFeature
                    .newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(this.probs.get(i))));
            branch.addFeature(new TimedTransitionFeature("0", "0"));

            Place act_in = pn.addPlace("p" + getActivity().name() + "_case" + i);
            pn.addPrecondition(in, branch);
            pn.addPostcondition(branch, act_in);
            act_ins.add(act_in);

            Place act_out = pn.addPlace("p" + getActivity().name() + "_end" + i);
            act_outs.add(act_out);
        }

        for (int i = 0; i < getActivity().activities().size(); i++) {
            Transition t = pn.addTransition(getActivity().activities().get(i).name() + "_timed");
            t.addFeature(StochasticTransitionFeature.newUniformInstance(getActivity().activities().get(i).min(), getActivity().activities().get(i).max()));
            t.addFeature(new TimedTransitionFeature(getActivity().activities().get(i).min().toString(), getActivity().activities().get(i).max().toString()));
            t.addFeature(new ConcurrencyTransitionFeature(getActivity().activities().get(i).C()));
            //t.addFeature(new RegenerationEpochLengthTransitionFeature(alternatives().get(i).R()));

            pn.addPrecondition(act_ins.get(i), t);
            pn.addPostcondition(t, act_outs.get(i));
        }

        for (int i = 0; i < getActivity().activities().size(); i++) {
            // Questo mi sa che tutto sommato non serve davvero.
            Transition merge = pn.addTransition(getActivity().name() + "_merge" + i);
            merge.addFeature(StochasticTransitionFeature
                    .newDeterministicInstance(BigDecimal.ZERO));
            merge.addFeature(new TimedTransitionFeature("0", "0"));
            // new priority not necessary: only one branch will be selected
            merge.addFeature(new Priority(prio++));
            pn.addPrecondition(act_outs.get(i), merge);
            pn.addPostcondition(merge, out);
        }
    }

    @Override
    public int buildSTPN(PetriNet pn, Place in, Place out, int prio) {
        // input/output places of alternative activities
        List<Place> act_ins = new ArrayList<>();
        List<Place> act_outs = new ArrayList<>();

        for (int i = 0; i < getActivity().activities().size(); i++) {
            Transition branch = pn.addTransition(getActivity().name() + "_case" + i);
            // same priority for all branches to create conflict
            branch.addFeature(new Priority(prio));
            branch.addFeature(StochasticTransitionFeature
                    .newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(probs.get(i))));

            Place act_in = pn.addPlace("p" + getActivity().name() + "_case" + i);
            pn.addPrecondition(in, branch);
            pn.addPostcondition(branch, act_in);
            act_ins.add(act_in);

            Place act_out = pn.addPlace("p" + getActivity().name() + "_end" + i);
            act_outs.add(act_out);
        }

        for (int i = 0; i < getActivity().activities().size(); i++) {
            getActivity().activities().get(i).buildSTPN(pn, act_ins.get(i), act_outs.get(i), prio++);
        }

        for (int i = 0; i < getActivity().activities().size(); i++) {
            Transition merge = pn.addTransition(getActivity().name() + "_merge" + i);
            merge.addFeature(StochasticTransitionFeature
                    .newDeterministicInstance(BigDecimal.ZERO));
            // new priority not necessary: only one branch will be selected
            merge.addFeature(new Priority(prio++));
            pn.addPrecondition(act_outs.get(i), merge);
            pn.addPostcondition(merge, out);
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
    public double[] analyze(BigDecimal timeLimit, BigDecimal timeTick, AnalysisHeuristicsVisitor visitor){
        return visitor.analyze(this, timeLimit, timeTick);
    }

    @Override
    public BigDecimal upp() {
        if(getChildren().stream().map(Activity::upp).filter(t -> t.doubleValue() == Double.MAX_VALUE).count() > 0)
            return BigDecimal.valueOf(Double.MAX_VALUE);

        return BigDecimal.valueOf(getChildren().stream().map(Activity::upp).mapToDouble(BigDecimal::doubleValue).max().getAsDouble());
        // return this.getActivity().max();
    }

    @Override
    public double getFairTimeLimit() {
        return getChildren().stream().mapToDouble(Activity::getFairTimeLimit).max().orElse(1.);
    }

    @Override
    public BigDecimal low() {
        return BigDecimal.valueOf(getChildren().stream().map(Activity::low).mapToDouble(BigDecimal::doubleValue).min().getAsDouble());
        // return this.getActivity().max();
    }

    @Override
    public ActivityType clone() {
        ArrayList<Activity> clonedActivities = getChildren().stream().map(Activity::clone).collect(Collectors.toCollection(ArrayList::new));
        // TODO le probs() me le copia per davvero o sono dannato?
        return new XORType(clonedActivities, probs());
    }

}
