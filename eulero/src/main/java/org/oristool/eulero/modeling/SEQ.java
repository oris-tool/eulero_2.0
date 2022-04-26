package org.oristool.eulero.modeling;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "SEQ")
public class SEQ extends DAG{
    protected SEQ(String name, List<Activity> activities){
        super(name);
        setMin(this.low());
        setMax(this.upp());
        setActivities(activities);
        initEdges();
    }

    public SEQ(){
        super("");
    };

    @Override
    public boolean isWellNested() {
        boolean isWellNested = true;
        for (Activity block: activities()) {
            isWellNested = isWellNested && block.isWellNested();
        }
        return isWellNested;
    }

    @Override
    public DAG copyRecursive(String suffix){
        DAG copy = DAG.sequence(this.name() + "_" + suffix, activities().stream()
                .map(a -> a.copyRecursive(suffix)).toArray(Activity[]::new));
        copy.setMin(copy.low());
        copy.setMax(copy.upp());
        copy.C();
        copy.Q();
        return copy;
    }

    private void initEdges(){
        ArrayList<DAGEdge> edges = new ArrayList<>();
        Activity prev = activities().get(0);
        for(int i = 1; i < activities().size(); i++){
            edges.add(new DAGEdge(prev.name(), activities().get(i).name()));
            prev = activities().get(i);
        }
        setEdges(edges);
    }
}
