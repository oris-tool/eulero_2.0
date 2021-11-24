package org.oristool.eulero.mains.onthefly;

import org.oristool.eulero.MainHelper;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.eulero.models.ModelBuilder_Deprecated;
import org.oristool.eulero.models.onthefly.DifferentUniformModelBuilderDeprecated;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

public class DifferentUniformTest {
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
        int groundTruthRuns = 1000000;
        boolean save = false;
        boolean GTFromFile = false;

        ArrayList<Map<String, Double>> featureParameters = new ArrayList<>();
        featureParameters.add(Map.ofEntries(Map.entry("low", 4.0), Map.entry("upp", 6.0)));
        featureParameters.add(Map.ofEntries(Map.entry("low", 1.0), Map.entry("upp", 2.3)));
        featureParameters.add(Map.ofEntries(Map.entry("low", 1.5), Map.entry("upp", 3.1)));
        featureParameters.add(Map.ofEntries(Map.entry("low", 0.9), Map.entry("upp", 1.15)));

        System.out.println("Starting Test.");
        ModelBuilder_Deprecated builder = new DifferentUniformModelBuilderDeprecated(featureParameters, approximator);
        BigDecimal timeLimit = builder.buildModelForSimulation().upp().add(BigDecimal.valueOf(2)); // TODO spostare dentro MainHelper.test?
        MainHelper.test("4Uniform-Distributions", builder, timeLimit, timeTick, timeError, groundTruthRuns, 171, save);
    }
}
