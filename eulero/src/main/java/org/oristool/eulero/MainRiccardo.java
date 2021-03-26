package org.oristool.eulero;

import org.oristool.eulero.graph.DAG;
import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.math.BigDecimal;
import java.util.List;

public class MainRiccardo {
    public static void main(String[] args) {
        BigDecimal timeBound = BigDecimal.valueOf(40);
        BigDecimal timeTick = BigDecimal.valueOf(0.1);
        BigDecimal error = BigDecimal.valueOf(0.001);
        int runs = 20000;

        //Simulation
        TransientSolution<DeterministicEnablingState, RewardRate> simulation = MainHelper.simulationSetup()
                .simulate(timeBound.toString(), timeTick.toString(), runs);

        DAG main = MainHelper.analysisSetup1(new EXPMixtureApproximation(), timeTick);

        System.out.println("Comincia l'analisi del main semplificato");
        TransientSolution<DeterministicEnablingState, RewardRate> analysis = main.analyze(timeBound.toString(), timeTick.toString(), error.toString());
        System.out.println("Finita l'analisi del main semplificato");

        ActivityViewer.plot(List.of("Simulation", "Analysis"), simulation, analysis);
    }
}