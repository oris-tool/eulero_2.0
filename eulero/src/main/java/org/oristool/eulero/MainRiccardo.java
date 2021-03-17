package org.oristool.eulero;

import org.oristool.eulero.graph.DAG;
import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.eulero.math.approximation.HistogramApproximator;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class MainRiccardo {
    public static void main(String[] args) {
        Map<String, HistogramDistribution> histograms = MainHelper.getHistogramsDistributionMap();
        HistogramApproximator approximator = new EXPMixtureApproximation();
        BigDecimal timeBound = BigDecimal.valueOf(45);
        BigDecimal timeTick = BigDecimal.valueOf(0.01);
        BigDecimal error = BigDecimal.valueOf(0.001);
        int runs = 10000;

        DAG mainForSimulation = MainHelper.simulationSetup(histograms, approximator);
        TransientSolution<DeterministicEnablingState, RewardRate> simulation =
                mainForSimulation.simulate(timeBound.toString(), timeTick.toString(), runs);

        DAG mainAnalysis = MainHelper.analysisSetup1(histograms, approximator, timeTick);

        TransientSolution<DeterministicEnablingState, RewardRate> analysis =
                mainAnalysis.analyze(timeBound.toString(), timeTick.toString(), error.toString());
        System.out.println("Finita l'analisi");

        ActivityViewer.plot(List.of("Simulation", "Analysis"), simulation, analysis);

    }
}