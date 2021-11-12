package org.oristool.eulero.analysisheuristics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oristool.eulero.graph.*;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.approximation.SplineBodyEXPTailApproximation;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class ComplexityMeasuresTest {

    @Test
    void TestSingleActivityComplexityMeasure(){
        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(0.8));

        BigInteger C =  BigInteger.valueOf(3);
        BigInteger R =  BigInteger.valueOf(10);
        Approximator approximator = new SplineBodyEXPTailApproximation(3);
        AnalysisHeuristicStrategy analyzer = new AnalysisHeuristic1(C, R, approximator);

        Activity model = new Analytical("A6", unif0_10);

        Assertions.assertTrue(model.C().compareTo(C) < 0);
        Assertions.assertEquals(0, model.C().compareTo(BigInteger.ONE));
    }

    @Test
    void TestANDComplexityMeasure(){
        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(0.8));
        BigInteger C =  BigInteger.valueOf(3);
        BigInteger R =  BigInteger.valueOf(10);

        Activity model = DAG.forkJoin("test",
                new Analytical("A6", unif0_10),
                new Analytical("A1", unif0_10)
        );

        Assertions.assertTrue(model.C().compareTo(C) < 0);
        Assertions.assertEquals(0, model.C().compareTo(BigInteger.valueOf(2)));
    }

    @Test
    void TestSEQComplexityMeasure(){
        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(0.8));
        BigInteger C =  BigInteger.valueOf(3);
        BigInteger R =  BigInteger.valueOf(10);

        Activity model = DAG.sequence("test",
                new Analytical("A6", unif0_10),
                new Analytical("A1", unif0_10)
        );

        Assertions.assertTrue(model.C().compareTo(C) < 0);
        Assertions.assertEquals(0, model.C().compareTo(BigInteger.ONE));
    }

    @Test
    void TestXORComplexityMeasure(){
        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(0.8));
        BigInteger C =  BigInteger.valueOf(3);
        BigInteger R =  BigInteger.valueOf(10);

        Activity simpleModel = new Xor("test",
                List.of(
                    new Analytical("A6", unif0_10),
                    new Analytical("A1", unif0_10)
                ),
                List.of(0.3, 0.4)
        );

        Activity complexModel = new Xor("testC",
                List.of(
                        new Analytical("A5", unif0_10),
                        DAG.forkJoin("AND",
                                new Analytical("A1", unif0_10),
                                new Analytical("A2", unif0_10)
                        )
                ),
                List.of(0.3, 0.4)
        );

        Activity veryComplexModel = new Xor("testB",
                List.of(
                        new Analytical("A6", unif0_10),
                        DAG.sequence("SEQ",
                                DAG.forkJoin("AND",
                                        new Analytical("A1", unif0_10),
                                        DAG.forkJoin("AND_inner",
                                                new Analytical("A3", unif0_10),
                                                new Analytical("A4", unif0_10)
                                        )
                                ), new Analytical("A5", unif0_10)

                        )
                ),
                List.of(0.3, 0.4)
        );

        Assertions.assertTrue(simpleModel.C().compareTo(C) < 0);
        Assertions.assertEquals(0, simpleModel.C().compareTo(BigInteger.ONE));

        Assertions.assertTrue(complexModel.C().compareTo(C) < 0);
        Assertions.assertEquals(0, complexModel.C().compareTo(BigInteger.valueOf(2)));

        Assertions.assertEquals(0, veryComplexModel.C().compareTo(C));
        Assertions.assertEquals(0, veryComplexModel.C().compareTo(BigInteger.valueOf(3)));
    }

    @Test
    void TestRepeatComplexityMeasure(){
        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(0.8));
        BigInteger C =  BigInteger.valueOf(3);
        BigInteger R =  BigInteger.valueOf(10);

        Activity repeatBody = new Xor("testB",
                List.of(
                        new Analytical("A6", unif0_10),
                        DAG.sequence("SEQ",
                                DAG.forkJoin("AND_inner",
                                        new Analytical("A3", unif0_10),
                                        new Analytical("A4", unif0_10)
                                ),
                                new Analytical("A5", unif0_10)
                        )
                ),
                List.of(0.3, 0.4)
        );

        Activity repeat = new Repeat("Repeat", 0.4, repeatBody);

        Activity complexRepeatBody = new Xor("testC",
                List.of(
                        new Analytical("A6", unif0_10),
                        DAG.forkJoin("FJ",
                                DAG.forkJoin("AND_inner",
                                        new Analytical("A3", unif0_10),
                                        new Analytical("A4", unif0_10)
                                ),
                                DAG.forkJoin("AND_inner2",
                                        new Analytical("A5", unif0_10),
                                        new Analytical("A7", unif0_10)
                                )
                        )
                ),
                List.of(0.3, 0.4)
        );

        Activity complexRepeat = new Repeat("CRepeat", 0.4, complexRepeatBody);

        Assertions.assertTrue(repeat.C().compareTo(C) < 0);
        Assertions.assertEquals(0, repeat.C().compareTo(BigInteger.valueOf(2)));

        Assertions.assertTrue(complexRepeat.C().compareTo(C) > 0);
        Assertions.assertEquals(0, complexRepeat.C().compareTo(BigInteger.valueOf(4)));
    }
}
