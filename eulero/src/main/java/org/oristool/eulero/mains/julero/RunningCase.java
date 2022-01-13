package org.oristool.eulero.mains.julero;

import org.oristool.eulero.analysisheuristics.AnalysisHeuristic1;
import org.oristool.eulero.analysisheuristics.AnalysisHeuristic2;
import org.oristool.eulero.analysisheuristics.AnalysisHeuristic3;
import org.oristool.eulero.analysisheuristics.AnalysisHeuristicStrategy;
import org.oristool.eulero.graph.*;
import org.oristool.eulero.mains.TestCaseHandler;
import org.oristool.eulero.mains.TestCaseResult;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.eulero.models.ModelBuilder;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class RunningCase {

    public static void main(String[] args) {
        String savePathPrefix = System.getProperty("user.dir") + "/results/JULERO/RunningCase";

        BigDecimal timeLimit = BigDecimal.valueOf(8);
        BigDecimal timeTick = BigDecimal.valueOf(0.01) ;
        BigDecimal timeError = timeTick.divide(BigDecimal.valueOf(10));
        int groundTruthRuns = 1000;
        BigInteger C = BigInteger.valueOf(2);
        BigInteger R = BigInteger.valueOf(7);

        Approximator approximator = new EXPMixtureApproximation();
        AnalysisHeuristicStrategy strategy1 = new AnalysisHeuristic1(C, R, approximator, true);
        AnalysisHeuristicStrategy strategy2 = new AnalysisHeuristic2(C, R, approximator, true);
        AnalysisHeuristicStrategy strategy3 = new AnalysisHeuristic3(C, R, approximator, true);
        List<AnalysisHeuristicStrategy> strategies = List.of(strategy1, strategy2);

        ModelBuilder modelBuilder = new ModelBuilder() {
            @Override
            public Activity buildModel() {
                StochasticTransitionFeature feature = StochasticTransitionFeature.newUniformInstance("0", "1");

                //Blocco Q
                Activity q1 = new Analytical("Act0", feature);
                Activity q2 = new Analytical("Act1", feature);
                Activity q3 = new Analytical("Act2", feature);
                Activity q4 = new Analytical("Act3", feature);



                /*Activity Q_rep = new Repeat("Q_rep", 0.3, new Analytical("Act1", feature));
                Activity Q = DAG.forkJoin("Q_and",
                        new Analytical("Act0", feature),
                        Q_rep
                );*/


                DAG Q = DAG.empty("Q");
                q1.addPrecondition(Q.begin());
                q2.addPrecondition(Q.begin());
                q3.addPrecondition(q1, q2);
                q4.addPrecondition(q2);
                Q.end().addPrecondition(q3, q4);

                //Blocco R
                Activity R = DAG.forkJoin("R",
                        new Xor("R1",
                                List.of(
                                        new Analytical("Act4", feature),
                                        new Analytical("Act5", feature)
                                ), List.of(0.3, 0.7)),
                        DAG.sequence("R2",
                                new Analytical("Act6", feature),
                                new Analytical("Act7", feature)
                        )
                );

                //Blocco S
                Activity S_and = DAG.forkJoin("S",
                        DAG.sequence("S1",
                                new Analytical("Act8", feature),
                                new Analytical("Act9", feature)
                        ),
                        DAG.sequence("S2",
                                new Analytical("Act10", feature),
                                new Analytical("Act11", feature)
                        ),
                        DAG.sequence("S3",
                                new Analytical("Act12", feature),
                                new Analytical("Act13", feature)
                        )
                );
                //Activity S = new Repeat("S", 0.2, S_and);


                //Blocco T
                Analytical act12 =  new Analytical("Act14", feature);
                Analytical act13 =  new Analytical("Act15", feature);
                Analytical act14 =  new Analytical("Act16", feature);
                Analytical act15 =  new Analytical("Act17", feature);
                Analytical act16 =  new Analytical("Act18", feature);
                DAG T = DAG.empty("T");

                act12.addPrecondition(T.begin());
                act13.addPrecondition(T.begin());
                act14.addPrecondition(T.begin());
                act15.addPrecondition(act12, act13);
                act16.addPrecondition(act13, act14);
                T.end().addPrecondition(act15, act16);
                T.setEFT(T.getSupportLowerBound(T.end()));
                T.setLFT(T.getSupportUpperBound(T.end()));

                DAG runningExample = DAG.empty("Main");
                Q.addPrecondition(runningExample.begin());
                R.addPrecondition(runningExample.begin());
                T.addPrecondition(R);
                S_and.addPrecondition(R, Q);
                runningExample.end().addPrecondition(T, S_and);
                runningExample.setEFT(runningExample.getSupportLowerBound(runningExample.end()));
                runningExample.setLFT(runningExample.getSupportUpperBound(runningExample.end()));

                return runningExample;
            }
        };
        TestCaseHandler testCaseHandler = new TestCaseHandler("RunningExample", modelBuilder, strategies , groundTruthRuns, 1575, savePathPrefix, true, true);
        ArrayList<TestCaseResult> results = null;
        try {
            results = testCaseHandler.runTestCase(timeLimit, timeTick, timeError);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        testCaseHandler.plotResults(results);
    }
}
