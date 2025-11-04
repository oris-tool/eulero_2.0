package org.oristool.eulero.examples.Studente;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.oristool.eulero.evaluation.approximator.LowerBoundTruncatedExponentialMixtureApproximation;
import org.oristool.eulero.evaluation.heuristics.EvaluationResult;
import org.oristool.eulero.evaluation.heuristics.RBFHeuristicsVisitor;
import org.oristool.eulero.modeling.ModelFactory;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.StochasticTime;
import org.oristool.eulero.modeling.stochastictime.UniformTime;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;


public class Fitter {

    String baseDir;

    private double[] startupTime;
    private double[] startupTimeCdf;
    private double[] agedTime;
    private double[] agedTimeCdf;
    private double[] faultTime;
    private double[] faultTimeCdf;

    public Fitter(String baseDir) {
        this.baseDir = baseDir;
        String startupFile = this.baseDir + "/startup_time_raw.csv";
        String startupFileCdf = this.baseDir + "/startup_time_cdf.csv";
        String agedFile = baseDir + "/aged_time_raw.csv";
        String agedFileCdf = baseDir + "/aged_time_cdf.csv";
        String faultFile = baseDir + "/fault_time_raw.csv";
        String faultFileCdf = baseDir + "/fault_time_cdf.csv";

        startupTime = parseDoubleArrayFromCsv(startupFile, false);
        startupTimeCdf = parseDoubleArrayFromCsv(startupFileCdf, true);
        agedTime = parseDoubleArrayFromCsv(agedFile, false);
        agedTimeCdf = parseDoubleArrayFromCsv(agedFileCdf, true);
        faultTime = parseDoubleArrayFromCsv(faultFile, false);
        faultTimeCdf = parseDoubleArrayFromCsv(faultFileCdf, true);

    }

    public double[] parseDoubleArrayFromCsv(String csvPath, Boolean insertFirstZero) {
        List<Double> valuesToParse = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(csvPath))) {
            String[] nextLine;
            boolean firstLine = true; // Per saltare l'intestazione

            while ((nextLine = reader.readNext()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Salta la riga di intestazione
                }
                valuesToParse.add(Double.parseDouble(nextLine[0]));

            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        // if (insertFirstZero) {
        // valuesToParse.add(0, 0.0);
        // }
        // Converti in array di double
        return valuesToParse.stream().mapToDouble(Double::doubleValue).toArray();

    }

    public static void main(String[] args) {

        Fitter fitter = new Fitter("/Users/riccardoreali/eulero_2.0/eulero/src/main/java/org/oristool/eulero/examples/Studente");

        // System.out.println("Startup Time: " +
        // java.util.Arrays.toString(fitter.startupTime));
        // System.out.println("aged Time: " +
        // java.util.Arrays.toString(fitter.agedTime));
        // System.out.println("Fault Time: " +
        // java.util.Arrays.toString(fitter.faultTime));

        Activity test = ModelFactory.sequence(
                new Simple("a", new UniformTime(1, 4)),
                new Simple("b", new UniformTime(2, 3)),
                new Simple("c", new UniformTime(4, 5)));

        List<EvaluationResult> results = new ArrayList<>();
        TransientSolution<DeterministicEnablingState, RewardRate> simulation = test.simulate(test.max().toString(),
                test.getFairTimeTick().toString(), 100000);
        double[] simul = new double[simulation.getSolution().length];
        for (int i = 0; i < simul.length; i++) {
            simul[i] = simulation.getSolution()[i][0][0];
        }
        results.add(new EvaluationResult("Simul", simul, 0, simul.length,
                test.getFairTimeTick().doubleValue(), 0));

        double[] appr = test.analyze(test.max(), test.getFairTimeTick(), new RBFHeuristicsVisitor(BigInteger.ONE,
                BigInteger.ONE, new LowerBoundTruncatedExponentialMixtureApproximation()));

        StochasticTime stochasticTimeTest = new LowerBoundTruncatedExponentialMixtureApproximation()
                .getApproximatedStochasticTime(appr, test.low().doubleValue(),
                        test.upp().doubleValue(),
                        test.getFairTimeTick());

        System.out.println(stochasticTimeTest);

        System.out.println("cdf: " + java.util.Arrays.toString(appr));
        System.out.println("cdf lenght: " + appr.length);
        System.out.println("low: " + test.low().doubleValue());
        System.out.println("up: " + test.upp().doubleValue());
        System.out.println("tick time: " + test.getFairTimeTick());

        double min = Arrays.stream(fitter.agedTime).min().orElseThrow();
        double max = Arrays.stream(fitter.agedTime).max().orElseThrow();

        System.out.println("cdf: " +
                java.util.Arrays.toString(fitter.agedTimeCdf));
        System.out.println("cdf lenght: " + fitter.agedTimeCdf.length);
        System.out.println("low: " + min);
        System.out.println("max: " + max);

        StochasticTime stochasticTime = new LowerBoundTruncatedExponentialMixtureApproximation()
                .getApproximatedStochasticTime(fitter.agedTimeCdf, min, max,
                        BigDecimal.valueOf(1.0));

        System.out.println(stochasticTime);

        Simple approximation = new Simple("we", stochasticTime);
        double[] appr0 = approximation.analyze(BigDecimal.valueOf(max), BigDecimal.valueOf(1), new RBFHeuristicsVisitor(
                BigInteger.ONE, BigInteger.ONE, new LowerBoundTruncatedExponentialMixtureApproximation()));

        System.out.println("cdf appr: " + java.util.Arrays.toString(appr0));
    }
}