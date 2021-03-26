package org.oristool.eulero;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.oristool.eulero.graph.*;
import org.oristool.eulero.math.approximation.Approximator;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.IntStream;

public class MainHelper {
    private final static String DATASET_PATH = System.getProperty("user.dir")  + "/target/resources/samples.json";
    private final static int BINS_NUMBER = 32;
    private final static String[] HISTOGRAM_NAMES = { "A", "B", "C", "D", "F", "G1", "G2", "H1", "H2", "IA", "IB", "J1",
            "J2", "J3", "KA1", "KA2", "KB1", "KB2", "N", "Y", "AP", "BP", "Z", "CP1", "CP2", "DP1", "DP2", "Q", "R", "S",
            "T1", "T2", "U", "V1", "V2", "W", "X1", "X2" };

    // Deprecated?
    public static Map<String, HistogramDistribution> getHistogramsDistributionMap() {
        Map<String, HistogramDistribution> distributionsMap = new HashMap<>();
        ArrayList<Integer> drawnDistributions = new ArrayList<>();

        // carico roba dalla cartella
        System.out.println("Generating histograms");

        FileReader reader = null;
        try {
            reader = new FileReader(DATASET_PATH);
        } catch (FileNotFoundException e) {
            System.out.println("Samples file is not on the indicated directory...");
        }

        JSONParser jsonParser = new JSONParser();

        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) jsonParser.parse(reader);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

            // Collect 100 histograms from data
            // TODO - insert code to store drawn histogram configuration
        while (drawnDistributions.size() < HISTOGRAM_NAMES.length) {
            int drawnIndex = (int) (Math.random() * 99);

            if (!drawnDistributions.contains(Integer.valueOf(drawnIndex))) {
                drawnDistributions.add(Integer.valueOf(drawnIndex));

                double[] samples = ((ArrayList<Double>) jsonObject.get("distribution" + String.format("%03d", drawnIndex)))
                        .stream().mapToDouble(Double::doubleValue).toArray();

                double[] regularizedSamples = TukeysRegularization(samples);

                double a = Arrays.stream(regularizedSamples)
                        .min().getAsDouble();

                double maxRegularized = Arrays.stream(regularizedSamples)
                        .max().getAsDouble();

                double b = Arrays.stream(samples)
                        .filter(x -> x > maxRegularized)
                        .min().getAsDouble();

                ArrayList<BigDecimal> histogramValues = histogramGeneration(regularizedSamples, a, b, BINS_NUMBER);

                HistogramDistribution histogram = new HistogramDistribution(HISTOGRAM_NAMES[drawnDistributions.size() - 1],
                        BigDecimal.valueOf(a), BigDecimal.valueOf(b), histogramValues);

                distributionsMap.put(histogram.getName(), histogram);
            }
        }

