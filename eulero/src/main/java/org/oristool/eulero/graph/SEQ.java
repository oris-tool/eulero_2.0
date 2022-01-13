package org.oristool.eulero.graph;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@XmlRootElement(name = "SEQ")
public class SEQ extends DAG{
    @XmlElementWrapper(name = "activities")
    @XmlElement(name = "activity", required = true)
    private List<Activity> activities;

    protected SEQ(String name, List<Activity> activities){
        super(name);
        setEFT(this.low());
        setLFT(this.upp());
        this.activities = activities;
    }

    public SEQ(){
        super("");
    };

    public List<Activity> activities() {
        return activities;
    }

    @Override
    public boolean isWellNested() {
        boolean isWellNested = true;
        for (Activity block: activities) {
            isWellNested = isWellNested && block.isWellNested();
        }
        return isWellNested;
    }

    /*public double[] getNumericalCDF(BigDecimal timeLimit, BigDecimal step) {
        if(!this.isWellNested()){
            throw new RuntimeException("Block is not well-nested...");
        }

        if (activities.size() == 1)
            return activities.get(0).getNumericalCDF(timeLimit, step);

        double[] cdf = new double[timeLimit.divide(step).intValue()];
        for(Activity act: activities){
            if (act.equals(activities.get(0))){
                cdf = act.getNumericalCDF(timeLimit, step);
            } else {
                double[] convolution = new double[cdf.length];
                double[] activityCDF = act.getNumericalCDF(timeLimit, step);

                for (int x = 1; x < cdf.length; x++) {
                    for (int u = 1; u <= x; u++)
                        convolution[x] += (cdf[u] - cdf[u - 1]) * (activityCDF[x - u + 1] + activityCDF[x - u]) * 0.5;
                }

                cdf = convolution;
            }
        }

        return cdf;
    }*/

    @Override
    public DAG copyRecursive(String suffix){
        DAG copy = DAG.sequence(this.name() + "_" + suffix, this.activities.stream()
                .map(a -> a.copyRecursive(suffix)).toArray(Activity[]::new));
        copy.setEFT(copy.low());
        copy.setLFT(copy.upp());
        copy.C();
        copy.R();
        return copy;
    }
}
