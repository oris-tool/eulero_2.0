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

package org.oristool.eulero.solver;

/**
 * Builds the STPN of a directed subgraph. 
 */
public class PetriBuilder {
    
//    public static void build(Activity root) {
//        // store in array to access in a closure
//        int[] currentPrio = new int[] { 1 };
//        
//        PetriNet net = new PetriNet();
//        Place start = net.addPlace("start");
//        
//        // for replicated dependencies: create transitions once,
//        // reuse them every time the dependency is shared
//        Map<Activity, Transition> tmap = new LinkedHashMap<>();
//        
//        root.dfs(new DFSObserver() {
//            @Override public void onOpen(Activity opened, Activity from) {
//                
//                
//            }
//            
//            @Override public void onSkipped(Activity skipped, Activity from) {
//            
//            }
//            
//            @Override public void onClose(Activity closed) {
//                // we already built the STPN of the dependencies
//                // they must have transitions in tmap
//                if (closed instanceof Analytical) {
//                    Transition t = net.addTransition(nextNameId(net, "f"));
//                    t.addFeature(((Analytical)closed).getPDF());
//                    t.addFeature(new Priority(++currentPrio[0]));
//                    
//                    net.addPrecondition(start, t);
//                    tmap.put(closed, t);
//                
//                } else if (closed instanceof Seq) {
//                    Place prev = null;
//                    for (int i = 0; i < closed.pre().size(); i++) {
//                        Transition tdep = tmap.get(closed.pre().get(i));
//                                
//                        if (prev != null)
//                            net.addPrecondition(prev, tdep);
//                        
//                        Place next = (i < closed.pre().size() - 1) 
//                                ? null : net.addPlace(nextNameId(net, "S"));
//                        net.addPostcondition(tdep, next);
//                        prev = next;
//                    }
//                    
//                    Transition t = net.addTransition(nextNameId(net, "s"));
//                    t.addFeature(StochasticTransitionFeature.newDeterministicInstance(BigDecimal.ZERO));
//                    t.addFeature(new Priority(++currentPrio[0]));
//                    net.addPrecondition(prev, t);
//                    tmap.put(closed,  t);
//                    
//                } else if (closed instanceof And) {
//                    Transition t = net.addTransition(nextNameId(net, "s"));
//                    t.addFeature(StochasticTransitionFeature.newDeterministicInstance(BigDecimal.ZERO));
//                    t.addFeature(new Priority(++currentPrio[0]));
//                    tmap.put(closed,  t);
//
//                    for (Activity dep : closed.pre()) {
//                        Transition tdep = tmap.get(dep);
//                        Place pdep = net.addPlace(nextNameId(net, "A"));                       
//                        net.addPostcondition(tdep, pdep);
//                        net.addPrecondition(pdep, t);
//                    }
//                    
//                } else if (closed instanceof Or) {
//                    // TODO for each dep, add update function setting completed=1, plus term &&completed=1 in stop condition
//                    
//                } else if (closed instanceof Xor) {
//                    // TODO add conflict with weights, places before, and postconditions to same place
//                                          
//                } else {
//                    throw new IllegalStateException();
//                }  
//            }
//        });
//        
//        Place end = net.addPlace("end");
//        net.addPostcondition(tmap.get(root), end);
//    }
//    
//    
//    private static String nextNameId(PetriNet net, String name) {
//        int id = 1;
//        String nameId = name + id;
//        
//        while (net.getTransition(nameId) != null) {
//            id++;
//            nameId = name + id;
//        }
//        
//        return nameId;
//    }
}
