package org.oristool.eulero;

import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.math.BigDecimal;
import java.util.List;

public class MainRiccardo {
    public static void main(String[] args) {
        BigDecimal timeBound = BigDecimal.valueOf(120);
        BigDecimal timeTick = BigDecimal.valueOf(0.01);
        BigDecimal error = BigDecimal.valueOf(0.001);
        int runs = 10000;

        //Simulation
        TransientSolution<DeterministicEnablingState, RewardRate> simulation = MainHelper.simulationSetup()
                .simulate(timeBound.toString(), timeTick.toString(), runs);

        TransientSolution<DeterministicEnablingState, RewardRate> analysis = MainHelper.analysisSetup1(new EXPMixtureApproximation(), timeTick)
                .analyze(timeBound.toString(), timeTick.toString(), error.toString());

        ActivityViewer.plot(List.of("Simulation", "Analysis"), simulation, analysis);
    }
}