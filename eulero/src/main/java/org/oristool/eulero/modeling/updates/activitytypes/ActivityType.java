package org.oristool.eulero.modeling.updates.activitytypes;

import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.modeling.updates.Activity;
import org.oristool.eulero.modeling.updates.Composite;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

public abstract class ActivityType {
    private Composite activity;
    private ArrayList<Activity> children;

    public ActivityType(ArrayList<Activity> children){
        this.children = children;
    }

    public ArrayList<Activity> getChildren() {
        return children;
    }

    public Composite getActivity() {
        return activity;
    }

    public void setChildren(ArrayList<Activity> children){
        this.children = children;
    }

    public void setActivity(Composite activity) {
        this.activity = activity;
    }

    public abstract void initActivity(Composite activity, Activity... children);
    public abstract void buildTPN(PetriNet pn, Place in, Place out, int prio);
    public abstract int buildSTPN(PetriNet pn, Place in, Place out, int prio);
    public abstract BigInteger computeQ(Composite activity, boolean getSimplified);
    public abstract Activity copyRecursive(String suffix);
    public abstract double[] analyze(BigDecimal timeLimit, BigDecimal timeTick, AnalysisHeuristicsVisitor visitor);
    public abstract BigDecimal upp();


}
