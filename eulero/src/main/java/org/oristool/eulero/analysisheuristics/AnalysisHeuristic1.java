package org.oristool.eulero.analysisheuristics;

import org.oristool.eulero.graph.*;
import org.oristool.eulero.math.approximation.Approximator;

import java.math.BigDecimal;
import java.math.BigInteger;

public class AnalysisHeuristic1 extends AnalysisHeuristicStrategy{
    public AnalysisHeuristic1(BigInteger CThreshold, BigInteger RThreshold, Approximator approximator) {
        super("Heuristic 1", CThreshold, RThreshold, approximator);
    }

    @Override
    public double[] analyze(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error, String tabSpaceChars) {

        if(model instanceof Analytical){
            // Nota che volendo se è annalytical può andare in fondo fino a regenerativeTransientAnalysis, senza che si necessario scrivere questa..
            return ((Analytical) model).getNumericalCDF(timeLimit, step);
        }

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
            }
        }

        if(model instanceof DAG) {
            // Check for Cycles and analyze them --> altrimenti complessità sarà sempre infinito
            System.out.println(tabSpaceChars + " Searching Repetitions in DAG " + model.name());
            checkREPinDAG(model, timeLimit, step, error, "---" + tabSpaceChars);

            // Check Complexity
            if (!(model.simplifiedC().compareTo(model.C()) == 0) || !(model.simplifiedR().compareTo(model.R()) == 0)) {
                if (model.simplifiedC().compareTo(this.CThreshold()) > 0 || model.simplifiedR().compareTo(this.RThreshold()) > 0) {
                    System.out.println(tabSpaceChars + " Performing Block Replication on " + model.name());
                    return InnerBlockReplicationAnalysis(model, timeLimit, step, error, tabSpaceChars);
                }

                if(model.C().compareTo(this.CThreshold()) > 0 || model.R().compareTo(this.RThreshold()) > 0){
                    System.out.println(tabSpaceChars + " Performing DAG Inner Block Analysis on " + model.name());
                    return DAGInnerBlockAnalysis(model, timeLimit, step, error, tabSpaceChars);
                }
            }

            // TODO serve un'azione per quando c'è qualcosa che è già in versione semplificata, ma ancora complessa.
            // (Che poi sono le azioni di sopra, ma cambiano le guardie degli IF THEN)
        }

        return regenerativeTransientAnalysis(model, timeLimit, step, error, tabSpaceChars);
    }
}
