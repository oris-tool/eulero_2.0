/* This program is called EULERO.
 * Copyright (C) 2022 The EULERO Authors.
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

package org.oristool.eulero.examples;

import org.oristool.eulero.evaluation.approximator.EXPMixtureApproximation;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristics1;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsStrategy;
import org.oristool.eulero.modelgeneration.blocksettings.*;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelSuiteGenerationParameter {
    public static final String directoryPath = System.getProperty("user.dir") + "/model_suite";
    public static final int casePerSetting = 10;
    public static final StochasticTransitionFeature feature = StochasticTransitionFeature.newUniformInstance("0", "1");
    public static final List<AnalysisHeuristicsStrategy> strategy = List.of(
            new AnalysisHeuristics1(
                BigInteger.valueOf(2),
                BigInteger.valueOf(7),
                new EXPMixtureApproximation()
            )
    );

    public static ArrayList<ArrayList<Set<BlockTypeSetting>>> SETTINGS(){
        ArrayList<ArrayList<Set<BlockTypeSetting>>> settings = new ArrayList<>();
        settings.add(settings1());
        settings.add(settings2());
        settings.add(settings3());

        return settings;
    }

    private static ArrayList<Set<BlockTypeSetting>> settings1(){
        ArrayList<Set<BlockTypeSetting>> settings1 = new ArrayList<>();

        Set<BlockTypeSetting> l1Settings = new HashSet<>();
        BlockTypeSetting l1AND = new ANDBlockSetting(0.25, 3);
        l1Settings.add(l1AND);
        BlockTypeSetting l1SEQ = new SEQBlockSetting(0.25, 3);
        l1Settings.add(l1SEQ);
        BlockTypeSetting l1XOR = new XORBlockSetting(0.25, 3);
        l1Settings.add(l1XOR);
        BlockTypeSetting l1DAG = new DAGBlockSetting(0.25, 2, 3, 2, 2, 1, 1, 2);
        l1Settings.add(l1DAG);

        Set<BlockTypeSetting> l2Settings = new HashSet<>();
        BlockTypeSetting l2AND = new ANDBlockSetting(0.25, 3);
        l2Settings.add(l2AND);
        BlockTypeSetting l2SEQ = new SEQBlockSetting(0.25, 3);
        l2Settings.add(l2SEQ);
        BlockTypeSetting l2XOR = new XORBlockSetting(0.25, 3);
        l2Settings.add(l2XOR);
        BlockTypeSetting l2DAG = new DAGBlockSetting(0.25, 2, 3, 2, 2, 1, 1, 2);
        l2Settings.add(l2DAG);

        settings1.add(l2Settings);
        settings1.add(l2Settings);

        return settings1;
    }

    private static ArrayList<Set<BlockTypeSetting>> settings2(){
        ArrayList<Set<BlockTypeSetting>> settings2 = settings1();

        Set<BlockTypeSetting> l3Settings = new HashSet<>();
        BlockTypeSetting l3AND = new ANDBlockSetting(0.25, 3);
        l3Settings.add(l3AND);
        BlockTypeSetting l3SEQ = new SEQBlockSetting(0.25, 3);
        l3Settings.add(l3SEQ);
        BlockTypeSetting l3XOR = new XORBlockSetting(0.25, 3);
        l3Settings.add(l3XOR);
        BlockTypeSetting l3DAG = new DAGBlockSetting(0.25, 2, 3, 2, 2, 1, 1, 2);
        l3Settings.add(l3DAG);
        settings2.add(l3Settings);

        return settings2;
    }

    private static ArrayList<Set<BlockTypeSetting>> settings3(){
        ArrayList<Set<BlockTypeSetting>> settings3 = settings2();

        Set<BlockTypeSetting> l4Settings = new HashSet<>();
        BlockTypeSetting l4AND = new ANDBlockSetting(0.25, 3);
        l4Settings.add(l4AND);
        BlockTypeSetting l4SEQ = new SEQBlockSetting(0.25, 3);
        l4Settings.add(l4SEQ);
        BlockTypeSetting l4XOR = new XORBlockSetting(0.25, 3);
        l4Settings.add(l4XOR);
        BlockTypeSetting l4DAG = new DAGBlockSetting(0.25, 2, 3, 2, 2, 1, 1, 2);
        l4Settings.add(l4DAG);
        settings3.add(l4Settings);

        return settings3;
    }
}
