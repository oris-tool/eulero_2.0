/* This program is part of the ORIS Tool.
 * Copyright (C) 2011-2020 The ORIS Authors.
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

package org.oristool.eulero.workflow;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.models.tpn.ConcurrencyTransitionFeature;
import org.oristool.models.tpn.TimedTransitionFeature;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

/**
 * XOR: A random choice between activities
 */
@XmlRootElement(name = "XOR")
public class XOR extends Activity {
    @XmlElementWrapper(name = "probs")
    @XmlElement(name = "prob", required = true)
    private List<Double> probs;

    @XmlElementWrapper(name = "alternatives")
    @XmlElement(name = "activity", required = true)
    private List<Activity> alternatives;

    public XOR(){
        super("");
    };
    
    public XOR(String name, List<Activity> alternatives, List<Double> probs) {
        super(name);
        if (alternatives.size() != probs.size())
            throw new IllegalArgumentException("Each alternative must have one probability");

        setEFT(alternatives.stream().reduce((a,b)-> a.low().compareTo(b.low()) != 1 ? a : b).get().low());
        setLFT(alternatives.stream().reduce((a,b)-> a.upp().compareTo(b.upp()) != -1 ? a : b).get().upp());
        this.probs = probs;
        this.alternatives = alternatives;

    }

    @Override
    public XOR copyRecursive(String suffix) {
        List<Activity> alternativesCopy = alternatives.stream()
                .map(a -> a.copyRecursive(suffix))
                .collect(Collectors.toList());
        
        return new XOR(this.name() + suffix, alternativesCopy, new ArrayList<>(probs));
    }

    @Override
    public void resetSupportBounds() {
        double min = Double.MAX_VALUE;
        double max = 0;

        for(Activity alternative: alternatives){
            alternative.resetSupportBounds();
            min = Math.min(min, alternative.EFT().doubleValue());
            max = Math.max(max, alternative.LFT().doubleValue());
        }

        setEFT(BigDecimal.valueOf(min));
        setLFT(BigDecimal.valueOf(max));
    }

    @Override
    public void buildTPN(PetriNet pn, Place in, Place out, int prio) {
        // input/output places of alternative activities
        List<Place> act_ins = new ArrayList<>();
        List<Place> act_outs = new ArrayList<>();

        for (int i = 0; i < alternatives.size(); i++) {
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

        for (int i = 0; i < alternatives.size(); i++) {
            Transition t = pn.addTransition(alternatives().get(i).name() + "_timed");
            t.addFeature(StochasticTransitionFeature.newUniformInstance(alternatives().get(i).EFT(), alternatives().get(i).LFT()));
            t.addFeature(new TimedTransitionFeature(alternatives().get(i).EFT().toString(), alternatives().get(i).LFT().toString()));
            t.addFeature(new ConcurrencyTransitionFeature(alternatives().get(i).C()));
            //t.addFeature(new RegenerationEpochLengthTransitionFeature(alternatives().get(i).R()));

            pn.addPrecondition(act_ins.get(i), t);
            pn.addPostcondition(t, act_outs.get(i));
        }

        for (int i = 0; i < alternatives.size(); i++) {
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
    
    public List<Activity> alternatives() {
        return alternatives;
    }
    
    @Override
    public List<Activity> nested() {
        return alternatives;
    }
    
    @Override
    public String yamlData() {
        StringBuilder b = new StringBuilder();
        
        b.append(String.format("  probs: [%s]\n", probs.stream()
                .map(d -> String.format("%.3f", d))
                .collect(Collectors.joining(", "))));
        
        b.append(String.format("  alternatives: [%s]\n", alternatives.stream()
                .map(a -> a.name())
                .collect(Collectors.joining(", "))));
        
        return b.toString();
    }
    
    @Override
    public int buildSTPN(PetriNet pn, Place in, Place out, int prio) {
        // input/output places of alternative activities
        List<Place> act_ins = new ArrayList<>();
        List<Place> act_outs = new ArrayList<>();
        
        for (int i = 0; i < alternatives.size(); i++) {
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

        for (int i = 0; i < alternatives.size(); i++) {
            alternatives.get(i).buildSTPN(pn, act_ins.get(i), act_outs.get(i), prio++);
        }
        
        for (int i = 0; i < alternatives.size(); i++) {
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
    public BigInteger computeQ(boolean getSimplified) {
        int maximumS = 0;
        for(Activity act: alternatives){
            maximumS = Math.max(maximumS, act.Q().intValue());
        }

        return getSimplified ? BigInteger.ONE : BigInteger.valueOf(maximumS);
    }

    @Override
    public BigDecimal low() {
        return this.EFT();
    }

    @Override
    public BigDecimal upp() {
        return this.LFT();
    }

    @Override
    public boolean isWellNested() {
        boolean isWellNested = true;
        for (Activity block: this.alternatives()) {
            isWellNested = isWellNested && block.isWellNested();
        }
        return isWellNested;
    }
}
