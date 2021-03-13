/* This program is part of the ORIS Tool.
 * Copyright (C) 2011-2020 The ORIS Authors.
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

package org.oristool.eulero;

import java.math.BigDecimal;
import java.util.Map;

import org.oristool.eulero.graph.*;
import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.eulero.math.approximation.HistogramApproximator;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.TransientSolutionViewer;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

public class Main {
    public static void main(String[] args) {
        Map<String, HistogramDistribution> histograms = MainHelper.getHistogramsDistributionMap();
        HistogramApproximator approximator = new EXPMixtureApproximation();
        BigDecimal timeTick = BigDecimal.valueOf(0.1);

        DAG mainAnalysis1 = MainHelper.analysisSetup1(histograms, approximator, timeTick);
        DAG mainForSimulation = MainHelper.simulationSetup(histograms, approximator);

        TransientSolution<DeterministicEnablingState, RewardRate> prova =  mainForSimulation.simulate("20", "0.1", 10000);

        new TransientSolutionViewer(prova);
    }
}
