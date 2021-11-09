package org.oristool.eulero.analysisheuristics;

import org.oristool.eulero.MainHelper;
import org.oristool.eulero.graph.*;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class AnalysisHeuristicStrategy {
    private BigDecimal CThreshold;
    private BigDecimal RThreshold;
    private Approximator approximator;

    public AnalysisHeuristicStrategy(BigDecimal CThreshold, BigDecimal RThreshold, Approximator approximator){
        this.CThreshold = CThreshold;
        this.RThreshold = RThreshold;
        this.approximator = approximator;
    }

    public abstract double[] analyze(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error);

    public BigDecimal CThreshold() {
        return CThreshold;
    }

    public BigDecimal RThreshold() {
        return RThreshold;
    }
    public Approximator approximator() {
        return approximator;
    }

    public double[] numericalXOR(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error){
        double[] solution = new double[timeLimit.divide(step).intValue() + 1];

        for(Activity act: ((Xor) model).alternatives()){
            double[] activityCDF = analyze(act, timeLimit, step, error);
            double prob = ((Xor) model).probs().get(((Xor) model).alternatives().indexOf(act));
            for(int t = 0; t < solution.length; t++){
                solution[t] += prob * activityCDF[t];
            }
        }
        return solution;
    }

    public double[] numericalAND(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error){
        double[] solution = new double[timeLimit.divide(step).intValue() + 1];

        Arrays.fill(solution, 1.0);
        for(Activity act: ((AND) model).activities()){
            double[] activityCDF = analyze(act, timeLimit, step, error);
            for(int t = 0; t < solution.length; t++){
                solution[t] *= activityCDF[t];
            }
        }
        return solution;
    }

    public double[] numericalSEQ(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error){
        double[] solution = new double[timeLimit.divide(step).intValue() + 1];

        for (Activity act : ((SEQ) model).activities()) {

            if (act.equals(((SEQ) model).activities().get(0))) {
                solution = analyze(act, timeLimit, step, error);
            } else {
                double[] convolution = new double[solution.length];
                double[] activityCDF = analyze(act, timeLimit, step, error);

                for (int x = 1; x < solution.length; x++) {
                    for (int u = 1; u <= x; u++)
                        convolution[x] += (solution[u] - solution[u - 1]) * (activityCDF[x - u + 1] + activityCDF[x - u]) * 0.5;
                }

                solution = convolution;
            }
        }
        return solution;
    }

    public void REPInnerBlockAnalysis(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error){
        Activity repeatBody = ((Repeat) model).repeatBody();
        Analytical replacingBody = new Analytical(repeatBody.name() + "_new",
                approximator().getApproximatedStochasticTransitionFeature(
                        analyze(repeatBody, /* è giusto? -->*/ repeatBody.LFT(), step, error), repeatBody.EFT().doubleValue(), repeatBody.LFT().doubleValue(), step)
                );

        MainHelper.ResultWrapper analysis = new MainHelper.ResultWrapper(analyze(repeatBody, timeLimit, step, error), repeatBody.EFT().divide(step).intValue(), repeatBody.LFT().divide(step).intValue(), step.doubleValue());
        MainHelper.ResultWrapper analysis2 = new MainHelper.ResultWrapper(analyze(replacingBody, timeLimit, step, error), replacingBody.EFT().divide(step).intValue(), replacingBody.LFT().divide(step).intValue(), step.doubleValue());
        ActivityViewer.CompareResults("XOR-TEST", false, "", List.of("real", "appr"), analysis, analysis2);
        ((Repeat) model).repeatBody().replace(replacingBody);
    }

    public void DAGBlockReplication(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error){
        // TO Be Tested
        ArrayList<DAG> nestedModels = new ArrayList<>();
        for(Activity activity: ((DAG) model).end().pre()){
            nestedModels.add(((DAG) model).nest(activity));
        }
        nestedModels.sort(Comparator.comparing(act -> act.C().add(act.R().multiply(BigDecimal.valueOf(0.5)))));
        int nestedModelCounter = 0;

        while(model.C().compareTo(this.CThreshold()) > 0 && model.R().compareTo(this.RThreshold()) > 0){
            DAG theNestedModel = nestedModels.get(nestedModelCounter);
            theNestedModel.replace(new Analytical(theNestedModel.name() + "_N",
                    approximator().getApproximatedStochasticTransitionFeature(analyze(theNestedModel, theNestedModel.LFT(), step, error),
                            theNestedModel.EFT().doubleValue(), theNestedModel.LFT().doubleValue(), step)));
            nestedModelCounter++;
        }
    }

    public double[] regenerativeTransientAnalysis(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error){
        TransientSolution<DeterministicEnablingState, RewardRate> transientSolution = model.analyze(timeLimit.toString(), step.toString(), error.toString());
        double[] solution = new double[transientSolution.getSolution().length];
        for(int i = 0; i < solution.length; i++){
            solution[i] = transientSolution.getSolution()[i][0][0];
        }

        return solution;
    }
}
