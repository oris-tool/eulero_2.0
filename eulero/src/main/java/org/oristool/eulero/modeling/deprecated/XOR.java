/* This program is called EULERO.
 * Copyright (C) 2022 The EULERO Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.oristool.eulero.modeling.deprecated;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.modeling.activitytypes.ActivityEnumType;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * XOR: A random choice between activities
 */
@XmlRootElement(name = "XOR")
public class XOR extends Activity {
    @XmlElementWrapper(name = "probs")
    @XmlElement(name = "prob", required = true)
    private List<Double> probs;

    public XOR(){
        super("");
        setEnumType(ActivityEnumType.XOR);
    };
    
    public XOR(String name, List<Activity> activities, List<Double> probs) {
        super(name);
        if (activities.size() != probs.size())
            throw new IllegalArgumentException("Each alternative must have one probability");
        setActivities(activities);
        setMin(activities.stream().reduce((a, b)-> a.low().compareTo(b.low()) != 1 ? a : b).get().low());
        setMax(activities.stream().reduce((a, b)-> a.upp().compareTo(b.upp()) != -1 ? a : b).get().upp());
        setEnumType(ActivityEnumType.XOR);
        this.probs = probs;
    }

    @Override
    public Activity copyRecursive(String suffix) {
        List<Activity> alternativesCopy = activities().stream()
                .map(a -> a.copyRecursive(suffix))
                .collect(Collectors.toList());
        
        return new XOR(this.name() + suffix, alternativesCopy, new ArrayList<>(probs));
    }

    @Override
    public void resetSupportBounds() {
        double min = Double.MAX_VALUE;
        double max = 0;

        for(Activity alternative: activities()){
            alternative.resetSupportBounds();
            min = Math.min(min, alternative.min().doubleValue());
            max = Math.max(max, alternative.max().doubleValue());
        }

        setMin(BigDecimal.valueOf(min));
        setMax(BigDecimal.valueOf(max));
    }

    @Override
    public void buildTPN(PetriNet pn, Place in, Place out, int prio) {
        // input/output places of alternative activities
        List<Place> act_ins = new ArrayList<>();
        List<Place> act_outs = new ArrayList<>();

        for (int i = 0; i < activities().size(); i++) {
            Transition branch = pn.addTransition(name() + "_case" + i);
            // same priority for all branches to create conflict
            branch.addFeature(new Priority(prio));
            branch.addFeature(StochasticTransitionFeature
                    .newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(probs.get(i))));
            branch.addFeature(new TimedTransitionFeature("0", "0"));

            Place act_in = pn.addPlace("p" + name() + "_case" + i);
            pn.addPrecondition(in, branch);
            pn.addPostcondition(branch, act_in);
            act_ins.add(act_in);

            Place act_out = pn.addPlace("p" + name() + "_end" + i);
            act_outs.add(act_out);
        }

        for (int i = 0; i < activities().size(); i++) {
            Transition t = pn.addTransition(activities().get(i).name() + "_timed");
            t.addFeature(StochasticTransitionFeature.newUniformInstance(activities().get(i).min(), activities().get(i).max()));
            t.addFeature(new TimedTransitionFeature(activities().get(i).min().toString(), activities().get(i).max().toString()));
            t.addFeature(new ConcurrencyTransitionFeature(activities().get(i).C()));
            //t.addFeature(new RegenerationEpochLengthTransitionFeature(alternatives().get(i).R()));

            pn.addPrecondition(act_ins.get(i), t);
            pn.addPostcondition(t, act_outs.get(i));
        }

        for (int i = 0; i < activities().size(); i++) {
            Transition merge = pn.addTransition(name() + "_merge" + i);
            merge.addFeature(StochasticTransitionFeature
                    .newDeterministicInstance(BigDecimal.ZERO));
            merge.addFeature(new TimedTransitionFeature("0", "0"));
            // new priority not necessary: only one branch will be selected
            merge.addFeature(new Priority(prio++));
            pn.addPrecondition(act_outs.get(i), merge);
            pn.addPostcondition(merge, out);
        }
    }

    public List<Double> probs() {
        return probs;
    }

    @Override
    public List<Activity> nested() {
        return activities();
    }
    
    @Override
    public String yamlData() {
        StringBuilder b = new StringBuilder();
        
        b.append(String.format("  probs: [%s]\n", probs.stream()
                .map(d -> String.format("%.3f", d))
                .collect(Collectors.joining(", "))));
        
        b.append(String.format("  alternatives: [%s]\n", activities().stream()
                .map(a -> a.name())
                .collect(Collectors.joining(", "))));
        
        return b.toString();
    }
    
    @Override
    public int buildSTPN(PetriNet pn, Place in, Place out, int prio) {
        // input/output places of alternative activities
        List<Place> act_ins = new ArrayList<>();
        List<Place> act_outs = new ArrayList<>();
        
        for (int i = 0; i < activities().size(); i++) {
            Transition branch = pn.addTransition(name() + "_case" + i);
            // same priority for all branches to create conflict
            branch.addFeature(new Priority(prio));
            branch.addFeature(StochasticTransitionFeature
                    .newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(probs.get(i))));
            
            Place act_in = pn.addPlace("p" + name() + "_case" + i);
            pn.addPrecondition(in, branch);
            pn.addPostcondition(branch, act_in);
            act_ins.add(act_in);
            
            Place act_out = pn.addPlace("p" + name() + "_end" + i);
            act_outs.add(act_out);
        }

        for (int i = 0; i < activities().size(); i++) {
            activities().get(i).buildSTPN(pn, act_ins.get(i), act_outs.get(i), prio++);
        }
        
        for (int i = 0; i < activities().size(); i++) {
            Transition merge = pn.addTransition(name() + "_merge" + i);
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
    public double[] analyze(BigDecimal timeLimit, BigDecimal timeStep, AnalysisHeuristicsVisitor visitor) {
        return new double[0];
    }

    @Override
    public BigInteger computeQ(boolean getSimplified) {
        int maximumS = 0;
        for(Activity act: activities()){
            maximumS = Math.max(maximumS, act.Q().intValue());
        }

        return getSimplified ? BigInteger.ONE : BigInteger.valueOf(maximumS);
    }

    @Override
    public BigDecimal low() {
        return this.min();
    }

    @Override
    public BigDecimal upp() {
        return this.max();
    }

    @Override
    public boolean isWellNested() {
        boolean isWellNested = true;
        for (Activity block: this.activities()) {
            isWellNested = isWellNested && block.isWellNested();
        }
        return isWellNested;
    }
}
