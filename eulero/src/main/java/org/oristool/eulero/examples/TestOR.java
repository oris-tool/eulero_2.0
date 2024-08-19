package org.oristool.eulero.examples;

import org.oristool.eulero.evaluation.approximator.TruncatedExponentialMixtureApproximation;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.evaluation.heuristics.EvaluationResult;
import org.oristool.eulero.evaluation.heuristics.SDFHeuristicsVisitor;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.ModelFactory;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.StochasticTime;
import org.oristool.eulero.modeling.stochastictime.UniformTime;
import org.oristool.eulero.ui.ActivityViewer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class TestOR {
    public static void main(String[] args) {
        AnalysisHeuristicsVisitor analyzer = new SDFHeuristicsVisitor(BigInteger.valueOf(2), BigInteger.valueOf(6), new TruncatedExponentialMixtureApproximation());
        Activity act = ORModel();
        Activity actSimulaton = ORModel();

        BigDecimal timeLimit = act.max();
        BigDecimal timeTick = act.getFairTimeTick();
        double[] analysis = act.analyze(timeLimit, timeTick, analyzer);

        EvaluationResult r1 = new EvaluationResult("Analysis", analysis, 0, analysis.length, timeTick.doubleValue(), 0);
        EvaluationResult r2 = new EvaluationResult("Simulation", act.simulate(timeLimit.toString(), timeTick.toString(), 10000), 0, analysis.length, 0.1, 0);

        ActivityViewer.plot("", List.of("Analysis", "Simulation"), timeLimit.doubleValue(), timeTick.doubleValue(), r1.cdf(), r2.cdf());



    }
    public static Activity ORModel(){
        StochasticTime time = new UniformTime(0, 2);

        Activity A = new Simple("A", time.clone());
        Activity B = new Simple("B", time.clone());
        Activity C = new Simple("C", time.clone());
        Activity D = new Simple("D", time.clone());
        C.addPrecondition(A);
        D.addPrecondition(A, B);
        Activity dag1 = ModelFactory.DAG(A, B, C, D);

        Activity E = new Simple("E", time.clone());
        Activity F = new Simple("F", time.clone());
        Activity G = new Simple("G", time.clone());
        Activity H = new Simple("H", time.clone());
        Activity I = new Simple("I", time.clone());
        Activity J = new Simple("J", time.clone());
        Activity K = new Simple("K", time.clone());
        K.addPrecondition(G, H);
        J.addPrecondition(I, H);
        I.addPrecondition(E);
        H.addPrecondition(E, F);
        G.addPrecondition(F);

        Activity dag2 = ModelFactory.DAG(E, F, G, H, I, J, K);

        return ModelFactory.OR(List.of(0.3, 0.5),
                dag1,
                dag2
        );

    }
}
