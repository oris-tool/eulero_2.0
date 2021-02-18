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
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Activity with uniform CDF. 
 */
public class Uniform implements Activity {
    private int min;
    private int max;
    private Activity[] dependencies;
    
    public Uniform(BigDecimal a, BigDecimal b, BigDecimal step) {
        this.min = a.divide(step, MathContext.DECIMAL128)
                .setScale(0, RoundingMode.HALF_UP).intValue();
        this.max = b.divide(step, MathContext.DECIMAL128)
                .setScale(0, RoundingMode.HALF_UP).intValue();

        dependencies = new Activity[] { };
    }
        
    @Override
    public int min() {
        return min;
    }
    
    @Override
    public int max() {
        return max;
    }

    @Override
    public double CDF(int time) {
        if (time <= min)
            return 0;
        else if (time >= max)
            return 1;
        else 
            return (time-min)/(double)(max-min);
    }
    
    @Override
    public Activity[] dependencies() {
        return dependencies;
    }
}
