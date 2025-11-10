package org.oristool.eulero.modeling.activitytypes;

import com.google.common.collect.Lists;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.eulero.evaluation.approximator.Approximator;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.modeling.*;
import org.oristool.eulero.modeling.stochastictime.StochasticTime;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.petrinet.Marking;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@XmlRootElement(name = "bad-nested-dag-type")
public class BadNestedDAGType extends DAGType {
    public BadNestedDAGType(ArrayList<Activity> children) {
        super(children);
    }

    public BadNestedDAGType(){
        super(new ArrayList<>());
    }

    @Override
    public void initPreconditions(Composite activity, Activity... children) {
        for(Activity act: children){
            if(act.pre().isEmpty()){
                act.addPrecondition(activity.begin());
            }
            if(act.post().isEmpty()){
                activity.end().addPrecondition(act);
            }
        }
    }

    @Override
    public void setEnumType(Composite activity) {
        activity.setEnumType(ActivityEnumType.DAG);
    }

    @Override
    public Composite copyRecursive(String suffix) {
        return copyRecursive(getActivity().begin(), getActivity().end(), suffix);
    }

    @Override
    public double[] analyze(BigDecimal timeLimit, BigDecimal timeTick, AnalysisHeuristicsVisitor visitor){
        return visitor.analyze(this, timeLimit, timeTick);
    }

    @Override
    public ActivityType clone() {
        ArrayList<Activity> clonedActivities = getChildren().stream().map(Activity::clone).collect(Collectors.toCollection(ArrayList::new));
        return new BadNestedDAGType(clonedActivities);
    }

    public double[] forwardTransientAnalysis(BigDecimal timeLimit, BigDecimal step){
        TransientSolution<Marking, RewardRate> transientSolution =  getActivity().forwardAnalyze(timeLimit.toString(), step.toString(), "0.001");
        double[] CDF = new double[transientSolution.getSolution().length];

        for(int i = 0; i < CDF.length; i++){
            CDF[i] = transientSolution.getSolution()[i][0][0];
        }

        return CDF;
    }

    public double[] innerBlockReplication(BigDecimal timeLimit, BigDecimal step, BigInteger CThreshold, BigInteger QThreshold, AnalysisHeuristicsVisitor visitor){
        ArrayList<Activity> replicatedBlocks = new ArrayList<>();
        for(Activity activity: getActivity().end().pre()){
            Activity replicatedBlock = this.getReplicatedBlockFromActivity(activity);
            replicatedBlock.C();
            replicatedBlock.Q();
            replicatedBlocks.add(replicatedBlock);
        }

        replicatedBlocks.sort(Comparator.comparing(Activity::C).thenComparing(Activity::Q));
        Activity chosenReplicatedBlock = replicatedBlocks.get(replicatedBlocks.size() - 1);
        List<String> chosenReplicatedBlockString = chosenReplicatedBlock.activities().stream().map(Activity::name).collect(Collectors.toList());
        Composite thisActivity = getActivity();
        List<Activity> activityDAGs = new ArrayList<>();

        for(Activity act: getActivity().activities()){
            if(!chosenReplicatedBlockString.contains(act.name())){
                activityDAGs.add(act.clone());
            } else if (!act.post().stream().map(Activity::name).filter(t -> !chosenReplicatedBlockString.contains(t)).collect(Collectors.toList()).isEmpty()
                    && act.post().stream().map(Activity::name).filter(t -> t.contains("END")).collect(Collectors.toList()).isEmpty()){
                activityDAGs.add(act.clone());
            }
        }

        for(Activity act: getActivity().activities().stream().filter(t -> activityDAGs.stream().map(Activity::name).collect(Collectors.toList()).contains(t.name())).collect(Collectors.toList())){
            List<String> preconditions = act.pre().stream().map(Activity::name).filter(t -> !t.contains("BEGIN")).collect(Collectors.toList());
            activityDAGs.stream().filter(t -> t.name().equals(act.name())).findFirst().get().addPrecondition(
                    activityDAGs.stream().filter(t -> preconditions.contains(t.name())).toArray(Activity[]::new));

        }

        Activity restOfTheDAG = ModelFactory.DAG(activityDAGs.toArray(Activity[]::new));

        if (replicatedBlocks.size() > 1){
            if(checkWellNesting((Composite) chosenReplicatedBlock)){
                chosenReplicatedBlock = dag2tree(((Composite) chosenReplicatedBlock).end().pre());
            }

            if(checkWellNesting((Composite) restOfTheDAG)){
                restOfTheDAG = dag2tree(((Composite) restOfTheDAG).end().pre());
            }
            Composite newAND = ModelFactory.forkJoin(restOfTheDAG, chosenReplicatedBlock);

            return newAND.analyze(timeLimit, step, visitor);
        }

        chosenReplicatedBlock = dag2tree(((Composite) chosenReplicatedBlock).end().pre());

        return chosenReplicatedBlock.analyze(timeLimit, step, visitor);


        // TODO per ROSPO trasformo tutto in albero (e questo può impattare sull'accuratezza, dovremmo cambiare alcune cose, ma lo potremmo fare più avanti)
        /*Activity newAND = dag2tree(getActivity().end().pre());

        return newAND.analyze(timeLimit, step, visitor);*/
    }

