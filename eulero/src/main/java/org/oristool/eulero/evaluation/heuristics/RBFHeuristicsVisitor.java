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
import org.oristool.eulero.modeling.activitytypes.BadNestedDAGType;

import java.math.BigDecimal;
import java.math.BigInteger;

public class RBFHeuristicsVisitor extends AnalysisHeuristicsVisitor {
    public RBFHeuristicsVisitor(BigInteger CThreshold, BigInteger SThreshold, Approximator approximator, boolean verbose) {
        super("Replace Block First Heuristics", CThreshold, SThreshold, approximator, verbose);
    }

    public RBFHeuristicsVisitor(BigInteger CThreshold, BigInteger SThreshold, Approximator approximator) {
        super("Replace Block First Heuristics", CThreshold, SThreshold, approximator, true);
    }

    @Override
    public double[] analyze(BadNestedDAGType modelType, BigDecimal timeLimit, BigDecimal step) {
        // check complexty
        //model.resetComplexityMeasure();
        BigInteger C = modelType.getActivity().C();
        BigInteger c = modelType.getActivity().simplifiedC();
        BigInteger Q = modelType.getActivity().Q();
        BigInteger q = modelType.getActivity().simplifiedQ();

        // Check Complexity
        if (!(c.compareTo(C) == 0) || !(q.compareTo(Q) == 0)) {
            if (c.compareTo(this.CThreshold()) > 0 || q.compareTo(this.QThreshold()) > 0) {
//                return modelType.innerBlockReplication(timeLimit, step);
                return modelType.innerBlockAnalysis(timeLimit, step, this.CThreshold(), this.QThreshold(), this, approximator());

            }

            if(C.compareTo(this.CThreshold()) > 0 || Q.compareTo(this.QThreshold()) > 0){
                return modelType.innerBlockReplication(timeLimit, step, this.CThreshold(), this.QThreshold(), this);
            }
        } else {
            if (c.compareTo(this.CThreshold()) > 0 || q.compareTo(this.QThreshold()) > 0) {
                //return modelType.innerBlockReplication(timeLimit, step);
                return modelType.innerBlockReplication(timeLimit, step, this.CThreshold(), this.QThreshold(), this);
            }
        }

        // if not complex
        return modelType.forwardTransientAnalysis(timeLimit, step);
    }
}
