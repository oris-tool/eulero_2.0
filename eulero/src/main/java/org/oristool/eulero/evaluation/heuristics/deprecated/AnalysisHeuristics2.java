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

package org.oristool.eulero.evaluation.heuristics.deprecated;

import org.oristool.eulero.evaluation.approximator.Approximator;
import org.oristool.eulero.modeling.deprecated.Activity;
import org.oristool.eulero.modeling.activitytypes.ActivityEnumType;

import java.math.BigDecimal;
import java.math.BigInteger;

public class AnalysisHeuristics2 extends AnalysisHeuristicsStrategy {
    public AnalysisHeuristics2(BigInteger CThreshold, BigInteger SThreshold, Approximator approximator, boolean verbose) {
        super("Heuristic 2", CThreshold, SThreshold, approximator, verbose);
    }

    public AnalysisHeuristics2(BigInteger CThreshold, BigInteger SThreshold, Approximator approximator) {
        super("Heuristic 2", CThreshold, SThreshold, approximator, true);
    }

    @Override
    public double[] analyze(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal forwardReductionFactor, BigDecimal error, String tabSpaceChars) {
        if(model.type().equals(ActivityEnumType.XOR)){
            return numericalXOR(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
        }

        if(model.type().equals(ActivityEnumType.AND)){
            return numericalAND(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
        }

        if(model.type().equals(ActivityEnumType.SEQ)) {
            return numericalSEQ(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
        }

        if(model.type().equals(ActivityEnumType.DAG)) {
            // Check Complexity
            if (!(model.simplifiedC().compareTo(model.C()) == 0) || !(model.simplifiedQ().compareTo(model.Q()) == 0)) {
                if(model.C().compareTo(this.CThreshold()) > 0 || model.Q().compareTo(this.QThreshold()) > 0){
                    if(verbose())
                        System.out.println(tabSpaceChars + " Performing DAG Inner Block Analysis on " + model.name());
                    return innerBlockAnalysis(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
                }

                if (model.simplifiedC().compareTo(this.CThreshold()) > 0 || model.simplifiedQ().compareTo(this.QThreshold()) > 0) {
                    if(verbose())
                        System.out.println(tabSpaceChars + " Performing Block Replication on " + model.name());
                    return innerBlockReplication(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
                }
            } else {
                if (model.simplifiedC().compareTo(this.CThreshold()) > 0 || model.simplifiedQ().compareTo(this.QThreshold()) > 0) {
                    if(verbose())
                        System.out.println(tabSpaceChars + " Performing Block Replication on " + model.name());
                    return innerBlockReplication(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
                }
            }
        }

        return forwardAnalysis(model, timeLimit, step, error, tabSpaceChars);
    }
}
