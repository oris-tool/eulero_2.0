package org.oristool.eulero.modelgeneration;

import org.apache.commons.lang3.tuple.Pair;
import org.oristool.eulero.modelgeneration.blocksettings.*;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.ModelFactory;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.StochasticTime;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.*;

public class RandomGenerator {
    private Set<Pair<List<StochasticTransitionFeature>, List<BigDecimal>>> features;
    private ArrayList<Set<BlockTypeSetting>> settings;

    // TODO, questo va capito come sistemarlo
    private StochasticTime stochasticTime;

    public ArrayList<Set<BlockTypeSetting>> settings() {
        return settings;
    }

    public Set<Pair<List<StochasticTransitionFeature>, List<BigDecimal>>> features() {
        return features;
    }

    public StochasticTime stochasticTime() {
        return stochasticTime;
    }

    public RandomGenerator(StochasticTime stochasticTime, ArrayList<Set<BlockTypeSetting>> settings){
        this.settings = settings;
        this.stochasticTime = stochasticTime;
        this.features = Set.of(Pair.of(List.of(stochasticTime.getStochasticTransitionFeature()), List.of(BigDecimal.ONE)));
    }
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
                // TODO atenzione che qui è come se non desse scelta tra il min e il max
                minBreadthValue = maxBreadthValue;
                Random rand = new Random();
                int breadth = rand.nextInt((maxBreadthValue - minBreadthValue) + 1) + minBreadthValue;

                ArrayList<Activity> activities = new ArrayList<>();
                for (int i = 0; i < breadth; i++) {
                    activities.add(generateBlock(depthLevel - 1, activityNameCounter, simpleActivityPrefix, simpleActivitySuffix));
                }

                if (chosenBlock instanceof ANDBlockSetting) {
                    return ModelFactory.forkJoin(activities.stream().toArray(Activity[]::new));
                }

                if (chosenBlock instanceof SEQBlockSetting) {
                    return ModelFactory.sequence(activities.stream().toArray(Activity[]::new));
                }

                if (chosenBlock instanceof XORBlockSetting) {
                    ArrayList<Double> weights = new ArrayList<>();
                    for (int i = 0; i < breadth; i++) {
                        weights.add((1 / (double) activities.size()));
                    }
                    return ModelFactory.XOR(weights, activities.stream().toArray(Activity[]::new));
                }
            }

            if (chosenBlock instanceof DAGBlockSetting) {
                ArrayList<ArrayList<Activity>> levels = new ArrayList<>();
                Random rand = new Random();
                int levelNumber = rand.nextInt((((DAGBlockSetting) chosenBlock).getMaximumLevels() - ((DAGBlockSetting) chosenBlock).getMinimumLevels()) + 1) + ((DAGBlockSetting) chosenBlock).getMinimumLevels();

                ArrayList<Activity> dagActivities = new ArrayList<>();
                for(int i = 0; i < levelNumber; i++){
                    ArrayList<Activity> level = new ArrayList<>();
                    int nodeNumber = rand.nextInt((((DAGBlockSetting) chosenBlock).getMaximumLevelBreadth() - ((DAGBlockSetting) chosenBlock).getMinimumLevelBreadth()) + 1) + ((DAGBlockSetting) chosenBlock).getMinimumLevelBreadth();

                    for(int j = 0; j < nodeNumber; j++){
                        Activity node = generateBlock(depthLevel - 1, activityNameCounter, simpleActivityPrefix, simpleActivitySuffix);
                        level.add(node);
                        dagActivities.add(node);
                    }

                    levels.add(level);
                }

                //ArrayList<DAGEdge> edges = new ArrayList<>();
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
                        if(act.pre().isEmpty() && !levels.get(0).contains(act)){
                            int maximumPredecessorNumber = rand.nextInt((((DAGBlockSetting) chosenBlock).getMaximumNodeConnection() - ((DAGBlockSetting) chosenBlock).getMinimumNodeConnection()) + 1) + ((DAGBlockSetting) chosenBlock).getMinimumNodeConnection();
                            ArrayList<Activity> removed = new ArrayList<>();
                            for(int j = 0; j < maximumPredecessorNumber; j++){
                                int nodeIndex = rand.nextInt(predecessors.size());
                                act.addPrecondition(predecessors.get(nodeIndex));
                                //DAGEdge edge = new DAGEdge(predecessors.get(nodeIndex).name(), act.name());
                                //edges.add(edge);
                                removed.add(predecessors.remove(nodeIndex));
                            }
                            predecessors.addAll(removed);
                        }

                        if(act.post().isEmpty() && !levels.get(levels.size() - 1).contains(act)) {
                            int maximumSuccessorNumber = rand.nextInt((((DAGBlockSetting) chosenBlock).getMaximumNodeConnection() - ((DAGBlockSetting) chosenBlock).getMinimumNodeConnection()) + 1) + ((DAGBlockSetting) chosenBlock).getMinimumNodeConnection();
                            ArrayList<Activity> removed = new ArrayList<>();
                            for (int j = 0; j < maximumSuccessorNumber; j++) {
                                int nodeIndex = rand.nextInt(successors.size());
                                successors.get(nodeIndex).addPrecondition(act);
                                //DAGEdge edge = new DAGEdge(act.name(), successors.get(nodeIndex).name());
                                //edges.add(edge);
                                removed.add(successors.remove(nodeIndex));
                            }
                            successors.addAll(removed);
                        }
                    }
                }

                // dag.getType().setEdges();
                // dag.setEdges(edges);
                return ModelFactory.DAG(dagActivities.toArray(new Activity[0]));
            }
        }

        //We reach the end of the tree --> generate an activity
        String name = simpleActivityPrefix + "A" + activityNameCounter[0]++ + simpleActivitySuffix;
        List<Pair<List<StochasticTransitionFeature>, List<BigDecimal>>> myList = new ArrayList<>(this.features);
        Collections.shuffle(myList);
        return new Simple(name, stochasticTime);
    }
}
