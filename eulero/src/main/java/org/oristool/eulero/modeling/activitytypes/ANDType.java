package org.oristool.eulero.modeling.activitytypes;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.Composite;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

@XmlRootElement(name = "and-type")
public class ANDType extends DAGType {
    public ANDType(ArrayList<Activity> children) {
        super(children);
    }

    public ANDType() {
        super(new ArrayList<>());
    }

    @Override
    public void initPreconditions(Composite activity, Activity... children) {
        for (Activity act : children) {
            act.addPrecondition(activity.begin());
            activity.end().addPrecondition(act);
        }
    }

    @Override
    public double getFairTimeLimit() {
        return getChildren().stream().mapToDouble(Activity::getFairTimeLimit).max().orElse(1.0);
    }

    @Override
    public BigDecimal upp() {
        // Check if any children has infinite support
        if (getChildren().stream().map(Activity::upp)
                .filter(t -> t.doubleValue() == Double.MAX_VALUE).count() > 0) {
            return BigDecimal.valueOf(Double.MAX_VALUE);
        }

        return BigDecimal.valueOf(getChildren().stream().map(Activity::upp)
                .mapToDouble(BigDecimal::doubleValue).max().getAsDouble());
    }

    @Override
    public BigDecimal low() {
        return BigDecimal.valueOf(getChildren().stream().map(Activity::low)
                .mapToDouble(BigDecimal::doubleValue).max().getAsDouble());
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
    public double[] analyze(BigDecimal timeLimit, BigDecimal timeTick,
            AnalysisHeuristicsVisitor visitor) {
        return visitor.analyze(this, timeLimit, timeTick);
    }

    @Override
    public ActivityType clone() {
        ArrayList<Activity> clonedActivities = getChildren().stream().map(Activity::clone)
                .collect(Collectors.toCollection(ArrayList::new));
        return new ANDType(clonedActivities);
    }
}
