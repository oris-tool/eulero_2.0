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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.oristool.analyzer.graph.SuccessionGraph;
import org.oristool.analyzer.log.NoOpLogger;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trans.RegTransient;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.tpn.TimedAnalysis;
import org.oristool.models.tpn.TimedAnalysis.Builder;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.MarkingCondition;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Postcondition;
import org.oristool.petrinet.Precondition;
import org.oristool.petrinet.Transition;
import org.oristool.simulator.Sequencer;
import org.oristool.simulator.TimeSeriesRewardResult;
import org.oristool.simulator.rewards.ContinuousRewardTime;
import org.oristool.simulator.rewards.RewardEvaluator;
import org.oristool.simulator.stpn.STPNSimulatorComponentsFactory;
import org.oristool.simulator.stpn.TransientMarkingConditionProbability;
import org.oristool.util.Pair;

/**
 * Represents a node in an activity DAG.
 */
public abstract class Activity {
    private List<Activity> pre = new ArrayList<>();
    private List<Activity> post = new ArrayList<>();
    private String name;
    
    /**
     * The activities that this activity directly depends on.
     */
    public final List<Activity> pre() {
        return pre;
    }

    /**
     * The activities that directly depend on this one.
     */
    public final List<Activity> post() {
        return post;
    }

    /**
     * @param pre the preconditions to set
     */
    public void setPre(List<Activity> pre) {
        this.pre = pre;
    }
    
    /**
     * @param post the postconditions to set
     */
    public void setPost(List<Activity> post) {
        this.post = post;
    }
    
    /**
     * The name of this activity.
     */
    public final String name() {
        return name;
    }

    /** Activities that are part of this one */
    public List<Activity> nested() {
        return List.of();
    }

    public Activity(String name) {
        this.name = name;
    }
    
    public abstract Activity copyRecursive(String suffix);
    
    @Override
    public final String toString() {
        return name;
    }
    
    /**
     * Adds activities as a direct dependency; also adds this 
     * activity to the dependencies' post().  
     */
    public final void addPrecondition(Activity... others) {
        for (Activity other : others) {
            if (pre.contains(other))
                throw new IllegalArgumentException(other + " already present in " + this);
            if (other.post.contains(this))
                throw new IllegalArgumentException(this + " already present in " + other);
            pre.add(other);
            other.post.add(this);
        }
    }
       
    /**
     * Removes an activity as a direct dependency; also removes
     * this activity from the dependency's post().  
     */
    public final void removePrecondition(Activity other) {
        if (!pre.remove(other))
            throw new IllegalArgumentException(other + " not present in " + this);
        if (!other.post.remove(this))
            throw new IllegalArgumentException(this + " not present in " + other);
    }
    
    /**
     * Replaces this activity with another in all pre/post.  
     */
    public final void replace(Activity withOther) {
        for (Activity p : new ArrayList<>(pre)) {
            this.removePrecondition(p);
            withOther.addPrecondition(p);
        }

        for (Activity p : new ArrayList<>(post)) {
            p.removePrecondition(this);
            p.addPrecondition(withOther);
        }
    }

    /**
     * Explores this activity and the ones that it transitively depends on (pre
     * == true) or that depend on it (pre == false), in DFS order. Returns false
     * if the visit is interrupted by the observer.
     */
    public final boolean dfs(boolean pre, DFSObserver observer) {
        
        Set<Activity> opened = new HashSet<>();
        Deque<Activity> open = new ArrayDeque<>();
        Deque<Iterator<Activity>> openAdj = new ArrayDeque<>();

        opened.add(this);
        if (!observer.onOpen(this, null))
            return false;       
        open.push(this);
        openAdj.push(pre ? this.pre().iterator() : this.post().iterator());

        while (!openAdj.isEmpty()) {
            Activity from = open.peek();
            if (openAdj.peek().hasNext()) {
                Activity next = openAdj.peek().next();
                if (!opened.contains(next)) {
                    // new node, mark as opened
                    opened.add(next);
                    if (!observer.onOpen(next, from))
                        return false;
                    // start exploring now
                    open.push(next);
                    openAdj.push(pre ? next.pre().iterator() : next.post().iterator());
                } else {
                    // already opened, skip to break loops
                    if (!observer.onSkip(next, from))
                        return false;
                }
            } else {
                // explored all reachable successors, close
                open.pop();
                openAdj.pop();
                if (!observer.onClose(from))
                    return false;
            }
        }
        
        return true;
    }

