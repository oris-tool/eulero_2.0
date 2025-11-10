package org.oristool.eulero.modeling.activitytypes;

import jakarta.xml.bind.annotation.*;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.Composite;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ActivityType implements Cloneable {
    @XmlTransient
    private Composite activity;
    @XmlElementWrapper(name = "children")
    @XmlElement(name = "child", required = true)
    private ArrayList<Activity> children;

    public ActivityType(){}

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

    public abstract void
    initActivity(Composite activity, Activity... children);
    public abstract void buildTPN(PetriNet pn, Place in, Place out, int prio);
    public abstract int buildSTPN(PetriNet pn, Place in, Place out, int prio);
    public abstract BigInteger computeQ(Composite activity, boolean getSimplified);
    public abstract Activity copyRecursive(String suffix);
    public abstract double[] analyze(BigDecimal timeLimit, BigDecimal timeTick, AnalysisHeuristicsVisitor visitor);
    public abstract BigDecimal upp();
    public abstract BigDecimal low();
    @Override
    public abstract ActivityType clone();

    public abstract double getFairTimeLimit();
}
