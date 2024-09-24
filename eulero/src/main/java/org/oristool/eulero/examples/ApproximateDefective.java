package org.oristool.eulero.examples;

import org.oristool.eulero.evaluation.approximator.TruncatedExponentialMixtureApproximation;
import org.oristool.eulero.modeling.stochastictime.StochasticTime;
import org.oristool.models.pn.PostUpdater;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trans.TreeTransient;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.*;

import java.math.BigDecimal;

public class ApproximateDefective {
    public static void main(String[] args) {
        PetriNet pn = new PetriNet();
        Marking m = new Marking();

        modello(pn, m);
        double[] defectiveCDF = forwardTransientAnalysis(pn, m, BigDecimal.valueOf(2), BigDecimal.valueOf(0.01));

        StochasticTime approximation = (new TruncatedExponentialMixtureApproximation()).getApproximatedStochasticTime(defectiveCDF, 0, 2, BigDecimal.valueOf(0.01));
        double[] defectiveApproximation = approximation.getNumericalPDF(0.01, 2);
        Double area = 0.;
        for(double v: defectiveApproximation){
           area += v * 0.01;
        }

        int test = 0;



    }

    private static void modello(PetriNet net, Marking marking){
        Place Finish = net.addPlace("Finish");
        Place p0 = net.addPlace("p0");
        Place p1 = net.addPlace("p1");
        Place p10 = net.addPlace("p10");
        Place p11 = net.addPlace("p11");
        Place p12 = net.addPlace("p12");
        Place p13 = net.addPlace("p13");
        Place p2 = net.addPlace("p2");
        Place p3 = net.addPlace("p3");
        Place p5 = net.addPlace("p5");
        Place p9 = net.addPlace("p9");
        Transition t0 = net.addTransition("t0");
        Transition t1 = net.addTransition("t1");
        Transition t10 = net.addTransition("t10");
        Transition t13 = net.addTransition("t13");
        Transition t14 = net.addTransition("t14");
        Transition t15 = net.addTransition("t15");
        Transition t3 = net.addTransition("t3");
        Transition t4 = net.addTransition("t4");
        Transition t5 = net.addTransition("t5");
        Transition t8 = net.addTransition("t8");

        //Generating Connectors
        net.addPostcondition(t4, p1);
        net.addPrecondition(p13, t3);
        net.addPostcondition(t3, Finish);
        net.addPrecondition(p1, t1);
        net.addPostcondition(t4, p9);
        net.addPrecondition(p10, t14);
        net.addPostcondition(t14, p13);
        net.addPrecondition(p10, t10);
        net.addPostcondition(t0, p2);
        net.addPrecondition(p0, t0);
        net.addPrecondition(p5, t4);
        net.addPostcondition(t4, p0);
        net.addPrecondition(p2, t15);
        net.addPrecondition(p9, t8);
        net.addPrecondition(p3, t13);
        net.addPrecondition(p3, t5);
        net.addPostcondition(t1, p3);
        net.addPostcondition(t15, p11);
        net.addPrecondition(p12, t3);
        net.addPostcondition(t13, p12);
        net.addPrecondition(p11, t3);
        net.addPostcondition(t8, p10);

        //Generating Properties
        marking.setTokens(Finish, 0);
        marking.setTokens(p0, 0);
        marking.setTokens(p1, 0);
        marking.setTokens(p10, 0);
        marking.setTokens(p11, 0);
        marking.setTokens(p12, 0);
        marking.setTokens(p13, 0);
        marking.setTokens(p2, 0);
        marking.setTokens(p3, 0);
        marking.setTokens(p5, 1);
        marking.setTokens(p9, 0);
        t0.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("0"), new BigDecimal("2")));
        t1.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("0"), new BigDecimal("2")));
        t10.addFeature(new EnablingFunction("p11==0||p12==0"));
        t10.addFeature(new PostUpdater("p0=0;p2=0;p11=0;p1=0;p3=0;p12=0", net));
        t10.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
        t10.addFeature(new Priority(0));
        t13.addFeature(new EnablingFunction("p11==1"));
        t13.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
        t13.addFeature(new Priority(0));
        t14.addFeature(new EnablingFunction("p11==1&&p12==1"));
        t14.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
        t14.addFeature(new Priority(0));
        t15.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
        t15.addFeature(new Priority(0));
        t3.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
        t3.addFeature(new Priority(0));
        t4.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
        t4.addFeature(new Priority(0));
        t5.addFeature(new EnablingFunction("p11==0"));
        t5.addFeature(new PostUpdater("p0=0;p2=0;p9=0", net));
        t5.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
        t5.addFeature(new Priority(0));
        t8.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("0"), new BigDecimal("2")));
    }

    public static double[] forwardTransientAnalysis(PetriNet pn, Marking m, BigDecimal timeLimit, BigDecimal step){
        TreeTransient.Builder builder = TreeTransient.builder();
        builder.timeBound(timeLimit);
        builder.timeStep(step);
        builder.greedyPolicy(timeLimit, BigDecimal.valueOf(0.001));
        builder.markingFilter(MarkingCondition.fromString("Finish"));

        TreeTransient analysis = builder.build();
        long start = System.nanoTime();
        TransientSolution<Marking, Marking> probs =
                analysis.compute(pn, m);
    /*System.out.println(String.format("Analysis took %.3f seconds",
            (System.nanoTime() - start)/1e9));*/

        // evaluate reward
        TransientSolution<Marking, RewardRate> transientSolution = TransientSolution.computeRewards(false, probs,
                RewardRate.fromString("Finish"));

        double[] CDF = new double[transientSolution.getSolution().length];

        for(int i = 0; i < CDF.length; i++){
            CDF[i] = transientSolution.getSolution()[i][0][0];
        }

        return CDF;
    }
}