    /**
     * Explores, in DFS order, this activity and the activities that it
     * transitively depends on. If any activity is has nested components, 
     * explore them first.
     * 
     * Returns false if the visit is interrupted by the observer.
     */
    public final boolean dfsNestedDepth(boolean pre, DFSNestedObserver observer) {
        
        observer.onNestedStart(this);
        this.dfs(pre, new DFSObserver() {
            @Override public boolean onOpen(Activity opened, Activity from) {
                if (!observer.onOpen(opened, from))
                    return false;
                
                for (Activity nested : opened.nested()) {
                    nested.dfsNestedDepth(pre, observer);
                }
                
                return true;
            }
            
            @Override public boolean onSkip(Activity skipped, Activity from) {
                return observer.onSkip(skipped, from);
            }
            @Override public boolean onClose(Activity closed) {
                return observer.onClose(closed);
            }
        });
        observer.onNestedEnd(this);
        
        return true;
    }
    
    /**
     * Explores, in DFS order, this activity and the activities that it
     * transitively depends on. Then explores their nested activities,
     * recursively (one nesting level at a time).
     * 
     * Returns false if the visit is interrupted by the observer.
     */
    public final boolean dfsNestedLayers(boolean pre, DFSNestedObserver observer) {
        Deque<Activity> nested = new ArrayDeque<>();
     
        nested.add(this);
        while (!nested.isEmpty()) {
            Activity next = nested.removeFirst();
            
            if (!observer.onNestedStart(next))
                return false;
            
            next.dfs(pre, new DFSObserver() {
                @Override public boolean onOpen(Activity opened, Activity from) {
                    if (opened.nested().size() > 0)
                        nested.addAll(opened.nested());
                    
                    return observer.onOpen(opened, from);
                }
                
                @Override public boolean onClose(Activity closed) {
                    return observer.onClose(closed);
                }
                
                @Override public boolean onSkip(Activity skipped, Activity from) {
                    return observer.onSkip(skipped, from);
                }
            });
            
            if (!observer.onNestedEnd(next))
                return false;
        }
        
        return true;
    }
   
    /**
     * Returns all activities explored by dfsNestedLayers.
     */
    public final List<Activity> activitiesNestedLayers(boolean pre) {
        List<Activity> visited = new ArrayList<>();
        
        this.dfsNestedLayers(pre, new DFSNestedObserver() {
            @Override
            public boolean onOpen(Activity opened, Activity from) {
                visited.add(opened);
                return true;
            }
        });
        
        return visited;
    }
    
    /**
     * Represents the activity as YAML.
     */
    public final String yaml() {
        StringBuilder b = new StringBuilder();
        b.append(this.name() + ":\n");
        b.append("  type: " + this.getClass().getSimpleName() + "\n");
        b.append("  data:\n" + this.yamlData().replaceAll("(?m)^", "  "));
        return b.toString();
    }

    /**
     * Represents the activity data as YAML.
     */
    public String yamlData() {
        return "";
    }
    
    /**
     * Represents the activity and its nested components as YAML.
     */
    public final String yamlRecursive() {
        StringBuilder b = new StringBuilder();
        b.append(yaml());
        
        List<Activity> nested = this.nested();
        if (nested.size() > 0) {
            b.append("  nested:\n");
        }
        
        for (Activity a : nested) {
            b.append(a.yamlRecursive().replaceAll("(?m)^", "    "));
        }
        
        return b.toString();
    }
    
    /**
     * Adds the activity as an STPN transition.
     * 
     * @param petriNet STPN where the activity is added
     * @param priority initial priority of the transitions of this activity 
     * @return next priority level for the rest of the network
     */
    public abstract int addPetriBlock(PetriNet pn, Place in, Place out, int prio);
    
    /**
     * Returns a string representation of the preconditions and postconditions 
     * for the STPN of this activity.
     * 
     * @return string representation of STPN edges 
     */
    public String petriArcs() {
        
        PetriNet pn = new PetriNet();
        Place in = pn.addPlace("pBEGIN");
        Place out = pn.addPlace("pEND");
        this.addPetriBlock(pn, in, out, 1);
        
        StringBuilder b = new StringBuilder();
        for (Transition t : pn.getTransitions()) {
            for (Precondition p : pn.getPreconditions(t))
                b.append(p.getPlace() + " " + p.getTransition() + "\n");
            for (Postcondition p : pn.getPostconditions(t))
                System.out.println(p.getTransition() + " " + p.getPlace());
        }
        
        return b.toString();
    }
    
