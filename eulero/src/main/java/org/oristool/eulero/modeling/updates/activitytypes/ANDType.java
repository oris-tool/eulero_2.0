package org.oristool.eulero.modeling.updates.activitytypes;

import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.modeling.*;
import org.oristool.eulero.modeling.updates.Composite;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ANDType extends DAGType{
    public ANDType(ArrayList<Activity> children) {
        super(children);
    }
    /*@Override
    public void initActivity(Composite activity, Activity... children){
        if (children.length == 0)
            throw new IllegalArgumentException("Parallel cannot be empty");

        double low = 0;
        double upp = 0;
        for(Activity act: children){
            low = Math.max(low, act.low().doubleValue());
            upp = Math.max(upp, act.upp().doubleValue());
        }

        initPreconditions(activity, children);

        activity.setMin(activity.low());
        activity.setMax(activity.upp());
        activity.setActivities(new ArrayList<>(List.of(children)));
        activity.setEnumType(ActivityEnumType.AND);
    }*/

    @Override
    public void initPreconditions(Composite activity, Activity... children) {
        for (Activity act : children) {
            act.addPrecondition(activity.begin());
            activity.end().addPrecondition(act);
        }
    }

    @Override
    public void setEnumType(Composite activity) {
        activity.setEnumType(ActivityEnumType.AND);
    }

    @Override
    public Activity copyRecursive(String suffix) {
        return null;
    }

    @Override
    public double[] analyze(BigDecimal timeLimit, BigDecimal timeTick, AnalysisHeuristicsVisitor visitor){
        return visitor.analyze(this, timeLimit, timeTick);
    };
}
