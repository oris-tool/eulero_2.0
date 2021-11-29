package org.oristool.eulero.graph;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AND extends DAG {
    private final List<Activity> activities;

    protected AND(String name, List<Activity> activities) {
        super(name);
        setEFT(this.low());
        setLFT(this.upp());
        //setC(activities.stream().mapToInt(act -> act.C()).sum());
        //setC(activities.stream().mapToInt(Activity::C).sum());
        //setR(activities.stream().mapToInt(Activity::R).sum());
        this.activities = activities;
        // Forse si dovrebbe trovare un modo per init EFT, LFT, C, R; per ora è fatto in quello statico. Se avessi il costruttore privato, forzerei quello statico e allora andrebbe bene.
    }

    public List<Activity> activities() {
        return activities;
    }

    @Override
    public boolean isWellNested() {
        boolean isWellNested = true;
        for (Activity block : activities) {
            isWellNested = isWellNested && block.isWellNested();
        }
        return isWellNested;
    }

    @Override
    public DAG copyRecursive(String suffix){
        DAG copy = DAG.forkJoin(this.name() + "_" + suffix, this.activities.stream()
                .map(a -> a.copyRecursive(suffix)).toArray(Activity[]::new));
        copy.setEFT(copy.low());
        copy.setLFT(copy.upp());
        copy.C();
        copy.R();
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

