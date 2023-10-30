/* This program is called EULERO.
 * Copyright (C) 2022 The EULERO Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.oristool.eulero.modeling.deprecated;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.eulero.modeling.activitytypes.ActivityEnumType;

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
        setEnumType(ActivityEnumType.SEQ);
    }

    public SEQ(){
        super("");
        setEnumType(ActivityEnumType.SEQ);
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
