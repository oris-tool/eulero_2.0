package org.oristool.eulero;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.oristool.eulero.graph.*;
import org.oristool.eulero.math.approximation.HistogramApproximator;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.TransientSolutionViewer;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

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

    public static DAG simulationSetup(Map<String, HistogramDistribution> histograms, HistogramApproximator approximator){
        SimulationActivity a = new SimulationActivity("A", histograms.get("A"), approximator);
        SimulationActivity b = new SimulationActivity("B", histograms.get("B"), approximator);
        SimulationActivity c = new SimulationActivity("C", histograms.get("C"), approximator);
        SimulationActivity d = new SimulationActivity("D", histograms.get("D"), approximator);
        SimulationActivity f = new SimulationActivity("F", histograms.get("F"), approximator);

        DAG g = DAG.sequence("G",
                new SimulationActivity("G1", histograms.get("G1"), approximator),
                new SimulationActivity("G2", histograms.get("G2"), approximator));

        DAG h = DAG.sequence("H",
                new SimulationActivity("H1", histograms.get("H1"), approximator),
                new SimulationActivity("H2", histograms.get("H2"), approximator));

        Xor i = new Xor("I",
                List.of(new SimulationActivity("IA", histograms.get("IA"), approximator),
                        new SimulationActivity("IB", histograms.get("IB"), approximator)),
                List.of(0.3, 0.7));

        DAG j = DAG.sequence("J",
                new SimulationActivity("J1", histograms.get("J1"), approximator),
                new SimulationActivity("J2", histograms.get("J2"), approximator),
                new SimulationActivity("J3", histograms.get("J3"), approximator));

        Xor k = new Xor("K", List.of(
                DAG.sequence("KA",
                        new SimulationActivity("KA1", histograms.get("KA1"), approximator),
                        new SimulationActivity("KA2", histograms.get("KA1"), approximator)),
                DAG.sequence("KB",
                        new SimulationActivity("KB1", histograms.get("KB1"), approximator),
                        new SimulationActivity("KB2", histograms.get("KB2"), approximator))),
                List.of(0.4, 0.6));

        SimulationActivity n = new SimulationActivity("N", histograms.get("N"), approximator);

        DAG o = DAG.forkJoin("O",
                DAG.sequence("YAPBP",
                        new SimulationActivity("Y", histograms.get("Y"), approximator),
                        DAG.forkJoin("APBP",
                                new SimulationActivity("AP", histograms.get("AP"), approximator),
                                new SimulationActivity("BP", histograms.get("BP"), approximator))),
                DAG.sequence("ZCPDP",
                        new SimulationActivity("Z", histograms.get("Z"), approximator),
                        DAG.forkJoin("CPDP",
                                DAG.sequence("CP",
                                        new SimulationActivity("CP1", histograms.get("CP1"), approximator),
                                        new SimulationActivity("CP2", histograms.get("CP2"), approximator)),
                                DAG.sequence("DP",
                                        new SimulationActivity("DP1", histograms.get("DP1"), approximator),
                                        new SimulationActivity("DP2", histograms.get("DP2"), approximator)))));

        o.flatten();  // to remove DAG nesting

        SimulationActivity q = new SimulationActivity("Q", histograms.get("Q"), approximator);
        SimulationActivity r = new SimulationActivity("R", histograms.get("R"), approximator);
        SimulationActivity s = new SimulationActivity("S", histograms.get("S"), approximator);

        DAG t = DAG.sequence("T",
                new SimulationActivity("T1", histograms.get("T1"), approximator),
                new SimulationActivity("T2", histograms.get("T2"), approximator));
        SimulationActivity u = new SimulationActivity("U", histograms.get("U"), approximator);
        DAG tu = DAG.forkJoin("TU", t, u);

        DAG v = DAG.sequence("V",
                new SimulationActivity("V1", histograms.get("V1"), approximator),
                new SimulationActivity("V2", histograms.get("V2"), approximator));

        SimulationActivity w = new SimulationActivity("W", histograms.get("W"), approximator);
        DAG x = DAG.sequence("X",
                new SimulationActivity("X1", histograms.get("X1"), approximator),
                new SimulationActivity("X2", histograms.get("X2"), approximator));

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

        //System.out.println(main.yamlRecursive());
        //System.out.println(main.petriArcs());
        //new TransientSolutionViewer(main.analyze("10", "0.1", "0.1"));
        return main;
    }

    public static DAG analysisSetup1(Map<String, HistogramDistribution> histograms, HistogramApproximator approximator, BigDecimal timeTick){
        BigDecimal timeBound = BigDecimal.valueOf(45);
        BigDecimal error = BigDecimal.valueOf(0.001);
        // Events handled as AnalyticalHistogram
        AnalyticalHistogram a = new AnalyticalHistogram("A", histograms.get("A"), approximator);
        AnalyticalHistogram b = new AnalyticalHistogram("B", histograms.get("B"), approximator);
        AnalyticalHistogram c = new AnalyticalHistogram("C", histograms.get("C"), approximator);
        AnalyticalHistogram d = new AnalyticalHistogram("D", histograms.get("D"), approximator);
        AnalyticalHistogram f = new AnalyticalHistogram("F", histograms.get("F"), approximator);
        AnalyticalHistogram n = new AnalyticalHistogram("N", histograms.get("N"), approximator);
        AnalyticalHistogram q = new AnalyticalHistogram("Q", histograms.get("Q"), approximator);
        AnalyticalHistogram r = new AnalyticalHistogram("R", histograms.get("R"), approximator);
        AnalyticalHistogram s = new AnalyticalHistogram("S", histograms.get("S"), approximator);

        // Blocks handled numerically
        Numerical g = Numerical.seq(List.of(
                Numerical.fromHistogram("G1", histograms.get("G1"), timeTick),
                Numerical.fromHistogram("G2", histograms.get("G2"), timeTick)
        ));

        Numerical h = Numerical.seq(
                List.of(
                        Numerical.fromHistogram("H1", histograms.get("H1"), timeTick),
                        Numerical.fromHistogram("H2", histograms.get("H2"), timeTick)
                )
        );

        Numerical i = Numerical.xor(
                List.of(0.3, 0.7),
                List.of(
                        Numerical.fromHistogram("IA", histograms.get("IA"), timeTick),
                        Numerical.fromHistogram("IB", histograms.get("IB"), timeTick)
                )
        );

        Numerical j = Numerical.seq(
                List.of(
                        Numerical.fromHistogram("J1", histograms.get("J1"), timeTick),
                        Numerical.fromHistogram("J2", histograms.get("J2"), timeTick),
                        Numerical.fromHistogram("J3", histograms.get("J3"), timeTick)
                )
        );

        Numerical k = Numerical.xor(
                List.of(0.4, 0.6),
                List.of(
                        Numerical.seq(
                                List.of(
                                        Numerical.fromHistogram("KA1", histograms.get("KA1"), timeTick),
                                        Numerical.fromHistogram("KA2", histograms.get("KA2"), timeTick)
                                )
                        ),
                        Numerical.seq(
                                List.of(
                                        Numerical.fromHistogram("KB1", histograms.get("KB1"), timeTick),
                                        Numerical.fromHistogram("KB2", histograms.get("KB2"), timeTick)
                                )
                        )
                )
        );

        Numerical o = Numerical.and(
                List.of(
                        Numerical.seq(
                                List.of(
                                        Numerical.fromHistogram("Y", histograms.get("Y"), timeTick),
                                        Numerical.and(
                                                List.of(
                                                        Numerical.fromHistogram("AP", histograms.get("AP"), timeTick),
                                                        Numerical.fromHistogram("BP", histograms.get("BP"), timeTick)
                                                )
                                        )
                                )
                        ),
                        Numerical.seq(
                                List.of(
                                        Numerical.fromHistogram("Z", histograms.get("Z"), timeTick),
                                        Numerical.and(
                                                List.of(
                                                        Numerical.seq(
                                                                List.of(
                                                                        Numerical.fromHistogram("CP1", histograms.get("CP1"), timeTick),
                                                                        Numerical.fromHistogram("CP2", histograms.get("CP2"), timeTick)
                                                                )
                                                        ),
                                                        Numerical.seq(
                                                                List.of(
                                                                        Numerical.fromHistogram("DP1", histograms.get("DP1"), timeTick),
                                                                        Numerical.fromHistogram("DP2", histograms.get("DP2"), timeTick)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );

        Numerical tu = Numerical.and(
                List.of(
                        Numerical.seq(
                                List.of(
                                        Numerical.fromHistogram("T1", histograms.get("T1"), timeTick),
                                        Numerical.fromHistogram("T2", histograms.get("T2"), timeTick)
                                )
                        ),
                        Numerical.fromHistogram("U", histograms.get("U"), timeTick)
                )
        );

        Numerical v = Numerical.seq(
                List.of(
                        Numerical.fromHistogram("V1", histograms.get("V1"), timeTick),
                        Numerical.fromHistogram("V2", histograms.get("V2"), timeTick)
                )
        );

        Numerical wx = Numerical.and(
                List.of(
                        Numerical.fromHistogram("W", histograms.get("W"), timeTick),
                        Numerical.seq(
                                List.of(
                                        Numerical.fromHistogram("X1", histograms.get("X1"), timeTick),
                                        Numerical.fromHistogram("X2", histograms.get("X2"), timeTick)
                                )
                        )
                )
        );

        DAG p = DAG.empty("P");
        q.addPrecondition(p.begin());
        r.addPrecondition(p.begin());
        s.addPrecondition(p.begin());
        tu.addPrecondition(q, r);
        v.addPrecondition(r);
        wx.addPrecondition(r, s);
        p.end().addPrecondition(tu, v, wx);

        TransientSolution<DeterministicEnablingState, RewardRate> pAnalysis = p.analyze(timeBound.toString(), timeTick.toString(), error.toString());
        pAnalysis.getSolution();
        double[] cdfP = new double[pAnalysis.getSolution().length];
        for(int count = 0; count < pAnalysis.getSolution().length; count++){
            cdfP[count] = pAnalysis.getSolution()[count][0][0];
        }
        int minP = IntStream.range(0, cdfP.length).filter(index -> cdfP[index] < 0.005).max().orElse(0);
        int maxP = IntStream.range(0, cdfP.length).filter(index -> cdfP[index] > 0.995).min().orElse(cdfP.length - 1);
        double[] newCdfP = Arrays.stream(cdfP).filter(x -> x >= 0.005 && x <= 0.995).toArray();
        System.out.println("Finita l'analisi del miniblocco P");
        // Get cdf from iBlockAnalysis, and support as int
        Numerical pApproximation = new Numerical("I_APPROXIMATION", timeTick, minP, maxP, newCdfP, approximator);
        p.replace(pApproximation); // sostituisce il sottodag con l'approssimante

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

        // Nesting node i
        DAG iBlock = main.nest(i);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> iBlockAnalysis = iBlock.analyze(timeBound.toString(), timeTick.toString(), error.toString());
        iBlockAnalysis.getSolution();
        double[] cdfI = new double[iBlockAnalysis.getSolution().length];
        for(int count = 0; count < iBlockAnalysis.getSolution().length; count++){
            cdfI[count] = iBlockAnalysis.getSolution()[count][0][0];
        }
        int minI = IntStream.range(0, cdfI.length).filter(index -> cdfI[index] < 0.005).max().orElse(0);
        int maxI = IntStream.range(0, cdfI.length).filter(index -> cdfI[index] > 0.995).min().orElse(cdfI.length - 1);
        double[] newCdfI = Arrays.stream(cdfI).filter(x -> x >= 0.005 && x <= 0.995).toArray();
        System.out.println("Finita l'analisi del miniblocco I");
        // Get cdf from iBlockAnalysis, and support as int
        Numerical iApproximation = new Numerical("I_APPROXIMATION", timeTick, minI, maxI, newCdfI, approximator);
        iBlock.replace(iApproximation); // sostituisce il sottodag con l'approssimante

        // Nesting node j
        DAG jBlock = main.nest(j);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> jBlockAnalysis = jBlock.analyze(timeBound.toString(), timeTick.toString(), error.toString());
        jBlockAnalysis.getSolution();
        double[] cdfJ = new double[jBlockAnalysis.getSolution().length];
        for(int count = 0; count < jBlockAnalysis.getSolution().length; count++){
            cdfJ[count] = jBlockAnalysis.getSolution()[count][0][0];
        }
        int minJ = IntStream.range(0, cdfJ.length).filter(index -> cdfJ[index] < 0.005).max().orElse(0);
        int maxJ = IntStream.range(0, cdfJ.length).filter(index -> cdfJ[index] > 0.995).min().orElse(cdfJ.length - 1);
        double[] newCdfJ = Arrays.stream(cdfJ).filter(x -> x >= 0.005 && x <= 0.995).toArray();
        System.out.println("Finita l'analisi del miniblocco J");
        // Get cdf from iBlockAnalysis, and support as int
        Numerical jApproximation = new Numerical("J_APPROXIMATION", timeTick, minJ, maxJ, newCdfJ, approximator);
        jBlock.replace(jApproximation);

        // Analyzing subnets and recombining them

        // System.out.println(main.yamlRecursive());
        // System.out.println(main.petriArcs());
        // new TransientSolutionViewer(main.analyze("10", "0.1", "0.1"));
        return main;
    }


}
