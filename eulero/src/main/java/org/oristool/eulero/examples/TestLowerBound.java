package org.oristool.eulero.examples;

import org.oristool.eulero.evaluation.approximator.LowerBoundTruncatedExponentialMixtureApproximation;
import org.oristool.eulero.evaluation.heuristics.EvaluationResult;
import org.oristool.eulero.evaluation.heuristics.RBFHeuristicsVisitor;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.ModelFactory;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.UniformTime;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class TestLowerBound {
    public static void main(String[] args) {
        Activity test = ModelFactory.sequence(
                new Simple("a", new UniformTime(1, 4)),
                new Simple("b", new UniformTime(2,3)),
                new Simple("c", new UniformTime(4,5))
        );


        List<EvaluationResult> results = new ArrayList<>();
        TransientSolution<DeterministicEnablingState, RewardRate> simulation = test.simulate(test.max().toString(), test.getFairTimeTick().toString(), 100000);
        double[] simul = new double[simulation.getSolution().length];
        for(int i = 0; i < simul.length; i++){
            simul[i] = simulation.getSolution()[i][0][0];
        }
        results.add(new EvaluationResult("Simul", simul, 0, simul.length, test.getFairTimeTick().doubleValue(), 0));

        double[] appr = test.analyze(test.max(), test.getFairTimeTick(), new RBFHeuristicsVisitor(BigInteger.ONE,BigInteger.ONE, new LowerBoundTruncatedExponentialMixtureApproximation()));
        Simple approximation = new Simple("we", new LowerBoundTruncatedExponentialMixtureApproximation().getApproximatedStochasticTime(appr, test.low().doubleValue(), test.upp().doubleValue(), test.getFairTimeTick()));
        double[] appr0 = approximation.analyze(test.max(), test.getFairTimeTick(), new RBFHeuristicsVisitor(BigInteger.ONE,BigInteger.ONE, new LowerBoundTruncatedExponentialMixtureApproximation()));
        results.add(new EvaluationResult("App", appr0, 0, appr0.length, test.getFairTimeTick().doubleValue(), 0));
        ActivityViewer.CompareResults("test", List.of("Simul", "Appr"), results);
    }
}
