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

/**
 * Represents the duration of an activity through its min, max and CDF.
 */
public interface Activity {
    int min();
    int max();
    double CDF(int time);
    Activity[] dependencies();
    
    default String toTimeSeries(BigDecimal step) {
        
        StringBuilder b = new StringBuilder();
        for (int t = this.min(); t <= this.max(); t++) {
            b.append(step.multiply(new BigDecimal(t)));
            b.append(" ");
            b.append(this.CDF(t));
            b.append("\n");
        }
        
        return b.toString();
    }    
}
