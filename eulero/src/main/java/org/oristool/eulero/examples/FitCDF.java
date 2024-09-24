package org.oristool.eulero.examples;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.oristool.eulero.evaluation.approximator.TruncatedExponentialMixtureApproximation;
import org.oristool.eulero.evaluation.heuristics.EvaluationResult;
import org.oristool.eulero.evaluation.heuristics.SDFHeuristicsVisitor;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.StochasticTime;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

public class FitCDF {
    public static void main(String[] args) {
        /*Activity se = ModelFactory.sequence(
                ModelFactory.XOR(
                        List.of(0.5, 0.5),
                        new Simple("A", new UniformTime(0, 4)),
                        new Simple("B", new UniformTime(0, 2))
                ),
                ModelFactory.forkJoin(
                        new Simple("C", new UniformTime(2, 3)),
                        new Simple("A", new UniformTime(4, 7))
                )
        );

        double[] cdfwe = se.analyze(se.max(), se.getFairTimeTick(), new SDFHeuristicsVisitor(BigInteger.ONE, BigInteger.ONE, new TruncatedExponentialMixtureApproximation()));
        StringBuilder b = new StringBuilder();
        for(double v: cdfwe){
            b.append(v + ",\n");
        }

        System.out.println(b);*/

        if (args.length < 2) {
            System.out.println("Two arguments required. First argument: CDF, Second argument: time tick.");
            return;
        }

        if (args.length > 2) {
            System.out.println("Too many arguments passed to the program. First argument: CDF, Second argument: time tick.");
            return;
        }




        String csvFile = args[0];
        Double timeTick = Double.parseDouble(args[1]);

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


        double[] cdf = new double[myValues.size()];
        for (Double v : myValues) {
            cdf[myValues.indexOf(v)] = v;
        }

        StochasticTime theApproximation = (new TruncatedExponentialMixtureApproximation()).getApproximatedStochasticTime(cdf, 0, cdf.length * timeTick, BigDecimal.valueOf(timeTick));
        Simple appr = new Simple("aappr", theApproximation);
        double[] cdfAppr = appr.analyze(appr.upp(), appr.getFairTimeTick(), new SDFHeuristicsVisitor(BigInteger.ONE, BigInteger.ONE, new TruncatedExponentialMixtureApproximation()));
        System.out.println(theApproximation.toString());

        EvaluationResult e1 = new EvaluationResult("Approximation", cdfAppr, 0, cdfAppr.length, appr.getFairTimeTick().doubleValue(), 0);
        EvaluationResult e2 = new EvaluationResult("CDF", cdf, 0, cdf.length, timeTick, 0);
    }
}
