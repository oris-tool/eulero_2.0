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

import java.util.List;

@XmlRootElement(name = "AND")
public class AND extends DAG {
    public AND(){
        super("");
        setEnumType(ActivityEnumType.AND);
    };

    protected AND(String name, List<Activity> activities) {
        super(name);
        setEnumType(ActivityEnumType.AND);
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

