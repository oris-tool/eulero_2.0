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

import org.apache.commons.math3.distribution.GammaDistribution;

/**
 * Activity with Erlang CDF.
 */
public class Erlang implements Activity {
    private int min;
    private int max;
    private double[] cdf;
    private Activity[] dependencies;

    public Erlang(int k, double lambda, double truncationError, BigDecimal step) {
        
        GammaDistribution g = new GammaDistribution(k, 1/lambda);
        
        this.min = 0;
        this.max = new BigDecimal(g.inverseCumulativeProbability(1-truncationError))
                .divide(step, MathContext.DECIMAL128)
                .setScale(0, RoundingMode.HALF_UP).intValue();
        
        double s = step.doubleValue();
        cdf = new double[max+1];
        for (int t=0; t < cdf.length; t++)
            cdf[t] = g.cumulativeProbability(t*s);
        
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
            return cdf[time];
    }
    
    @Override
    public Activity[] dependencies() {
        return dependencies;
    }
}
