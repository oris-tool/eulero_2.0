package org.oristool.eulero.analysisheuristics;

import org.checkerframework.checker.units.qual.A;
import org.oristool.eulero.graph.*;
import org.oristool.eulero.math.approximation.Approximator;

import java.math.BigDecimal;
import java.math.BigInteger;

public class AnalysisHeuristic1 extends AnalysisHeuristicStrategy{
    public AnalysisHeuristic1(BigInteger CThreshold, BigInteger RThreshold, Approximator approximator) {
        super(CThreshold, RThreshold, approximator);
    }

    @Override
    public double[] analyze(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error) {
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
            return numericalXOR(model, timeLimit, step, error);
        }

        if(model instanceof AND){
            return numericalAND(model, timeLimit, step, error);
        }

        if(model instanceof SEQ) {
            return numericalSEQ(model, timeLimit, step, error);
        }

        if(model instanceof Repeat) {
            if(model.C().compareTo(this.CThreshold()) > 0 || model.R().compareTo(this.RThreshold()) > 0) {
                System.out.println("Performing Inner Block Analysis on " + model.name());
                REPInnerBlockAnalysis(model, timeLimit, step, error);
            }
        }

        if(model instanceof DAG){
            if(model.simplifiedC().compareTo(this.CThreshold()) > 0 && model.simplifiedR().compareTo(this.RThreshold()) > 0){
                // Cosa succede se replicarlo non lo cambia? Farei delle operazioni a vuoto?
                System.out.println("Performing Block Replication on " + model.name());
                InnerBlockReplicationAnalysis(model, timeLimit, step, error);
                //DAGINNERBLOCKANALYSIS???
            }

            if(model.C().compareTo(this.CThreshold()) > 0){
                System.out.println("Performing Inner Block Analysis on " + model.name());
                DAGInnerBlockAnalysis(model, timeLimit, step, error);
            }
        }

        return regenerativeTransientAnalysis(model, timeLimit, step, error);
    }
}