        return distributionsMap;
    }

    // Deprecated?
    public static double[] TukeysRegularization(double[] samples){
        double[] v = new double[samples.length];
        System.arraycopy(samples, 0, v, 0, samples.length);
        Arrays.sort(v);

        int n_25 = (int) Math.round(v.length * 25 / 100);
        int n_75 = (int) Math.round(v.length * 75 / 100);

        double firstQuartile = v[n_25];
        double thirdQuartile = v[n_75];
        double IQR = thirdQuartile - firstQuartile;

        ArrayList<Double> selectedSamples = new ArrayList<>();
        for (double sample : samples) {
            if (sample >= firstQuartile - 1.5 * IQR && sample < thirdQuartile + 1.5 * IQR) {
                selectedSamples.add(sample);
            }
        }

        return selectedSamples.stream().mapToDouble(Double::doubleValue).toArray();
    }

    // Deprecated?
    public static ArrayList<BigDecimal> histogramGeneration(double[] samples, double a, double b, int bins){
        double[] histogram = new double[bins];
        ArrayList<BigDecimal> histogramAsList = new ArrayList<>();

        for (double sample : samples) {
            histogram[(int) Math.floor((sample - a) * bins / (b - a))]++;
        }

        for (int k = 0; k < bins; k++) {
            histogram[k] /= samples.length;
            histogramAsList.add(BigDecimal.valueOf(histogram[k]));
        }

        return histogramAsList;
    }

    public static DAG simulationSetup(){
        StochasticTransitionFeature unif0_2 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(2));

        StochasticTransitionFeature unif2_6 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(2), BigDecimal.valueOf(6));

        StochasticTransitionFeature unif1_3 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ONE, BigDecimal.valueOf(3));

        StochasticTransitionFeature unif0_1 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);

        Analytical a = new Analytical("A", unif0_2);
        Analytical b = new Analytical("B", unif2_6);
        Analytical c = new Analytical("C", unif1_3);
        Analytical d = new Analytical("D", unif0_1);
        Analytical f = new Analytical("F", unif0_2);

        DAG g = DAG.sequence("G",
                new Analytical("G1", unif2_6),
                new Analytical("G2", unif1_3));

        DAG h = DAG.sequence("H",
                new Analytical("H1", unif0_1),
                new Analytical("H2", unif0_2));

        Xor i = new Xor("I",
                List.of(new Analytical("IA", unif2_6),
                        new Analytical("IB", unif1_3)),
                List.of(0.3, 0.7));

        DAG j = DAG.sequence("J",
                new Analytical("J1", unif0_1),
                new Analytical("J2", unif0_2),
                new Analytical("J3", unif2_6));

        Xor k = new Xor("K", List.of(
                DAG.sequence("KA",
                        new Analytical("KA1", unif1_3),
                        new Analytical("KA2", unif0_1)),
                DAG.sequence("KB",
                        new Analytical("KB1", unif0_2),
                        new Analytical("KB2", unif2_6))),
                List.of(0.4, 0.6));

        Analytical n = new Analytical("N", unif1_3);

        DAG o = DAG.forkJoin("O",
                DAG.sequence("YAPBP",
                        new Analytical("Y", unif0_1),
                        DAG.forkJoin("APBP",
                                new Analytical("AP", unif0_2),
                                new Analytical("BP", unif2_6))),
                DAG.sequence("ZCPDP",
                        new Analytical("Z", unif1_3),
                        DAG.forkJoin("CPDP",
                                DAG.sequence("CP",
                                        new Analytical("CP1", unif0_1),
                                        new Analytical("CP2", unif0_2)),
                                DAG.sequence("DP",
                                        new Analytical("DP1", unif2_6),
                                        new Analytical("DP2", unif1_3)))));

        o.flatten();  // to remove DAG nesting

        Analytical q = new Analytical("Q", unif0_1);
        Analytical r = new Analytical("R", unif0_2);
        Analytical s = new Analytical("S", unif2_6);

        DAG t = DAG.sequence("T",
                new Analytical("T1", unif1_3),
                new Analytical("T2", unif0_1));
        Analytical u = new Analytical("U", unif0_2);
        DAG tu = DAG.forkJoin("TU", t, u);

        DAG v = DAG.sequence("V",
                new Analytical("V1", unif2_6),
                new Analytical("V2", unif1_3));

        Analytical w = new Analytical("W", unif0_1);
        DAG x = DAG.sequence("X",
                new Analytical("X1", unif0_2),
                new Analytical("X2", unif2_6));

        DAG wx = DAG.forkJoin("WX", w, x);

        DAG p = DAG.empty("P");
        q.addPrecondition(p.begin());
        r.addPrecondition(p.begin());
        s.addPrecondition(p.begin());
        tu.addPrecondition(q, r);
        v.addPrecondition(r);
        wx.addPrecondition(r, s);
        p.end().addPrecondition(tu, v, wx);

        Repeat e = new Repeat("E", 0.1,
                DAG.sequence("L", new Repeat("M", 0.2, p), n, o));

        DAG main = DAG.empty("MAIN");
        a.addPrecondition(main.begin());
        b.addPrecondition(main.begin());
        c.addPrecondition(main.begin());
        d.addPrecondition(main.begin());
        e.addPrecondition(a, b);
        f.addPrecondition(b);
        g.addPrecondition(c);
        h.addPrecondition(c);
        i.addPrecondition(e, f);
        j.addPrecondition(f, g, h);
        k.addPrecondition(h, d);
        main.end().addPrecondition(i, j, k);

        return main;
    }

   public static DAG analysisSetup1(Approximator approximator, BigDecimal timeTick) {
       StochasticTransitionFeature unif0_2 =
               StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(2));

       StochasticTransitionFeature unif2_6 =
               StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(2), BigDecimal.valueOf(6));

       StochasticTransitionFeature unif1_3 =
               StochasticTransitionFeature.newUniformInstance(BigDecimal.ONE, BigDecimal.valueOf(3));

       StochasticTransitionFeature unif0_1 =
               StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);

       Analytical a = new Analytical("A", unif0_2);
       Analytical b = new Analytical("B", unif2_6);
       Analytical c = new Analytical("C", unif1_3);
       Analytical d = new Analytical("D", unif0_1);
       Analytical f = new Analytical("F", unif0_2);

       DAG g = DAG.sequence("G",
               new Analytical("G1", unif2_6),
               new Analytical("G2", unif1_3));

       DAG h = DAG.sequence("H",
               new Analytical("H1", unif0_1),
               new Analytical("H2", unif0_2));

       Xor i = new Xor("I",
               List.of(new Analytical("IA", unif2_6),
                       new Analytical("IB", unif1_3)),
               List.of(0.3, 0.7));

       DAG j = DAG.sequence("J",
               new Analytical("J1", unif0_1),
               new Analytical("J2", unif0_2),
               new Analytical("J3", unif2_6));

       Xor k = new Xor("K", List.of(
               DAG.sequence("KA",
                       new Analytical("KA1", unif1_3),
                       new Analytical("KA2", unif0_1)),
               DAG.sequence("KB",
                       new Analytical("KB1", unif0_2),
                       new Analytical("KB2", unif2_6))),
               List.of(0.4, 0.6));

       Analytical n = new Analytical("N", unif1_3);

       DAG o = DAG.forkJoin("O",
               DAG.sequence("YAPBP",
                       new Analytical("Y", unif0_1),
                       DAG.forkJoin("APBP",
                               new Analytical("AP", unif0_2),
                               new Analytical("BP", unif2_6))),
               DAG.sequence("ZCPDP",
                       new Analytical("Z", unif1_3),
                       DAG.forkJoin("CPDP",
                               DAG.sequence("CP",
                                       new Analytical("CP1", unif0_1),
                                       new Analytical("CP2", unif0_2)),
                               DAG.sequence("DP",
                                       new Analytical("DP1", unif2_6),
                                       new Analytical("DP2", unif1_3)))));

       o.flatten();  // to remove DAG nesting

       Analytical q = new Analytical("Q", unif0_1);
       Analytical r = new Analytical("R", unif0_2);
       Analytical s = new Analytical("S", unif2_6);

       DAG t = DAG.sequence("T",
               new Analytical("T1", unif1_3),
               new Analytical("T2", unif0_1));
       Analytical u = new Analytical("U", unif0_2);
       DAG tu = DAG.forkJoin("TU", t, u);

       DAG v = DAG.sequence("V",
               new Analytical("V1", unif2_6),
               new Analytical("V2", unif1_3));

       Analytical w = new Analytical("W", unif0_1);
       DAG x = DAG.sequence("X",
               new Analytical("X1", unif0_2),
               new Analytical("X2", unif2_6));

       DAG wx = DAG.forkJoin("WX", w, x);

       DAG p = DAG.empty("P");
       q.addPrecondition(p.begin());
       r.addPrecondition(p.begin());
       s.addPrecondition(p.begin());
       tu.addPrecondition(q, r);
       v.addPrecondition(r);
       wx.addPrecondition(r, s);
       p.end().addPrecondition(tu, v, wx);

       // Get time bound from histogram supports.
       TransientSolution<DeterministicEnablingState, RewardRate> pBlockAnalysis = p.analyze("120", timeTick.toString(), "0.001");
       double[] cdfP = new double[pBlockAnalysis.getSolution().length];
       for(int count = 0; count < pBlockAnalysis.getSolution().length; count++){
           cdfP[count] = pBlockAnalysis.getSolution()[count][0][0];
       }
       int minP = IntStream.range(0, cdfP.length).filter(index -> cdfP[index] < 0.001).max().orElse(0);
       int maxP = IntStream.range(0, cdfP.length).filter(index -> cdfP[index] > 0.999).min().orElse(cdfP.length - 1);
       double[] newCdfP = Arrays.stream(cdfP).filter(value -> value >= 0.001 && value <= 0.999).toArray();
       System.out.println("Finita l'analisi del miniblocco P");
       // Get cdf from iBlockAnalysis, and support as int
       Numerical pApproximation = new Numerical("I_APPROXIMATION", timeTick, minP, maxP, newCdfP, approximator);

       Repeat e = new Repeat("E", 0.1,
               DAG.sequence("L", new Repeat("M", 0.2, pApproximation  ), n, o));

       DAG main = DAG.empty("MAIN");
       a.addPrecondition(main.begin());
       b.addPrecondition(main.begin());
       c.addPrecondition(main.begin());
       d.addPrecondition(main.begin());
       e.addPrecondition(a, b);
       f.addPrecondition(b);
       g.addPrecondition(c);
       h.addPrecondition(c);
       i.addPrecondition(e, f);
       j.addPrecondition(f, g, h);
       k.addPrecondition(h, d);
       main.end().addPrecondition(i, j, k);


       DAG iBlock = main.nest(i);
       // Get time bound from histogram supports.
       TransientSolution<DeterministicEnablingState, RewardRate> iBlockAnalysis = iBlock.analyze("120", timeTick.toString(), "0.001");
       iBlockAnalysis.getSolution();
       double[] cdfI = new double[iBlockAnalysis.getSolution().length];
       for(int count = 0; count < iBlockAnalysis.getSolution().length; count++){
           cdfI[count] = iBlockAnalysis.getSolution()[count][0][0];
       }
       int minI = IntStream.range(0, cdfI.length).filter(index -> cdfI[index] < 0.001).max().orElse(0);
       int maxI = IntStream.range(0, cdfI.length).filter(index -> cdfI[index] > 0.999).min().orElse(cdfI.length - 1);
       double[] newCdfI = Arrays.stream(cdfI).filter(value -> value >= 0.001 && value <= 0.999).toArray();
       System.out.println("Finita l'analisi del miniblocco I");
        // Get cdf from iBlockAnalysis, and support as int
       Numerical iApproximation = new Numerical("I_APPROXIMATION", timeTick, minI, maxI, newCdfI, approximator);
       iBlock.replace(iApproximation); // sostituisce il sottodag con l'approssimante

        // Nesting node j
       DAG jBlock = main.nest(j);
       // Get time bound from histogram supports.
       TransientSolution<DeterministicEnablingState, RewardRate> jBlockAnalysis = jBlock.analyze("120", timeTick.toString(), "0.001");
       jBlockAnalysis.getSolution();
       double[] cdfJ = new double[jBlockAnalysis.getSolution().length];
       for(int count = 0; count < jBlockAnalysis.getSolution().length; count++){
           cdfJ[count] = jBlockAnalysis.getSolution()[count][0][0];
       }
       int minJ = IntStream.range(0, cdfJ.length).filter(index -> cdfJ[index] < 0.001).max().orElse(0);
       int maxJ = IntStream.range(0, cdfJ.length).filter(index -> cdfJ[index] > 0.999).min().orElse(cdfJ.length - 1);
       double[] newCdfJ = Arrays.stream(cdfJ).filter(value -> value >= 0.001 && value <= 0.999).toArray();
       System.out.println("Finita l'analisi del miniblocco J");
       // Get cdf from iBlockAnalysis, and support as int
       Numerical jApproximation = new Numerical("J_APPROXIMATION", timeTick, minJ, maxJ, newCdfJ, approximator);
       jBlock.replace(jApproximation);

       return main;
   }

}
