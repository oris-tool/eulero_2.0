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
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.oristool.eulero.evaluation.approximator.EXPMixtureApproximation;
import org.oristool.eulero.evaluation.approximator.Approximator;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;

/**
 * Activity with an empirical CDF.
 */
public class Numerical extends Activity {
    private BigDecimal step;
    private int min;
    private int max;
    private double[] cdf;

    public Approximator getApproximator() {
        return approximator;
    }

    public void setApproximator(Approximator approximator) {
        this.approximator = approximator;
    }

    private Approximator approximator;
    
    public Numerical(String name, BigDecimal step, int min, int max, double[] cdf, Approximator approximator) {
        super(name);
        this.step = step;
        this.min = min;
        this.max = max;
        this.cdf = cdf;
        setEFT(BigDecimal.valueOf(step.doubleValue() * min));
        setLFT(BigDecimal.valueOf(step.doubleValue() * min));
        this.approximator = approximator;
    }

    public Numerical(String name, BigDecimal step, int min, int max, double[] cdf){
        this(name, step, min, max, cdf, new EXPMixtureApproximation());
    }
    
    @Override
    public Numerical copyRecursive(String suffix) {
        double[] cdf = this.cdf.clone();
        return new Numerical(this.name() + suffix, 
                this.step, this.min, this.max, cdf);
    }

    @Override
    public BigInteger computeS(boolean getSimplified) {
        return null;
    }

    @Override
    public void buildTPN(PetriNet pn, Place in, Place out, int prio) {

    }

    public BigDecimal step() {
        return step;
    }
    
    public int min() {
        return min;
    }
    
    public int max() {
        return max;
    }

    public double[] getCdf() {
        return cdf;
    }

    @Override
    public int buildSTPN(PetriNet pn, Place in, Place out, int prio) {
        // TODO anche se questa è una classe deprecated
        return prio + 1;
    }

    @Override
    public BigDecimal low() {
        return step().multiply(BigDecimal.valueOf(min()));
    }

    @Override
    public BigDecimal upp() {
        return step().multiply(BigDecimal.valueOf(max()));
    }

    @Override
    public boolean isWellNested() {
        return true;
    }

    public double CDF(int time) {
        if (time <= min)
            return 0;
        else if (time >= max)
            return 1;
        else 
            return cdf[time-min-1];
    }
    
    @Override
    public String yamlData() {
        StringBuilder b = new StringBuilder();
        b.append(String.format("  support: [%s, %s]\n",
                new BigDecimal(min).multiply(step),
                new BigDecimal(max).multiply(step)));
        b.append(String.format("  pdf: %d samples with step %s\n",
                this.cdf.length, step));
        return b.toString();
    }
    
    public static Numerical copyOf(Numerical activity) {
        int min = activity.min();
        int max = activity.max();
        double[] cdf = new double[max-min-1]; 

        for (int x = 0; x < cdf.length; x++) {
            int t = min + 1 + x;
            cdf[x] = activity.CDF(t);
        }
        
        return new Numerical(activity.name(), activity.step(), min, max, cdf);
    }
    
    public static Numerical uniform(String name, BigDecimal a, BigDecimal b, 
            BigDecimal step) {
        
        int min = a.divide(step, MathContext.DECIMAL128)
                .setScale(0, RoundingMode.HALF_UP).intValue();
        int max = b.divide(step, MathContext.DECIMAL128)
                .setScale(0, RoundingMode.HALF_UP).intValue();

        double[] cdf = new double[max-min-1];
        for (int x = 0; x < cdf.length; x++) {
            int t = min + 1 + x;
            cdf[x] = (t-min)/(double)(max-min);
        }
        
        return new Numerical(name, step, min, max, cdf);
    }

    public static Numerical exp(String name, double lambda, 
            double truncationError, BigDecimal step) {
        
        ExponentialDistribution g = new ExponentialDistribution(1/lambda);
        
        int min = 0;
        int max = new BigDecimal(g.inverseCumulativeProbability(1-truncationError))
                .divide(step, MathContext.DECIMAL128)
                .setScale(0, RoundingMode.HALF_UP).intValue();
        
        double s = step.doubleValue();
        double[] cdf = new double[max+1];
        for (int t=0; t < cdf.length; t++)
            cdf[t] = g.cumulativeProbability(t*s);
        
        return new Numerical(name, step, min, max, cdf);
    }

    public static Numerical truncatedExp(String name, double lambda,
             double a, double b, BigDecimal step){
        int min = (int) (a / step.doubleValue());
        int max = (int) (b / step.doubleValue());
        double s = step.doubleValue();
        double[] cdf = new double[max-min+1];
        for (int x = 0; x < cdf.length; x++) {
            int t = min + 1 + x;
            cdf[x] = (1 - Math.exp(-lambda * (t * s - a)))/(1 - Math.exp(-lambda * (b - a)));
        }

        return new Numerical(name, step, min, max, cdf);

    }
    
