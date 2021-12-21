package org.oristool.eulero.analysisheuristics;

import org.oristool.eulero.graph.*;
import org.oristool.eulero.mains.TestCaseResult;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class AnalysisHeuristic1 extends AnalysisHeuristicStrategy{
    public AnalysisHeuristic1(BigInteger CThreshold, BigInteger RThreshold, Approximator approximator) {
        super("Heuristic 1", CThreshold, RThreshold, approximator);
    }

    @Override
    public double[] analyze(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error, String tabSpaceChars) {

        /*if(model instanceof Analytical){
            // Nota che volendo se è annalytical può andare in fondo fino a regenerativeTransientAnalysis, senza che si necessario scrivere questa..
            return ((Analytical) model).getNumericalCDF(timeLimit, step);
        }*/

        //doppio metodo analyze, in uno si richiama quello che non fa i plot...
        /*TransientSolution<DeterministicEnablingState, RewardRate> simulate = model.simulate(timeLimit.toString(), step.toString(), 1000);
        double[] simulation = new double[simulate.getSolution().length];
        for(int i = 0; i < simulation.length; i++){
            simulation[i] = simulate.getSolution()[i][0][0];
        }*/

        if(model instanceof Xor){
            double[] analysis = numericalXOR(model, timeLimit, step, error, tabSpaceChars);
            //ActivityViewer.CompareResults(model.name(), List.of("simulation", "real"), List.of(new TestCaseResult("sim", simulation, 0, simulation.length, step.doubleValue(), 0), new TestCaseResult("an", analysis, 0, simulation.length, step.doubleValue(), 0)));
            return analysis;
        }

        if(model instanceof AND){
            double[] analysis = numericalAND(model, timeLimit, step, error, tabSpaceChars);
            //ActivityViewer.CompareResults(model.name(), List.of("simulation", "real"), List.of(new TestCaseResult("sim", simulation, 0, simulation.length, step.doubleValue(), 0), new TestCaseResult("an", analysis, 0, simulation.length, step.doubleValue(), 0)));
            return analysis;
        }

        if(model instanceof SEQ) {
            double[] analysis = numericalSEQ(model, timeLimit, step, error, tabSpaceChars);
            //ActivityViewer.CompareResults(model.name(), List.of("simulation", "real"), List.of(new TestCaseResult("sim", simulation, 0, simulation.length, step.doubleValue(), 0), new TestCaseResult("an", analysis, 0, simulation.length, step.doubleValue(), 0)));
            return analysis;
        }

        if(model instanceof Repeat) {
            if (!(model.simplifiedC().compareTo(model.C()) == 0) && !(model.simplifiedR().compareTo(model.R()) == 0)) {
                if(model.C().compareTo(this.CThreshold()) > 0 || model.R().compareTo(this.RThreshold()) > 0) {
                    System.out.println(tabSpaceChars + " Performing REP Inner Block Analysis on " + model.name());
                    double[] analysis = REPInnerBlockAnalysis(model, timeLimit, step, error, tabSpaceChars);
                    //ActivityViewer.CompareResults(model.name(), List.of("simulation", "real"), List.of(new TestCaseResult("sim", simulation, 0, simulation.length, step.doubleValue(), 0), new TestCaseResult("an", analysis, 0, simulation.length, step.doubleValue(), 0)));
                    return analysis;
                }

                double[] analysis = regenerativeTransientAnalysis(model, timeLimit, step, BigDecimal.valueOf(1), error, tabSpaceChars);
                //ActivityViewer.CompareResults(model.name(), List.of("simulation", "real"), List.of(new TestCaseResult("sim", simulation, 0, simulation.length, step.doubleValue(), 0), new TestCaseResult("an", analysis, 0, simulation.length, step.doubleValue(), 0)));
                return analysis;
            }

            //TODO forse qui ne va messa una che deve gestire il timeLimit???
        }

        if(model instanceof DAG) {
            // Check for Cycles and analyze them --> altrimenti complessità sarà sempre infinito
            System.out.println(tabSpaceChars + " Searching Repetitions in DAG " + model.name());
            checkREPinDAG(model, timeLimit, step, error, "---" + tabSpaceChars);

            // Check Complexity
            if (!(model.simplifiedC().compareTo(model.C()) == 0) || !(model.simplifiedR().compareTo(model.R()) == 0)) {
                if (model.simplifiedC().compareTo(this.CThreshold()) >= 0 || model.simplifiedR().compareTo(this.RThreshold()) >= 0) {
                    System.out.println(tabSpaceChars + " Performing Block Replication on " + model.name());
                    double[] analysis = InnerBlockReplicationAnalysis(model, timeLimit, step, error, tabSpaceChars);
                    //ActivityViewer.CompareResults(model.name(), List.of("simulation", "real"), List.of(new TestCaseResult("sim", simulation, 0, simulation.length, step.doubleValue(), 0), new TestCaseResult("an", analysis, 0, simulation.length, step.doubleValue(), 0)));
                    return analysis;
                }

                if(model.C().compareTo(this.CThreshold()) > 0 || model.R().compareTo(this.RThreshold()) > 0){
                    System.out.println(tabSpaceChars + " Performing DAG Inner Block Analysis on " + model.name());
                    double[] analysis = DAGInnerBlockAnalysis(model, timeLimit, step, error, tabSpaceChars);
                    //ActivityViewer.CompareResults(model.name(), List.of("simulation", "real"), List.of(new TestCaseResult("sim", simulation, 0, simulation.length, step.doubleValue(), 0), new TestCaseResult("an", analysis, 0, simulation.length, step.doubleValue(), 0)));
                    return analysis;
                }
            }

            // TODO serve un'azione per quando c'è qualcosa che è già in versione semplificata, ma ancora complessa.
            // (Che poi sono le azioni di sopra, ma cambiano le guardie degli IF THEN)
        }

        double[] analysis = forwardAnalysis(model, timeLimit, step, error, tabSpaceChars);
        //ActivityViewer.CompareResults(model.name(), List.of("simulation", "real"), List.of(new TestCaseResult("sim", simulation, 0, simulation.length, step.doubleValue(), 0), new TestCaseResult("an", analysis, 0, simulation.length, step.doubleValue(), 0)));
        return analysis;
    }
}
