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
        // Forse si può rimuovere il well-nested: si mette instance of Analytica; solo analytical avrà getNumerical...
        // e quindi in pratica, i calcoli numerici vengono gestiti dall'analyzer, e Activity serve solo a rappresentare qualcosa, come dovrebbe essere...
        // Però forse non è detto... lo vediamo quando facciamo gli altri...

        /*if(model.isWellNested()){
            return model.getNumericalCDF(timeLimit, step);
        }*/

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
            System.out.println("TODO on " + model.name());
            if(model.C().compareTo(this.CThreshold()) > 0 || model.R().compareTo(this.RThreshold()) > 0) {
                System.out.println("Performing Inner Block Analysis on " + model.name());
                REPInnerBlockAnalysis(model, timeLimit, step, error, tabSpaceChars);
            }
        }

        if(model instanceof DAG) {
            if (!(model.simplifiedC().compareTo(model.C()) == 0) && !(model.simplifiedR().compareTo(model.R()) == 0)) {
                if (model.simplifiedC().compareTo(this.CThreshold()) > 0 || model.simplifiedR().compareTo(this.RThreshold()) > 0) {
                    System.out.println(tabSpaceChars + " Performing Block Replication on " + model.name());
                    return InnerBlockReplicationAnalysis(model, timeLimit, step, error, tabSpaceChars);
                }

                if(model.C().compareTo(this.CThreshold()) > 0 || model.R().compareTo(this.RThreshold()) > 0){
                    System.out.println(tabSpaceChars + " Performing Inner Block Analysis on " + model.name());
                    return DAGInnerBlockAnalysis(model, timeLimit, step, error, tabSpaceChars);
                }
            }
        }

        return regenerativeTransientAnalysis(model, timeLimit, step, error, tabSpaceChars);
    }
}