    public static Numerical erlang(String name, int k, double lambda, 
            double truncationError, BigDecimal step) {
        
        GammaDistribution g = new GammaDistribution(k, 1/lambda);
        
        int min = 0;
        int max = new BigDecimal(g.inverseCumulativeProbability(1-truncationError))
                .divide(step, MathContext.DECIMAL128)
                .setScale(0, RoundingMode.HALF_UP).intValue();
        
        double s = step.doubleValue();
        double[] cdf = new double[max+1];
        for (int t=0; t < cdf.length; t++)
            cdf[t] = g.cumulativeProbability(t*s);
        
        return new Numerical(name, step, min, max, cdf);
    }

    public String toTimeSeries(BigDecimal step) {
        
        StringBuilder b = new StringBuilder();
        for (int t = this.min(); t <= this.max(); t++) {
            b.append(step.multiply(new BigDecimal(t)));
            b.append(" ");
            b.append(this.CDF(t));
            b.append("\n");
        }
        
        return b.toString();
    }
    
    public static Numerical and(List<Numerical> activities) {
        BigDecimal step = activities.get(0).step();
        int min = activities.stream().mapToInt(s -> s.min()).max().getAsInt(); // max of mins
        int max = activities.stream().mapToInt(s -> s.max()).max().getAsInt(); // max of maxs
        String name = "max(" + activities.stream().map(Activity::name).collect(Collectors.joining(",")) + ")";
        double[] cdf = new double[max-min-1]; 
        for (int x = 0; x < cdf.length; x++) {
            // CDF of max is F(x)*G(x)
            int t = min + 1 + x;
            cdf[x] = 1.0;
            for (Numerical a : activities) {
                if (a.step().compareTo(step) != 0)
                    throw new IllegalArgumentException("Steps should be the same");
                cdf[x] *= a.CDF(t);
            }
        }

        return new Numerical(name, step, min, max, cdf);
    }

    public static Numerical or(List<Numerical> activities) {
        BigDecimal step = activities.get(0).step();
        int min = activities.stream().mapToInt(s -> s.min()).min().getAsInt(); // min of mins
        int max = activities.stream().mapToInt(s -> s.max()).min().getAsInt(); // min of maxs
        String name = "min(" + activities.stream().map(Activity::name).collect(Collectors.joining(",")) + ")";
        
        double[] cdf = new double[max-min-1]; 
        for (int x = 0; x < cdf.length; x++) {
            // CDF of min is 1-(1-F(x))*(1-G(x))
            int t = min + 1 + x;
            cdf[x] = 1.0;
            for (Numerical a : activities) {
                if (a.step().compareTo(step) != 0)
                    throw new IllegalArgumentException("Steps should be the same");
                cdf[x] *= 1 - a.CDF(t);
            }
            cdf[x] = 1 - cdf[x];
        }

        return new Numerical(name, step, min, max, cdf);
    }
    
    public static Numerical seq(List<Numerical> activities) {
        
        Numerical s1 = activities.get(0);
        if (activities.size() == 1)
            return new Numerical(s1.name(), s1.step, s1.min, s1.max, s1.cdf.clone());

        String name = "sum(" + activities.stream().map(Activity::name).collect(Collectors.joining(",")) + ")";
        BigDecimal step = s1.step();
        
        for (int i = 1; i < activities.size(); i++) {
            Numerical s2 = activities.get(i);
            if (s2.step().compareTo(step) != 0)
                throw new IllegalArgumentException("Steps should be the same");
            
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
            
            s1 = new Numerical(name, step, min, max, cdf);
        }
        
        return s1;
    }
    
    public static Numerical xor(List<Double> probs, 
            List<Numerical> activities) {
        
        BigDecimal step = activities.get(0).step(); 
        for (Numerical a : activities) {
            if (a.step().compareTo(step) != 0) {
                throw new IllegalArgumentException("Steps should be the same");
            }
        }
        
        int min = activities.stream().mapToInt(s -> s.min()).min().getAsInt(); // min of mins
        int max = activities.stream().mapToInt(s -> s.max()).max().getAsInt(); // max of maxs
        String name = "choice(" + activities.stream().map(Activity::name).collect(Collectors.joining(",")) + ")";

        double[] cdf = new double[max-min-1];
        
        for (int x = 0; x < cdf.length; x++) {
            // CDF of mixture is the weighted sum
            int t = min + 1 + x;
            cdf[x] = 0.0;
            for (int i = 0; i < probs.size(); i++)
                cdf[x] += probs.get(i) * activities.get(i).CDF(t);
        }

        return new Numerical(name, step, min, max, cdf);
    }
}
