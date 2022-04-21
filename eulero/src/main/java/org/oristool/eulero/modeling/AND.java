package org.oristool.eulero.modeling;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "AND")
public class AND extends DAG {


    public AND(){
        super("");
    };

    protected AND(String name, List<Activity> activities) {
        super(name);
        setMin(this.low());
        setMax(this.upp());
        setActivities(activities);
    }

    @Override
    public boolean isWellNested() {
        boolean isWellNested = true;
        for (Activity block : activities()) {
            isWellNested = isWellNested && block.isWellNested();
        }
        return isWellNested;
    }

    @Override
    public DAG copyRecursive(String suffix){
        DAG copy = DAG.forkJoin(this.name() + "_" + suffix, activities().stream()
                .map(a -> a.copyRecursive(suffix)).toArray(Activity[]::new));
        copy.setMin(copy.low());
        copy.setMax(copy.upp());
        copy.C();
        copy.Q();
        return copy;

    }
}

