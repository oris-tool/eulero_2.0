package org.oristool.eulero.analysisheuristics;

import org.oristool.eulero.graph.*;
import org.oristool.eulero.math.approximation.Approximator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class AnalysisHeuristic3 extends AnalysisHeuristicStrategy{
    public AnalysisHeuristic3(BigInteger CThreshold, BigInteger RThreshold, Approximator approximator, boolean verbose) {
        super("Heuristic 3", CThreshold, RThreshold, approximator, verbose);
    }

    @Override
    public double[] analyze(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal forwardReductionFactor, BigDecimal error, String tabSpaceChars) {
        if(model instanceof Repeat) {
            if (!(model.simplifiedC().compareTo(model.C()) == 0) && !(model.simplifiedR().compareTo(model.R()) == 0)) {
                if(model.C().compareTo(this.CThreshold()) > 0 || model.R().compareTo(this.RThreshold()) > 0) {
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
            //checkREPinDAG(model, timeLimit, step, forwardReductionFactor, error, "---" + tabSpaceChars);

            // Check Complexity
            if (!(model.simplifiedC().compareTo(model.C()) == 0) || !(model.simplifiedR().compareTo(model.R()) == 0)) {
                if (model.simplifiedC().compareTo(this.CThreshold()) >= 0 || model.simplifiedR().compareTo(this.RThreshold()) >= 0) {
                    if(verbose())
                        System.out.println(tabSpaceChars + " Performing Block Replication on " + model.name());
                    //return InnerBlockReplicationAnalysis(model, timeLimit, step, error, tabSpaceChars);
                    return InnerBlockReplicationAnalysisAndApproximation(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
                }

                if(model.C().compareTo(this.CThreshold()) >= 0 || model.R().compareTo(this.RThreshold()) >= 0){
                    if(verbose())
                        System.out.println(tabSpaceChars + " Performing DAG Inner Block Analysis on " + model.name());
                    return DAGInnerBlockAnalysis(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
                }
            } else {
                if (model.simplifiedC().compareTo(this.CThreshold()) >= 0 || model.simplifiedR().compareTo(this.RThreshold()) >= 0) {
                    if(verbose())
                        System.out.println(tabSpaceChars + " Performing Block Replication on " + model.name());
                    return InnerBlockReplicationAnalysis(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
                }
            }
        }

        return forwardAnalysis(model, timeLimit, step, error, tabSpaceChars);
    }
}
