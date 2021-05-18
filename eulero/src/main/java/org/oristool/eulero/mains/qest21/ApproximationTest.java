package org.oristool.eulero.mains.qest21;

import org.oristool.eulero.graph.Analytical;
import org.oristool.eulero.graph.DAG;
import org.oristool.eulero.graph.Numerical;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.approximation.EXPMixtureApproximation;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.IntStream;

public class ApproximationTest {
    public static void main(String[] args) {
        String savePath = System.getProperty("user.dir") + "/results/approximantAppendixResult/";
        Approximator approximator = new EXPMixtureApproximation();
        StochasticTransitionFeature feature = StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);
        BigDecimal timeLimit = BigDecimal.valueOf(8);
        BigDecimal timeTick = BigDecimal.valueOf(0.01);
        BigDecimal timeError = BigDecimal.valueOf(0.001);

        firstExample(savePath, approximator, feature, timeLimit, timeTick, timeError);
    }

    public static void firstExample(String savePath, Approximator approximator, StochasticTransitionFeature feature, BigDecimal timeLimit, BigDecimal timeTick, BigDecimal timeError){
        DAG testDAG = DAG.forkJoin("nvfnfnvfnd", DAG.sequence("dh", new Analytical("okj", feature), new Analytical("asjn", feature)), new Analytical("sjkvnsfkjv", feature));
        TransientSolution<DeterministicEnablingState, RewardRate> test = testDAG.analyze(timeLimit.toString(), timeTick.toString(), timeError.toString());
        double[] testCdf = new double[test.getSolution().length];
        for(int count = 0; count < test.getSolution().length; count++){
            testCdf[count] = test.getSolution()[count][0][0];
        }

        // stampo il delta
        int q1Index = approximator.getApproximationSupportIndices(testCdf, 0, testCdf.length).get("body").get("start").intValue();
        double q1 = testCdf[q1Index];
        double q1X = q1Index * timeTick.doubleValue();

        // stampo il Q3
        int q3Index = approximator.getApproximationSupportIndices(testCdf, 0, testCdf.length).get("tail").get("start").intValue();
        double q3 = testCdf[q3Index];
        double q3X = q3Index * timeTick.doubleValue();


        // calcolo il flesso
        double[] pdf = new double[testCdf.length];
        double[] x = new double[testCdf.length];
        for(int i = 0; i < testCdf.length - 1; i++){
            pdf[i] = (testCdf[i+1] - testCdf[i]) / timeTick.doubleValue();
            x[i] = i * timeTick.doubleValue();
        }

        double pdfMax = Arrays.stream(pdf, 0, testCdf.length).max().getAsDouble();
        int xMaxIndex = IntStream.range(0, testCdf.length)
                .filter(i ->  pdf[i] == pdfMax)
                .findFirst() // first occurrence
                .orElse(-1);
        double xMax = 0 + timeTick.doubleValue() * xMaxIndex;
        double cdfMax = testCdf[xMaxIndex];


        // calcolo l'approssimante
        Numerical numericalTest = new Numerical("Test", timeTick, 0, testCdf.length, testCdf, approximator);
        TransientSolution<DeterministicEnablingState, RewardRate> numericalTestSolution = numericalTest.analyze(timeLimit.toString(), timeTick.toString(), timeError.toString());
        double[] numericalTestCdf = new double[numericalTestSolution.getSolution().length];
        for(int count = 0; count < numericalTestSolution.getSolution().length; count++){
            numericalTestCdf[count] = numericalTestSolution.getSolution()[count][0][0];
        }

        // memorizzo tutto
        File saveFolder = new File(savePath + "/");
        if(!saveFolder.exists()){
            saveFolder.mkdirs();
        }

        StringBuilder functionString = new StringBuilder();
        for(int j = 0; j < testCdf.length; j++){
            BigDecimal value = BigDecimal.valueOf(j * timeTick.doubleValue());
            functionString.append(value.toString()).append(", ").append(testCdf[j]).append("\n");
        }

        StringBuilder apprString = new StringBuilder();
        for(int j = 0; j < numericalTestCdf.length; j++){
            BigDecimal value = BigDecimal.valueOf(j * timeTick.doubleValue());
            apprString.append(value.toString()).append(", ").append(numericalTestCdf[j]).append("\n");
        }

        StringBuilder lineString = new StringBuilder();
        for(int j = 0; j < numericalTestCdf.length; j++){
            BigDecimal value = BigDecimal.valueOf(j * timeTick.doubleValue());
            BigDecimal yValue = BigDecimal.valueOf((value.doubleValue() - q1X) * cdfMax / (xMax - q1X));
            lineString.append(value.toString()).append(", ").append(yValue.toString()).append("\n");
        }

        StringBuilder deltaString = new StringBuilder();
        deltaString.append(q1X).append(", ").append(q1);

        StringBuilder flectionString = new StringBuilder();
        flectionString.append(xMax).append(", ").append(cdfMax);

        StringBuilder q3String = new StringBuilder();
        q3String.append(q3X).append(", ").append(q3);


        try {
            FileWriter functionWriter = new FileWriter(savePath + "/function.txt");
            FileWriter apprWriter = new FileWriter(savePath + "/approximation.txt");
            FileWriter deltaWriter = new FileWriter(savePath + "/delta.txt");
            FileWriter flectionWriter = new FileWriter(savePath + "/flection.txt");
            FileWriter q3Writer = new FileWriter(savePath + "/q3.txt");
            FileWriter lineWriter = new FileWriter(savePath + "/line.txt");
            functionWriter.write(functionString.toString());
            functionWriter.close();
            apprWriter.write(apprString.toString());
            apprWriter.close();
            deltaWriter.write(deltaString.toString());
            deltaWriter.close();
            flectionWriter.write(flectionString.toString());
            flectionWriter.close();
            q3Writer.write(q3String.toString());
            q3Writer.close();
            lineWriter.write(lineString.toString());
            lineWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
