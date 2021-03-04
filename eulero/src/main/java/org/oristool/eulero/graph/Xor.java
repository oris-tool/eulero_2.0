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

import java.util.List;
import java.util.stream.Collectors;

/**
 * XOR: A random choice between activities
 */
public class Xor extends Activity {
    private List<Double> probs;
    private List<Activity> alternatives;
    
    public Xor(String name, List<Activity> alternatives, List<Double> probs) {
        super(name);
        
        this.probs = probs;
        this.alternatives = alternatives;
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
}
