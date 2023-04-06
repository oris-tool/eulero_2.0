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

import org.apache.commons.lang3.tuple.Pair;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.evaluation.heuristics.EvaluationResult;
import org.oristool.eulero.modelgeneration.deprecated.RandomGenerator;
import org.oristool.eulero.modeling.Activity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GenerateModelSuiteAndEvaluate {
    public static void main(String[] args) throws Exception {
        String suiteDirectoryName;
        if(args.length > 1){
            throw new Exception("Too many arguments passed.");
        }
        if(args.length == 1){
            // this overwrites the folder
            suiteDirectoryName = args[0];
        } else {
            suiteDirectoryName = ModelSuiteGenerationParameter.directoryPath;
        }


        for(ArrayList settings: ModelSuiteGenerationParameter.SETTINGS()){
            for(int i = 0; i < ModelSuiteGenerationParameter.casePerSetting; i++){
                String directory = suiteDirectoryName + "/" + "depth_" + settings.size();

                RandomGenerator randomGenerator = new RandomGenerator(Set.of(Pair.of(List.of(ModelSuiteGenerationParameter.feature), List.of(BigDecimal.ONE))), settings);
                Activity model = randomGenerator.generateBlock(settings.size());

                ExampleHelper.jaxbObjectToXML(model, directory + "/model_" + String.format("%02d" , i));
                AnalysisHeuristicsVisitor strategy = ModelSuiteGenerationParameter.strategy.get(0);
                double[] cdf = strategy.analyze(model, model.max().add(BigDecimal.ONE), model.getFairTimeTick());

                EvaluationResult result = new EvaluationResult(
                        "Test",
                        cdf,
                        0,
                        cdf.length,
                        model.getFairTimeTick().doubleValue(),
                        0
                );

                ExampleHelper.storeResults(result, directory + "/model_" + String.format("%02d" , i));
            }
        }

    }
}
