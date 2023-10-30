package org.oristool.eulero.evaluation.heuristics.deprecated;

import org.apache.commons.lang3.tuple.Pair;
import org.oristool.eulero.evaluation.approximator.Approximator;
import org.oristool.eulero.evaluation.heuristics.EvaluationResult;
import org.oristool.eulero.modeling.activitytypes.ActivityEnumType;
import org.oristool.eulero.modeling.deprecated.*;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.Marking;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AnalysisHeuristicsStrategy {
    private final String heuristicName;
    private final BigInteger CThreshold;
    private final BigInteger QThreshold;
    private final Approximator approximator;
    private final boolean plotIntermediate;
    private final boolean verbose;

    public AnalysisHeuristicsStrategy(String heuristicName, BigInteger CThreshold, BigInteger QThreshold, Approximator approximator, boolean verbose, boolean plotIntermediate){
        this.heuristicName = heuristicName;
        this.CThreshold = CThreshold;
        this.QThreshold = QThreshold;
        this.approximator = approximator;
        this.plotIntermediate = plotIntermediate;
        this.verbose = verbose;
    }

    public AnalysisHeuristicsStrategy(String heuristicName, BigInteger CThreshold, BigInteger QThreshold, Approximator approximator){
        this(heuristicName, CThreshold, QThreshold, approximator, false, false);
    }

    public AnalysisHeuristicsStrategy(String heuristicName, BigInteger CThreshold, BigInteger QThreshold, Approximator approximator, boolean verbose){
        this(heuristicName, CThreshold, QThreshold, approximator, verbose, false);
    }


    public abstract double[] analyze(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal forwardReductionFactor, BigDecimal error, String tabSpaceChars);

    public double[] analyze(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error, String tabSpaceChars){
        return analyze(model, timeLimit, step, BigDecimal.ONE, error, tabSpaceChars);
    }

    public double[] analyze(Activity model, BigDecimal timeLimit, BigDecimal step){
        return analyze(model, timeLimit, step, BigDecimal.ONE, BigDecimal.valueOf(0.001), "---");
    }

    public boolean verbose() { return verbose; }

    public BigInteger CThreshold() {
        return CThreshold;
    }

    public BigInteger QThreshold() {
        return QThreshold;
    }

    public Approximator approximator() {
        return approximator;
    }

    public String heuristicName() {
        return heuristicName;
    }

    public double[] analyze(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal forwardReductionFactor, BigDecimal error) {
        return this.analyze(model, timeLimit, step, forwardReductionFactor, error, "---");
    }

    public double[] numericalXOR(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal forwardReductionFactor, BigDecimal error, String tabSpaceChars, boolean verbose){
        double[] solution = new double[timeLimit.divide(step).intValue() + 1];

        if(verbose)
            System.out.println(tabSpaceChars + " Numerical XOR Analysis of " + model.name());

        long time = System.nanoTime();
        for(Activity act: model.activities()){
            double[] activityCDF = analyze(act, timeLimit, step, forwardReductionFactor, error, tabSpaceChars + "---");

            for(int t = 0; t < solution.length; t++){
                solution[t] += ((XOR)model).probs().get(model.activities().indexOf(act)) * activityCDF[t];
            }
        }

        if(verbose)
            System.out.println(tabSpaceChars +  " Analysis of " +  model.name() + " done in " + String.format("%.3f seconds",
                    (System.nanoTime() - time)/1e9) + "...");

        return solution;
    }

    public double[] numericalXOR(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal forwardReductionFactor, BigDecimal error, String tabSpaceChars){
        return numericalXOR(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars, false);
    }

    public double[] numericalAND(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal forwardReductionFactor, BigDecimal error, String tabSpaceChars){
        double[] solution = new double[timeLimit.divide(step).intValue() + 1];

        if(verbose)
            System.out.println(tabSpaceChars + " Numerical AND Analysis of " + model.name());

        long time = System.nanoTime();

        Arrays.fill(solution, 1.0);
        for(Activity act: model.activities()){
            double[] activityCDF = analyze(act, timeLimit, step, forwardReductionFactor, error, tabSpaceChars + "---");
            for(int t = 0; t < solution.length; t++){
                solution[t] *= activityCDF[t];
            }
        }

        if(verbose)
            System.out.println(tabSpaceChars +  " Analysis of " +  model.name() + " done in " + String.format("%.3f seconds",
                    (System.nanoTime() - time)/1e9) + "...");

        return solution;
    }

    public double[] numericalSEQ(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal forwardReductionFactor, BigDecimal error, String tabSpaceChars){
        double[] solution = new double[timeLimit.divide(step).intValue() + 1];

        if(verbose)
            System.out.println(tabSpaceChars + " Numerical SEQ Analysis of " + model.name());

        long time = System.nanoTime();

        for (Activity act : model.activities()) {

            if (act.equals(model.activities().get(0))) {
                solution = analyze(act, timeLimit, step, forwardReductionFactor, error, tabSpaceChars + "---");
            } else {
                double[] convolution = new double[solution.length];
                double[] activityCDF = analyze(act, timeLimit, step, forwardReductionFactor, error, tabSpaceChars + "---");

                for (int x = 1; x < solution.length; x++) {
                    for (int u = 1; u <= x; u++)
                        convolution[x] += (solution[u] - solution[u - 1]) * (activityCDF[x - u + 1] + activityCDF[x - u]) * 0.5;
                }

                solution = convolution;
            }
        }

        if(verbose)
            System.out.println(tabSpaceChars +  " Analysis of " +  model.name() + " done in " + String.format("%.3f seconds",
                    (System.nanoTime() - time)/1e9) + "...");

        return solution;
    }

    public double[] innerBlockAnalysis(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal forwardReductionFactor, BigDecimal error, String tabSpaceChars){
        Map<String, Activity> toBeSimplifiedActivityMap = getDeepestComplexDAG(model);
        Activity toBeSimplifiedActivity = toBeSimplifiedActivityMap.get("activity");
        Activity toBeSimplifiedActivityParent = toBeSimplifiedActivityMap.get("parent");
        double aux = toBeSimplifiedActivity.max().doubleValue();
        int mag = 1;
        while (aux > 10) {
            mag = mag * 10;
            aux = aux / 10;
        }
        BigDecimal innerActivityStep = BigDecimal.valueOf(mag * Math.pow(10, -2));

        ArrayList<Pair<BigDecimal, StochasticTransitionFeature>> approximationFeature =  approximator().getApproximatedStochasticTransitionFeatures(
                analyze(toBeSimplifiedActivity, toBeSimplifiedActivity.max().precision() >= 309 ? timeLimit : toBeSimplifiedActivity.max(), innerActivityStep, forwardReductionFactor, error, tabSpaceChars + "---"  ),
                toBeSimplifiedActivity.min().doubleValue(), (toBeSimplifiedActivity.max().precision() >= 309 ? timeLimit : toBeSimplifiedActivity.max()).doubleValue(), innerActivityStep);

        Activity newActivity = new Simple(toBeSimplifiedActivity.name() + "_N",
                approximationFeature.stream().map(Pair::getRight).collect(Collectors.toCollection(ArrayList::new)),
                approximationFeature.stream().map(Pair::getLeft).collect(Collectors.toCollection(ArrayList::new)));

        if(verbose)
            System.out.println(tabSpaceChars + "---"  + " Block Analysis: Choose inner block " + toBeSimplifiedActivity.name());

        if(plotIntermediate){
            TransientSolution<DeterministicEnablingState, RewardRate> testAct = toBeSimplifiedActivity.simulate(timeLimit.toString(), step.toString(), 5000);
            double[] testActCDF = new double[testAct.getSolution().length];
            for(int i = 0; i < testActCDF.length; i++){
                testActCDF[i] = testAct.getSolution()[i][0][0];
            }

            TransientSolution<DeterministicEnablingState, RewardRate> newAct = newActivity.analyze(timeLimit.toString(), step.toString(), "0.001");
            double[] newActcdf = new double[newAct.getSolution().length];
            for(int i = 0; i < newActcdf.length; i++){
                newActcdf[i] = newAct.getSolution()[i][0][0];
            }

            toBeSimplifiedActivity.replace(newActivity);
            int activityIndex = toBeSimplifiedActivityParent.activities().indexOf(toBeSimplifiedActivity);
            toBeSimplifiedActivityParent.activities().set(activityIndex, newActivity);
            toBeSimplifiedActivityParent.resetComplexityMeasure();
            ActivityViewer.CompareResults(newActivity.name(), List.of("Real", "Appr"), List.of(new EvaluationResult("real", testActCDF, 0, testActCDF.length, step.doubleValue(), 0), new EvaluationResult("appr", newActcdf, 0, newActcdf.length, step.doubleValue(), 0)));
        } else {
            toBeSimplifiedActivity.replace(newActivity);
            int activityIndex = toBeSimplifiedActivityParent.activities().indexOf(toBeSimplifiedActivity);
            toBeSimplifiedActivityParent.activities().set(activityIndex, newActivity);
            toBeSimplifiedActivityParent.resetComplexityMeasure();
        }
        if(verbose)
            System.out.println(tabSpaceChars + "---"  + " Approximated inner block " + toBeSimplifiedActivity.name());

        model.resetComplexityMeasure();

        return this.analyze(model, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
    }

    public Map<String, Activity> getDeepestComplexDAG(Activity model){
        ArrayList<Activity> innerActivities = model.activities().stream().filter(t -> (t.C().doubleValue() > 1 || t.Q().doubleValue() > 1)).distinct().sorted(Comparator.comparing(Activity::C).thenComparing(Activity::Q)).collect(Collectors.toCollection(ArrayList::new));
        Activity mostComplexActivity = innerActivities.get(innerActivities.size() - 1);
        boolean modelIsNotADag = mostComplexActivity.type().equals(ActivityEnumType.AND) || mostComplexActivity.type().equals(ActivityEnumType.SEQ) || mostComplexActivity.type().equals(ActivityEnumType.XOR) || mostComplexActivity.type().equals(ActivityEnumType.SIMPLE);

        if(!modelIsNotADag && mostComplexActivity.C().compareTo(CThreshold) > 0 && mostComplexActivity.Q().compareTo(QThreshold) > 0){
            return getDeepestComplexDAG(mostComplexActivity);
        }

        return Map.ofEntries(
                Map.entry("parent", model),
                Map.entry("activity", mostComplexActivity)
        );
    }

    public double[] innerBlockReplication(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal forwardReductionFactor, BigDecimal error, String tabSpaceChars){
        ArrayList<DAG> replicatedBlocks = new ArrayList<>();
        ArrayList<DAG> sortedReplicatedBlocks = new ArrayList<>();
        for(Activity activity: ((DAG) model).end().pre()){
            DAG replicatedBlock = ((DAG) model).copyRecursive(((DAG) model).begin(), activity, "_before_" + activity.name());
            replicatedBlocks.add(replicatedBlock);
            replicatedBlock.C();
            replicatedBlock.Q();
            sortedReplicatedBlocks.add(replicatedBlock);
        }

        sortedReplicatedBlocks.sort(Comparator.comparing(Activity::C).thenComparing(Activity::Q));
        DAG chosenReplicatedBlock = sortedReplicatedBlocks.get(sortedReplicatedBlocks.size() - 1);

        DAG nestedDAG;
        if(sortedReplicatedBlocks.size() > 1){
            nestedDAG = ((DAG) model).nest(((DAG) model).end().pre().get(replicatedBlocks.indexOf(chosenReplicatedBlock)));
        } else {
            Activity endActivity = chosenReplicatedBlock.end().pre().get(0);
            chosenReplicatedBlock.end().removePrecondition(endActivity);
            for(Activity activity: endActivity.pre()){
                chosenReplicatedBlock.end().addPrecondition(activity);
            }
            while(endActivity.pre().size() > 0){
                endActivity.removePrecondition(endActivity.pre().get(endActivity.pre().size() - 1));
            }
            nestedDAG = DAG.sequence(model.name() + "_sequenced",
                    chosenReplicatedBlock, endActivity);
        }

        if(verbose)
            System.out.println(tabSpaceChars + "---"  + " Replicated block before " + sortedReplicatedBlocks.get(sortedReplicatedBlocks.size() - 1).name().split("_before_")[1]);

        nestedDAG.setMin(nestedDAG.low());
        nestedDAG.setMax(nestedDAG.upp());

        if(plotIntermediate){
            TransientSolution<DeterministicEnablingState, RewardRate> simulate = model.simulate(timeLimit.toString(), step.toString(), 5000);
            double[] simulation = new double[simulate.getSolution().length];
            for(int i = 0; i < simulation.length; i++){
                simulation[i] = simulate.getSolution()[i][0][0];
            }

            double[] simulation2 = this.analyze(nestedDAG, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);

            ActivityViewer.CompareResults("Inner Block Replication: " + model.name(), List.of("Real", "Appr"), List.of(new EvaluationResult("real", simulation, 0, simulation.length, step.doubleValue(), 0), new EvaluationResult("appr", simulation2, 0, simulation2.length, step.doubleValue(), 0)));
        }

        model.resetComplexityMeasure();
        return this.analyze(nestedDAG, timeLimit, step, forwardReductionFactor, error, tabSpaceChars);
    }

    public double[] regenerativeTransientAnalysis(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal sampleFactor, BigDecimal error, String tabSpaceChars){
        if(verbose)
            System.out.println(tabSpaceChars + " Reg Analysis of block " + model.name());

        long time = System.nanoTime();
        TransientSolution<DeterministicEnablingState, RewardRate> transientSolution = model.analyze(timeLimit.toString(), step.divide(sampleFactor).toString(), error.toString());
        if(verbose)
            System.out.println(tabSpaceChars +  " Analysis done in " + String.format("%.3f seconds",
                    (System.nanoTime() - time)/1e9) + "...");

        double[] solution = new double[timeLimit.divide(step, RoundingMode.HALF_DOWN).intValue()];
        for(int i = 0; i < solution.length; i++){
            solution[i] = transientSolution.getSolution()[i * sampleFactor.intValue()][0][0];
        }

        return solution;
    }

    public double[] forwardAnalysis(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error, String tabSpaceChars){
        if(verbose)
            System.out.println(tabSpaceChars + " Forward Analysis of block " + model.name());

        long time = System.nanoTime();
        TransientSolution<Marking, RewardRate> transientSolution = model.forwardAnalyze(timeLimit.toString(), step.toString(), error.toString());

        if(verbose)
            System.out.println(tabSpaceChars +  " Analysis done in " + String.format("%.3f seconds",
                    (System.nanoTime() - time)/1e9) + "...");

        double[] solution = new double[transientSolution.getSolution().length];
        for(int i = 0; i < solution.length; i++){
            solution[i] = transientSolution.getSolution()[i][0][0];
        }

        return solution;
    }
}
