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

import com.google.common.collect.Lists;
import org.oristool.eulero.evaluation.approximator.Approximator;
import org.oristool.eulero.evaluation.approximator.EXPMixtureApproximation;
import org.oristool.eulero.evaluation.heuristics.SDFHeuristicsVisitor;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.evaluation.heuristics.EvaluationResult;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.DAG;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.XOR;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
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
                new XOR("R1",
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
        top.setMin(top.getMinBound(top.end()));
        top.setMax(top.getMaxBound(top.end()));
        top.setActivities(Lists.newArrayList(Q, R, S, T));

        BigInteger tC = BigInteger.valueOf(3);
        BigInteger tQ = BigInteger.valueOf(7);
        BigDecimal timeLimit = top.max();
        BigDecimal step = BigDecimal.valueOf(0.01);
        Approximator approximator = new EXPMixtureApproximation();
        AnalysisHeuristicsVisitor strategy = new SDFHeuristicsVisitor(tC, tQ, approximator);
        double[] evaluation = strategy.analyze(top, timeLimit.add(BigDecimal.ONE), step);
        EvaluationResult result = new EvaluationResult("Heuristic 1", evaluation, 0, evaluation.length, top.getFairTimeTick().doubleValue(), 0);

        // To Store results...
        String directoryPath = System.getProperty("user.dir") + "/BuildAndEvaluateExample/";
        File thisExampleFolder = new File(directoryPath);
        if(!thisExampleFolder.exists()){
            thisExampleFolder.mkdirs();
        }

        double[] cdf = result.cdf();
        double[] pdf = result.pdf();

        StringBuilder cdfString = new StringBuilder();
        StringBuilder pdfString = new StringBuilder();
        cdfString.append("t,f\n");
        pdfString.append("t,f\n");
        for(int j = 0; j < cdf.length; j++){
            BigDecimal x = BigDecimal.valueOf((result.min() + j) * result.step())
                    .setScale(BigDecimal.valueOf(result.step()).scale(), RoundingMode.HALF_DOWN);
            cdfString.append(x.toString()).append(",").append(cdf[j]).append("\n");
            pdfString.append(x.toString()).append(",").append(pdf[j]).append("\n");
        }

        try {
            FileWriter cdfWriter = new FileWriter(directoryPath + "CDF.txt");
            FileWriter pdfWriter = new FileWriter(directoryPath + "PDF.txt");

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
