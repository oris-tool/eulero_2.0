package org.oristool.eulero.modeling.activitytypes;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.Composite;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

@XmlRootElement(name = "seq-type")
public class SEQType extends DAGType {
    public SEQType(ArrayList<Activity> children) {
        super(children);
    }

    public SEQType(){
        super(new ArrayList<>());
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
    public BigDecimal upp(){
        // Check if any children has infinite support
        if (getChildren().stream().map(Activity::upp).filter(t -> t.doubleValue() == Double.MAX_VALUE).count() > 0){
            return BigDecimal.valueOf(Double.MAX_VALUE);
        }

        return BigDecimal.valueOf(getChildren().stream().map(Activity::upp).mapToDouble(BigDecimal::doubleValue).sum());
    }

    @Override
    public double getFairTimeLimit() {
        double sum = getChildren().stream().mapToDouble(Activity::getFairTimeLimit).sum();
        return (sum == 0.0) ? 1.0 : sum; 
    }

    @Override
    public BigDecimal low(){
        // Check if any children has infinite support
        return BigDecimal.valueOf(getChildren().stream().map(Activity::low).mapToDouble(BigDecimal::doubleValue).sum());
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
