package org.oristool.eulero.examples;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.oristool.eulero.evaluation.approximator.TruncatedExponentialApproximation;
import org.oristool.eulero.evaluation.approximator.TruncatedExponentialMixtureApproximation;
import org.oristool.eulero.evaluation.heuristics.EvaluationResult;
import org.oristool.eulero.evaluation.heuristics.SDFHeuristicsVisitor;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.HistogramTime;
import org.oristool.eulero.modeling.stochastictime.StochasticTime;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FitHistogram {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Two arguments required. First argument: CDF, Second argument: bin number.");
            return;
        }

        if (args.length > 2) {
            System.out.println("Too many arguments passed to the program. First argument: CDF, Second argument: bin number.");
            return;
        }

        String csvFile = args[0];
        Integer binNumber = Integer.parseInt(args[1]);

        ArrayList<Double> myValues = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {
                myValues.add(Double.valueOf(nextLine[0]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        myValues.sort(Comparator.comparing(Double::doubleValue));
        double q1 = getPercentile(myValues, 25);
        double q3 = getPercentile(myValues, 75);
        double iqr = q3 - q1;

        List<Double> regularValues = myValues.stream().filter(t -> t.doubleValue() >= q1 - 1.5 * iqr && t.doubleValue() <= q3 + 1.5 * iqr).collect(Collectors.toList());
        HistogramTime histogramTime = histogram(regularValues, binNumber);
        Simple a = new Simple("a", histogramTime);
        double[] cdf = a.analyze(a.upp(), a.getFairTimeTick(), new SDFHeuristicsVisitor(BigInteger.ONE, BigInteger.ONE, new TruncatedExponentialMixtureApproximation()));

        StochasticTime theApproximation = (new TruncatedExponentialApproximation()).getApproximatedStochasticTime(cdf, a.low().doubleValue(), a.upp().doubleValue(), a.getFairTimeTick());
        Simple appr = new Simple("aappr", theApproximation);
        double[] cdfAppr = appr.analyze(appr.upp(), appr.getFairTimeTick(), new SDFHeuristicsVisitor(BigInteger.ONE, BigInteger.ONE, new TruncatedExponentialMixtureApproximation()));
        System.out.println(theApproximation.toString());

        EvaluationResult e1 = new EvaluationResult("Approximation", cdfAppr, 0, cdfAppr.length, appr.getFairTimeTick().doubleValue(), 0);
        EvaluationResult e2 = new EvaluationResult("Histogram", cdf, 0, cdf.length, a.getFairTimeTick().doubleValue(), 0);

    }

    static <T extends Comparable<T>> T getPercentile(Collection<T> input, double percentile) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("The input dataset cannot be null or empty.");
        }
        if (percentile < 0 || percentile > 100) {
            throw new IllegalArgumentException("Percentile must be between 0 and 100 inclusive.");
        }
        List<T> sortedList = input.stream()
                .sorted()
                .collect(Collectors.toList());

        int rank = percentile == 0 ? 1 : (int) Math.ceil(percentile / 100.0 * input.size());
        return sortedList.get(rank - 1);
    }

    public static HistogramTime histogram(List<Double> data, int numClasses){
        double min = data.get(0);
        double max = data.get(data.size() - 1);

        int frequency[];
        int k = 0;
        double midpoint = 0;
        frequency = new int[numClasses];

        int classWidth = 0;
        classWidth = (((int)max - (int)min) + 1) / numClasses;

        for (int i = 0; i < data.size() - 1; i++){
            if ((0 <= i) && (i < numClasses)){

                k = (int)((data.get(i) - (min - 0.5)) / classWidth);
                frequency[k]++;
            }
        }

        ArrayList<Double> histogram = new ArrayList<>();

        for (int i = 0; i < numClasses; i++){
            midpoint = (int)max - 0.5 + (k + 0.5) * classWidth;
            histogram.add((double) frequency[i]);
        }

        return new HistogramTime(min, max, numClasses, histogram);

    }
}
