package org.oristool.eulero.examples;

import org.oristool.eulero.evaluation.approximator.TruncatedExponentialMixtureApproximation;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.evaluation.heuristics.SDFHeuristicsVisitor;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.ModelFactory;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.StochasticTime;
import org.oristool.eulero.modeling.stochastictime.UniformTime;

import java.math.BigInteger;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        StochasticTime time = new UniformTime(0,1);

        Activity A = new Simple("A", time.clone());
        Activity B = new Simple("B", time.clone());
        Activity C = new Simple("C", time.clone());
        Activity D = new Simple("D", time.clone());
        Activity E = new Simple("E", time.clone());
        Activity F = new Simple("F", time.clone());

        D.addPrecondition(A);
        E.addPrecondition(A, B);
        F.addPrecondition(A, C);
        Activity dag = ModelFactory.DAG(A, B, C, D, E, F);

        Activity A1 = new Simple("A1", time.clone());
        Activity B1 = new Simple("B1", time.clone());
        Activity C1 = new Simple("C1", time.clone());
        Activity D1 = new Simple("D1", time.clone());
        Activity E1 = new Simple("E1", time.clone());
        Activity F1 = new Simple("F1", time.clone());

        Activity myModel = ModelFactory.sequence(
                ModelFactory.forkJoin(
                        A1, B1,
                        ModelFactory.sequence(C1, D1)
                ),
                ModelFactory.XOR(
                        List.of(0.1, 0.9), E1, F1
                ),
                dag
        );



        AnalysisHeuristicsVisitor v = new SDFHeuristicsVisitor(BigInteger.ONE, BigInteger.ONE, new TruncatedExponentialMixtureApproximation());
        dag.analyze(dag.max(), dag.getFairTimeTick(), v);
        int test = 0;

    }
}
