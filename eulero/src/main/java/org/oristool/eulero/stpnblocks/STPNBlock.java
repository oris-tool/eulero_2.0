package org.oristool.eulero.stpnblocks;

import org.oristool.analyzer.log.NoOpLogger;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trans.RegTransient;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.MarkingCondition;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.simulator.Sequencer;
import org.oristool.simulator.TimeSeriesRewardResult;
import org.oristool.simulator.rewards.ContinuousRewardTime;
import org.oristool.simulator.rewards.RewardEvaluator;
import org.oristool.simulator.stpn.STPNSimulatorComponentsFactory;
import org.oristool.simulator.stpn.TransientMarkingConditionProbability;

import java.math.BigDecimal;
import java.util.List;

public abstract class STPNBlock {
    private String name;

    private BigDecimal EFT;
    private BigDecimal LFT;
    private BigDecimal C;
    private BigDecimal R;

    public STPNBlock(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public BigDecimal EFT() {
        return EFT;
    }

    public void setEFT(BigDecimal EFT) {
        this.EFT = EFT;
    }

    public BigDecimal LFT() {
        return LFT;
    }

    public void setLFT(BigDecimal LFT) {
        this.LFT = LFT;
    }

    public BigDecimal C() {
        return C;
    }

    public void setC(BigDecimal c) {
        C = c;
    }

    public BigDecimal R() {
        return R;
    }

    public void setR(BigDecimal r) {
        R = r;
    }

    public double[] regTransAnalysis(String timeBound, String timeStep, String error){
        // input data
        BigDecimal bound = new BigDecimal(timeBound);
        BigDecimal step = new BigDecimal(timeStep);
        BigDecimal epsilon = new BigDecimal(error);
        String cond = "pEND > 0";

        // build STPN
        PetriNet pn = new PetriNet();
        Place in = pn.addPlace("pBEGIN");
        Place out = pn.addPlace("pEND");
        this.parseToPetriNet(pn, in, out, 1);

        Marking m = new Marking();
        m.addTokens(in, 1);

        // analyze
        RegTransient.Builder builder = RegTransient.builder();
        builder.timeBound(bound);
        builder.timeStep(step);
        builder.greedyPolicy(bound, epsilon);
        builder.markingFilter(MarkingCondition.fromString(cond));

        RegTransient analysis = builder.build();
        long start = System.nanoTime();
        TransientSolution<DeterministicEnablingState, Marking> probs =
                analysis.compute(pn, m);
        System.out.println(String.format("Analysis took %.3f seconds",
                (System.nanoTime() - start)/1e9));

        // evaluate reward
        TransientSolution<DeterministicEnablingState, RewardRate> transientSolution = TransientSolution.computeRewards(false, probs,
                RewardRate.fromString(cond));

        double[] cdf = new double[transientSolution.getSolution().length];
        for(int count = 0; count < transientSolution.getSolution().length; count++){
            cdf[count] = transientSolution.getSolution()[count][0][0];
        }

        return cdf;
    }

    public double[] simulate(String timeBound, String timeStep, int runs){
        // input data
        BigDecimal bound = new BigDecimal(timeBound);
        BigDecimal step = new BigDecimal(timeStep);
        int samples = bound.divide(step).intValue() + 1;
        String cond = "pEND > 0";

        // build STPN
        PetriNet pn = new PetriNet();
        Place in = pn.addPlace("pBEGIN");
        Place out = pn.addPlace("pEND");
        this.parseToPetriNet(pn, in, out, 1);

        Marking m = new Marking();
        m.addTokens(in, 1);

        // simulate
        System.out.println("Starting Simulation...");
        Sequencer s = new Sequencer(pn, m,
                new STPNSimulatorComponentsFactory(), NoOpLogger.INSTANCE);
        TransientMarkingConditionProbability reward =
                new TransientMarkingConditionProbability(s,
                        new ContinuousRewardTime(step), samples,
                        MarkingCondition.fromString(cond));
        RewardEvaluator rewardEvaluator = new RewardEvaluator(reward, runs);
        long start = System.nanoTime();
        s.simulate();
        System.out.println(String.format("Simulation took %.3f seconds",
                (System.nanoTime() - start)/1e9));

        // evaluate reward
        TimeSeriesRewardResult probs = (TimeSeriesRewardResult) rewardEvaluator.getResult();
        DeterministicEnablingState initialReg = new DeterministicEnablingState(m, pn);
        TransientSolution<DeterministicEnablingState, RewardRate> result =
                new TransientSolution<>(bound, step, List.of(initialReg),
                        List.of(RewardRate.fromString(cond)), initialReg);

        for (int t = 0; t < result.getSolution().length; t++) {
            for (Marking x : probs.getMarkings()) {
                result.getSolution()[t][0][0] += probs.getTimeSeries(x)[t].doubleValue();
            }
        }

        double[] cdf = new double[result.getSolution().length];
        for(int count = 0; count < result.getSolution().length; count++){
            cdf[count] = result.getSolution()[count][0][0];
        }

        return cdf;
    }

    public abstract boolean isWellNested();

    public abstract double[] getNumericalCDF(BigDecimal step);

    public abstract int parseToPetriNet(PetriNet pn, Place in, Place out, int prio);
}
