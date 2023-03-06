package org.oristool.eulero.modeling.updates.activitytypes;

import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsStrategy;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.ActivityEnumType;
import org.oristool.eulero.modeling.updates.Composite;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;

import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class ActivityType {
    public abstract void initActivity(Composite activity, Activity... children);
    public abstract void buildTPN(Composite activity, PetriNet pn, Place in, Place out, int prio);
    public abstract int buildSTPN(Composite activity, PetriNet pn, Place in, Place out, int prio);
    public abstract BigInteger computeQ(Composite activity, boolean getSimplified);

    public abstract Activity copyRecursive(String suffix);
}
