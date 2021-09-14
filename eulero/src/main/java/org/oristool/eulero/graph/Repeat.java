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
import java.util.List;

import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

/**
 * Repeat: A random choice at end of each iteration
 */
public class Repeat extends Activity {
    private double repeatProb;
    private Activity repeatBody;
    
    public Repeat(String name, double repeatProb, Activity repeatBody) {
        super(name);
        this.repeatProb = repeatProb;
        this.repeatBody = repeatBody;
    }

    @Override
    public Repeat copyRecursive(String suffix) {
        Activity bodyCopy = repeatBody.copyRecursive(suffix);
        return new Repeat(this.name() + suffix, this.repeatProb, bodyCopy);
    }
    
    public Activity repeatBody() {
        return repeatBody;
    }
    
    public double repeatProb() {
        return repeatProb;
    }
    
    @Override
    public List<Activity> nested() {
        return List.of(repeatBody);
    }
    
    @Override
    public String yamlData() {
        StringBuilder b = new StringBuilder();
        b.append(String.format("  prob: %.3f\n", repeatProb));
        b.append(String.format("  body: %s\n", repeatBody.name()));
        return b.toString();
    }
    
    @Override
    public int addPetriBlock(PetriNet pn, Place in, Place out, int prio) {
        
        Transition repeat = pn.addTransition(name() + "_repeat");
        repeat.addFeature(new Priority(prio++));
        repeat.addFeature(StochasticTransitionFeature
                .newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(repeatProb)));
        
        Transition exit = pn.addTransition(name() + "_continue");
        exit.addFeature(new Priority(prio++));
        exit.addFeature(StochasticTransitionFeature
                .newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(1.0 - repeatProb)));
        
        Place choose = pn.addPlace("p" + name() + "_choose");
        pn.addPrecondition(choose, repeat);
        pn.addPostcondition(repeat, in);
        pn.addPrecondition(choose, exit);
        pn.addPostcondition(exit, out);
        
        prio = repeatBody.addPetriBlock(pn, in, choose, prio);
        return prio;
    }

    // TODO: here must be understood how to handle upper bound. In principle is Infinity, but in practice we don't use it.
    @Override
    public BigDecimal low() {
        return repeatBody.low();
    }

    @Override
    public BigDecimal upp() {
        return repeatBody.upp();
    }
}
