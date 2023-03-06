package org.oristool.eulero.modeling.updates.activitytypes;

import org.apache.commons.lang3.tuple.Pair;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.ActivityEnumType;
import org.oristool.eulero.modeling.DAGEdge;
import org.oristool.eulero.modeling.DFSObserver;
import org.oristool.eulero.modeling.updates.Composite;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.models.tpn.ConcurrencyTransitionFeature;
import org.oristool.models.tpn.TimedTransitionFeature;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public abstract class DAGType extends ActivityType{
    @Override
    public void initActivity(Composite activity, Activity... children) {
        // check final and starting activity
        initPreconditions(activity, children);
        activity.setActivities(new ArrayList<>(List.of(children)));
        activity.setMin(activity.getMinBound(activity.end()));
        activity.setMax(activity.getMaxBound(activity.end()));
        setEnumType(activity);
    }

    @Override
    public void buildTPN(Composite activity, PetriNet pn, Place in, Place out, int prio) {
        Map<Activity, Place> actOut = new LinkedHashMap<>();
        Map<Activity, Transition> actPost = new LinkedHashMap<>();
        Map<Activity, Transition> actPre = new LinkedHashMap<>();
        Map<Activity, Place> actIn = new LinkedHashMap<>();
        List<Activity> act = new ArrayList<>();
        int[] priority = new int[] { prio };
        //int[] priority = new int[] { prio };  // to access in closure

        boolean useBegin = activity.begin().post().size() > 1;
        boolean useEnd = activity.end().pre().size() > 1;

        activity.end().dfs(true, new DFSObserver() {
            @Override public boolean onSkip(Activity opened, Activity from) {
                return onOpenOrSkip(opened, from);
            }

            @Override public boolean onOpen(Activity opened, Activity from) {
                return onOpenOrSkip(opened, from);
            }

            private boolean onOpenOrSkip(Activity opened, Activity from) {
                if (opened.equals(activity.begin()) && from.equals(activity.end())) {
                    throw new IllegalStateException("Empty DAG");
                }

                if (!act.contains(opened)) {
                    // will be in visit order (END to BEGIN)
                    act.add(opened);
                }

                if (from == null) {
                    return true;  // END is not a real dependency, continue
                }

                // general structure:

                // [OPENED]    ->  (pOPENED_out)  -> [OPENED_POST]
                //             ->  (pOPENED_FROM) ->
                // [FROM_PRE]  ->  (pFROM_in)     -> [FROM]

                if (!actOut.containsKey(opened)) {
                    Place openedOut = opened.equals(activity.begin()) && useBegin ? in :
                            pn.addPlace("p" + opened + "_out");  // add pOPENED_out
                    actOut.put(opened, openedOut);

                    if (opened.post().size() > 1) {  // add pOPENED_out, OPENED_POST
                        Transition openedPost = pn.addTransition(opened + "_POST");
                        openedPost.addFeature(StochasticTransitionFeature.newDeterministicInstance(BigDecimal.ZERO));
                        openedPost.addFeature(new TimedTransitionFeature("0", "0"));
                        openedPost.addFeature(new Priority(priority[0]++));
                        pn.addPrecondition(openedOut, openedPost);
                        actPost.put(opened, openedPost);
                    }
                }

                if (!actIn.containsKey(from)) {
                    Place fromIn = from.equals(activity.end()) && useEnd ? out :
                            pn.addPlace("p" + from + "_in");  // add pFROM_in
                    actIn.put(from, fromIn);

                    if (from.pre().size() > 1) {  // add FROM_PRE, pFROM_in
                        Transition fromPre = pn.addTransition(from + "_PRE");
                        fromPre.addFeature(StochasticTransitionFeature.newDeterministicInstance(BigDecimal.ZERO));
                        fromPre.addFeature(new TimedTransitionFeature("0", "0"));
                        fromPre.addFeature(new Priority(priority[0]++));
                        pn.addPostcondition(fromPre, fromIn);
                        actPre.put(from, fromPre);
                    }
                }

                if (opened.post().size() > 1 && from.pre().size() > 1) {  // use intermediate pOPENED_FROM
                    Transition openedPost = actPost.get(opened);
                    Transition fromPre = actPre.get(from);
                    Place openedFrom = pn.addPlace("p" + opened + "_" + from);
                    pn.addPostcondition(openedPost, openedFrom);
                    pn.addPrecondition(openedFrom, fromPre);

                } else if (opened.post().size() > 1) {  // add token directly to fromIn
                    Transition openedPost = actPost.get(opened);
                    Place fromIn = actIn.get(from);
                    pn.addPostcondition(openedPost, fromIn);

                } else if (from.pre().size() > 1) {  // take token directly from openedOut
                    Place openedOut = actOut.get(opened);
                    Transition fromPre = actPre.get(from);
                    pn.addPrecondition(openedOut, fromPre);

                } else {  // "opened" and "from" should share a place
                    Place openedFrom = pn.addPlace("p" + opened + "_" + from);
                    pn.removePlace(actOut.get(opened));
                    actOut.put(opened, openedFrom);
                    pn.removePlace(actIn.get(from));
                    actIn.put(from, openedFrom);
                }

                return true;  // continue
            }
        });

        // recursively add nested activities
        for (int i = act.size() - 1; i >= 0; i--) {
            Activity a = act.get(i);
            a.setMin(a.low());
            a.setMax(a.upp());

            if (a.equals(activity.begin())) {
                if (useBegin) {
                    pn.addPrecondition(in, actPost.get(a));
                }

            } else if (a.equals(activity.end())) {
                if (useEnd) {
                    pn.addPostcondition(actPre.get(a), out);
                }

            } else {
                Place aIn = actIn.get(a);
                if (aIn.equals(actOut.get(activity.begin())) && !useBegin)
                    aIn = in;
                Place aOut = actOut.get(a);
                if (aOut.equals(actIn.get(activity.end())) && !useEnd)
                    aOut = out;

                Transition t = pn.addTransition(a.name() + "_untimed");
                // A fake stochastic feature to make the timed analysis properly work.
                t.addFeature(StochasticTransitionFeature.newUniformInstance(a.min().toString(), a.max().toString()));
                t.addFeature(new TimedTransitionFeature(a.min().toString(), a.max().toString()));
                t.addFeature(new ConcurrencyTransitionFeature(a.C()));
                //t.addFeature(new RegenerationEpochLengthTransitionFeature(a.R()));
                pn.addPostcondition(t, aOut);
                pn.addPrecondition(aIn, t);

                //a.buildTimedPetriNet(pn, in, out, prio);
            }
        };
    }

    @Override
    public int buildSTPN(Composite activity, PetriNet pn, Place in, Place out, int prio) {
        Map<Activity, Place> actOut = new LinkedHashMap<>();
        Map<Activity, Transition> actPost = new LinkedHashMap<>();
        Map<Activity, Transition> actPre = new LinkedHashMap<>();
        Map<Activity, Place> actIn = new LinkedHashMap<>();
        List<Activity> act = new ArrayList<>();
        int[] priority = new int[] { prio };  // to access in closure

        boolean useBegin = activity.begin().post().size() > 1;
        boolean useEnd = activity.end().pre().size() > 1;

        activity.end().dfs(true, new DFSObserver() {
            @Override public boolean onSkip(Activity opened, Activity from) {
                return onOpenOrSkip(opened, from);
            }

            @Override public boolean onOpen(Activity opened, Activity from) {
                return onOpenOrSkip(opened, from);
            }

            private boolean onOpenOrSkip(Activity opened, Activity from) {
                if (opened.equals(activity.begin()) && from.equals(activity.end())) {
                    throw new IllegalStateException("Empty DAG");
                }

                if (!act.contains(opened)) {
                    // will be in visit order (END to BEGIN)
                    act.add(opened);
                }

                if (from == null) {
                    return true;  // END is not a real dependency, continue
                }

                // general structure:

                // [OPENED]    ->  (pOPENED_out)  -> [OPENED_POST]
                //             ->  (pOPENED_FROM) ->
                // [FROM_PRE]  ->  (pFROM_in)     -> [FROM]

                if (!actOut.containsKey(opened)) {
                    Place openedOut = opened.equals(activity.begin()) && useBegin ? in :
                            pn.addPlace("p" + opened + "_out");  // add pOPENED_out
                    actOut.put(opened, openedOut);

                    if (opened.post().size() > 1) {  // add pOPENED_out, OPENED_POST
                        Transition openedPost = pn.addTransition(opened + "_POST");
                        openedPost.addFeature(StochasticTransitionFeature.newDeterministicInstance(BigDecimal.ZERO));
                        openedPost.addFeature(new Priority(priority[0]++));
                        pn.addPrecondition(openedOut, openedPost);
                        actPost.put(opened, openedPost);
                    }
                }

                if (!actIn.containsKey(from)) {
                    Place fromIn = from.equals(activity.end()) && useEnd ? out :
                            pn.addPlace("p" + from + "_in");  // add pFROM_in
                    actIn.put(from, fromIn);

                    if (from.pre().size() > 1) {  // add FROM_PRE, pFROM_in
                        Transition fromPre = pn.addTransition(from + "_PRE");
                        fromPre.addFeature(StochasticTransitionFeature.newDeterministicInstance(BigDecimal.ZERO));
                        fromPre.addFeature(new Priority(priority[0]++));
                        pn.addPostcondition(fromPre, fromIn);
                        actPre.put(from, fromPre);
                    }
                }

                if (opened.post().size() > 1 && from.pre().size() > 1) {  // use intermediate pOPENED_FROM
                    Transition openedPost = actPost.get(opened);
                    Transition fromPre = actPre.get(from);
                    Place openedFrom = pn.addPlace("p" + opened + "_" + from);
                    pn.addPostcondition(openedPost, openedFrom);
                    pn.addPrecondition(openedFrom, fromPre);

                } else if (opened.post().size() > 1) {  // add token directly to fromIn
                    Transition openedPost = actPost.get(opened);
                    Place fromIn = actIn.get(from);
                    pn.addPostcondition(openedPost, fromIn);

                } else if (from.pre().size() > 1) {  // take token directly from openedOut
                    Place openedOut = actOut.get(opened);
                    Transition fromPre = actPre.get(from);
                    pn.addPrecondition(openedOut, fromPre);

                } else {  // "opened" and "from" should share a place
                    Place openedFrom = pn.addPlace("p" + opened + "_" + from);
                    pn.removePlace(actOut.get(opened));
                    actOut.put(opened, openedFrom);
                    pn.removePlace(actIn.get(from));
                    actIn.put(from, openedFrom);
                }

                return true;  // continue
            }
        });

        // recursively add nested activities
        for (int i = act.size() - 1; i >= 0; i--) {
            Activity a = act.get(i);

            if (a.equals(activity.begin())) {
                if (useBegin) {
                    pn.addPrecondition(in, actPost.get(a));
                }

            } else if (a.equals(activity.end())) {
                if (useEnd) {
                    pn.addPostcondition(actPre.get(a), out);
                }

            } else {
                Place aIn = actIn.get(a);
                if (aIn.equals(actOut.get(activity.begin())) && !useBegin)
                    aIn = in;
                Place aOut = actOut.get(a);
                if (aOut.equals(actIn.get(activity.end())) && !useEnd)
                    aOut = out;
                a.buildSTPN(pn, aIn, aOut, priority[0]++);
            }
        }

        return priority[0];
    }

    @Override
    public BigInteger computeQ(Composite activity, boolean getSimplified) {
        int simplifiedS = 0;
        int S = 0;
        for(Activity act: activity.activities()){
            simplifiedS += 1;
            S += act.Q().intValue();
        }

        activity.setQ(BigInteger.valueOf(S));
        activity.setSimplifiedQ(BigInteger.valueOf(simplifiedS));

        return getSimplified ? BigInteger.valueOf(simplifiedS) : BigInteger.valueOf(S);
    }

    public abstract void initPreconditions(Composite activity, Activity... children);
    public abstract void setEnumType(Composite activity);
}
