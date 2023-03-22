package on_going_works;

import org.oristool.eulero.evaluation.approximator.EXPMixtureApproximation;
import org.oristool.eulero.evaluation.heuristics.SDFHeuristicsVisitor;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.evaluation.heuristics.EvaluationResult;
import org.oristool.eulero.evaluation.heuristics.backup.AnalysisHeuristics1;
import org.oristool.eulero.evaluation.heuristics.backup.AnalysisHeuristicsStrategy;

import org.oristool.eulero.modeling.DAG;
import org.oristool.eulero.modeling.DAGEdge;
import org.oristool.eulero.modeling.ModelFactory;
import org.oristool.eulero.modeling.XOR;
import org.oristool.eulero.modeling.stochastictime.StochasticTime;
import org.oristool.eulero.modeling.stochastictime.UniformTime;
import org.oristool.eulero.modeling.updates.Activity;
import org.oristool.eulero.modeling.updates.Simple;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class eulero {
    public static void main(String[] args) {

        Activity test1 = modelloDAG();
        org.oristool.eulero.modeling.Activity test2 = modelloDAGVecchio();

        AnalysisHeuristicsStrategy strat = new AnalysisHeuristics1(BigInteger.valueOf(3), BigInteger.TEN, new EXPMixtureApproximation());
        double[] cane = test1.analyze(test1.max().add(BigDecimal.ONE), test1.getFairTimeTick(), new SDFHeuristicsVisitor(BigInteger.valueOf(3), BigInteger.TEN, new EXPMixtureApproximation()));
        double[] cane2 = strat.analyze(test2, test2.max().add(BigDecimal.ONE), test2.getFairTimeTick());
        int i = 0;
        ActivityViewer.CompareResults("", List.of("Nuovo", "Vecchio"), List.of(
                new EvaluationResult("nuovo", cane, 0, cane.length, 0.01, 0),
                new EvaluationResult("nuovo", cane2, 0, cane2.length, 0.01, 0)
        ));

    }


    public static Activity modelloDAG() {
        StochasticTransitionFeature feat = StochasticTransitionFeature.newUniformInstance("0", "1");
        StochasticTime time = new UniformTime(BigDecimal.ZERO, BigDecimal.ONE);

        Activity test = ModelFactory.DAG(
                List.of(
                        DAGEdge.of(0, 2),
                        DAGEdge.of(0, 3),
                        DAGEdge.of(1, 3)
                ),
                ModelFactory.forkJoin(
                        ModelFactory.forkJoin(
                                ModelFactory.XOR(
                                        List.of(0.4, 0.6),
                                        new Simple("A", time),
                                        new Simple("B", time)
                                ),
                                new Simple("C", time)
                        ),
                        ModelFactory.sequence(
                                ModelFactory.sequence(
                                        new Simple("D", time),
                                        new Simple("E", time)
                                ),
                                new Simple("F", time)
                        )
                ),
                new Simple("B", time),
                new Simple("C", time),
                new Simple("D", time)
        );

        return test;
    }
    public static org.oristool.eulero.modeling.Activity modelloDAGVecchio() {
        StochasticTime time = new UniformTime(BigDecimal.ZERO, BigDecimal.ONE);
        StochasticTransitionFeature feat = StochasticTransitionFeature.newUniformInstance("0", "1");


        DAG test2 = DAG.empty("DAG");
        org.oristool.eulero.modeling.Activity a = DAG.forkJoin("AND(AND(XOR(A, B), C), SEQ(SEQ(D, E), F))",
                DAG.forkJoin("AND(XOR(A, B), C)",
                        new XOR("XOR(A, B)",
                                List.of(
                                        new org.oristool.eulero.modeling.Simple("A", feat),
                                        new org.oristool.eulero.modeling.Simple("B", feat)
                                ), List.of(0.40, 0.6)
                        ),
                        new org.oristool.eulero.modeling.Simple("C", feat)
                ),
                DAG.sequence("SEQ(SEQ(D, E), F)",
                        DAG.sequence("SEQ(D, E)",
                                new org.oristool.eulero.modeling.Simple("D", feat),
                                new org.oristool.eulero.modeling.Simple("E", feat)
                        ),
                        new org.oristool.eulero.modeling.Simple("F", feat)
                )
        );
        org.oristool.eulero.modeling.Simple b = new org.oristool.eulero.modeling.Simple("B", feat);
        org.oristool.eulero.modeling.Simple c = new org.oristool.eulero.modeling.Simple("C", feat);
        org.oristool.eulero.modeling.Simple d = new org.oristool.eulero.modeling.Simple("D", feat);
        test2.end().addPrecondition(c,d);
        c.addPrecondition(a);
        d.addPrecondition(a, b);
        a.addPrecondition(test2.begin());
        b.addPrecondition(test2.begin());
        test2.setActivities(List.of(a, b, c, d));
        test2.resetSupportBounds();

        return test2;
    }

    /*public static Activity modelloWellN1() {
        StochasticTransitionFeature feat = StochasticTransitionFeature.newUniformInstance("0", "1");
        StochasticTime time = new UniformTime(BigDecimal.ZERO, BigDecimal.ONE);

        Activity test = ModelFactory.forkJoin(
                ModelFactory.forkJoin(
                        ModelFactory.forkJoin(
                                ModelFactory.forkJoin(
                                        ModelFactory.XOR(
                                                List.of(0.41, 0.6),
                                                new Simple("A", time),
                                                new Simple("B", time)
                                        ),
                                        new Simple("C", time)
                                ),
                                ModelFactory.sequence(
                                        ModelFactory.sequence(
                                                new Simple("D", time),
                                                new Simple("E", time)
                                        ),
                                        new Simple("F", time)
                                )
                        ),
                        ModelFactory.sequence(
                                ModelFactory.XOR(
                                        List.of(0.4, 0.6),
                                        new Simple("G", time),
                                        new Simple("H", time)
                                ),
                                ModelFactory.forkJoin(
                                        new Simple("I", time),
                                        new Simple("J", time)
                                )
                        )
                ),
                ModelFactory.forkJoin(
                        ModelFactory.forkJoin(
                                ModelFactory.forkJoin(
                                        ModelFactory.sequence(
                                                new Simple("K", time),
                                                new Simple("L", time)
                                        ),
                                        ModelFactory.sequence(
                                                new Simple("M", time),
                                                new Simple("N", time)
                                        )
                                ),
                                ModelFactory.sequence(
                                        ModelFactory.XOR(
                                                List.of(0.35, 0.65),
                                                new Simple("O", time),
                                                new Simple("P", time)
                                        ),
                                        new Simple("Q", time)

                                )
                        ),
                        ModelFactory.sequence(
                                new Simple("R", time),
                                ModelFactory.forkJoin(
                                        new Simple("S", time),
                                        new Simple("T", time)
                                )
                        )
                )
        );

        return test;

    }

    public static Activity modelloWN1Vecchio(){
        StochasticTransitionFeature feat = StochasticTransitionFeature.newUniformInstance("0", "1");
        StochasticTime time = new UniformTime(BigDecimal.ZERO, BigDecimal.ONE);


        Activity test2 = DAG.forkJoin("AND(AND(AND(AND(XOR(A, B), C), SEQ(SEQ(D, E), F)), SEQ(XOR(G, H), AND(I, J))), AND(AND(AND(SEQ(K, L), SEQ(M, N)), SEQ(XOR(O, P), Q)), SEQ(R, AND(S, T))))",
                DAG.forkJoin("AND(AND(AND(XOR(A, B), C), SEQ(SEQ(D, E), F)), SEQ(XOR(G, H), AND(I, J)))",
                        DAG.forkJoin("AND(AND(XOR(A, B), C), SEQ(SEQ(D, E), F))",
                                DAG.forkJoin("AND(XOR(A, B), C)",
                                        new XOR("XOR(A, B)",
                                                List.of(
                                                        new Simple("A", time),
                                                        new Simple("B", time)
                                                ), List.of(0.41, 0.6)
                                        ),
                                        new Simple("C", time)
                                ),
                                DAG.sequence("SEQ(SEQ(D, E), F)",
                                        DAG.sequence("SEQ(D, E)",
                                                new Simple("D", time),
                                                new Simple("E", time)
                                        ),
                                        new Simple("F", time)
                                )
                        ),
                        DAG.sequence("SEQ(XOR(G, H), AND(I, J))",
                                new XOR("XOR(G, H)",
                                        List.of(
                                                new Simple("G", time),
                                                new Simple("H", time)
                                        ), List.of(0.4, 0.6)
                                ),
                                DAG.forkJoin("AND(I, J)",
                                        new Simple("I", time),
                                        new Simple("J", time)
                                )
                        )
                ),
                DAG.forkJoin("AND(AND(AND(SEQ(K, L), SEQ(M, N)), SEQ(XOR(O, P), Q)), SEQ(R, AND(S, T)))",
                        DAG.forkJoin("AND(AND(SEQ(K, L), SEQ(M, N)), SEQ(XOR(O, P), Q))",
                                DAG.forkJoin("AND(SEQ(K, L), SEQ(M, N))",
                                        DAG.sequence("SEQ(K, L)",
                                                new Simple("K", time),
                                                new Simple("L", time)
                                        ),
                                        DAG.sequence("SEQ(M, N)",
                                                new Simple("M", time),
                                                new Simple("N", time)
                                        )
                                ),
                                DAG.sequence("SEQ(XOR(O, P), Q)",
                                        new XOR("XOR(O, P)",
                                                List.of(
                                                        new Simple("O", time),
                                                        new Simple("P", time)
                                                ), List.of(0.35, 0.65)
                                        ),
                                        new Simple("Q", time)

                                )
                        ),
                        DAG.sequence("SEQ(R, AND(S, T))",
                                new Simple("R", time),
                                DAG.forkJoin("AND(S, T)",
                                        new Simple("S", time),
                                        new Simple("T", time)
                                )
                        )
                )
        );

        return test2;
    }*/
}
