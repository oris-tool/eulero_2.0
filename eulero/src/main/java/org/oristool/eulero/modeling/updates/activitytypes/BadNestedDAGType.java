package org.oristool.eulero.modeling.updates.activitytypes;

import org.apache.commons.lang3.tuple.Pair;
import org.oristool.eulero.evaluation.approximator.Approximator;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.modeling.ActivityEnumType;
import org.oristool.eulero.modeling.DAGEdge;

import org.oristool.eulero.modeling.stochastictime.DeterministicTime;
import org.oristool.eulero.modeling.updates.Activity;
import org.oristool.eulero.modeling.updates.Composite;
import org.oristool.eulero.modeling.updates.Simple;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.Marking;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class BadNestedDAGType extends DAGType{
    List<DAGEdge> edges;

    public BadNestedDAGType(ArrayList<Activity> children, List<DAGEdge> edges) {
        super(children);
        this.edges = edges;
    }


    @Override
    public void initPreconditions(Composite activity, Activity... children) {
        {
            List<Integer> startingNodes = edges.stream().map(DAGEdge::getPreInt).collect(Collectors.toList());
            List<Integer> endingNodes = edges.stream().map(DAGEdge::getPostInt).collect(Collectors.toList());
            List<Integer> intermediateNodes = (new ArrayList<>(startingNodes));
            intermediateNodes.retainAll(endingNodes);
            startingNodes.removeAll(intermediateNodes);
            endingNodes.removeAll(intermediateNodes);

            for(Activity act: children){
                if(startingNodes.contains(List.of(children).indexOf(act))){
                    act.addPrecondition(activity.begin());
                }
                if(endingNodes.contains(List.of(children).indexOf(act))){
                    activity.end().addPrecondition(act);
                }
            }

            for(DAGEdge edge: edges){
                Arrays.stream(children).collect(Collectors.toList()).get(edge.getPostInt()).addPrecondition(
                        Arrays.stream(children).collect(Collectors.toList()).get(edge.getPreInt())
                );
            }
        }
    }

    @Override
    public void setEnumType(Composite activity) {
        activity.setEnumType(ActivityEnumType.DAG);
    }

    @Override
    public Activity copyRecursive(String suffix) {
        return null;
    }

    @Override
    public double[] analyze(BigDecimal timeLimit, BigDecimal timeTick, AnalysisHeuristicsVisitor visitor){
        return visitor.analyze(this, timeLimit, timeTick);
    };

    public double[] forwardTransientAnalysis(BigDecimal timeLimit, BigDecimal step){
        TransientSolution<Marking, RewardRate> transientSolution =  getActivity().forwardAnalyze(timeLimit.toString(), step.toString(), "0.001");
        double[] CDF = new double[transientSolution.getSolution().length];

        for(int i = 0; i < CDF.length; i++){
            CDF[i] = transientSolution.getSolution()[i][0][0];
        }

        return CDF;
    }

    public double[] innerBlockReplication(BigDecimal timeLimit, BigDecimal step, BigInteger CThreshold, BigInteger QThreshold){
        return null;
    }

    public double[] innerBlockAnalysis(BigDecimal timeLimit, BigDecimal step, BigInteger CThreshold, BigInteger QThreshold, AnalysisHeuristicsVisitor visitor, Approximator approximator){
        Map<String, Activity> toBeSimplifiedActivityMap = getMostComplexChild(getActivity(), CThreshold, QThreshold);
        Activity toBeSimplifiedActivity = toBeSimplifiedActivityMap.get("activity");
        Activity toBeSimplifiedActivityParent = toBeSimplifiedActivityMap.get("parent");
        double aux = toBeSimplifiedActivity.max().doubleValue();
        int mag = 1;
        while (aux > 10) {
            mag = mag * 10;
            aux = aux / 10;
        }
        BigDecimal innerActivityStep = BigDecimal.valueOf(mag * Math.pow(10, -2));


        ArrayList<Pair<BigDecimal, StochasticTransitionFeature>> approximationFeature =  approximator.getApproximatedStochasticTransitionFeatures(
                toBeSimplifiedActivity.analyze(toBeSimplifiedActivity.max().precision() >= 309 ? timeLimit : toBeSimplifiedActivity.max(), step, visitor),
                toBeSimplifiedActivity.min().doubleValue(), (toBeSimplifiedActivity.max().precision() >= 309 ? timeLimit : toBeSimplifiedActivity.max()).doubleValue(), innerActivityStep);

        Activity newActivity = new Simple(toBeSimplifiedActivity.name() + "_N",
                new DeterministicTime(BigDecimal.ONE)
                //approximationFeature.stream().map(Pair::getRight).collect(Collectors.toCollection(ArrayList::new)),
                //approximationFeature.stream().map(Pair::getLeft).collect(Collectors.toCollection(ArrayList::new))
                );

        toBeSimplifiedActivity.replace(newActivity);
        int activityIndex = toBeSimplifiedActivityParent.activities().indexOf(toBeSimplifiedActivity);
        toBeSimplifiedActivityParent.activities().set(activityIndex, newActivity);
        toBeSimplifiedActivityParent.resetComplexityMeasure();

        getActivity().resetComplexityMeasure();

        return visitor.analyze(this, timeLimit, step);
    }

    public Map<String, Activity> getMostComplexChild(Composite model, BigInteger CThreshold, BigInteger QThreshold){
            ArrayList<Activity> innerActivities = model.activities().stream().filter(t -> (t.C().doubleValue() > 1 || t.Q().doubleValue() > 1)).distinct().sorted(Comparator.comparing(Activity::C).thenComparing(Activity::Q)).collect(Collectors.toCollection(ArrayList::new));
            Activity mostComplexActivity = innerActivities.get(innerActivities.size() - 1);
            boolean modelIsNotADag = mostComplexActivity.type().equals(ActivityEnumType.AND) || mostComplexActivity.type().equals(ActivityEnumType.SEQ) || mostComplexActivity.type().equals(ActivityEnumType.XOR) || mostComplexActivity.type().equals(ActivityEnumType.SIMPLE);

            if(!modelIsNotADag && mostComplexActivity.C().compareTo(CThreshold) > 0 && mostComplexActivity.Q().compareTo(QThreshold) > 0){
                // TODO forse c'è un modo, tramite i type, di non dover fare il cast
                return getMostComplexChild((Composite) mostComplexActivity, CThreshold, QThreshold);
            }

            return Map.ofEntries(
                    Map.entry("parent", model),
                    Map.entry("activity", mostComplexActivity)
            );

    }
}
