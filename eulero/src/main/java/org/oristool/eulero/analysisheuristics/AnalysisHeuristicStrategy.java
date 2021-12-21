package org.oristool.eulero.analysisheuristics;

import org.oristool.eulero.graph.*;
import org.oristool.eulero.mains.TestCaseResult;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.petrinet.Marking;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AnalysisHeuristicStrategy {
    private final String heuristicName;
    private final BigInteger CThreshold;
    private final BigInteger RThreshold;
    private final Approximator approximator;

    public AnalysisHeuristicStrategy(String heuristicName, BigInteger CThreshold, BigInteger RThreshold, Approximator approximator){
        this.heuristicName = heuristicName;
        this.CThreshold = CThreshold;
        this.RThreshold = RThreshold;
        this.approximator = approximator;
    }

    public abstract double[] analyze(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error, String tabSpaceChars);

    public BigInteger CThreshold() {
        return CThreshold;
    }

    public BigInteger RThreshold() {
        return RThreshold;
    }

    public Approximator approximator() {
        return approximator;
    }

    public String heuristicName() {
        return heuristicName;
    }

    public double[] numericalXOR(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error, String tabSpaceChars){
        double[] solution = new double[timeLimit.divide(step).intValue() + 1];
        System.out.println(tabSpaceChars + " Numerical XOR Analysis of " + model.name());
        long time = System.nanoTime();

        for(Activity act: ((Xor) model).alternatives()){
            double[] activityCDF = analyze(act, timeLimit, step, error, tabSpaceChars + "---");
            double prob = ((Xor) model).probs().get(((Xor) model).alternatives().indexOf(act));
            for(int t = 0; t < solution.length; t++){
                solution[t] += prob * activityCDF[t];
            }
        }

        System.out.println(tabSpaceChars +  " Analysis of " +  model.name() + "done in " + String.format("%.3f seconds",
                (System.nanoTime() - time)/1e9) + "...");

        return solution;
    }

    public double[] analyze(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error) {
        double[] analysisResult = this.analyze(model, timeLimit, step, error, "---");
        return analysisResult;
    }

    public double[] numericalAND(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error, String tabSpaceChars){
        double[] solution = new double[timeLimit.divide(step).intValue() + 1];

        System.out.println(tabSpaceChars + " Numerical AND Analysis of " + model.name());
        long time = System.nanoTime();

        Arrays.fill(solution, 1.0);
        for(Activity act: ((AND) model).activities()){
            double[] activityCDF = analyze(act, timeLimit, step, error, tabSpaceChars + "---");
            for(int t = 0; t < solution.length; t++){
                solution[t] *= activityCDF[t];
            }
        }

        System.out.println(tabSpaceChars +  " Analysis of " +  model.name() + "done in " + String.format("%.3f seconds",
                (System.nanoTime() - time)/1e9) + "...");
        return solution;
    }

    public double[] numericalSEQ(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error, String tabSpaceChars){
        double[] solution = new double[timeLimit.divide(step).intValue() + 1];

        System.out.println(tabSpaceChars + " Numerical SEQ Analysis of " + model.name());
        long time = System.nanoTime();


        for (Activity act : ((SEQ) model).activities()) {

            if (act.equals(((SEQ) model).activities().get(0))) {
                solution = analyze(act, timeLimit, step, error, tabSpaceChars + "---");
            } else {
                double[] convolution = new double[solution.length];
                double[] activityCDF = analyze(act, timeLimit, step, error, tabSpaceChars + "---");

                for (int x = 1; x < solution.length; x++) {
                    for (int u = 1; u <= x; u++)
                        convolution[x] += (solution[u] - solution[u - 1]) * (activityCDF[x - u + 1] + activityCDF[x - u]) * 0.5;
                }

                solution = convolution;
            }
        }

        System.out.println(tabSpaceChars +  " Analysis of " +  model.name() + "done in " + String.format("%.3f seconds",
                (System.nanoTime() - time)/1e9) + "...");
        return solution;
    }

    public double[] REPInnerBlockAnalysis(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error, String tabSpaceChars){
        Activity repeatBody = ((Repeat) model).repeatBody();

        Analytical replacingBody = new Analytical(repeatBody.name() + "_new",
                approximator().getApproximatedStochasticTransitionFeatures(
                        analyze(repeatBody, repeatBody.LFT(), step, error, tabSpaceChars + "---" ), repeatBody.EFT().doubleValue(), repeatBody.LFT().doubleValue(), step),
                approximator().stochasticTransitionFeatureWeights()
                );

        ((Repeat) model).replaceBody(replacingBody);

        model.resetComplexityMeasure();
        return this.analyze(model, timeLimit, step, error, tabSpaceChars);
    }

    public void checkREPinDAG(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error, String tabSpaceChars){
        for(Activity act: ((DAG) model).activitiesBetween(((DAG) model).begin(), ((DAG) model).end())){
            if(act instanceof Repeat){
                System.out.println(tabSpaceChars + " Repetition found! It's " + act.name());

                double[] analysis = analyze(act, timeLimit, step, error, tabSpaceChars + "---");
                int cut = IntStream.range(0, analysis.length)
                        .filter(t -> analysis[t] >= 0.99)
                        .findFirst()
                        .orElse(analysis.length);

                /*Analytical replacingActivity = new Analytical(act.name() + "_new",
                        approximator().getApproximatedStochasticTransitionFeature(
                                Arrays.copyOfRange(analysis, 0, cut) , act.EFT().doubleValue(), act.EFT().doubleValue() + cut * step.doubleValue(), step)
                );*/
                Analytical replacingActivity = new Analytical(act.name() + "_new",
                        approximator().getApproximatedStochasticTransitionFeatures(
                                analyze(act, timeLimit, step, error, tabSpaceChars + "---"),
                                act.EFT().doubleValue(), timeLimit.min(act.LFT()).doubleValue(), step),
                        approximator().stochasticTransitionFeatureWeights());



                act.replace(replacingActivity);

                System.out.println(tabSpaceChars + " Block " + act.name() + " replaced...");

                long time = System.nanoTime();
                model.resetComplexityMeasure();
                System.out.println("Anodo: " + String.format("%.3f seconds",
                        (System.nanoTime() - time)/1e9) + "...");
            }
        }

           //return this.analyze(model, timeLimit, step, error, tabSpaceChars);
    }

    public void checkWellNestedInDAG(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error, String tabSpaceChars){
        for(Activity act: ((DAG) model).activitiesBetween(((DAG) model).begin(), ((DAG) model).end())){
            if(act instanceof SEQ || act instanceof Xor || act instanceof AND){
                System.out.println(tabSpaceChars + " Well Nested Block found! It's " + act.name());

                Analytical replacingActivity = new Analytical(act.name() + "_new",
                        approximator().getApproximatedStochasticTransitionFeatures(
                                analyze(act, act.LFT().precision() >= 309 ? timeLimit : act.LFT(), step, error, tabSpaceChars + "---"),
                                act.EFT().doubleValue(), act.LFT().precision() >= 309 ? timeLimit.doubleValue() : act.LFT().doubleValue(), step),
                        approximator().stochasticTransitionFeatureWeights());

                act.replace(replacingActivity);

                System.out.println(tabSpaceChars + " Block " + act.name() + " replaced...");
            }
        }

        model.resetComplexityMeasure();
        //return this.analyze(model, timeLimit, step, error, tabSpaceChars);
    }

    public double[] DAGInnerBlockAnalysis(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error, String tabSpaceChars){
        // prendo le attività composte con complessità non banale (escludo attività semplici e IMM)
        ArrayList<Activity> innerActivities = ((DAG) model).activitiesBetween(((DAG) model).begin(), ((DAG) model).end())
                .stream().filter(t -> (t.C().doubleValue() > 1 && t.R().doubleValue() > 1)).distinct().sorted(Comparator.comparing(Activity::C).thenComparing(Activity::R)).collect(Collectors.toCollection(ArrayList::new));

        System.out.println(tabSpaceChars + "---"  + " Block Analysis: Choose inner block " + innerActivities.get(innerActivities.size() - 1).name());

        /*BigDecimal cic = (innerActivities.get(innerActivities.size() - 1).LFT().precision() > 309) ? timeLimit : innerActivities.get(innerActivities.size() - 1).LFT();
        double[] real = analyze(innerActivities.get(innerActivities.size() - 1),  cic, step, error, tabSpaceChars + "---"  );
        Activity test = new Analytical(innerActivities.get(innerActivities.size() - 1).name() + "_N",
                approximator().getApproximatedStochasticTransitionFeatures(
                        real,
                        innerActivities.get(innerActivities.size() - 1).EFT().doubleValue(), (innerActivities.get(innerActivities.size() - 1).LFT().precision() >= 309 ? timeLimit : innerActivities.get(innerActivities.size() - 1).LFT()).doubleValue(), step),  approximator().stochasticTransitionFeatureWeights());
        double[] an = analyze(test, cic, step, error, tabSpaceChars);
        ActivityViewer.CompareResults(innerActivities.get(innerActivities.size() - 1).name(), List.of("Real", "appr"), List.of(
                new TestCaseResult("Real", real,
                        innerActivities.get(innerActivities.size() - 1).EFT().divide(step, 3, RoundingMode.HALF_DOWN).intValue(),
                        (innerActivities.get(innerActivities.size() - 1).LFT().precision() >= 309 ? timeLimit : innerActivities.get(innerActivities.size() - 1).LFT()).divide(step, 3, RoundingMode.HALF_DOWN).intValue(),
                        step.doubleValue(), System.nanoTime()),
                new TestCaseResult("AN", an,
                        test.EFT().divide(step, 3, RoundingMode.HALF_DOWN).intValue(),
                        test.LFT().divide(step, 3, RoundingMode.HALF_DOWN).intValue(),
                        step.doubleValue(), System.nanoTime()))
        );*/

        innerActivities.get(innerActivities.size() - 1).replace(
                new Analytical(innerActivities.get(innerActivities.size() - 1).name() + "_N",
                        approximator().getApproximatedStochasticTransitionFeatures(
                                analyze(innerActivities.get(innerActivities.size() - 1), innerActivities.get(innerActivities.size() - 1).LFT().precision() >= 309 ? timeLimit : innerActivities.get(innerActivities.size() - 1).LFT(), step, error, tabSpaceChars + "---"  ),
                                innerActivities.get(innerActivities.size() - 1).EFT().doubleValue(), (innerActivities.get(innerActivities.size() - 1).LFT().precision() >= 309 ? timeLimit : innerActivities.get(innerActivities.size() - 1).LFT()).doubleValue(), step),
                        approximator().stochasticTransitionFeatureWeights())
        );

        System.out.println(tabSpaceChars + "---"  + " Approximated inner block " + innerActivities.get(innerActivities.size() - 1).name());

        model.resetComplexityMeasure();
        return this.analyze(model, timeLimit, step, error, tabSpaceChars);
    }

    public double[] InnerBlockReplicationAnalysis(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error, String tabSpaceChars){
        ArrayList<DAG> innerBlocks = new ArrayList<>();
        ArrayList<DAG> sortedInnerBlocks = new ArrayList<>();
        int counter = 0;
        for(Activity activity: ((DAG) model).end().pre()){
            DAG innerBlock = ((DAG) model).copyRecursive(((DAG) model).begin(), activity, "before_" + activity.name());
            innerBlocks.add(innerBlock);
            innerBlock.C();
            innerBlock.R();
            sortedInnerBlocks.add(innerBlock);
            counter++;
        }

        sortedInnerBlocks.sort(Comparator.comparing(Activity::C).thenComparing(Activity::R));

        DAG nestedDAG = ((DAG) model).nest(((DAG) model).end().pre().get(innerBlocks.indexOf(sortedInnerBlocks.get(sortedInnerBlocks.size() - 1))));
        model.replace(nestedDAG);
        System.out.println(tabSpaceChars + "---"  + " Replicated block before " + nestedDAG.end().pre().get(1).name().replace(model.name()+"_nestingOf__", ""));
        nestedDAG.setEFT(nestedDAG.low());
        nestedDAG.setLFT(nestedDAG.upp());


        /*nestedDAG.replace(
                new Analytical(sortedInnerBlocks.get(sortedInnerBlocks.size() - 1).name(),
                        approximator().getApproximatedStochasticTransitionFeatures(
                                analyze(nestedDAG, nestedDAG.LFT().precision() >= 309 ? timeLimit : nestedDAG.LFT(), step, error, tabSpaceChars + "---"  ),
                                nestedDAG.EFT().doubleValue(),
                                (nestedDAG.LFT().precision() >= 309 ? timeLimit : nestedDAG.LFT()).doubleValue(),
                                step
                        ),
                        approximator.stochasticTransitionFeatureWeights()
                )
        );*/


        //model.checkWellNesting();
        model.resetComplexityMeasure();
        return this.analyze(nestedDAG, timeLimit, step, error, tabSpaceChars);
    }

    public double[] regenerativeTransientAnalysis(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal sampleFactor, BigDecimal error, String tabSpaceChars){
        System.out.println(tabSpaceChars + " Regenerative Analysis of block" + model.name());
        long time = System.nanoTime();
        TransientSolution<DeterministicEnablingState, RewardRate> transientSolution = model.analyze(timeLimit.toString(), step.divide(sampleFactor, RoundingMode.HALF_DOWN).toString(), error.toString());
        System.out.println(tabSpaceChars +  " Analysis done in " + String.format("%.3f seconds",
                (System.nanoTime() - time)/1e9) + "...");
        double[] solution = new double[timeLimit.divide(step, RoundingMode.HALF_DOWN).intValue()];
        for(int i = 0; i < solution.length; i++){
            solution[i] = transientSolution.getSolution()[i * sampleFactor.intValue()][0][0];
        }

        return solution;
    }

    public double[] forwardAnalysis(Activity model, BigDecimal timeLimit, BigDecimal step, BigDecimal error, String tabSpaceChars){
        System.out.println(tabSpaceChars + " Forward Analysis of block " + model.name());
        long time = System.nanoTime();
        TransientSolution<DeterministicEnablingState, RewardRate> transientSolution = model.analyze(timeLimit.toString(), step.toString(), error.toString());
        System.out.println(tabSpaceChars +  " Analysis done in " + String.format("%.3f seconds",
                (System.nanoTime() - time)/1e9) + "...");
        double[] solution = new double[transientSolution.getSolution().length];
        for(int i = 0; i < solution.length; i++){
            solution[i] = transientSolution.getSolution()[i][0][0];
        }

        return solution;
    }
}
