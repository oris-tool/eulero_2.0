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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    
    @Override
    public final String toString() {
        return name;
    }
    
    /**
     * Adds an activity as a direct dependency; also adds this 
     * activity to the dependecy's depedencyOf.  
     */
    public final void addPrecondition(Activity other) {
        pre.add(other);
        other.post.add(this);
    }
    
    /**
     * Removes an activity as a direct dependency; also removes
     * this activity from the dependecy's depedencyOf.  
     */
    public final void removePrecondition(Activity other) {
        if (!pre.remove(other))
            throw new IllegalArgumentException(other + " not present in " + this);
        if (!other.post.remove(this))
            throw new IllegalArgumentException(this + " not present in " + other);
    }
    
    /**
     * Replaces the i-th dependency of this activity.
     * The current i-th activity is also updated.  
     */
    public final void replacePrecondition(int index, Activity other) {
        pre.get(index).pre.remove(this);
        pre.set(index, other);
        other.post.add(this);
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
                    if (!observer.onSkipped(next, from))
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
            
            @Override public boolean onSkipped(Activity skipped, Activity from) {
                return observer.onSkipped(skipped, from);
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
                
                @Override public boolean onSkipped(Activity skipped, Activity from) {
                    return observer.onSkipped(skipped, from);
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
}
