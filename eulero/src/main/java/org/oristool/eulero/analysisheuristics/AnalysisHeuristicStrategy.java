package org.oristool.eulero.analysisheuristics;

import org.oristool.eulero.MainHelper;
import org.oristool.eulero.graph.*;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AnalysisHeuristicStrategy {
    private final BigInteger CThreshold;
    private final BigInteger RThreshold;
    private final Approximator approximator;

    public AnalysisHeuristicStrategy(BigInteger CThreshold, BigInteger RThreshold, Approximator approximator){
        this.CThreshold = CThreshold;
        this.RThreshold = RThreshold;
        this.approximator = approximator;
    }

    public abstract double[] analyze(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error);

    public BigInteger CThreshold() {
        return CThreshold;
    }

    public BigInteger RThreshold() {
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

        // TODO Remove
        MainHelper.ResultWrapper analysis = new MainHelper.ResultWrapper(analyze(repeatBody, timeLimit, step, error), repeatBody.EFT().divide(step).intValue(), repeatBody.LFT().divide(step).intValue(), step.doubleValue());
        MainHelper.ResultWrapper analysis2 = new MainHelper.ResultWrapper(analyze(replacingBody, timeLimit, step, error), replacingBody.EFT().divide(step).intValue(), replacingBody.LFT().divide(step).intValue(), step.doubleValue());
        ActivityViewer.CompareResults("XOR-TEST", false, "", List.of("real", "appr"), analysis, analysis2);
        ((Repeat) model).repeatBody().replace(replacingBody);

        //TODO Place Return here
    }

    public double[] DAGInnerBlockAnalysis(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error){
        // prendo le attività composte con complessità non banale (escludo attività semplici e IMM)
        ArrayList<Activity> innerActivities = ((DAG) model).activitiesBetween(((DAG) model).begin(), ((DAG) model).end())
                .stream().filter(t -> (t.C().doubleValue() > 1 && t.R().doubleValue() > 1)).distinct().sorted(Comparator.comparing(Activity::C).thenComparing(Activity::R)).collect(Collectors.toCollection(ArrayList::new));

        innerActivities.get(innerActivities.size() - 1).replace(
                new Analytical(innerActivities.get(innerActivities.size() - 1).name() + "_N",
                        approximator().getApproximatedStochasticTransitionFeature(analyze(innerActivities.get(innerActivities.size() - 1), innerActivities.get(innerActivities.size() - 1).LFT(), step, error),
                                innerActivities.get(innerActivities.size() - 1).EFT().doubleValue(), innerActivities.get(innerActivities.size() - 1).LFT().doubleValue(), step))
        );

        model.resetComplexityMeasure();

        return this.analyze(model, timeLimit, step, error);


        /*int innerActivitiesCounter = innerActivities.size() - 1;

        while(innerActivitiesCounter >= 0 && (model.C().compareTo(CThreshold) > 0 || model.R().compareTo(RThreshold) > 0)){
            innerActivities.get(innerActivitiesCounter).replace(
                    new Analytical(innerActivities.get(innerActivitiesCounter).name() + "_N",
                            approximator().getApproximatedStochasticTransitionFeature(analyze(innerActivities.get(innerActivitiesCounter), innerActivities.get(innerActivitiesCounter).LFT(), step, error),
                                    innerActivities.get(innerActivitiesCounter).EFT().doubleValue(), innerActivities.get(innerActivitiesCounter).LFT().doubleValue(), step))
            );

            innerActivitiesCounter--;
            model.resetComplexityMeasure();
        }*/
    }

    public double[] InnerBlockReplicationAnalysis(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error){
        ArrayList<DAG> innerBlocks = new ArrayList<>();
        ArrayList<DAG> sortedInnerBlocks = new ArrayList<>();
        int counter = 0;
        for(Activity activity: ((DAG) model).end().pre()){
            DAG innerBlock = ((DAG) model).copyRecursive(((DAG) model).begin(), activity, "_N_act" + counter);
            innerBlocks.add(innerBlock);
            innerBlock.C();
            innerBlock.R();
            sortedInnerBlocks.add(innerBlock);
            counter++;
        }

        sortedInnerBlocks.sort(Comparator.comparing(Activity::C).thenComparing(Activity::R));

        //int innerActivitiesCounter = sortedInnerBlocks.size() - 1;

        DAG nestedDAG = ((DAG) model).nest(((DAG) model).end().pre().get(innerBlocks.indexOf(sortedInnerBlocks.get(sortedInnerBlocks.size() - 1))));
        nestedDAG.setEFT(nestedDAG.low());
        nestedDAG.setLFT(nestedDAG.upp());
        nestedDAG.replace(
                new Analytical(nestedDAG.name() + "_from_" + sortedInnerBlocks.get(sortedInnerBlocks.size() - 1).name(),
                        approximator().getApproximatedStochasticTransitionFeature(
                                analyze(nestedDAG, nestedDAG.LFT(), step, error),
                                nestedDAG.EFT().doubleValue(),
                                nestedDAG.LFT().doubleValue(),
                                step
                        )
                )
        );

        model.resetComplexityMeasure();
        return this.analyze(model, timeLimit, step, error);
    }

    public double[] regenerativeTransientAnalysis(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error){
        System.out.println("Analyzing Block: " + model.name());
        TransientSolution<DeterministicEnablingState, RewardRate> transientSolution = model.analyze(timeLimit.toString(), step.toString(), error.toString());
        System.out.println("Analyzing Block: Done");
        double[] solution = new double[transientSolution.getSolution().length];
        for(int i = 0; i < solution.length; i++){
            solution[i] = transientSolution.getSolution()[i][0][0];
        }

        return solution;
    }
}
