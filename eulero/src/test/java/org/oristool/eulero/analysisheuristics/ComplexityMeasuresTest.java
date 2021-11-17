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

        // C
        Assertions.assertTrue(model.C().compareTo(C) < 0);
        Assertions.assertEquals(0, model.C().compareTo(BigInteger.ONE));

        // Simplified C
        Assertions.assertTrue(model.simplifiedC().compareTo(C) < 0);
        Assertions.assertEquals(0, model.simplifiedC().compareTo(BigInteger.ONE));
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

        // C
        Assertions.assertTrue(model.C().compareTo(C) < 0);
        Assertions.assertEquals(0, model.C().compareTo(BigInteger.valueOf(2)));

        // Simplified C
        Assertions.assertTrue(model.simplifiedC().compareTo(C) < 0);
        Assertions.assertEquals(0, model.simplifiedC().compareTo(BigInteger.valueOf(2)));
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

        // C
        Assertions.assertTrue(model.C().compareTo(C) < 0);
        Assertions.assertEquals(0, model.C().compareTo(BigInteger.ONE));

        // Simplified C
        Assertions.assertTrue(model.simplifiedC().compareTo(C) < 0);
        Assertions.assertEquals(0, model.simplifiedC().compareTo(BigInteger.ONE));
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

        // C
        Assertions.assertTrue(simpleModel.C().compareTo(C) < 0);
        Assertions.assertEquals(0, simpleModel.C().compareTo(BigInteger.ONE));

        Assertions.assertTrue(complexModel.C().compareTo(C) < 0);
        Assertions.assertEquals(0, complexModel.C().compareTo(BigInteger.valueOf(2)));

        Assertions.assertEquals(0, veryComplexModel.C().compareTo(C));
        Assertions.assertEquals(0, veryComplexModel.C().compareTo(BigInteger.valueOf(3)));

        // Simplified C
        Assertions.assertTrue(simpleModel.simplifiedC().compareTo(C) < 0);
        Assertions.assertEquals(0, simpleModel.simplifiedC().compareTo(BigInteger.ONE));

        Assertions.assertTrue(complexModel.simplifiedC().compareTo(C) < 0);
        Assertions.assertEquals(0, complexModel.simplifiedC().compareTo(BigInteger.ONE));

        Assertions.assertTrue(veryComplexModel.simplifiedC().compareTo(C) < 0);
        Assertions.assertEquals(0, veryComplexModel.simplifiedC().compareTo(BigInteger.ONE));
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

        // C
        Assertions.assertTrue(repeat.C().compareTo(C) < 0);
        Assertions.assertEquals(0, repeat.C().compareTo(BigInteger.valueOf(2)));

        Assertions.assertTrue(complexRepeat.C().compareTo(C) > 0);
        Assertions.assertEquals(0, complexRepeat.C().compareTo(BigInteger.valueOf(4)));

        // Simplified C
        Assertions.assertTrue(repeat.simplifiedC().compareTo(C) < 0);
        Assertions.assertEquals(0, repeat.simplifiedC().compareTo(BigInteger.ONE));

        Assertions.assertTrue(complexRepeat.simplifiedC().compareTo(C) < 0);
        Assertions.assertEquals(0, complexRepeat.simplifiedC().compareTo(BigInteger.ONE));

        // R
        Assertions.assertTrue(repeat.R().compareTo(R) < 0);
        Assertions.assertEquals(0, repeat.R().compareTo(BigInteger.valueOf(2)));

        Assertions.assertTrue(complexRepeat.R().compareTo(R) < 0);
        Assertions.assertEquals(0, complexRepeat.R().compareTo(BigInteger.valueOf(4)));

        // Simplified R
        Assertions.assertTrue(repeat.simplifiedR().compareTo(R) < 0);
        Assertions.assertEquals(0, repeat.simplifiedR().compareTo(BigInteger.valueOf(1)));

        Assertions.assertTrue(complexRepeat.simplifiedR().compareTo(R) < 0);
        Assertions.assertEquals(0, complexRepeat.simplifiedR().compareTo(BigInteger.valueOf(1)));
    }

    @Test
    void TestSimpleDAGComplexityMeasure(){
        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(0.8));
        BigInteger C =  BigInteger.valueOf(3);
        BigInteger R =  BigInteger.valueOf(10);

        Analytical q = new Analytical("Q", unif0_10);
        Analytical r = new Analytical("R", unif0_10);
        Analytical s = new Analytical("S", unif0_10);
        Analytical u = new Analytical("U", unif0_10);
        Analytical v = new Analytical("V", unif0_10);

        DAG pSimple = DAG.empty("P");
        q.addPrecondition(pSimple.begin());
        r.addPrecondition(pSimple.begin());
        s.addPrecondition(pSimple.begin());
        u.addPrecondition(q, r);
        v.addPrecondition(s, r);
        pSimple.end().addPrecondition(u, v);

        // C
        Assertions.assertEquals(0, pSimple.C().compareTo(C));
        Assertions.assertEquals(0, pSimple.C().compareTo(BigInteger.valueOf(3)));

        // Simplified C
        Assertions.assertEquals(0, pSimple.simplifiedC().compareTo(C));
        Assertions.assertEquals(0, pSimple.simplifiedC().compareTo(BigInteger.valueOf(3)));

        // R
        Assertions.assertTrue(pSimple.R().compareTo(R) < 0);
        Assertions.assertEquals(0, pSimple.R().compareTo(BigInteger.valueOf(8)));

        // Simplified R
        Assertions.assertTrue(pSimple.R().compareTo(R) < 0);
        Assertions.assertEquals(0, pSimple.simplifiedR().compareTo(BigInteger.valueOf(8)));
    }

    @Test
    void TestComplexDAGComplexityMeasure(){
        StochasticTransitionFeature unif0_10 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(1.0));
        BigInteger C =  BigInteger.valueOf(3);
        BigInteger R =  BigInteger.valueOf(10);

        Analytical q = new Analytical("Q", unif0_10);
        Analytical r = new Analytical("R", unif0_10);
        Analytical s = new Analytical("S", unif0_10);
        Analytical v = new Analytical("V", unif0_10);

        DAG tu = DAG.forkJoin("TU",
                DAG.sequence("T",
                        new Analytical("T1", unif0_10),
                        new Analytical("T2", unif0_10)
                ), new Analytical("U", unif0_10)
        );

        DAG wx = DAG.forkJoin("WX",
                DAG.sequence("X",
                        new Analytical("X1", unif0_10),
                        new Analytical("X2", unif0_10)
                ),
                new Analytical("W", unif0_10)
        );

        DAG pComplex = DAG.empty("P");
        q.addPrecondition(pComplex.begin());
        r.addPrecondition(pComplex.begin());
        s.addPrecondition(pComplex.begin());
        tu.addPrecondition(q, r);
        v.addPrecondition(r);
        wx.addPrecondition(s, r);
        pComplex.end().addPrecondition(tu, v, wx);


        Assertions.assertTrue(pComplex.C().compareTo(C) > 0);
        Assertions.assertEquals(0, pComplex.C().compareTo(BigInteger.valueOf(5)));

        // Simplified C
        Assertions.assertEquals(0, pComplex.simplifiedC().compareTo(C));
        Assertions.assertEquals(0, pComplex.simplifiedC().compareTo(BigInteger.valueOf(3)));

        // R
        Assertions.assertTrue(pComplex.R().compareTo(R) > 0);
        Assertions.assertEquals(0, pComplex.R().compareTo(BigInteger.valueOf(14)));

        // Simplified R
        Assertions.assertTrue(pComplex.R().compareTo(R) > 0);
        Assertions.assertEquals(0, pComplex.simplifiedR().compareTo(BigInteger.valueOf(9)));
    }
}

// TODO check also R, SimplifiedR, SimplifiedC
