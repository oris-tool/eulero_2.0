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

/**
 * SEQ: Completes the given activities in sequence. 
 */
public class Seq implements Activity {
    private int min;
    private int max;
    private double[] cdf;
    private Activity[] dependencies;
    
    public Seq(Activity... dependencies) {
        
        Activity s1 = dependencies[0];
        for (int i = 1; i < dependencies.length; i++) {
            Activity s2 = dependencies[i];
            
            int min = s1.min() + s2.min();
            int max = s1.max() + s2.max();            
            
            double[] cdf = new double[max-min-1]; 
            for (int x = 0; x < cdf.length; x++) {
                // P(T <= t) = sum_{s1_min < u <= min(t,s1_max)} P(s1 = u)*P(s2 <= t-u)
                int t = x+min+1;
                int lower = s1.min()+1;
                int upper = Math.min(s1.max(), t);
                upper = Math.min(upper, t-s2.min());  // t-u+1 >= s2_min+1  =>  u <= t-s2_min
                
                for (int u = lower; u <= upper; u++)
                    cdf[x] += (s1.CDF(u)-s1.CDF(u-1)) * (s2.CDF(t-u+1)+s2.CDF(t-u))/2; 
            }
            
            s1 = new Numerical(min, max, cdf);
        }
        
        min = s1.min();
        max = s1.max();
        cdf = new double[max-min-1];
        for (int x = 0; x < cdf.length; x++) {
            int t = min + 1 + x;
            cdf[x] = s1.CDF(t);
        }
        
        this.dependencies = dependencies;
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
            return cdf[time-min-1];
    }
    
    @Override
    public Activity[] dependencies() {
        return dependencies;
    }
}
