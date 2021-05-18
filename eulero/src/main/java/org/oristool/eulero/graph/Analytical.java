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

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

/**
 * Activity with an analytical CDF.
 */
public class Analytical extends Activity {
    
    private StochasticTransitionFeature pdf;
    
    /**
     * Creates an activity with analytical PDF. 
     */
    public Analytical(String name, StochasticTransitionFeature pdf) {
        super(name, pdf.density().getDomainsEFT().bigDecimalValue(), pdf.density().getDomainsLFT().bigDecimalValue());
        this.pdf = pdf;
        }
    
    @Override
    public Analytical copyRecursive(String suffix) {
        return new Analytical(this.name() + suffix, this.pdf());
    }
    
    public StochasticTransitionFeature pdf() {
        return pdf;
    }
    
    @Override
    public String yamlData() {
        StringBuilder b = new StringBuilder();
        List<? extends DBMZone> domains = pdf.density().getDomains();
        List<? extends Expolynomial> densities = pdf.density().getDensities();
        for (int i = 0; i < domains.size(); i++) {
            b.append(String.format("  pdf: [%s, %s] %s\n",
                    domains.get(i).getBound(Variable.TSTAR, Variable.X).negate(),
                    domains.get(i).getBound(Variable.X, Variable.TSTAR),
                    densities.get(i).toString()));
        }
        return b.toString();
    }
    
    public static Analytical uniform(String name, BigDecimal a, BigDecimal b) {
        return new Analytical(name, 
                StochasticTransitionFeature.newUniformInstance(a, b));
    }

    public static Analytical exp(String name, BigDecimal lambda) {
        return new Analytical(name, 
                StochasticTransitionFeature.newExponentialInstance(lambda));
    }

    public static Analytical erlang(String name, int k, BigDecimal lambda) {
        return new Analytical(name, 
                StochasticTransitionFeature.newErlangInstance(k, lambda));
    }
    
    @Override
    public int addPetriBlock(PetriNet pn, Place in, Place out, int prio) {
        Transition t = pn.addTransition(this.name());
        t.addFeature(new Priority(prio));
        t.addFeature(pdf);
        pn.addPrecondition(in, t);
        pn.addPostcondition(t, out);
        return prio + 1;
    }
}
