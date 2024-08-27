package org.oristool.eulero.evaluation.heuristics;

import org.oristool.eulero.evaluation.approximator.Approximator;
import org.oristool.eulero.modeling.activitytypes.BadNestedDAGType;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TreeficationHeursticsVisitor extends AnalysisHeuristicsVisitor {
    public TreeficationHeursticsVisitor(String heuristicName, BigInteger CThreshold, BigInteger QThreshold, Approximator approximator, boolean verbose, boolean plotIntermediate) {
        super(heuristicName, CThreshold, QThreshold, approximator, verbose, plotIntermediate);
    }

    public TreeficationHeursticsVisitor(String heuristicName, BigInteger CThreshold, BigInteger QThreshold, Approximator approximator) {
        super(heuristicName, CThreshold, QThreshold, approximator);
    }

    public TreeficationHeursticsVisitor(String heuristicName, BigInteger CThreshold, BigInteger QThreshold, Approximator approximator, boolean verbose) {
        super(heuristicName, CThreshold, QThreshold, approximator, verbose);
    }

    @Override
    public double[] analyze(BadNestedDAGType modelType, BigDecimal timeLimit, BigDecimal step) {
        long time = System.nanoTime();
        BigInteger C = modelType.getActivity().C();
        BigInteger Q = modelType.getActivity().Q();

        if(C.compareTo(this.CThreshold()) > 0 || Q.compareTo(this.QThreshold()) > 0){
            //System.out.println("Replico!");
            return modelType.treeficationAnalysis(timeLimit, step, this.CThreshold(), this.QThreshold(), this);
        }

        // if not complex
        return modelType.forwardTransientAnalysis(timeLimit, step);
    }
}
