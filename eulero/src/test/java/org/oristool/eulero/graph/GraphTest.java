/* This program is part of the ORIS Tool.
 * Copyright (C) 2011-2020 The ORIS Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.oristool.eulero.graph;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

class GraphTest {

    @Test
    void testFlatten() {
        Analytical a = Analytical.uniform("A", BigDecimal.ZERO, BigDecimal.ONE);
        Numerical b = Numerical.uniform("B", BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("0.1"));
        Numerical c = Numerical.uniform("C", BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("0.1"));
        
        DAG forkJoin = DAG.forkJoin("FJ", b, c); 
        DAG seq = DAG.sequence("SEQ", a, forkJoin);

        assertEquals(seq.yamlRecursive(),
                "SEQ:\n" +
                "  type: DAG\n" +
                "  data:\n" +
                "    SEQ_BEGIN: [A]\n" +
                "    A: [FJ]\n" +
                "    FJ: [SEQ_END]\n" +
                "  nested:\n" +
                "    A:\n" +
                "      type: Analytical\n" +
                "      data:\n" +
                "        pdf: [0, 1] 1\n" +
                "    FJ:\n" +
                "      type: DAG\n" +
                "      data:\n" +
                "        FJ_BEGIN: [B, C]\n" +
                "        B: [FJ_END]\n" +
                "        C: [FJ_END]\n" +
                "      nested:\n" +
                "        B:\n" +
                "          type: Numerical\n" +
                "          data:\n" +
                "            support: [0.0, 1.0]\n" +
                "            pdf: 9 samples with step 0.1\n" +
                "        C:\n" +
                "          type: Numerical\n" +
                "          data:\n" +
                "            support: [0.0, 1.0]\n" +
                "            pdf: 9 samples with step 0.1\n");
        
        seq.flatten();
        
        assertEquals(seq.yamlRecursive(),
                "SEQ:\n" +
                "  type: DAG\n" +
                "  data:\n" +
                "    SEQ_BEGIN: [A]\n" +
                "    A: [B, C]\n" +
                "    B: [SEQ_END]\n" +
                "    C: [SEQ_END]\n" +
                "  nested:\n" +
                "    A:\n" +
                "      type: Analytical\n" +
                "      data:\n" +
                "        pdf: [0, 1] 1\n" +
                "    B:\n" +
                "      type: Numerical\n" +
                "      data:\n" +
                "        support: [0.0, 1.0]\n" +
                "        pdf: 9 samples with step 0.1\n" +
                "    C:\n" +
                "      type: Numerical\n" +
                "      data:\n" +
                "        support: [0.0, 1.0]\n" +
                "        pdf: 9 samples with step 0.1\n");
        
//        Numerical d = Numerical.uniform("D", BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("0.1"));
//        Repeat rep = new Repeat("REP", 0.3, seq);
//        Xor xor = new Xor("XOR", List.of(d, rep), List.of(0.1, 0.9));
//        System.out.println(xor.yamlRecursive());
//        System.out.println(seq.end().preNestedLayers(true));
//        System.out.println(seq.nested());
//
//        System.out.println(seq);
//        seq.flatten();
//        System.out.println(seq);
    }

    @Test
    void testFlattenTwice() {
        Numerical a = Numerical.uniform("A", BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("0.1"));
        Numerical b = Numerical.uniform("B", BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("0.1"));
        Numerical c = Numerical.uniform("C", BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("0.1"));
        
        DAG join = DAG.forkJoin("FJ", b, c); 
        DAG seq1 = DAG.sequence("SEQ1", a);
        DAG seq2 = DAG.sequence("SEQ2", join);
        DAG seq = DAG.sequence("SEQ", seq1, seq2);
        
        assertEquals(seq.yamlRecursive(),
                "SEQ:\n" +
                "  type: DAG\n" +
                "  data:\n" +
                "    SEQ_BEGIN: [SEQ1]\n" +
                "    SEQ1: [SEQ2]\n" +
                "    SEQ2: [SEQ_END]\n" +
                "  nested:\n" +
                "    SEQ1:\n" +
                "      type: DAG\n" +
                "      data:\n" +
                "        SEQ1_BEGIN: [A]\n" +
                "        A: [SEQ1_END]\n" +
                "      nested:\n" +
                "        A:\n" +
                "          type: Numerical\n" +
                "          data:\n" +
                "            support: [0.0, 1.0]\n" +
                "            pdf: 9 samples with step 0.1\n" +
                "    SEQ2:\n" +
                "      type: DAG\n" +
                "      data:\n" +
                "        SEQ2_BEGIN: [FJ]\n" +
                "        FJ: [SEQ2_END]\n" +
                "      nested:\n" +
                "        FJ:\n" +
                "          type: DAG\n" +
                "          data:\n" +
                "            FJ_BEGIN: [B, C]\n" +
                "            B: [FJ_END]\n" +
                "            C: [FJ_END]\n" +
                "          nested:\n" +
                "            B:\n" +
                "              type: Numerical\n" +
                "              data:\n" +
                "                support: [0.0, 1.0]\n" +
                "                pdf: 9 samples with step 0.1\n" +
                "            C:\n" +
                "              type: Numerical\n" +
                "              data:\n" +
                "                support: [0.0, 1.0]\n" +
                "                pdf: 9 samples with step 0.1\n");
        seq.flatten();
        assertEquals(seq.yamlRecursive(),
                "SEQ:\n" +
                "  type: DAG\n" +
                "  data:\n" +
                "    SEQ_BEGIN: [A]\n" +
                "    A: [B, C]\n" +
                "    B: [SEQ_END]\n" +
                "    C: [SEQ_END]\n" +
                "  nested:\n" +
                "    A:\n" +
                "      type: Numerical\n" +
                "      data:\n" +
                "        support: [0.0, 1.0]\n" +
                "        pdf: 9 samples with step 0.1\n" +
                "    B:\n" +
                "      type: Numerical\n" +
                "      data:\n" +
                "        support: [0.0, 1.0]\n" +
                "        pdf: 9 samples with step 0.1\n" +
                "    C:\n" +
                "      type: Numerical\n" +
                "      data:\n" +
                "        support: [0.0, 1.0]\n" +
                "        pdf: 9 samples with step 0.1\n");
    }
    
    @Test
    void testRepeated() {
        Numerical a = Numerical.uniform("A", BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("0.1"));
        Numerical ap = Numerical.uniform("A", BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("0.1"));
        
        DAG repeated = DAG.sequence("REP", a, ap);
        List<String> problems = repeated.problems();
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).toLowerCase().contains("repeated"));
    }

    @Test
    void testRepeatedNested() {
        Numerical a = Numerical.uniform("A", BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("0.1"));
        Numerical ap = Numerical.uniform("A", BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("0.1"));
        
        DAG repeated = DAG.sequence("REP", a, DAG.sequence("SEQ", ap));
        List<String> problems = repeated.problems();
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).toLowerCase().contains("repeated"));
    }

    @Test
    void testCycle() {
        Numerical a = Numerical.uniform("A", BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("0.1"));
        Numerical b = Numerical.uniform("B", BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("0.1"));
        
        DAG cycle = DAG.sequence("SEQ", a, b);
        a.addPrecondition(b);
        
        List<String> problems = cycle.problems();
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).toLowerCase().contains("cycle"));
    }

    @Test
    void testCycleBegin() {
        Numerical a = Numerical.uniform("A", BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("0.1"));
        
        DAG cycle = DAG.sequence("SEQ", a);
        cycle.begin().addPrecondition(a);
        
        List<String> problems = cycle.problems();
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).toLowerCase().contains("cycle"));
    }

    void testCycleEnd() {
        Numerical a = Numerical.uniform("A", BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("0.1"));
        
        DAG cycle = DAG.sequence("SEQ", a);
        a.addPrecondition(cycle.end());
        
        List<String> problems = cycle.problems();
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).toLowerCase().contains("cycle"));
    }

    @Test
    void testSequenceSupport(){
        DAG sequence = DAG.sequence("Test",
                new Analytical("T1", StochasticTransitionFeature.newUniformInstance("0", "3")),
                new Analytical("T1", StochasticTransitionFeature.newUniformInstance("2", "4.2"))
        );

        assertEquals(sequence.low().compareTo(BigDecimal.valueOf(2)), 0);
        assertEquals(sequence.upp().compareTo(BigDecimal.valueOf(7.2)), 0);
    }

    @Test
    void testAndSupport(){
        DAG and = DAG.forkJoin("Test",
                new Analytical("T1", StochasticTransitionFeature.newUniformInstance("0", "3")),
                new Analytical("T1", StochasticTransitionFeature.newUniformInstance("2", "4.2"))
        );

        assertEquals(and.low().compareTo(BigDecimal.valueOf(2)), 0);
        assertEquals(and.upp().compareTo(BigDecimal.valueOf(4.2)), 0);

    }

    @Test
    void testXorSupport(){
        Xor xor = new Xor("Test", List.of(
                new Analytical("T1", StochasticTransitionFeature.newUniformInstance("0", "3")),
                new Analytical("T1", StochasticTransitionFeature.newUniformInstance("2", "4.2"))
            ), List.of(0.3, 0.7)
        );

        assertEquals(xor.low().compareTo(BigDecimal.valueOf(0)), 0);
        assertEquals(xor.upp().compareTo(BigDecimal.valueOf(4.2)), 0);

    }
}
