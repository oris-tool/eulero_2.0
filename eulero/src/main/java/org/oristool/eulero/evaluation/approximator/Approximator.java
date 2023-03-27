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

package org.oristool.eulero.evaluation.approximator;

import org.apache.commons.lang3.tuple.Pair;
import org.oristool.eulero.modeling.stochastictime.StochasticTime;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;

import java.util.ArrayList;

public abstract class Approximator {

    public Approximator(){};

    public abstract Pair<BigDecimal,StochasticTransitionFeature> getApproximatedStochasticTransitionFeature(double[] cdf, double low, double upp, BigDecimal step);

    public abstract ArrayList<Pair<BigDecimal,StochasticTransitionFeature>> getApproximatedStochasticTransitionFeatures(double[] cdf, double low, double upp, BigDecimal step);

    public abstract StochasticTime getApproximatedStochasticTime(double[] cdf, double low, double upp, BigDecimal step);
}
