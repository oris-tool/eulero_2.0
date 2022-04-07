package org.oristool.eulero.workflow;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "AND")
public class AND extends DAG {


    public AND(){
        super("");
    };

    protected AND(String name, List<Activity> activities) {
        super(name);
        setEFT(this.low());
        setLFT(this.upp());
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
        copy.setEFT(copy.low());
        copy.setLFT(copy.upp());
        copy.C();
        copy.Q();
        return copy;

    }
}

