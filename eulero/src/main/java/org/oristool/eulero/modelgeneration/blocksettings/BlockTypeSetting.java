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

public abstract class BlockTypeSetting {
    private String type;
    private double probability;

    public BlockTypeSetting(String type, double probability) {
        this.type = type;
        this.probability = probability;
    }

    public String getType() {
        return type;
    }

    public double getProbability() {
        return probability;
    }
}
