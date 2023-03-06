package org.oristool.eulero.modeling.updates.activitytypes;

import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.ActivityEnumType;
import org.oristool.eulero.modeling.DAGEdge;
import org.oristool.eulero.modeling.updates.Composite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BadNestedDAGType extends DAGType{
    List<DAGEdge> edges;

    public BadNestedDAGType(List<DAGEdge> edges) {
        this.edges = edges;
    }

    @Override
    public void initPreconditions(Composite activity, Activity... children) {
        {
            List<String> startingNodes = edges.stream().map(DAGEdge::getPre).collect(Collectors.toList());
            List<String> endingNodes = edges.stream().map(DAGEdge::getPost).collect(Collectors.toList());
            List<String> intermediateNodes = (new ArrayList<>(startingNodes));
            intermediateNodes.retainAll(endingNodes);
            startingNodes.removeAll(intermediateNodes);
            endingNodes.removeAll(intermediateNodes);

            for(Activity act: children){
                if(startingNodes.contains(act.name())){
                    act.addPrecondition(activity.begin());
                }
                if(endingNodes.contains(act.name())){
                    activity.end().addPrecondition(act);
                }
            }

            for(DAGEdge edge: edges){
                Arrays.stream(children).filter(t->t.name().equals(edge.getPost())).collect(Collectors.toList()).get(0)
                        .addPrecondition(Arrays.stream(children).filter(t->t.name().equals(edge.getPre())).collect(Collectors.toList()).get(0));
            }
        }
    }

    @Override
    public void setEnumType(Composite activity) {
        activity.setEnumType(ActivityEnumType.DAG);
    }
}
