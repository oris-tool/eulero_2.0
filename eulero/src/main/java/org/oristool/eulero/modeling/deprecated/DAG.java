/* This program is called EULERO.
 * Copyright (C) 2022 The EULERO Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

    package org.oristool.eulero.modeling.deprecated;

import com.google.common.collect.Lists;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.modeling.activitytypes.ActivityEnumType;
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

    /**
     * DAG: A graph of activities
     */
    @XmlRootElement(name = "DAG")
    @XmlSeeAlso({AND.class, SEQ.class})
    public class DAG extends Activity {
        @XmlElement(name = "begin", required = true)
        private Activity begin;

        @XmlElement(name = "end", required = true)
        private Activity end;

        @XmlElementWrapper(name = "edges")
        @XmlElement(name = "edge", required = true)
        private List<DAGEdge> edges = new ArrayList<>();

        /**
         * An empty DAG
         */
        public static DAG empty(String name) {
            return new DAG(name);
        }

        /**
         * Builds a DAG with the given activities in sequence.
         */
        public static DAG sequence(String name, Activity... activities) {

            if (activities.length == 0)
                throw new IllegalArgumentException("Sequence cannot be empty");

            DAG dag = new SEQ(name, Arrays.asList(activities));

            Activity prev = dag.begin();
            for (Activity a : activities) {
                a.addPrecondition(prev);
                prev = a;
            }

            dag.end().addPrecondition(prev);

            dag.setMin(dag.low());
            dag.setMax(dag.upp());

            return dag;
        }

        /**
         * Builds a DAG starting the given activities in parallel, then
         * synchronizing on them.
         */
        public static DAG forkJoin(String name, Activity... activities) {

            if (activities.length == 0)
                throw new IllegalArgumentException("Parallel cannot be empty");

            double low = 0;
            double upp = 0;
            for(Activity activity: activities){
                low = Math.max(low, activity.low().doubleValue());
                upp = Math.max(upp, activity.upp().doubleValue());
            }

            DAG dag = new AND(name, Arrays.asList(activities));

            for (Activity a : activities) {
                a.addPrecondition(dag.begin());
                dag.end().addPrecondition(a);
            }

            dag.setMin(dag.low());
            dag.setMax(dag.upp());

            return dag;
        }

        public DAG(){
            super("");
            setEnumType(ActivityEnumType.DAG);
        };

        protected DAG(String name) {  // force use of static methods
            super(name);
            this.begin = new Simple(name + "_BEGIN",
                    StochasticTransitionFeature.newDeterministicInstance(BigDecimal.ZERO));
            this.end = new Simple(name + "_END",
                    StochasticTransitionFeature.newDeterministicInstance(BigDecimal.ZERO));
            setEnumType(ActivityEnumType.DAG);
        }

        @Override public DAG copyRecursive(String suffix) {
            return copyRecursive(begin(), end(), suffix);
        }

        @Override
        public void resetSupportBounds() {
            setMin(getMinBound(this.end));
            setMax(getMaxBound(this.end));
        }

        @Override
        public void buildTPN(PetriNet pn, Place in, Place out, int prio) {
            Map<Activity, Place> actOut = new LinkedHashMap<>();
            Map<Activity, Transition> actPost = new LinkedHashMap<>();
            Map<Activity, Transition> actPre = new LinkedHashMap<>();
            Map<Activity, Place> actIn = new LinkedHashMap<>();
            List<Activity> act = new ArrayList<>();
            int[] priority = new int[] { prio };
            //int[] priority = new int[] { prio };  // to access in closure

            boolean useBegin = begin().post().size() > 1;
            boolean useEnd = end().pre().size() > 1;

            this.end().dfs(true, new DFSObserver() {
                @Override public boolean onSkip(Activity opened, Activity from) {
                    return onOpenOrSkip(opened, from);
                }

                @Override public boolean onOpen(Activity opened, Activity from) {
                    return onOpenOrSkip(opened, from);
                }

                private boolean onOpenOrSkip(Activity opened, Activity from) {
                    if (opened.equals(begin()) && from.equals(end())) {
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
                        Place openedOut = opened.equals(begin()) && useBegin ? in :
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
                        Place fromIn = from.equals(end()) && useEnd ? out :
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

                if (a.equals(begin())) {
                    if (useBegin) {
                        pn.addPrecondition(in, actPost.get(a));
                    }

                } else if (a.equals(end())) {
                    if (useEnd) {
                        pn.addPostcondition(actPre.get(a), out);
                    }

                } else {
                    Place aIn = actIn.get(a);
                    if (aIn.equals(actOut.get(begin())) && !useBegin)
                        aIn = in;
                    Place aOut = actOut.get(a);
                    if (aOut.equals(actIn.get(end())) && !useEnd)
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

        /**
         * Fictitious initial activity for this DAG.
         *
         * @return begin activity
         */
        public Activity begin() {
            return begin;
        }

        /**
         * Fictitious final activity for this DAG.
         *
         * @return end activity
         */
        public Activity end() {
            return end;
        }

        /**
         * Checks whether the activity graph and its subgraphs are single-entry,
         * single-exit DAGs, without repeated activity names.
         *
         * @return a list of problems
         */

        public List<String> problems() {

            List<String> problems = new ArrayList<>();
            Set<String> activities = new HashSet<>();
            Set<String> repeated = new HashSet<>();
            Deque<DAG> nested = new ArrayDeque<>();

            nested.add(this);
            while (!nested.isEmpty()) {
                // explore the next DAG
                DAG next = nested.removeFirst();
                next.end().dfs(true, new DFSObserver() {
                    @Override public boolean onOpen(Activity opened, Activity from) {
                        if (opened.equals(begin()) && from.equals(end())) {
                            throw new IllegalStateException("Empty DAG");
                        }

                        if (!activities.add(opened.toString())) {
                            repeated.add(opened.toString());
                        } else {
                            if (opened instanceof DAG) {
                                nested.addLast((DAG)opened);
                            }
                        }
                        return true;  // continue
                    }

                    @Override public boolean onSkip(Activity skipped, Activity from) {
                        problems.add(String.format("%s: Cycle from %s to %s", nested, from, skipped));
                        return true;  // continue
                    }
                });
            }


            for (String activity : repeated)
                problems.add("Repeated activity name " + activity);

            return problems;
        }

        public List<Activity> nested() {
            List<Activity> activities = new ArrayList<>();

            this.begin().dfs(false, new DFSObserver() {
                @Override public boolean onOpen(Activity opened, Activity from) {
                    if (opened != begin() && opened != end()) {
                        activities.add(opened);
                    }

                    return true;  // continue
                }
            });

            return activities;
        }

        public void flatten() {
            boolean flat[] = new boolean[] { false };

            while (!flat[0]) {
                List<DAG> nested = new ArrayList<>();

                this.end().dfs(true, new DFSObserver() {
                    @Override public boolean onOpen(Activity opened, Activity from) {
                        if (opened instanceof DAG) {
                            nested.add((DAG)opened);
                        }

                        return true;  // continue
                    }
                });

                for (DAG dag : nested) {
                    List<Activity> nestedPre = List.copyOf(dag.pre());
                    List<Activity> nestedPost = List.copyOf(dag.post());
                    List<Activity> nestedBeginPost = List.copyOf(dag.begin().post());
                    List<Activity> nestedEndPre = List.copyOf(dag.end().pre());

                    for (Activity pre : nestedPre) {
                        // move incoming edge from DAG to its begin
                        dag.removePrecondition(pre);
                        for (Activity beginPost : nestedBeginPost) {
                            beginPost.addPrecondition(pre);
                            beginPost.removePrecondition(dag.begin());
                        }
                    }

                    for (Activity post : nestedPost) {
                        // move outgoing edge from DAG to its end
                        post.removePrecondition(dag);
                        for (Activity endPre : nestedEndPre) {
                            post.addPrecondition(endPre);
                            dag.end().removePrecondition(endPre);
                        }
                    }
                }

                flat[0] = nested.size() == 0;  // end when no nested DAGs are found
            }
        }

        @Override
        public String yamlData() {
            Map<String, String> edges = new LinkedHashMap<>();
            this.begin().dfs(false, new DFSObserver() {
                void addEdge(Activity from, Activity to) {
                    if (from != null) {
                        edges.merge(from.name(), to.name(), (x, y) -> x + ", " + y);
                    }
                }

                @Override public boolean onOpen(Activity opened, Activity from) {
                    addEdge(from, opened);
                    return true;
                }

                @Override
                public boolean onSkip(Activity skipped, Activity from) {
                    addEdge(from, skipped);
                    return true;
                }
            });

            StringBuilder b = new StringBuilder();
            for (Map.Entry<String, String> e : edges.entrySet()) {
                b.append("  " + e.getKey() + ": [" + e.getValue() + "]\n");
            }
            return b.toString();
        }

        @Override
        public void addPrecondition(Activity... others) {
            super.addPrecondition(others);
        }

        @Override
        public int buildSTPN(PetriNet pn, Place in, Place out, int prio) {
            Map<Activity, Place> actOut = new LinkedHashMap<>();
            Map<Activity, Transition> actPost = new LinkedHashMap<>();
            Map<Activity, Transition> actPre = new LinkedHashMap<>();
            Map<Activity, Place> actIn = new LinkedHashMap<>();
            List<Activity> act = new ArrayList<>();
            int[] priority = new int[] { prio };  // to access in closure

            boolean useBegin = begin().post().size() > 1;
            boolean useEnd = end().pre().size() > 1;

            this.end().dfs(true, new DFSObserver() {
                @Override public boolean onSkip(Activity opened, Activity from) {
                    return onOpenOrSkip(opened, from);
                }

                @Override public boolean onOpen(Activity opened, Activity from) {
                    return onOpenOrSkip(opened, from);
                }

                private boolean onOpenOrSkip(Activity opened, Activity from) {
                    if (opened.equals(begin()) && from.equals(end())) {
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
                        Place openedOut = opened.equals(begin()) && useBegin ? in :
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
                        Place fromIn = from.equals(end()) && useEnd ? out :
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

                if (a.equals(begin())) {
                    if (useBegin) {
                        pn.addPrecondition(in, actPost.get(a));
                    }

                } else if (a.equals(end())) {
                    if (useEnd) {
                        pn.addPostcondition(actPre.get(a), out);
                    }

                } else {
                    Place aIn = actIn.get(a);
                    if (aIn.equals(actOut.get(begin())) && !useBegin)
                        aIn = in;
                    Place aOut = actOut.get(a);
                    if (aOut.equals(actIn.get(end())) && !useEnd)
                        aOut = out;
                    a.buildSTPN(pn, aIn, aOut, priority[0]++);
                }
            }

            return priority[0];
        }

        @Override
        public double[] analyze(BigDecimal timeLimit, BigDecimal timeStep, AnalysisHeuristicsVisitor visitor) {
            return new double[0];
        }

        /**
         * Returns the activities between {@code begin} and
         * {@code end} (inclusive)
         *
         * @param begin  initial activity
         * @param end    final activity
         * @return       activities in-between (including BEGIN, END)
         */
        public Set<Activity> activitiesBetween(Activity begin, Activity end) {

            Set<Activity> activitiesBetween = new HashSet<>();
            Set<Activity> nodesOpen = new HashSet<>();
            activitiesBetween.add(begin);

            end.dfs(true, new DFSObserver() {
                @Override public boolean onOpen(Activity opened, Activity from) {
                    nodesOpen.add(opened);
                    if (activitiesBetween.contains(opened)) {
                        // all open nodes are between "begin" and "end"
                        activitiesBetween.addAll(nodesOpen);
                    }

                    return true;  // continue
                }

                @Override public boolean onSkip(Activity skipped, Activity from) {
                    if (activitiesBetween.contains(skipped)) {
                        // all open nodes are between "begin" and "end"
                        activitiesBetween.addAll(nodesOpen);
                    }

                    return true;
                }

                @Override public boolean onClose(Activity closed) {
                    nodesOpen.remove(closed);
                    if (closed.equals(end))
                        return false;  // stop
                    else
                        return true;  // continue
                }
            });

          return activitiesBetween;
        }

        /**
         * Creates a DAG with a copy of the activities between {@code begin} and
         * {@code end} (inclusive). If nested activities are found, they are
         * duplicated too, recursively.
         *
         * @param begin  initial activity (replaced with BEGIN in the copy)
         * @param end    final activity (replaced with END in the copy)
         * @param suffix suffix to be added to the duplicated activities and DAGs
         * @return       a DAG with duplicated activities between "from" and "to"
         */
        public DAG copyRecursive(Activity begin, Activity end, String suffix) {
            DAG copy = new DAG(this.name() + suffix);
            Map<Activity, Activity> nodeCopies = new HashMap<>();

            if (begin().equals(begin)) {
                nodeCopies.put(begin, copy.begin());
            } else {
                Activity ax = begin.copyRecursive(suffix);
                nodeCopies.put(begin, ax);
                ax.addPrecondition(copy.begin());
            }

            if (end().equals(end)) {
                nodeCopies.put(end, copy.end());
            } else {
                Activity ax = end.copyRecursive(suffix);
                nodeCopies.put(end, ax);
                copy.end().addPrecondition(ax);
            }

            Set<Activity> activitiesBetween = activitiesBetween(begin, end);
            ArrayList<Activity> createdActivities = new ArrayList<>();
            for (Activity a : activitiesBetween) {
                Activity ax = nodeCopies.computeIfAbsent(a, k -> k.copyRecursive(suffix));
                if (!a.equals(begin)) {
                    List<Activity> aprex = a.pre().stream()
                            .filter(p -> activitiesBetween.contains(p))
                            .map(p -> nodeCopies.computeIfAbsent(p, k -> k.copyRecursive(suffix)))
                            .collect(Collectors.toCollection(ArrayList::new));
                    ax.setPre(aprex);

                    createdActivities.add(ax);
                }

                if (!a.equals(end)) {
                    List<Activity> apostx = a.post().stream()
                            .filter(p -> activitiesBetween.contains(p))
                            .map(p -> nodeCopies.computeIfAbsent(p, k -> k.copyRecursive(suffix)))
                            .collect(Collectors.toCollection(ArrayList::new));
                    ax.setPost(apostx);

                    createdActivities.add(ax);
                }
            }

            copy.setMin(copy.low());
            copy.setMax(copy.upp());
            copy.setActivities(createdActivities);
            return copy;
        }

        /**
         * Removes all activities between {@code begin} and {@code end} if they are
         * not pre/post of other activities.
         *
         * @param begin          initial activity
         * @param end            final activity
         * @param removeShared  whether to remove nodes also reachable without end
         */
        public void removeBetween(Activity begin, Activity end, boolean removeShared) {

            Set<Activity> activitiesBetween = this.activitiesBetween(begin, end);

            if (!removeShared) {
                for (Activity p : new ArrayList<>(end.post())) {
                    p.removePrecondition(end);
                }

                activitiesBetween.removeAll(activitiesBetween(begin(), end()));
            }

            List<Activity> all = this.nested();
            all.add(this.begin());
            all.add(this.end());

            for (Activity a : all) {
                List<Activity> pre = a.pre().stream()
                        .filter(x -> !activitiesBetween.contains(a) &&
                                     !activitiesBetween.contains(x))
                        .collect(Collectors.toCollection(ArrayList::new));
                a.setPre(pre);

                List<Activity> post = a.post().stream()
                        .filter(x -> !activitiesBetween.contains(a) &&
                                     !activitiesBetween.contains(x))
                        .collect(Collectors.toCollection(ArrayList::new));
                a.setPost(post);
            }
        }

        /**
         * Moves all predecessors of the given activity into a nested DAG.
         *
         * This operation is supported only for preconditions of {@code this.end()}.
         * Predecessors are duplicated with the "_N" suffix and removed from this DAG
         * if not used by other activities.
         *
         * @param end activity to nest
         * @return the nested DAG with endPre and its predecessors
         */
        public DAG nest(Activity end) {
            DAG copy = this.copyRecursive(this.begin(), end, "_up".replace(this.name(),""));

            this.removeBetween(this.begin(), end, false);
            DAG restOfDAG = this.copyRecursive("_down");

            Activity up = copy;
            Activity down = restOfDAG;

            if(copy.checkWellNesting()){
                up = wellNestIt(copy.begin().post());
            }

            if(restOfDAG.checkWellNesting()){
                down = wellNestIt(restOfDAG.begin().post());
            }

            return DAG.forkJoin(this.name() + "_N",
                    up,
                    down
            );
        }

        public boolean checkWellNesting(){
            Deque<Set<Activity>> levelNode = new LinkedList<>();
            levelNode.push(Collections.singleton(this.begin()));

            while(!levelNode.isEmpty()) {
                Set<Activity> thisLevelActivities = levelNode.pop();
                Set<Activity> nextLevelActivities = new HashSet<>();

                for (Activity act : thisLevelActivities) {
                    Set<Activity> theOtherNodes = new HashSet<>(thisLevelActivities);
                    theOtherNodes.remove(act);

                    for (Activity other : theOtherNodes) {
                        boolean samePredecessors = act.pre().containsAll(other.pre()) && act.pre().size() == 1 && other.pre().size() == 1;
                        boolean disjointPrecedessors = act.pre().stream().filter(other.pre()::contains).collect(Collectors.toList()).isEmpty();

                        boolean isWellNested = (samePredecessors || disjointPrecedessors);

                        if(!isWellNested){
                            return false;
                        }
                    }

                    nextLevelActivities.addAll(act.post());
                }

                if(!nextLevelActivities.isEmpty()){
                    levelNode.push(nextLevelActivities);
                }
            }

            return true;
        }

        /* Pass begin of dag as first parameter*/
        public static Activity wellNestIt(List<Activity> nodes){
            if (nodes.size() > 1){
                Set<Activity> theseNodeSuccessors = new HashSet<>();
                // Get nodes from successive level
                for(Activity act: nodes){
                    theseNodeSuccessors.addAll(act.post());
                }

                // If nodes share a single successor, and this is an END, then create a fork-join
                if (theseNodeSuccessors.size() == 1){
                    StringBuilder name = new StringBuilder("AND(");
                    for(Activity act: nodes){
                        name.append((nodes.indexOf(act) == nodes.size() - 1) ? act.name() + ")" : act.name() + ", ");
                    }
                    Activity forkJoin = DAG.forkJoin(name.toString(), nodes.toArray(Activity[]::new));

                    if(!List.copyOf(theseNodeSuccessors).get(0).max().equals(BigDecimal.ZERO)){
                        ArrayList<Activity> theSequence = new ArrayList<>();
                        theSequence.add(forkJoin);
                        Activity sequenceLastNode = wellNestIt(Lists.newArrayList(theseNodeSuccessors));
                        theSequence.add(sequenceLastNode);

                        return DAG.sequence("SEQ(" + name + ", " + sequenceLastNode.name() +")", theSequence.toArray(Activity[]::new));
                    }
                    return forkJoin;
                }

                StringBuilder name = new StringBuilder("AND(");
                ArrayList<Activity> forkJoinNodes = new ArrayList<>();
                for(Activity act: nodes) {
                    Activity andNode = wellNestIt(Lists.newArrayList(act));
                    forkJoinNodes.add(andNode);
                    name.append((nodes.indexOf(act) == nodes.size() - 1) ? andNode.name() + ")" : andNode.name() + ", ");
                }

                return DAG.forkJoin(name.toString(), forkJoinNodes.toArray(Activity[]::new));
            }

            if(nodes.size() == 1 && nodes.get(0).post().size() > 1){
                 List<Activity> sequenceNodes = new ArrayList<>();
                 StringBuilder name = new StringBuilder("SEQ(");
                 sequenceNodes.add(nodes.get(0));
                 sequenceNodes.add(wellNestIt(nodes.get(0).post()));
                 List<Activity> joinPoint = findJoinPoint(nodes.get(0));
                 if(!joinPoint.isEmpty() && !joinPoint.get(0).max().equals(BigDecimal.ZERO)){
                     sequenceNodes.add(wellNestIt(joinPoint));
                 }

                 for(Activity act: sequenceNodes){
                     name.append((sequenceNodes.indexOf(act) == sequenceNodes.size() - 1) ? act.name() + ")" : act.name() + ", ");
                 }
                 return DAG.sequence(name.toString(), sequenceNodes.toArray(Activity[]::new));
            }

            if(nodes.size() == 1 && nodes.get(0).post().size() == 1 && !nodes.get(0).post().get(0).max().equals(BigDecimal.ZERO)){
                List<Activity> sequenceNodes = new ArrayList<>();
                StringBuilder name = new StringBuilder("SEQ(");
                sequenceNodes.add(nodes.get(0));
                sequenceNodes.add(wellNestIt(nodes.get(0).post()));

                for(Activity act: sequenceNodes){
                    act.pre().clear();
                    act.post().clear();
                    name.append((sequenceNodes.indexOf(act) == sequenceNodes.size() - 1) ? act.name() + ")" : act.name() + ", ");
                }
                return DAG.sequence(name.toString(), sequenceNodes.toArray(Activity[]::new));
            }

            return nodes.get(0);
        }

        public static List<Activity> findJoinPoint(Activity startingNode){
            // La lista ritornata contiene o 0 o 1 elementi
            Set<Activity> theSetOfSuccessors = new HashSet<>();
            theSetOfSuccessors.addAll(startingNode.post());

            while(theSetOfSuccessors.size() > 1){
                Set<Activity> nextSetOfSuccessors = new HashSet<>();
                for(Activity act: theSetOfSuccessors){
                    nextSetOfSuccessors.addAll(act.post().stream().filter(t -> !t.max().equals(BigDecimal.ZERO)).collect(Collectors.toList()));
                }

                theSetOfSuccessors = nextSetOfSuccessors;
            }
            return List.copyOf(theSetOfSuccessors);
        }

        @Override
        public BigDecimal low() {
            return getMinBound(this.end);
        }

        @Override
        public BigDecimal upp() {
            return getMaxBound(this.end);
        }

        public List<DAGEdge> edges() {
            return edges;
        }

        @Override
        public boolean isWellNested() {
            return false;
        }

        public BigDecimal getMinBound(Activity activity){
            if(activity.equals(this.begin)){
                return activity.low();
            }

            BigDecimal maximumPredecessorLow = BigDecimal.ZERO;
            for(Activity predecessor: activity.pre()){
                maximumPredecessorLow = maximumPredecessorLow.max(getMinBound(predecessor));
            }

            return activity.low().add(maximumPredecessorLow);
        }

        public BigDecimal getMaxBound(Activity activity){
            if(activity.equals(this.begin)){
                return activity.upp();
            }

            BigDecimal maximumPredecessorUpp = BigDecimal.ZERO;
            for(Activity predecessor: activity.pre()){
                maximumPredecessorUpp = maximumPredecessorUpp.max(getMaxBound(predecessor));
                if(maximumPredecessorUpp.doubleValue() >= Double.MAX_VALUE){
                    return BigDecimal.valueOf(Double.MAX_VALUE);
                }
            }

            return activity.upp().add(maximumPredecessorUpp);
        }

        public void setEdges( List<DAGEdge> edges){
            this.edges = edges;
        }

        @Override
        public BigInteger computeQ(boolean getSimplified) {
            int simplifiedS = 0;
            int S = 0;
            for(Activity act: activities()){
                simplifiedS += 1;
                S += act.Q().intValue();
            }
            setQ(BigInteger.valueOf(S));
            setSimplifiedQ(BigInteger.valueOf(simplifiedS));

            return getSimplified ? BigInteger.valueOf(simplifiedS) : BigInteger.valueOf(S);
        }
    }
