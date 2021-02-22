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
 * Represents the duration of an activity through its min, max and CDF.
 */
public abstract class Activity {
    
    private List<Activity> dependencies = new ArrayList<>();
    private List<Activity> dependencyOf = new ArrayList<>();
    
    /**
     * The activities that this activity directly depends on.
     */
    public List<Activity> dependencies() {
        return dependencies;
    }

    /**
     * The activities that directly depend on this one.
     */
    public  List<Activity> dependencyOf() {
        return dependencyOf;
    }

    /**
     * Adds an activity as a direct dependency; also adds this 
     * activity to the dependecy's depedencyOf.  
     */
    public void addDependency(Activity other) {
        dependencies.add(other);
        other.dependencyOf.add(this);
    }
    
    /**
     * Removes an activity as a direct dependency; also removes
     * this activity from the dependecy's depedencyOf.  
     */
    public void removeDependency(Activity other) {
        dependencies.remove(other);
        other.dependencyOf.remove(this);
    }
    
    /**
     * Replaces the i-th dependency of this activity.
     * The current i-th activity is also updated.  
     */
    public void replaceDependency(int index, Activity other) {
        dependencies.get(index).dependencies.remove(this);
        dependencies.set(index, other);
        other.dependencyOf.add(this);
    }

    /**
     * Returns the leaf activities that this activity transitively 
     * depends on, or this activity if it is a leaf.  
     */
    public void dfs(DFSObserver observer) {
        
        Set<Activity> opened = new HashSet<>();
        Deque<Activity> open = new ArrayDeque<>();
        Deque<Iterator<Activity>> openAdj = new ArrayDeque<>();

        opened.add(this);
        observer.onOpen(this, null);       
        open.push(this);
        openAdj.push(this.dependencies().iterator());

        while (!openAdj.isEmpty()) {
            Activity from = open.peek();
            if (openAdj.peek().hasNext()) {
                Activity next = openAdj.peek().next();
                if (!opened.contains(next)) {
                    // new node, mark as opened
                    opened.add(next);
                    observer.onOpen(next, from);
                    // start exploring now
                    open.push(next);
                    openAdj.push(next.dependencies().iterator());
                } else {
                    // already opened, skip to break loops
                    observer.onSkipped(next, from);
                }
            } else {
                // explored all reachable successors
                observer.onClose(from);
                open.pop();
                openAdj.pop();
            }
        }
    }
    
    /**
     * Returns the leaf activities that this activity transitively 
     * depends on, or this activity if it is a leaf.  
     */
    public List<Activity> findLeaves() {
        
        List<Activity> leaves = new ArrayList<>();
        this.dfs(new DFSObserver() {
            @Override public void onClose(Activity closed) {
                if (closed.dependencies().isEmpty())
                    leaves.add(closed);
            }
        });
        
        return leaves;
    }
    
    /**
     * Returns the leaf activities in common with another activity.  
     */
    public List<Activity> findCommonLeaves(Activity other) {
        
        List<Activity> leaves = this.findLeaves();
        List<Activity> otherLeaves = other.findLeaves();
        leaves.retainAll(otherLeaves);
        
        return leaves;
    }
}
