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

package org.oristool.eulero.evaluation.heuristics;

import org.oristool.eulero.evaluation.approximator.Approximator;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.activitytypes.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.*;

public abstract class AnalysisHeuristicsVisitor {
    private final String heuristicName;
    private final BigInteger CThreshold;
    private final BigInteger QThreshold;
    private final Approximator approximator;
    private final boolean plotIntermediate;
    private final boolean verbose;

    public AnalysisHeuristicsVisitor(String heuristicName, BigInteger CThreshold, BigInteger QThreshold, Approximator approximator, boolean verbose, boolean plotIntermediate){
        this.heuristicName = heuristicName;
        this.CThreshold = CThreshold;
        this.QThreshold = QThreshold;
        this.approximator = approximator;
        this.plotIntermediate = plotIntermediate;
        this.verbose = verbose;
    }

    public AnalysisHeuristicsVisitor(String heuristicName, BigInteger CThreshold, BigInteger QThreshold, Approximator approximator){
        this(heuristicName, CThreshold, QThreshold, approximator, false, false);
    }

    public AnalysisHeuristicsVisitor(String heuristicName, BigInteger CThreshold, BigInteger QThreshold, Approximator approximator, boolean verbose){
        this(heuristicName, CThreshold, QThreshold, approximator, verbose, false);
    }

    public abstract double[] analyze(BadNestedDAGType modelType, BigDecimal timeLimit, BigDecimal step);

    public double[] analyze(XORType modelType, BigDecimal timeLimit, BigDecimal timeTick) {
        double[] CDF = new double[timeLimit.divide(timeTick, MathContext.DECIMAL64).intValue() + 1];

        for(Activity act: modelType.getChildren()){
            double[] activityCDF = act.analyze(timeLimit, timeTick, this);

            for(int t = 0; t < CDF.length; t++){
                CDF[t] += modelType.probs().get(modelType.getChildren().indexOf(act)) * activityCDF[t];
            }
        }
        return CDF;
    }

    public double[] analyze(ANDType modelType, BigDecimal timeLimit, BigDecimal step) {
        double[] CDF = new double[timeLimit.divide(step, MathContext.DECIMAL64).intValue() + 1];

        long time = System.nanoTime();

        Arrays.fill(CDF, 1.0);
        for(Activity act: modelType.getChildren()){
            double[] activityCDF = act.analyze(timeLimit, step, this);
            for(int t = 0; t < CDF.length; t++){
                CDF[t] *= activityCDF[t];
            }
        }

        return CDF;
    }

    public double[] analyze(ORType modelType, BigDecimal timeLimit, BigDecimal step) {
        double[] CDF = new double[timeLimit.divide(step, MathContext.DECIMAL64).intValue() + 1];

        return CDF;
    }

    public double[] analyze(SEQType modelType, BigDecimal timeLimit, BigDecimal step) {
        double[] CDF = new double[timeLimit.divide(step, MathContext.DECIMAL64).intValue() + 1];

        long time = System.nanoTime();

        for (Activity act : modelType.getChildren()) {
            if (act.equals(modelType.getChildren().get(0))) {
                CDF = act.analyze(timeLimit, step, this);
            } else {
                double[] convolution = new double[CDF.length];
                double[] activityCDF = act.analyze(timeLimit, step, this);

                for (int x = 1; x < CDF.length; x++) {
                    for (int u = 1; u <= x; u++)
                        convolution[x] += (CDF[u] - CDF[u - 1]) * (activityCDF[x - u + 1] + activityCDF[x - u]) * 0.5;
                }

                CDF = convolution;
            }
        }
        return CDF;
    }

    public boolean verbose() { return verbose; }
    public BigInteger CThreshold() {
        return CThreshold;
    }
    public BigInteger QThreshold() {
        return QThreshold;
    }
    public Approximator approximator() {
        return approximator;
    }
    public String heuristicName() {
        return heuristicName;
    }
}
