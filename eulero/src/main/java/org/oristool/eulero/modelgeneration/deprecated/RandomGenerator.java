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

package org.oristool.eulero.modelgeneration.deprecated;

import org.apache.commons.lang3.tuple.Pair;
import org.oristool.eulero.modelgeneration.blocksettings.*;
import org.oristool.eulero.modeling.deprecated.*;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class RandomGenerator {
    private Set<Pair<List<StochasticTransitionFeature>, List<BigDecimal>>> features;
    private ArrayList<Set<BlockTypeSetting>> settings;

    public RandomGenerator(StochasticTransitionFeature feature, ArrayList<Set<BlockTypeSetting>> settings){
        this.settings = settings;
        this.features = Set.of(Pair.of(List.of(feature), List.of(BigDecimal.ONE)));
    }
    public RandomGenerator(Set<Pair<List<StochasticTransitionFeature>, List<BigDecimal>>> features, ArrayList<Set<BlockTypeSetting>> settings){
        this.settings = settings;
        this.features = features;
    }

    public Activity generateBlock(int depthLevel){
        int[] activityNameCounter = new int[1];
        return generateBlock(depthLevel, activityNameCounter, "", "");
    }

    public Activity generateBlock(int depthLevel, int[] activityNameCounter, String simpleActivityPrefix,  String simpleActivitySuffix){
        if(depthLevel > 0) {
            Set<BlockTypeSetting> levelSetting = settings.get(settings.size() - depthLevel);

            //Choosing Block Type
            double randomSample = Math.random();
            double low = 0;
            double upp = 0;
            BlockTypeSetting chosenBlock = null;
            for (BlockTypeSetting blockTypeSetting : levelSetting) {
                upp += blockTypeSetting.getProbability();
                if (randomSample >= low && randomSample < upp) {
                    chosenBlock = blockTypeSetting;
                }
                low = upp;
            }

            if (chosenBlock instanceof WellNestedBlockSetting) {
                int minBreadthValue = ((WellNestedBlockSetting) chosenBlock).getMinimumBreadth();
                int maxBreadthValue = ((WellNestedBlockSetting) chosenBlock).getMaximumBreadth();
                minBreadthValue = maxBreadthValue;
                Random rand = new Random();
                int breadth = rand.nextInt((maxBreadthValue - minBreadthValue) + 1) + minBreadthValue;

                ArrayList<Activity> activities = new ArrayList<>();
                for (int i = 0; i < breadth; i++) {
                    activities.add(generateBlock(depthLevel - 1, activityNameCounter, simpleActivityPrefix, simpleActivitySuffix));
                }

                if (chosenBlock instanceof ANDBlockSetting) {
                    String name = "AND(" + activities.stream().map(t -> t.name()).collect(Collectors.joining (",")) + ")";
                    Activity and = DAG.forkJoin(name, activities.stream().toArray(Activity[]::new));
                    return and;
                }

                if (chosenBlock instanceof SEQBlockSetting) {
                    String name = "SEQ(" + activities.stream().map(t -> t.name()).collect(Collectors.joining (",")) + ")";
                    Activity seq = DAG.sequence(name, activities.stream().toArray(Activity[]::new));
                    return seq;
                }

                if (chosenBlock instanceof XORBlockSetting) {
                    String name = "XOR(" + activities.stream().map(t -> t.name()).collect(Collectors.joining (",")) + ")";
                    ArrayList<Double> weights = new ArrayList<>();
                    for (int i = 0; i < breadth; i++) {
                        weights.add((1 / (double) activities.size()));
                    }
                    Activity xor = new XOR(name, activities, weights);
                    return xor;
                }
            }

            if (chosenBlock instanceof DAGBlockSetting) {
                StringBuilder name = new StringBuilder("DAG(");
                ArrayList<ArrayList<Activity>> levels = new ArrayList<>();
                Random rand = new Random();
                int levelNumber = rand.nextInt((((DAGBlockSetting) chosenBlock).getMaximumLevels() - ((DAGBlockSetting) chosenBlock).getMinimumLevels()) + 1) + ((DAGBlockSetting) chosenBlock).getMinimumLevels();

                ArrayList<Activity> dagActivities = new ArrayList<>();
                for(int i = 0; i < levelNumber; i++){
                    ArrayList<Activity> level = new ArrayList<>();
                    int nodeNumber = rand.nextInt((((DAGBlockSetting) chosenBlock).getMaximumLevelBreadth() - ((DAGBlockSetting) chosenBlock).getMinimumLevelBreadth()) + 1) + ((DAGBlockSetting) chosenBlock).getMinimumLevelBreadth();

                    for(int j = 0; j < nodeNumber; j++){
                        Activity node = generateBlock(depthLevel - 1, activityNameCounter, simpleActivityPrefix, simpleActivitySuffix);
                        name.append(node.name()).append(j != nodeNumber - 1 ? ", " : "");
                        level.add(node);
                        dagActivities.add(node);
                    }
                    name.append(i != levelNumber - 1 ? " | " : "");

                    levels.add(level);
                }
                name.append(")");

                DAG dag = DAG.empty(name.toString());
                dag.setActivities(dagActivities);
                // Set begin
                for(Activity act: levels.get(0)){
                    act.addPrecondition(dag.begin());
                }
                // Set end
                dag.end().addPrecondition(levels.get(levels.size()-1).stream().toArray(Activity[]::new));

                ArrayList<DAGEdge> edges = new ArrayList<>();
                // Set intermediate
                for(int i = 0; i < levels.size(); i++){
                    ArrayList<Activity> nodes = levels.get(i);

                    ArrayList<Activity> predecessors = new ArrayList<>();
                    for(int j = Math.max(0, i - ((DAGBlockSetting) chosenBlock).getMaximumAdjacencyDistance()); j < i; j++){
                        for(Activity act: levels.get(j)){
                            predecessors.add(act);
                        }
                    }

                    ArrayList<Activity> successors = new ArrayList<>();
                    for(int j = i + 1; j < Math.min(levels.size(), i + 1 +((DAGBlockSetting) chosenBlock).getMaximumAdjacencyDistance()); j++){
                        for(Activity act: levels.get(j)){
                            successors.add(act);
                        }
                    }

                    for(Activity act: nodes){
                        if(act.pre().isEmpty()){
                            int maximumPredecessorNumber = rand.nextInt((((DAGBlockSetting) chosenBlock).getMaximumNodeConnection() - ((DAGBlockSetting) chosenBlock).getMinimumNodeConnection()) + 1) + ((DAGBlockSetting) chosenBlock).getMinimumNodeConnection();
                            ArrayList<Activity> removed = new ArrayList<>();
                            for(int j = 0; j < maximumPredecessorNumber; j++){
                                int nodeIndex = rand.nextInt(predecessors.size());
                                act.addPrecondition(predecessors.get(nodeIndex));
                                DAGEdge edge = new DAGEdge(predecessors.get(nodeIndex).name(), act.name());
                                edges.add(edge);
                                removed.add(predecessors.remove(nodeIndex));
                            }
                            predecessors.addAll(removed);
                        }

                        if(act.post().isEmpty()) {
                            int maximumSuccessorNumber = rand.nextInt((((DAGBlockSetting) chosenBlock).getMaximumNodeConnection() - ((DAGBlockSetting) chosenBlock).getMinimumNodeConnection()) + 1) + ((DAGBlockSetting) chosenBlock).getMinimumNodeConnection();
                            ArrayList<Activity> removed = new ArrayList<>();
                            for (int j = 0; j < maximumSuccessorNumber; j++) {
                                int nodeIndex = rand.nextInt(successors.size());
                                successors.get(nodeIndex).addPrecondition(act);
                                DAGEdge edge = new DAGEdge(act.name(), successors.get(nodeIndex).name());
                                edges.add(edge);
                                removed.add(successors.remove(nodeIndex));
                            }
                            successors.addAll(removed);
                        }
                    }
                }

                dag.setEdges(edges);

                dag.setMin(dag.low());
                dag.setMax(dag.upp());
                return dag;
            }
        }

        //We reach the end of the tree --> generate an activity
        String name = simpleActivityPrefix + "A" + activityNameCounter[0]++ + simpleActivitySuffix;
        List<Pair<List<StochasticTransitionFeature>, List<BigDecimal>>> myList = new ArrayList<>(this.features);
        Collections.shuffle(myList);
        return new Simple(name, new ArrayList<>(myList.get(0).getLeft()), new ArrayList<>(myList.get(0).getRight()));
    }

}
