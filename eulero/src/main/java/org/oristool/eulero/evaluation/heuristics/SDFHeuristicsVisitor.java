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
import org.oristool.eulero.evaluation.approximator.TruncatedExponentialMixtureApproximation;
import org.oristool.eulero.modeling.activitytypes.BadNestedDAGType;

import java.math.BigDecimal;
import java.math.BigInteger;

public class SDFHeuristicsVisitor extends AnalysisHeuristicsVisitor {
    public SDFHeuristicsVisitor(BigInteger CThreshold, BigInteger SThreshold, Approximator approximator, boolean verbose) {
        super("Split Dependencies First Heuristics", CThreshold, SThreshold, approximator, verbose);
    }

    public SDFHeuristicsVisitor(BigInteger CThreshold, BigInteger SThreshold, Approximator approximator) {
        super("Split Dependencies First Heuristics", CThreshold, SThreshold, approximator, true);
    }

    @Override
    public double[] analyze(BadNestedDAGType modelType, BigDecimal timeLimit, BigDecimal step) {
        // check complexty
        //model.resetComplexityMeasure();
        long time = System.nanoTime();
        BigInteger C = modelType.getActivity().C();
        BigInteger c = modelType.getActivity().simplifiedC();
        BigInteger Q = modelType.getActivity().Q();
        BigInteger q = modelType.getActivity().simplifiedQ();

        // Check Complexity
        if (!(c.compareTo(C) == 0) || !(q.compareTo(Q) == 0)) {
            if (c.compareTo(this.CThreshold()) > 0 || q.compareTo(this.QThreshold()) > 0) {
//                return modelType.innerBlockReplication(timeLimit, step);
                //System.out.println("Replico!");
                return modelType.innerBlockReplication(timeLimit, step, this.CThreshold(), this.QThreshold(), this);

            }

            if(C.compareTo(this.CThreshold()) > 0 || Q.compareTo(this.QThreshold()) > 0){
                //System.out.println("Replico!");
                return modelType.innerBlockReplication(timeLimit, step, this.CThreshold(), this.QThreshold(), this);
            }
        } else {
            if (c.compareTo(this.CThreshold()) > 0 || q.compareTo(this.QThreshold()) > 0) {
                //return modelType.innerBlockReplication(timeLimit, step);
                //System.out.println("Replico!");
                return modelType.innerBlockReplication(timeLimit, step, this.CThreshold(), this.QThreshold(), this);
            }
        }

        // if not complex
        return modelType.forwardTransientAnalysis(timeLimit, step);
    }
}
