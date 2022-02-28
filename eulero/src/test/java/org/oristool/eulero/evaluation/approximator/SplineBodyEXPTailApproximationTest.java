package org.oristool.eulero.evaluation.approximator;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.oristool.eulero.graph.Numerical;
import org.oristool.eulero.ui.ActivityViewer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class SplineBodyEXPTailApproximationTest {
    public static void main(String[] args) {
        NormalDistribution normalDistribution = new NormalDistribution(5, 1.4);

        ArrayList<Double> cdfList = new ArrayList<>();

        for(int i = 0; i < 900; i++){
            cdfList.add(normalDistribution.cumulativeProbability(0.01*i));
        }

        double[] cdf = new double[cdfList.size()];
        for(int i =0; i< cdfList.size(); i++){
            cdf[i] = cdfList.get(i);
        }

        int a = IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.001)
                .findFirst()
                .orElse(0);

        int b = IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.999)
                .findFirst()
                .orElse(cdf.length-1);


        Numerical test = new Numerical("Test", BigDecimal.valueOf(0.01), a, b, Arrays.copyOfRange(cdf, a, b));
        test.setApproximator(new SplineBodyEXPTailApproximation(3));

        ActivityViewer.plot("Test", List.of("io"), test.analyze("10", "0.01", "0.01"));
    }
}
