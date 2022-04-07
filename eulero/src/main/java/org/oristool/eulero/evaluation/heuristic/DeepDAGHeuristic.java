package org.oristool.eulero.evaluation.heuristic;

import org.oristool.eulero.evaluation.approximator.Approximator;
import org.oristool.eulero.workflow.*;

import java.math.BigDecimal;
import java.math.BigInteger;

public class DeepDAGHeuristic extends AnalysisHeuristicStrategy {
    public DeepDAGHeuristic(BigInteger CThreshold, BigInteger SThreshold, Approximator approximator, boolean verbose) {
        super("Deep DAG", CThreshold, SThreshold, approximator, verbose);
    }

    @Override
    public double[] analyze(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal forwardReductionFactor, BigDecimal error, String tabSpaceChars) {
        if(model instanceof Xor){
            return numericalXOR(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
        }

        if(model instanceof AND){
            return numericalAND(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
        }

        if(model instanceof SEQ) {
            return numericalSEQ(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
        }

        if(model instanceof Repeat) {
            if (!(model.simplifiedC().compareTo(model.C()) == 0) && !(model.simplifiedQ().compareTo(model.Q()) == 0)) {
                if(model.C().compareTo(this.CThreshold()) > 0 || model.Q().compareTo(this.QThreshold()) > 0) {
                    if(verbose())
                        System.out.println(tabSpaceChars + " Performing REP Inner Block Analysis on " + model.name());
                    return REPInnerBlockAnalysis(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
                }
                return regenerativeTransientAnalysis(model, timeLimit, step, BigDecimal.valueOf(10), error, tabSpaceChars);
            }
        }

        if(model instanceof DAG) {
            // Check for Cycles and analyze them --> altrimenti complessità sarà sempre infinito
            if(verbose())
                System.out.println(tabSpaceChars + " Searching Repetitions in DAG " + model.name());
            checkREPinDAG(model, timeLimit, step, forwardReductionFactor, error, "---" + tabSpaceChars);

            // Check Complexity
            if (!(model.simplifiedC().compareTo(model.C()) == 0) || !(model.simplifiedQ().compareTo(model.Q()) == 0)) {
                if(model.C().compareTo(this.CThreshold()) > 0 || model.Q().compareTo(this.QThreshold()) > 0){
                    if(verbose())
                        System.out.println(tabSpaceChars + " Performing DAG Inner Block Analysis on " + model.name());
                    return DAGInnerBlockAnalysis(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
                }

                if (model.simplifiedC().compareTo(this.CThreshold()) >= 0 || model.simplifiedQ().compareTo(this.QThreshold()) >= 0) {
                    if(verbose())
                        System.out.println(tabSpaceChars + " Performing Block Replication on " + model.name());
                    return InnerBlockReplicationAnalysis(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
                }
            } else {
                if (model.simplifiedC().compareTo(this.CThreshold()) >= 0 || model.simplifiedQ().compareTo(this.QThreshold()) >= 0) {
                    if(verbose())
                        System.out.println(tabSpaceChars + " Performing Block Replication on " + model.name());
                    return InnerBlockReplicationAnalysis(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
                }
            }
        }

        return forwardAnalysis(model, timeLimit, step, error, tabSpaceChars);
    }
}
