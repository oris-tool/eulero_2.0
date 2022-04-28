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

public class WellNestedBlockSetting extends BlockTypeSetting {
    int maximumBreadth;
    int minimumBreadth;

    public WellNestedBlockSetting(String type, double probability, int maximumBreadth, int minimumBreadth) {
        super(type, probability);
        this.maximumBreadth = maximumBreadth;
        this.minimumBreadth = minimumBreadth;
    }

    public WellNestedBlockSetting(String type, double probability, int maximumBreadth) {
        this(type, probability, maximumBreadth, 2);
    }

    public int getMaximumBreadth() {
        return maximumBreadth;
    }

    public int getMinimumBreadth() {
        return minimumBreadth;
    }
}
