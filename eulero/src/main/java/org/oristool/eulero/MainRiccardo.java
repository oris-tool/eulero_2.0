package org.oristool.eulero;

import org.oristool.eulero.graph.DAG;
import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.eulero.math.approximation.HistogramApproximator;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;
import org.oristool.models.stpn.TransientSolutionViewer;

import java.math.BigDecimal;
import java.util.Map;

public class MainRiccardo {
    public static void main(String[] args) {
        Map<String, HistogramDistribution> histograms = MainHelper.getHistogramsDistributionMap();
        HistogramApproximator approximator = new EXPMixtureApproximation();
        BigDecimal timeTick = BigDecimal.valueOf(0.1);

        DAG mainAnalysis1 = MainHelper.analysisSetup1(histograms, approximator, timeTick);
        DAG mainForSimulation = MainHelper.simulationSetup(histograms, approximator);

        new TransientSolutionViewer(mainForSimulation.simulate("15", "0.05", 10000));

    }
}