    public double[] treeficationAnalysis(BigDecimal timeLimit, BigDecimal step, BigInteger CThreshold, BigInteger QThreshold, AnalysisHeuristicsVisitor visitor){
        Activity newAND = dag2tree(getActivity().end().pre());
        return newAND.analyze(timeLimit, step, visitor);
    }

    public double[] innerBlockAnalysis(BigDecimal timeLimit, BigDecimal step, BigInteger CThreshold, BigInteger QThreshold, AnalysisHeuristicsVisitor visitor, Approximator approximator){
        // TODO Add clone() support in modo che il modello originale non venga mai aggiornato
        Activity clonedActivity =  getActivity().clone();
        Map<String, Activity> toBeSimplifiedActivityMap = getMostComplexChild((Composite) clonedActivity, CThreshold, QThreshold);
        Activity toBeSimplifiedActivity = toBeSimplifiedActivityMap.get("activity");
        Activity toBeSimplifiedActivityParent = toBeSimplifiedActivityMap.get("parent");
        double aux = toBeSimplifiedActivity.max().doubleValue();
        int mag = 1;
        while (aux > 10) {
            mag = mag * 10;
            aux = aux / 10;
        }
        BigDecimal innerActivityStep = BigDecimal.valueOf(mag * Math.pow(10, -2));


        StochasticTime approximatedStochasticTime =  approximator.getApproximatedStochasticTime(
                toBeSimplifiedActivity.analyze(toBeSimplifiedActivity.max().precision() >= 309 ? timeLimit : toBeSimplifiedActivity.max(), toBeSimplifiedActivity.getFairTimeTick(), visitor),
                toBeSimplifiedActivity.min().doubleValue(), (toBeSimplifiedActivity.max().precision() >= 309 ? timeLimit : toBeSimplifiedActivity.max()).doubleValue(), toBeSimplifiedActivity.getFairTimeTick());

        Activity newActivity = new Simple(toBeSimplifiedActivity.name() + "_N", approximatedStochasticTime);

        toBeSimplifiedActivity.replace(newActivity);
        int activityIndex = toBeSimplifiedActivityParent.activities().indexOf(toBeSimplifiedActivity);
        toBeSimplifiedActivityParent.activities().set(activityIndex, newActivity);
        toBeSimplifiedActivityParent.resetComplexityMeasure();

        clonedActivity.resetComplexityMeasure();

        return visitor.analyze((BadNestedDAGType) clonedActivity.getType(), timeLimit, step);
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

    public Composite getReplicatedBlockFromActivity(Activity end) {
        Stack<Activity> activityStack = new Stack<>();
        activityStack.push(end);
        Set<Activity> clonedActivities = new HashSet<>();

        // Retrieve activities backward and clone it.
        while(!activityStack.isEmpty()){
            Activity a = activityStack.pop();
            if(!a.name().contains("END") && !a.name().contains("BEGIN")
                    &&  clonedActivities.stream().noneMatch(obj -> obj.name().equals(a.name()))) {
                Activity clone = a.clone();
                clonedActivities.add(clone);
            }

            for(Activity pre: a.pre()){
                if(!pre.name().contains("END") && !pre.name().contains("BEGIN"))
                    activityStack.push(pre);
            }
        }

        activityStack.push(end);
        while(!activityStack.isEmpty()){
            Activity a = activityStack.pop();
            Activity currentClone = clonedActivities.stream().filter(obj -> obj.name().equals(a.name())).findFirst().orElseThrow();

            for(Activity pre: a.pre().stream().filter(t -> clonedActivities.stream().map(Activity::name).collect(Collectors.toList()).contains(t.name())).collect(Collectors.toList())){
                if(!pre.name().contains("END") && !pre.name().contains("BEGIN")) {
                    currentClone.addPrecondition(clonedActivities.stream().filter(t -> t.name().equals(pre.name())).findFirst().get());
                    activityStack.push(pre);
                }
            }
        }

        return ModelFactory.DAG(clonedActivities.toArray(new Activity[0]));
    }



    public Composite copyRecursive(Activity begin, Activity end, String suffix) {
        Composite copy = new Composite(getActivity().name() + suffix, new BadNestedDAGType(new ArrayList<>()), ActivityEnumType.DAG);

        Map<Activity, Activity> nodeCopies = new HashMap<>();

        if (getActivity().begin().equals(begin)) {
            nodeCopies.put(begin, copy.begin());
        } else {
            Activity ax = begin.copyRecursive(suffix);
            nodeCopies.put(begin, ax);
            ax.addPrecondition(copy.begin());
        }

        if (getActivity().end().equals(end)) {
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

    public void removeBetween(Activity begin, Activity end, boolean removeShared) {

        Set<Activity> activitiesBetween = this.activitiesBetween(begin, end);

        if (!removeShared) {
            for (Activity p : new ArrayList<>(end.post())) {
                p.removePrecondition(end);
            }

            activitiesBetween.removeAll(activitiesBetween(getActivity().begin(), getActivity().end()));
        }

        List<Activity> all = this.nested();
        all.add(getActivity().begin());
        all.add(getActivity().end());

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

    public List<Activity> nested() {
        List<Activity> activities = new ArrayList<>();

        getActivity().begin().dfs(false, new DFSObserver() {
            @Override public boolean onOpen(Activity opened, Activity from) {
                if (opened != getActivity().begin() && opened != getActivity().end()) {
                    activities.add(opened);
                }

                return true;  // continue
            }
        });

        return activities;
    }

    public Composite nest(Activity end) {
        Composite copy = this.copyRecursive(getActivity().begin(), end, "_up".replace(getActivity().name(),""));

        this.removeBetween(getActivity().begin(), end, false);
        Composite restOfDAG = this.copyRecursive("_down");

        Activity up = copy;
        Activity down = restOfDAG;

        if(this.checkWellNesting(getActivity())){
            up = wellNestIt(copy.begin().post());
        }

        if(this.checkWellNesting(restOfDAG)){
            down = wellNestIt(restOfDAG.begin().post());
        }

        return ModelFactory.forkJoin(up, down);
    }

    public boolean checkWellNesting(Composite modelToCheck){
        Deque<Set<Activity>> levelNode = new LinkedList<>();
        levelNode.push(Collections.singleton(modelToCheck.begin()));

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
                Activity forkJoin = ModelFactory.forkJoin(nodes.toArray(Activity[]::new));

                if(!List.copyOf(theseNodeSuccessors).get(0).max().equals(BigDecimal.ZERO)){
                    ArrayList<Activity> theSequence = new ArrayList<>();
                    theSequence.add(forkJoin);
                    Activity sequenceLastNode = wellNestIt(Lists.newArrayList(theseNodeSuccessors));
                    theSequence.add(sequenceLastNode);

                    return ModelFactory.sequence(theSequence.toArray(Activity[]::new));
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

            return ModelFactory.forkJoin(forkJoinNodes.toArray(Activity[]::new));
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
            return ModelFactory.sequence(sequenceNodes.toArray(Activity[]::new));
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
            return ModelFactory.sequence(sequenceNodes.toArray(Activity[]::new));
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

    public Activity dag2tree(List<Activity> nodes){
        // TODO: questo origina side effects sul grafo originale. Verificare se si può evitare, e chi è che causa ciò.
        int breakPoint = 0;
        if (nodes.size() > 1){
            StringBuilder name = new StringBuilder("AND(");
            ArrayList<Activity> forkJoinNodes = new ArrayList<>();
            for(Activity act: nodes){
                Activity andNode = dag2tree(Lists.newArrayList(act));
                forkJoinNodes.add(andNode);
            }

            for(Activity act: forkJoinNodes){
                name.append((forkJoinNodes.indexOf(act) == nodes.size() - 1) ? act.name() + ")" : act.name() + ", ");
            }

            return ModelFactory.forkJoin(forkJoinNodes.toArray(Activity[]::new));//ModelFactory.forkJoin(forkJoinNodes.toArray(Activity[]::new));
        }

        //!nodes.get(0).pre().get(0).max().equals(BigDecimal.ZERO
        if(nodes.size() == 1 && nodes.get(0).pre().size() > 1 && nodes.get(0).pre().stream().noneMatch(t -> t.max().equals(BigDecimal.ZERO))){
            List<Activity> sequenceNodes = new ArrayList<>();
            StringBuilder name = new StringBuilder("SEQ(");

            sequenceNodes.add(dag2tree(nodes.get(0).pre()));
            sequenceNodes.add(nodes.get(0).clone());

            for(Activity act: sequenceNodes){
                act.pre().clear();
                act.post().clear();
                name.append((sequenceNodes.indexOf(act) == sequenceNodes.size() - 1) ? act.name() + ")" : act.name() + ", ");
            }
            return ModelFactory.sequence(sequenceNodes.toArray(Activity[]::new));
        }

        if(nodes.size() == 1 && nodes.get(0).pre().size() == 1 && !nodes.get(0).pre().get(0).max().equals(BigDecimal.ZERO)){
            List<Activity> sequenceNodes = new ArrayList<>();
            StringBuilder name = new StringBuilder("SEQ(");
            sequenceNodes.add(dag2tree(nodes.get(0).pre()));
            sequenceNodes.add(nodes.get(0).clone());

            for(Activity act: sequenceNodes){
                act.pre().clear();
                act.post().clear();
                name.append((sequenceNodes.indexOf(act) == sequenceNodes.size() - 1) ? act.name() + ")" : act.name() + ", ");
            }
            return ModelFactory.sequence(sequenceNodes.toArray(Activity[]::new));
        }

        return nodes.get(0).clone();
    }

    @Override
    public double getFairTimeLimit() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }
}
