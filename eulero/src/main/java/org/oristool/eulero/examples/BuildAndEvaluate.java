package org.oristool.eulero.examples;

import com.google.common.collect.Lists;
import org.oristool.eulero.evaluation.approximator.Approximator;
import org.oristool.eulero.evaluation.approximator.EXPMixtureApproximation;
import org.oristool.eulero.evaluation.heuristic.AnalysisHeuristic1;
import org.oristool.eulero.evaluation.heuristic.AnalysisHeuristicStrategy;
import org.oristool.eulero.evaluation.heuristic.EvaluationResult;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.eulero.workflow.Activity;
import org.oristool.eulero.workflow.DAG;
import org.oristool.eulero.workflow.Simple;
import org.oristool.eulero.workflow.Xor;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class BuildAndEvaluate {
    public static void main(String[] args) {
        StochasticTransitionFeature feature = StochasticTransitionFeature.newUniformInstance("0", "1");

        Activity Q = DAG.sequence("Q",
                DAG.forkJoin("Q1",
                        new Simple("Q1A", feature),
                        new Simple("Q1B", feature)

                ),
                DAG.forkJoin("Q2",
                        new Simple("Q2A", feature),
                        new Simple("Q2B", feature)
                )
        );

        Activity R = DAG.forkJoin("R",
                new Xor("R1",
                        List.of(
                                new Simple("R1A", feature),
                                new Simple("R1b", feature)
                        ),
                        List.of(0.3, 0.7)),
                DAG.sequence("R2",
                        new Simple("R2A", feature),
                        new Simple("R2B", feature)
                )
        );

        Activity S = DAG.forkJoin("S",
                DAG.sequence("S1",
                        new Simple("S1A", feature),
                        new Simple("S1B", feature),
                        new Simple("S1C", feature)
                ),
                DAG.sequence("S2",
                        new Simple("S2A", feature),
                        new Simple("S2B", feature),
                        new Simple("S2C", feature)
                )
        );

        DAG T = DAG.sequence("T",
                DAG.forkJoin("T1",
                        new Simple("T1A", feature),
                        new Simple("T1B", feature),
                        new Simple("T1C", feature)
                ),
                DAG.forkJoin("T2",
                        new Simple("T2A", feature),
                        new Simple("T2B", feature),
                        new Simple("T2C", feature)
                )
        );

        DAG top = DAG.empty("TOP");
        Q.addPrecondition(top.begin());
        R.addPrecondition(top.begin());
        T.addPrecondition(R);
        S.addPrecondition(R, Q);
        top.end().addPrecondition(T, S);
        top.setEFT(top.getEFTBound(top.end()));
        top.setLFT(top.getLFTBound(top.end()));
        top.setActivities(Lists.newArrayList(Q, R, S, T));

        BigInteger tC = BigInteger.valueOf(3);
        BigInteger tQ = BigInteger.valueOf(7);
        BigDecimal timeLimit = top.LFT();
        BigDecimal step = BigDecimal.valueOf(0.01);
        Approximator approximator = new EXPMixtureApproximation();
        AnalysisHeuristicStrategy strategy = new AnalysisHeuristic1(tC, tQ, approximator);
        double[] cdf = strategy.analyze(top, timeLimit.add(BigDecimal.ONE), step);

        ActivityViewer.CompareResults("Example", List.of("Heuristic 1"), List.of(
                new EvaluationResult("Heuristic 1", cdf, 0, cdf.length, top.getFairTimeTick().doubleValue(), 0)
        ));
    }
}
