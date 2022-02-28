package org.oristool.eulero.modelgenerator;

import org.oristool.eulero.graph.*;
import org.oristool.eulero.modelgenerator.blocksettings.*;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class RandomModelBuilder extends ModelBuilder{
    private int treeDepth;
    private ArrayList<Set<BlockTypeSetting>> settings;

    public RandomModelBuilder(StochasticTransitionFeature feature, ArrayList<Set<BlockTypeSetting>> settings){
        super(feature);
        this.settings = settings;
        this.treeDepth = settings.size();
    }

    public RandomModelBuilder(ArrayList<StochasticTransitionFeature> features, ArrayList<BigDecimal> weights, ArrayList<Set<BlockTypeSetting>> settings){
        super(features,weights);
        this.settings = settings;
        this.treeDepth = settings.size();
    }

    @Override
    public Activity buildModel() {
        // TODO check settings...
        int[] activityNameCounter = new int[1];

        return generateBlock(treeDepth, activityNameCounter);
    }

    private Activity generateBlock(int depthLevel, int[] activityNameCounter){
        if(depthLevel > 0) {
            Set<BlockTypeSetting> levelSetting = settings.get(treeDepth - depthLevel);

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
                    activities.add(generateBlock(depthLevel - 1, activityNameCounter));
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
                    Activity xor = new Xor(name, activities, weights);
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
                        Activity node = generateBlock(depthLevel - 1, activityNameCounter);
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

                dag.setEFT(dag.low());
                dag.setLFT(dag.upp());
                return dag;
            }

            if (chosenBlock instanceof CustomDAGBlockSetting){
                StringBuilder name = new StringBuilder("DAG(");
                ArrayList<ArrayList<Activity>> levels = new ArrayList<>();
                Random rand = new Random();
                int levelNumber = rand.nextInt(1) + 2;

                ArrayList<Activity> dagActivities = new ArrayList<>();
                boolean threeNodesInALevel = false;

                for(int i = 0; i < levelNumber; i++){
                    ArrayList<Activity> level = new ArrayList<>();

                    int nodeNumber = (threeNodesInALevel ? rand.nextInt(1) : rand.nextInt(2)) + 2;
                    if(nodeNumber == 3){
                        threeNodesInALevel = true;
                    }

                    for(int j = 0; j < nodeNumber; j++){
                        Activity node = generateBlock(depthLevel - 1, activityNameCounter);
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
                    for(int j = Math.max(0, i - 1); j < i; j++){
                        for(Activity act: levels.get(j)){
                            predecessors.add(act);
                        }
                    }

                    ArrayList<Activity> successors = new ArrayList<>();
                    for(int j = i + 1; j < Math.min(levels.size(), i + 1 + 1); j++){
                        for(Activity act: levels.get(j)){
                            successors.add(act);
                        }
                    }

                    for(Activity act: nodes){
                        if(act.pre().isEmpty()){
                            int maximumPredecessorNumber = rand.nextInt(1 + 1) + 1;
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
                            int maximumSuccessorNumber = rand.nextInt(1 + 1) + 1;
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

                dag.setEFT(dag.low());
                dag.setLFT(dag.upp());
                return dag;
            }
        }

        //We reach the end of the tree --> generate an activity
        String name = "A" + activityNameCounter[0]++;
        return new Analytical(name, this.getFeatures(), this.getWeights());
    }
}
