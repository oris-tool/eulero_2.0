package org.oristool.eulero.graph;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement(name = "AND")
public class AND extends DAG {


    public AND(){
        super("");
    };

    protected AND(String name, List<Activity> activities) {
        super(name);
        setEFT(this.low());
        setLFT(this.upp());
        setActivities(activities);
    }

    @Override
    public boolean isWellNested() {
        boolean isWellNested = true;
        for (Activity block : activities()) {
            isWellNested = isWellNested && block.isWellNested();
        }
        return isWellNested;
    }

    @Override
    public DAG copyRecursive(String suffix){
        DAG copy = DAG.forkJoin(this.name() + "_" + suffix, activities().stream()
                .map(a -> a.copyRecursive(suffix)).toArray(Activity[]::new));
        copy.setEFT(copy.low());
        copy.setLFT(copy.upp());
        copy.C();
        copy.S();
        return copy;

    }
    /*@Override
    public double[] getNumericalCDF(BigDecimal timeLimit, BigDecimal step) {
        double[] cdf = activities.get(0).getNumericalCDF(timeLimit, step);

        for (int i = 1; i < activities.size(); i++) {
            double[] factorCdf = activities.get(i).getNumericalCDF(timeLimit, step);

            for (int x = 0; x < cdf.length; x++) {
                // CDF of max is F(x)*G(x)
                cdf[x] *= factorCdf[x];
            }
        }
        return cdf;
    }*/
}

