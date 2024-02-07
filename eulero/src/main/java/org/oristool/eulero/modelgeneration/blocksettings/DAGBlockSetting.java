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

package org.oristool.eulero.modelgeneration.blocksettings;

public class DAGBlockSetting extends BlockTypeSetting{
    private final int minimumLevels;
    private final int maximumLevels;
    private final int minimumLevelBreadth;
    private final int maximumLevelBreadth;
    private final int maximumAdjacencyDistance;
    private final int maximumNodeConnection;
    private final int minimumNodeConnection;

    public DAGBlockSetting(double probability, int minimumLevels, int maximumLevels, int minimumLevelBreadth, int maximumLevelBreadth, int maximumAdjacencyDistance, int minimumNodeConnection, int maximumNodeConnection) {
        super("DAG", probability);
        this.minimumLevels = minimumLevels;
        this.maximumLevels = maximumLevels;
        this.minimumLevelBreadth = minimumLevelBreadth;
        this.maximumLevelBreadth = maximumLevelBreadth;
        this.maximumAdjacencyDistance = maximumAdjacencyDistance;
        this.maximumNodeConnection = maximumNodeConnection;
        this.minimumNodeConnection = minimumNodeConnection;
    }

    public DAGBlockSetting(double probability){
        super("DAG", probability);
        this.minimumLevels = 2;
        this.maximumLevels = 4;
        this.minimumLevelBreadth = 2;
        this.maximumLevelBreadth = 3;
        this.maximumAdjacencyDistance = 1;
        this.maximumNodeConnection = 2;
        this.minimumNodeConnection = 1;
    }

    public int getMinimumLevels() {
        return minimumLevels;
    }

    public int getMaximumLevels() {
        return maximumLevels;
    }

    public int getMinimumLevelBreadth() {
        return minimumLevelBreadth;
    }

    public int getMaximumLevelBreadth() {
        return maximumLevelBreadth;
    }

    public int getMaximumAdjacencyDistance() {
        return maximumAdjacencyDistance;
    }

    public int getMaximumNodeConnection() {
        return maximumNodeConnection;
    }

    public int getMinimumNodeConnection() {
        return minimumNodeConnection;
    }

}
