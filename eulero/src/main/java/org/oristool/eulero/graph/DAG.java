/* This program is part of the ORIS Tool.
 * Copyright (C) 2011-2020 The ORIS Authors.
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

package org.oristool.eulero.graph;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

/**
 * DAG: A graph of activities
 */
public class DAG extends Activity {
    private final Activity begin;
    private final Activity end;       

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
        
        DAG dag = new DAG(name);
        
        Activity prev = dag.begin();
        for (Activity a : activities) {
            a.addPrecondition(prev);
            prev = a;
        }
        
        dag.end().addPrecondition(prev);
        
        return dag;
    }

    /**
     * Builds a DAG starting the given activities in parallel, then
     * synchronizing on them.
     */
    public static DAG forkJoin(String name, Activity... activities) {
        
        if (activities.length == 0)
            throw new IllegalArgumentException("Parallel cannot be empty");        

        DAG dag = new DAG(name);
        
        for (Activity a : activities) {
            a.addPrecondition(dag.begin());
            dag.end().addPrecondition(a);
        }
        
        return dag;
    }

    private DAG(String name) {  // force use of static methods
        super(name);
        this.begin = new Analytical(name + "_BEGIN", 
                StochasticTransitionFeature.newDeterministicInstance(BigDecimal.ZERO));
        this.end = new Analytical(name + "_END",
                StochasticTransitionFeature.newDeterministicInstance(BigDecimal.ZERO));
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
    public int addPetriBlock(PetriNet pn, Place in, Place out, int prio) {
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
                a.addPetriBlock(pn, aIn, aOut, priority[0]++);
            }
        }
        
        return priority[0];
    }
}
