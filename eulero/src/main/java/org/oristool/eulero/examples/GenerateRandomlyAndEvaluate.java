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

import org.oristool.eulero.evaluation.approximator.Approximator;
import org.oristool.eulero.evaluation.approximator.EXPMixtureApproximation;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristics1;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsStrategy;
import org.oristool.eulero.evaluation.heuristics.EvaluationResult;
import org.oristool.eulero.modelgeneration.RandomGenerator;
import org.oristool.eulero.modelgeneration.blocksettings.ANDBlockSetting;
import org.oristool.eulero.modelgeneration.blocksettings.BlockTypeSetting;
import org.oristool.eulero.modelgeneration.blocksettings.DAGBlockSetting;
import org.oristool.eulero.modelgeneration.blocksettings.SEQBlockSetting;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.eulero.modeling.Activity;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenerateRandomlyAndEvaluate {
    public static void main(String[] args) {
        ArrayList<Set<BlockTypeSetting>> settings = new ArrayList<>();
        // Level 1
        Set<BlockTypeSetting> l1Settings = new HashSet<>();
        BlockTypeSetting AND = new ANDBlockSetting(0.5, 3);
        BlockTypeSetting SEQ = new SEQBlockSetting(0.5, 3);
        l1Settings.add(AND);
        l1Settings.add(SEQ);

        // Level 2
        Set<BlockTypeSetting> l2Settings = new HashSet<>();
        BlockTypeSetting DAG = new DAGBlockSetting(1.,2, 3, 2, 3, 1, 1, 2);
        l2Settings.add(DAG);

        settings.add(l1Settings);
        settings.add(l2Settings);

        StochasticTransitionFeature feature = StochasticTransitionFeature.newUniformInstance("0", "1");
        RandomGenerator randomGenerator = new RandomGenerator(feature, settings);
        Activity model = randomGenerator.generateBlock(settings.size());


        BigInteger tC = BigInteger.valueOf(2);
        BigInteger tQ = BigInteger.valueOf(7);
        BigDecimal timeLimit = model.max();
        BigDecimal step = BigDecimal.valueOf(0.01);
        Approximator approximator = new EXPMixtureApproximation();
        AnalysisHeuristicsStrategy strategy = new AnalysisHeuristics1(tC, tQ, approximator);
        double[] evaluation = strategy.analyze(model, timeLimit.add(BigDecimal.ONE), step);
        EvaluationResult result =  new EvaluationResult("Heuristic 1", evaluation, 0, evaluation.length, model.getFairTimeTick().doubleValue(), 0);

        ActivityViewer.CompareResults("Example", List.of("Heuristic 1"), List.of(result));

        // To Store results...
        String directoryPath = System.getProperty("user.dir") + "/GenerateRandomlyAndEvaluateExample/";
        File thisExampleFolder = new File(directoryPath);
        if(!thisExampleFolder.exists()){
            thisExampleFolder.mkdirs();
        }

        double[] cdf = result.cdf();
        double[] pdf = result.pdf();

        StringBuilder cdfString = new StringBuilder();
        StringBuilder pdfString = new StringBuilder();
        cdfString.append("t,f");
        pdfString.append("t,f");
        for(int j = 0; j < cdf.length; j++){
            BigDecimal x = BigDecimal.valueOf((result.min() + j) * result.step())
                    .setScale(BigDecimal.valueOf(result.step()).scale(), RoundingMode.HALF_DOWN);
            cdfString.append(x.toString()).append(",").append(cdf[j]).append("\n");
            pdfString.append(x.toString()).append(",").append(pdf[j]).append("\n");
        }

        try {
            FileWriter cdfWriter = new FileWriter(directoryPath + "CDF.txt");
            FileWriter pdfWriter = new FileWriter(directoryPath + "CDF.txt");

            cdfWriter.write(cdfString.toString());
            cdfWriter.close();
            pdfWriter.write(pdfString.toString());
            pdfWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
