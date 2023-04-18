package org.oristool.eulero.modeling.activitytypes;

import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.modeling.deprecated.ActivityEnumType;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.Composite;

import java.math.BigDecimal;
import java.util.ArrayList;

public class SEQType extends DAGType{
    public SEQType(ArrayList<Activity> children) {
        super(children);
    }
    /*@Override
    public void initActivity(Composite activity, Activity... children){
        if (children.length == 0)
            throw new IllegalArgumentException("Sequence cannot be empty");

        initPreconditions(activity, children);

        activity.setMin(activity.low());
        activity.setMax(activity.upp());
        activity.setActivities(new ArrayList<>(List.of(children)));
        activity.setEnumType(ActivityEnumType.SEQ);
    }*/

    @Override
    public void initPreconditions(Composite activity, Activity... children) {
        Activity prev = activity.begin();
        for (Activity a : children) {
            a.addPrecondition(prev);
            prev = a;
        }
        activity.end().addPrecondition(prev);
    }

    @Override
    public void setEnumType(Composite activity) {
        activity.setEnumType(ActivityEnumType.SEQ);
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
