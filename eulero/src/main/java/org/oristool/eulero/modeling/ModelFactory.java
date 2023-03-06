package org.oristool.eulero.modeling;

import org.apache.commons.lang3.tuple.Pair;
import org.oristool.eulero.modeling.updates.activitytypes.*;
import org.oristool.eulero.modeling.updates.Composite;

import java.util.ArrayList;
import java.util.List;

public class ModelFactory {
    public static Activity sequence(Activity... activities){
        StringBuilder name = new StringBuilder("SEQ(");
        for (Activity act: activities) {
            name.append(act.name()).append(", ");
        }
        name.deleteCharAt(name.length() - 1).deleteCharAt(name.length() - 1).append(")");
        Composite sequence = new Composite(name.toString(), new SEQType(), ActivityEnumType.SEQ);
        sequence.getType().initActivity(sequence, activities);
        return sequence;
    };

    public static Activity forkJoin(Activity... activities){
        StringBuilder name = new StringBuilder("AND(");
        for (Activity act: activities) {
            name.append(act.name()).append(", ");
        }
        name.deleteCharAt(name.length() - 1).deleteCharAt(name.length() - 1).append(")");

        Composite forkJoin = new Composite(name.toString(), new ANDType(), ActivityEnumType.AND);
        forkJoin.setActivities(new ArrayList<>(List.of(activities)));
        forkJoin.getType().initActivity(forkJoin, activities);
        return forkJoin;
    };

    public static Activity DAG(List<DAGEdge> edges, Activity... activities){
        StringBuilder name = new StringBuilder("DAG(");
        for (Activity act: activities) {
            name.append(act.name()).append(", ");
        }
        name.deleteCharAt(name.length() - 1).deleteCharAt(name.length() - 1).append(")");

        Composite dag = new Composite(name.toString(), new BadNestedDAGType(edges), ActivityEnumType.DAG);
        dag.getType().initActivity(dag, activities);
        return dag;
    };

    public static Activity XOR(List<Double> probs, Activity... activities){
        StringBuilder name = new StringBuilder("XOR(");
        for (Activity act: activities) {
            name.append(act.name()).append(", ");
        }
        name.deleteCharAt(name.length() - 1).deleteCharAt(name.length() - 1).append(")");

        Composite xor = new Composite(name.toString(), new XORType(probs), ActivityEnumType.XOR);
        xor.getType().initActivity(xor, activities);
        return xor;
    }


}
