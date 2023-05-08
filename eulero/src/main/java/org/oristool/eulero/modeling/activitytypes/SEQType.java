package org.oristool.eulero.modeling.activitytypes;

import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.modeling.deprecated.ActivityEnumType;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.Composite;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class SEQType extends DAGType{
    public SEQType(ArrayList<Activity> children) {
        super(children);
    }

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
    }

    @Override
    public ActivityType clone() {
        ArrayList<Activity> clonedActivities = getChildren().stream().map(Activity::clone).collect(Collectors.toCollection(ArrayList::new));
        return new SEQType(clonedActivities);
    }
}
