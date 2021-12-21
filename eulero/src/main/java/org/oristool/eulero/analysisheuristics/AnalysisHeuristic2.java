package org.oristool.eulero.analysisheuristics;

import org.oristool.eulero.graph.*;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;

public class AnalysisHeuristic2 extends AnalysisHeuristicStrategy{
    public AnalysisHeuristic2(BigInteger CThreshold, BigInteger RThreshold, Approximator approximator) {
        super("Heuristic 2", CThreshold, RThreshold, approximator);
    }

    @Override
    public double[] analyze(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error, String tabSpaceChars, boolean verbose) {
        if(model instanceof Xor){
            return numericalXOR(model, timeLimit, step, error, tabSpaceChars);
        }

        if(model instanceof AND){
            return numericalAND(model, timeLimit, step, error, tabSpaceChars);
        }

        if(model instanceof SEQ) {
            return numericalSEQ(model, timeLimit, step, error, tabSpaceChars);
        }

        if(model instanceof Repeat) {
            if (!(model.simplifiedC().compareTo(model.C()) == 0) && !(model.simplifiedR().compareTo(model.R()) == 0)) {
                if(model.C().compareTo(this.CThreshold()) > 0 || model.R().compareTo(this.RThreshold()) > 0) {
                    System.out.println(tabSpaceChars + " Performing REP Inner Block Analysis on " + model.name());
                    return REPInnerBlockAnalysis(model, timeLimit, step, error, tabSpaceChars);
                }
                return regenerativeTransientAnalysis(model, timeLimit, step, BigDecimal.valueOf(1), error, tabSpaceChars, verbose);
            }
        }

        if(model instanceof DAG) {
            // Check for Cycles and analyze them --> altrimenti complessità sarà sempre infinito
            System.out.println(tabSpaceChars + " Searching Repetitions in DAG " + model.name());
            checkREPinDAG(model, timeLimit, step, error, "---" + tabSpaceChars, verbose);

            // Check Complexity
            if (!(model.simplifiedC().compareTo(model.C()) == 0) || !(model.simplifiedR().compareTo(model.R()) == 0)) {
                if(model.C().compareTo(this.CThreshold()) > 0 || model.R().compareTo(this.RThreshold()) > 0){
                    System.out.println(tabSpaceChars + " Performing DAG Inner Block Analysis on " + model.name());
                    return DAGInnerBlockAnalysis(model, timeLimit, step, error, tabSpaceChars, verbose);
                }

                if (model.simplifiedC().compareTo(this.CThreshold()) >= 0 || model.simplifiedR().compareTo(this.RThreshold()) >= 0) {
                    System.out.println(tabSpaceChars + " Performing Block Replication on " + model.name());
                    return InnerBlockReplicationAnalysis(model, timeLimit, step, error, tabSpaceChars, verbose);
                }
            } else {
                if (model.simplifiedC().compareTo(this.CThreshold()) >= 0 || model.simplifiedR().compareTo(this.RThreshold()) >= 0) {
                    System.out.println(tabSpaceChars + " Performing Block Replication on " + model.name());
                    return InnerBlockReplicationAnalysis(model, timeLimit, step, error, tabSpaceChars, verbose);
                }
            }
        }

        return forwardAnalysis(model, timeLimit, step, error, tabSpaceChars, verbose);
    }
}