    public TransientSolution<DeterministicEnablingState, RewardRate> 
            analyze(String timeBound, String timeStep, String error) {
        
        // input data
        BigDecimal bound = new BigDecimal(timeBound);
        BigDecimal step = new BigDecimal(timeStep);
        BigDecimal epsilon = new BigDecimal(error);
        String cond = "pEND > 0";

        // build STPN
        PetriNet pn = new PetriNet();
        Place in = pn.addPlace("pBEGIN");
        Place out = pn.addPlace("pEND");
        this.addPetriBlock(pn, in, out, 1);
        
        Marking m = new Marking();
        m.addTokens(in, 1);
        
        // analyze
        RegTransient.Builder builder = RegTransient.builder();
        builder.timeBound(bound);
        builder.timeStep(step);
        builder.greedyPolicy(bound, epsilon);
        builder.markingFilter(MarkingCondition.fromString(cond));

        RegTransient analysis = builder.build();
        long start = System.nanoTime(); 
        TransientSolution<DeterministicEnablingState, Marking> probs =
                analysis.compute(pn, m);
        System.out.println(String.format("Analysis took %.3f seconds",
                (System.nanoTime() - start)/1e9));

        // evaluate reward
        return TransientSolution.computeRewards(false, probs, 
                RewardRate.fromString(cond));
    }
    
    public Pair<SuccessionGraph, PetriNet> classGraph() {
        
        // input data
        String cond = "pEND > 0";

        // build STPN
        PetriNet pn = new PetriNet();
        Place in = pn.addPlace("pBEGIN");
        Place out = pn.addPlace("pEND");
        this.addPetriBlock(pn, in, out, 1);
        
        Marking m = new Marking();
        m.addTokens(in, 1);
        
        // analyze
        Builder builder = TimedAnalysis.builder()
                .excludeZeroProb(true)
                .markRegenerations(true)
                .stopOn(MarkingCondition.fromString(cond));
               
        TimedAnalysis analysis = builder.build();
        SuccessionGraph graph = analysis.compute(pn, m);
        
        return Pair.of(graph, pn);
    }
    
    public TransientSolution<DeterministicEnablingState, RewardRate> 
            simulate(String timeBound, String timeStep, int runs) {
        
        // input data
        BigDecimal bound = new BigDecimal(timeBound);
        BigDecimal step = new BigDecimal(timeStep);
        int samples = bound.divide(step).intValue() + 1;
        String cond = "pEND > 0";

        // build STPN
        PetriNet pn = new PetriNet();
        Place in = pn.addPlace("pBEGIN");
        Place out = pn.addPlace("pEND");
        this.addPetriBlock(pn, in, out, 1);

        Marking m = new Marking();
        m.addTokens(in, 1);
        
        // simulate
        System.out.println("Starting Simulation...");
        Sequencer s = new Sequencer(pn, m, 
                new STPNSimulatorComponentsFactory(), NoOpLogger.INSTANCE);
        TransientMarkingConditionProbability reward = 
                new TransientMarkingConditionProbability(s, 
                    new ContinuousRewardTime(step), samples, 
                    MarkingCondition.fromString(cond));
        RewardEvaluator rewardEvaluator = new RewardEvaluator(reward, runs);
        long start = System.nanoTime(); 
        s.simulate();
        System.out.println(String.format("Simulation took %.3f seconds",
                (System.nanoTime() - start)/1e9));
        
        // evaluate reward
        TimeSeriesRewardResult probs = (TimeSeriesRewardResult) rewardEvaluator.getResult();
        DeterministicEnablingState initialReg = new DeterministicEnablingState(m, pn);
        TransientSolution<DeterministicEnablingState, RewardRate> result = 
                new TransientSolution<>(bound, step, List.of(initialReg), 
                        List.of(RewardRate.fromString(cond)), initialReg);
        
        for (int t = 0; t < result.getSolution().length; t++) {
            for (Marking x : probs.getMarkings()) {
                result.getSolution()[t][0][0] += probs.getTimeSeries(x)[t].doubleValue();
            }
        }

        return result;
    }
}
