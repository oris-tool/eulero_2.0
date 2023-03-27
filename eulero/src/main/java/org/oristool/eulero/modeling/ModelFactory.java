package org.oristool.eulero.modeling;

import org.apache.commons.lang3.tuple.Pair;
import org.oristool.eulero.modeling.updates.activitytypes.*;
import org.oristool.eulero.modeling.updates.Composite;
import org.oristool.eulero.modeling.updates.Activity;

import java.util.ArrayList;
import java.util.List;

public class ModelFactory {
    public static Composite sequence(Activity... activities){
        StringBuilder name = new StringBuilder("SEQ(");
        for (Activity act: activities) {
            name.append(act.name()).append(", ");
        }
        name.deleteCharAt(name.length() - 1).deleteCharAt(name.length() - 1).append(")");

        ArrayList<Activity> children = new ArrayList<>(List.of(activities));
        Composite sequence = new Composite(name.toString(), new SEQType(children), ActivityEnumType.SEQ);
        sequence.getType().initActivity(sequence, activities);
        return sequence;
    };

    public static Composite forkJoin(Activity... activities){
        StringBuilder name = new StringBuilder("AND(");
        for (Activity act: activities) {
            name.append(act.name()).append(", ");
        }
        name.deleteCharAt(name.length() - 1).deleteCharAt(name.length() - 1).append(")");

        ArrayList<Activity> children = new ArrayList<>(List.of(activities));
        Composite forkJoin = new Composite(name.toString(), new ANDType(children), ActivityEnumType.AND);
        forkJoin.setActivities(children);
        forkJoin.getType().initActivity(forkJoin, activities);
        return forkJoin;
    };

    public static Composite DAG(List<DAGEdge> edges, Activity... activities){
        StringBuilder name = new StringBuilder("DAG(");
        for (Activity act: activities) {
            name.append(act.name()).append(", ");
        }
        name.deleteCharAt(name.length() - 1).deleteCharAt(name.length() - 1).append(")");

        ArrayList<Activity> children = new ArrayList<>(List.of(activities));
        Composite dag = new Composite(name.toString(), new BadNestedDAGType(children, edges), ActivityEnumType.DAG);
        dag.getType().initActivity(dag, activities);
        return dag;
    };

    public static Composite XOR(List<Double> probs, Activity... activities){
        StringBuilder name = new StringBuilder("XOR(");
        for (Activity act: activities) {
            name.append(act.name()).append(", ");
        }
        name.deleteCharAt(name.length() - 1).deleteCharAt(name.length() - 1).append(")");

        ArrayList<Activity> children = new ArrayList<>(List.of(activities));
        Composite xor = new Composite(name.toString(), new XORType(children, probs), ActivityEnumType.XOR);
        xor.getType().initActivity(xor, activities);
        return xor;
    }


}
