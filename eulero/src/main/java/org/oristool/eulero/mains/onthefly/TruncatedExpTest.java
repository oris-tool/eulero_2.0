package org.oristool.eulero.mains.onthefly;

import org.oristool.eulero.MainHelper;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.eulero.models.ModelBuilder;
import org.oristool.eulero.models.onthefly.TruncatedExpModelBuilder;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

public class TruncatedExpTest {
    public static void main(String[] args) {
        String GTPathPrefix = System.getProperty("user.dir") + "/results/Test";
        String GTCDF = "/CDF";
        String GTtimes = "/times";
        String GTPathSuffix = "/GroundTruth.txt";
        Approximator approximator = new EXPMixtureApproximation();
        StochasticTransitionFeature feature = StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);
        //BigDecimal timeLimit = BigDecimal.valueOf(35);
        BigDecimal timeTick = BigDecimal.valueOf(0.01);
        BigDecimal timeError = BigDecimal.valueOf(0.001);
        int groundTruthRuns = 20000;
        boolean save = false;
        boolean GTFromFile = false;

        ArrayList<Map<String, Double>> featureParameters = new ArrayList<>();
        featureParameters.add(Map.ofEntries(Map.entry("low", 2.0), Map.entry("upp", 3.6), Map.entry("lambda", 0.4)));
        featureParameters.add(Map.ofEntries(Map.entry("low", 1.0), Map.entry("upp", 2.3), Map.entry("lambda", 1.2)));
        featureParameters.add(Map.ofEntries(Map.entry("low", 0.9), Map.entry("upp", 1.75), Map.entry("lambda", 0.85)));
        featureParameters.add(Map.ofEntries(Map.entry("low", 0.3), Map.entry("upp", 3.1), Map.entry("lambda", 0.15)));


        System.out.println("Starting Test.");
        ModelBuilder builder = new TruncatedExpModelBuilder(featureParameters, approximator);
        BigDecimal timeLimit = builder.buildModelForSimulation().upp().add(BigDecimal.valueOf(2)); // TODO spostare dentro MainHelper.test?
        MainHelper.test("4TrExp-Distributions", builder, timeLimit, timeTick, timeError, groundTruthRuns, 171, save);
    }
}
