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

package org.oristool.eulero.graph;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

/**
 * XOR: A random choice between activities
 */
public class Xor extends Activity {
    private List<Double> probs;
    private List<Activity> alternatives;
    
    public Xor(String name, List<Activity> alternatives, List<Double> probs) {
        super(name);
        if (alternatives.size() != probs.size())
            throw new IllegalArgumentException("Each alternative must have one probability");
        this.probs = probs;
        this.alternatives = alternatives;
    }

    @Override
    public Xor copyRecursive(String suffix) {
        List<Activity> alternativesCopy = alternatives.stream()
                .map(a -> a.copyRecursive(suffix))
                .collect(Collectors.toList());
        
        return new Xor(this.name() + suffix, alternativesCopy, new ArrayList<>(probs));
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
    public int addPetriBlock(PetriNet pn, Place in, Place out, int prio) {

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
            alternatives.get(i).addPetriBlock(pn, act_ins.get(i), act_outs.get(i), prio++);
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
}
