package org.oristool.eulero;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.oristool.eulero.graph.*;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;
import org.oristool.eulero.models.ModelBuilder;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

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

    public static ResultWrapper simulate(Activity activity, BigDecimal timeLimit, BigDecimal timeTick, int runs){
        //Simulation
        TransientSolution<DeterministicEnablingState, RewardRate> simulation = activity
                .simulate(timeLimit.toString(), timeTick.toString(), runs);

        double[] simulationCDF = new double[simulation.getSolution().length];
        for(int i = 0; i < simulationCDF.length; i++){
            simulationCDF[i] = simulation.getSolution()[i][0][0];
        }

        return new ResultWrapper(simulationCDF, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());
    }

    public static ResultWrapper analyze(Numerical activity, BigDecimal timeTick){
        return new ResultWrapper(activity.getCdf(), activity.min(), activity.max(), timeTick.doubleValue());
    }

    public static ResultWrapper analyze(DAG activity, BigDecimal timeLimit, BigDecimal timeTick, BigDecimal error){
        TransientSolution<DeterministicEnablingState, RewardRate> analysis = activity
                .analyze(timeLimit.toString(), timeTick.toString(), error.toString());

        double[] analysisCDF = new double[analysis.getSolution().length];
        for(int i = 0; i < analysisCDF.length; i++){
            analysisCDF[i] = analysis.getSolution()[i][0][0];
        }

        return new ResultWrapper(analysisCDF, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());
    }

    /*public static DAG simulationModelSetup1(){
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

    public static DAG modelSetup1(Approximator approximator, BigDecimal timeTick, BigDecimal timeLimit) {
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


       System.out.println("Comincia l'analisi del miniblocco WX");
       DAG wxBlock = p.nest(wx);
       // Get time bound from histogram supports.
       TransientSolution<DeterministicEnablingState, RewardRate> wxBlockAnalysis = wxBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
       wxBlockAnalysis.getSolution();
       double[] cdfWX = new double[wxBlockAnalysis.getSolution().length];
       for(int count = 0; count < wxBlockAnalysis.getSolution().length; count++){
           cdfWX[count] = wxBlockAnalysis.getSolution()[count][0][0];
       }
       System.out.println("Finita l'analisi del miniblocco WX");
       Numerical wxApproximation = new Numerical("wx_APPROXIMATION", timeTick, 0, cdfWX.length + 1, cdfWX, approximator);
       wxBlock.replace(wxApproximation); // sostituisce il sottodag con l'approssimante

       System.out.println("Comincia l'analisi del miniblocco UT");
       DAG tuBlock = p.nest(tu);
       // Get time bound from histogram supports.
       TransientSolution<DeterministicEnablingState, RewardRate> tuBlockAnalysis = tuBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
       tuBlockAnalysis.getSolution();
       double[] cdfTU = new double[tuBlockAnalysis.getSolution().length];
       for(int count = 0; count < tuBlockAnalysis.getSolution().length; count++){
           cdfTU[count] = tuBlockAnalysis.getSolution()[count][0][0];
       }
       System.out.println("Finita l'analisi del miniblocco UT");
       Numerical tuApproximation = new Numerical("tu_APPROXIMATION", timeTick, 0, cdfTU.length + 1, cdfTU, approximator);
       tuBlock.replace(tuApproximation); // sostituisce il sottodag con l'approssimante


       System.out.println("Cominicia l'analisi del miniblocco P");
       // Get time bound from histogram supports.
       TransientSolution<DeterministicEnablingState, RewardRate> pBlockAnalysis = p.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
       double[] cdfP = new double[pBlockAnalysis.getSolution().length];
       for(int count = 0; count < pBlockAnalysis.getSolution().length; count++){
           cdfP[count] = pBlockAnalysis.getSolution()[count][0][0];
       }
       System.out.println("Finita l'analisi del miniblocco P");
       Numerical pApproximation = new Numerical("P_APPROXIMATION", timeTick, 0, cdfP.length + 1, cdfP, approximator);

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


       System.out.println("Comincia l'analisi del miniblocco I");
       DAG iBlock = main.nest(i);
       // Get time bound from histogram supports.
       TransientSolution<DeterministicEnablingState, RewardRate> iBlockAnalysis = iBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.0001");
       iBlockAnalysis.getSolution();
       double[] cdfI = new double[iBlockAnalysis.getSolution().length];
       for(int count = 0; count < iBlockAnalysis.getSolution().length; count++){
           cdfI[count] = iBlockAnalysis.getSolution()[count][0][0];
       }
       System.out.println("Finita l'analisi del miniblocco I");
       Numerical iApproximation = new Numerical("I_APPROXIMATION", timeTick, 0, cdfI.length + 1, cdfI, approximator);
       iBlock.replace(iApproximation); // sostituisce il sottodag con l'approssimante

       System.out.println("Comincia l'analisi del miniblocco J");
       // Nesting node j
       DAG jBlock = main.nest(j);
       // Get time bound from histogram supports.
       TransientSolution<DeterministicEnablingState, RewardRate> jBlockAnalysis = jBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
       jBlockAnalysis.getSolution();
       double[] cdfJ = new double[jBlockAnalysis.getSolution().length];
       for(int count = 0; count < jBlockAnalysis.getSolution().length; count++){
           cdfJ[count] = jBlockAnalysis.getSolution()[count][0][0];
       }
       System.out.println("Finita l'analisi del miniblocco J");
       Numerical jApproximation = new Numerical("J_APPROXIMATION", timeTick, 0, cdfJ.length + 1, cdfJ, approximator);
       jBlock.replace(jApproximation);

       System.out.println("Comincia l'analisi del miniblocco K");
       // Nesting node j
       DAG kBlock = main.nest(k);
       // Get time bound from histogram supports.
       TransientSolution<DeterministicEnablingState, RewardRate> kBlockAnalysis = kBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
       kBlockAnalysis.getSolution();
       double[] cdfK = new double[kBlockAnalysis.getSolution().length];
       for(int count = 0; count < kBlockAnalysis.getSolution().length; count++){
           cdfK[count] = kBlockAnalysis.getSolution()[count][0][0];
       }
       System.out.println("Finita l'analisi del miniblocco K");
       Numerical kApproximation = new Numerical("K_APPROXIMATION", timeTick, 0, cdfK.length + 1, cdfK, approximator);
       kBlock.replace(kApproximation);

       return main;
   }

    public static Numerical modelNumericalSetup1(Approximator approximator, BigDecimal timeTick, BigDecimal timeLimit) {
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

        Numerical o = Numerical.and(List.of(
                Numerical.seq(List.of(
                        Numerical.uniform("Y", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                        Numerical.and(List.of(
                                Numerical.uniform("AP", BigDecimal.ZERO, BigDecimal.valueOf(2), timeTick),
                                Numerical.uniform("BP", BigDecimal.valueOf(2), BigDecimal.valueOf(6), timeTick)
                        ))
                )),
                Numerical.seq(List.of(
                        Numerical.uniform("X", BigDecimal.ONE, BigDecimal.valueOf(3), timeTick),
                        Numerical.and(List.of(
                                Numerical.seq(List.of(
                                        Numerical.uniform("CP1", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                                        Numerical.uniform("CP2", BigDecimal.ZERO, BigDecimal.valueOf(2), timeTick)
                                )),
                                Numerical.seq(List.of(
                                        Numerical.uniform("DP1", BigDecimal.valueOf(2), BigDecimal.valueOf(6), timeTick),
                                        Numerical.uniform("DP2", BigDecimal.valueOf(1), BigDecimal.valueOf(3), timeTick)
                                ))

                        ))
                ))
        ));

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


        System.out.println("Comincia l'analisi del miniblocco WX");
        DAG wxBlock = p.nest(wx);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> wxBlockAnalysis = wxBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        wxBlockAnalysis.getSolution();
        double[] cdfWX = new double[wxBlockAnalysis.getSolution().length];
        for(int count = 0; count < wxBlockAnalysis.getSolution().length; count++){
            cdfWX[count] = wxBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco WX");


        System.out.println("Comincia l'analisi del miniblocco UT");
        DAG tuBlock = p.nest(tu);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> tuBlockAnalysis = tuBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        tuBlockAnalysis.getSolution();
        double[] cdfTU = new double[tuBlockAnalysis.getSolution().length];
        for(int count = 0; count < tuBlockAnalysis.getSolution().length; count++){
            cdfTU[count] = tuBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco UT");


        System.out.println("Comincia l'analisi del miniblocco V");
        DAG vBlock = p.nest(v);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> vBlockAnalysis = vBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        vBlockAnalysis.getSolution();
        double[] cdfV = new double[vBlockAnalysis.getSolution().length];
        for(int count = 0; count < vBlockAnalysis.getSolution().length; count++){
            cdfV[count] = vBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco V");


        Numerical pApproximation = Numerical.and(List.of(
                new Numerical("wx_APPROXIMATION", timeTick, 0, cdfWX.length + 1, cdfWX, approximator),
                new Numerical("v_APPROXIMATION", timeTick, 0, cdfV.length + 1, cdfV, approximator),
                new Numerical("tu_APPROXIMATION", timeTick, 0, cdfTU.length + 1, cdfTU, approximator)
        ));

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


        System.out.println("Comincia l'analisi del miniblocco I");
        DAG iBlock = main.nest(i);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> iBlockAnalysis = iBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        iBlockAnalysis.getSolution();
        double[] cdfI = new double[iBlockAnalysis.getSolution().length];
        for(int count = 0; count < iBlockAnalysis.getSolution().length; count++){
            cdfI[count] = iBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco I");

        System.out.println("Comincia l'analisi del miniblocco J");
        // Nesting node j
        DAG jBlock = main.nest(j);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> jBlockAnalysis = jBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        jBlockAnalysis.getSolution();
        double[] cdfJ = new double[jBlockAnalysis.getSolution().length];
        for(int count = 0; count < jBlockAnalysis.getSolution().length; count++){
            cdfJ[count] = jBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco J");

        System.out.println("Comincia l'analisi del miniblocco K");
        // Nesting node j
        DAG kBlock = main.nest(k);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> kBlockAnalysis = kBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        kBlockAnalysis.getSolution();
        double[] cdfK = new double[kBlockAnalysis.getSolution().length];
        for(int count = 0; count < kBlockAnalysis.getSolution().length; count++){
            cdfK[count] = kBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco K");

        return Numerical.and(List.of(
                new Numerical("K_APPROXIMATION", timeTick, 0, cdfK.length + 1, cdfK, approximator),
                new Numerical("J_APPROXIMATION", timeTick, 0, cdfJ.length + 1, cdfJ, approximator),
                new Numerical("I_APPROXIMATION", timeTick, 0, cdfI.length + 1, cdfI, approximator)
        ));
    }

    public static void test1(){
        BigDecimal timeLimit = BigDecimal.valueOf(40);
        BigDecimal timeTick = BigDecimal.valueOf(0.1);
        BigDecimal error = BigDecimal.valueOf(0.001);

        int runs = 20000;

        //Simulation
        TransientSolution<DeterministicEnablingState, RewardRate> simulation = MainHelper.simulationModelSetup1()
                .simulate(timeLimit.toString(), timeTick.toString(), runs);

        System.out.println("Comincia l'analisi del main");
        DAG analysisModel = MainHelper.modelSetup1(new EXPMixtureApproximation(), timeTick, timeLimit);
        TransientSolution<DeterministicEnablingState, RewardRate> analysis = analysisModel
                .analyze(timeLimit.toString(), timeTick.toString(), error.toString());
        System.out.println("Finita l'analisi del main");

        ActivityViewer.plot(List.of("Simulation", "Analysis"), simulation, analysis);
    }

    public static void testNumerical1() {
        BigDecimal timeLimit = BigDecimal.valueOf(40);
        BigDecimal timeTick = BigDecimal.valueOf(0.1);

        int runs = 20000;



        System.out.println("Comincia l'analisi del main");
        Numerical main = MainHelper.modelNumericalSetup1(new EXPMixtureApproximation(), timeTick, timeLimit);
        System.out.println("Finita l'analisi del main");

        // MainHelper.ResultWrapper analysisResult =

        //ActivityViewer.CompareResults(List.of("Simulation", "Analysis"), simulationResult, analysisResult);
    }

    public static void checkIntermediate1() {
        Approximator approximator = new EXPMixtureApproximation();

        StochasticTransitionFeature unif0_2 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(2));

        StochasticTransitionFeature unif2_6 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.valueOf(2), BigDecimal.valueOf(6));

        StochasticTransitionFeature unif1_3 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ONE, BigDecimal.valueOf(3));

        StochasticTransitionFeature unif0_1 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);

        BigDecimal timeLimit = BigDecimal.valueOf(20);
        BigDecimal timeTick = BigDecimal.valueOf(0.1);
        int runs = 20000;

        // Model
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

        //Simulation
        TransientSolution<DeterministicEnablingState, RewardRate> simulation = p.simulate(timeLimit.toString(), timeTick.toString(), runs);
        double[] simulationCDF = new double[simulation.getSolution().length];
        for(int i = 0; i < simulationCDF.length; i++){
            simulationCDF[i] = simulation.getSolution()[i][0][0];
        }
        ResultWrapper simulationWrapper = new ResultWrapper(simulationCDF, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());


        // Numerical
        DAG wxBlock = p.nest(wx);
        TransientSolution<DeterministicEnablingState, RewardRate> wxBlockAnalysis = wxBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        wxBlockAnalysis.getSolution();
        double[] cdfWX = new double[wxBlockAnalysis.getSolution().length];
        for(int count = 0; count < wxBlockAnalysis.getSolution().length; count++){
            cdfWX[count] = wxBlockAnalysis.getSolution()[count][0][0];
        }

        DAG tuBlock = p.nest(tu);
        TransientSolution<DeterministicEnablingState, RewardRate> tuBlockAnalysis = tuBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        tuBlockAnalysis.getSolution();
        double[] cdfTU = new double[tuBlockAnalysis.getSolution().length];
        for(int count = 0; count < tuBlockAnalysis.getSolution().length; count++){
            cdfTU[count] = tuBlockAnalysis.getSolution()[count][0][0];
        }

        DAG vBlock = p.nest(v);
        TransientSolution<DeterministicEnablingState, RewardRate> vBlockAnalysis = vBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        vBlockAnalysis.getSolution();
        double[] cdfV = new double[vBlockAnalysis.getSolution().length];
        for(int count = 0; count < vBlockAnalysis.getSolution().length; count++){
            cdfV[count] = vBlockAnalysis.getSolution()[count][0][0];
        }

        Numerical wxApproximation = new Numerical("wx_APPROXIMATION", timeTick, 0, cdfWX.length + 1, cdfWX, approximator);
        Numerical vApproximation = new Numerical("v_APPROXIMATION", timeTick, 0, cdfV.length + 1, cdfV, approximator);
        Numerical tuApproximation = new Numerical("tu_APPROXIMATION", timeTick, 0, cdfTU.length + 1, cdfTU, approximator);


        Numerical numerical = Numerical.and(List.of(
                wxApproximation,
                vApproximation,
                tuApproximation
        ));

        ResultWrapper numericalWrapper = new ResultWrapper(numerical.getCdf(), 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());

        // Analysis of Approximations
        wxBlock.replace(wxApproximation);
        vBlock.replace(vApproximation);
        tuBlock.replace(tuApproximation);

        TransientSolution<DeterministicEnablingState, RewardRate> pAnalysis = p.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        pAnalysis.getSolution();
        double[] cdfP = new double[pAnalysis.getSolution().length];
        for(int count = 0; count < pAnalysis.getSolution().length; count++){
            cdfP[count] = pAnalysis.getSolution()[count][0][0];
        }
        ResultWrapper approximationWrapper = new ResultWrapper(cdfP, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());

        // Approximation of Numerical
        TransientSolution<DeterministicEnablingState, RewardRate> finalNumericalAnalysis = numerical.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        finalNumericalAnalysis.getSolution();
        double[] finalNumericalCDF = new double[finalNumericalAnalysis.getSolution().length];
        for(int count = 0; count < finalNumericalAnalysis.getSolution().length; count++){
            finalNumericalCDF[count] = finalNumericalAnalysis.getSolution()[count][0][0];
        }
        ResultWrapper approxOfNumericalWrapper = new ResultWrapper(finalNumericalCDF, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());


        // Approximation of Analysis
        Numerical approxOfAnalysis = new Numerical("p_APPROXIMATION", timeTick, 0, cdfP.length + 1, cdfP, approximator);
        TransientSolution<DeterministicEnablingState, RewardRate> finalAnalysis = approxOfAnalysis.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        finalAnalysis.getSolution();
        double[] finalCDF = new double[finalAnalysis.getSolution().length];
        for(int count = 0; count < finalAnalysis.getSolution().length; count++){
            finalCDF[count] = finalAnalysis.getSolution()[count][0][0];
        }
        ResultWrapper approxOfAnalysisWrapper = new ResultWrapper(finalCDF, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());


        //ActivityViewer.CompareResults(List.of("Simulation", "Numerical", "AnalysisOfApprox"), simulationWrapper, numericalWrapper, approximationWrapper);

        //ActivityViewer.CompareResults(List.of("Simulation", "ApproxOfNumerical", "ApproxOfAnalysisOfApprox"), simulationWrapper, approxOfNumericalWrapper, approxOfAnalysisWrapper);


    }

    public static DAG simulationModelSetup2(){
        StochasticTransitionFeature unif0_2 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(2));

        StochasticTransitionFeature unif0_6 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(6));

        StochasticTransitionFeature unif0_3 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(3));

        StochasticTransitionFeature unif0_1 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);

        Analytical a = new Analytical("A", unif0_2);
        Analytical b = new Analytical("B", unif0_6);
        Analytical c = new Analytical("C", unif0_3);
        Analytical d = new Analytical("D", unif0_1);
        Analytical f = new Analytical("F", unif0_2);

        DAG g = DAG.sequence("G",
                new Analytical("G1", unif0_6),
                new Analytical("G2", unif0_3));

        DAG h = DAG.sequence("H",
                new Analytical("H1", unif0_1),
                new Analytical("H2", unif0_2));

        Xor i = new Xor("I",
                List.of(new Analytical("IA", unif0_6),
                        new Analytical("IB", unif0_3)),
                List.of(0.3, 0.7));

        DAG j = DAG.sequence("J",
                new Analytical("J1", unif0_1),
                new Analytical("J2", unif0_2),
                new Analytical("J3", unif0_6));

        Xor k = new Xor("K", List.of(
                DAG.sequence("KA",
                        new Analytical("KA1", unif0_3),
                        new Analytical("KA2", unif0_1)),
                DAG.sequence("KB",
                        new Analytical("KB1", unif0_2),
                        new Analytical("KB2", unif0_6))),
                List.of(0.4, 0.6));

        Analytical n = new Analytical("N", unif0_3);

        DAG o = DAG.forkJoin("O",
                DAG.sequence("YAPBP",
                        new Analytical("Y", unif0_1),
                        DAG.forkJoin("APBP",
                                new Analytical("AP", unif0_2),
                                new Analytical("BP", unif0_6))),
                DAG.sequence("ZCPDP",
                        new Analytical("Z", unif0_3),
                        DAG.forkJoin("CPDP",
                                DAG.sequence("CP",
                                        new Analytical("CP1", unif0_1),
                                        new Analytical("CP2", unif0_2)),
                                DAG.sequence("DP",
                                        new Analytical("DP1", unif0_6),
                                        new Analytical("DP2", unif0_3)))));

        o.flatten();  // to remove DAG nesting

        Analytical q = new Analytical("Q", unif0_1);
        Analytical r = new Analytical("R", unif0_2);
        Analytical s = new Analytical("S", unif0_6);

        DAG t = DAG.sequence("T",
                new Analytical("T1", unif0_3),
                new Analytical("T2", unif0_1));
        Analytical u = new Analytical("U", unif0_2);
        DAG tu = DAG.forkJoin("TU", t, u);

        DAG v = DAG.sequence("V",
                new Analytical("V1", unif0_6),
                new Analytical("V2", unif0_3));

        Analytical w = new Analytical("W", unif0_1);
        DAG x = DAG.sequence("X",
                new Analytical("X1", unif0_2),
                new Analytical("X2", unif0_6));

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

    public static DAG modelSetup2(Approximator approximator, BigDecimal timeTick, BigDecimal timeLimit) {
        StochasticTransitionFeature unif0_2 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(2));

        StochasticTransitionFeature unif0_6 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(6));

        StochasticTransitionFeature unif0_3 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(3));

        StochasticTransitionFeature unif0_1 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);

        Analytical a = new Analytical("A", unif0_2);
        Analytical b = new Analytical("B", unif0_6);
        Analytical c = new Analytical("C", unif0_3);
        Analytical d = new Analytical("D", unif0_1);
        Analytical f = new Analytical("F", unif0_2);

        DAG g = DAG.sequence("G",
                new Analytical("G1", unif0_6),
                new Analytical("G2", unif0_3));

        DAG h = DAG.sequence("H",
                new Analytical("H1", unif0_1),
                new Analytical("H2", unif0_2));

        Xor i = new Xor("I",
                List.of(new Analytical("IA", unif0_6),
                        new Analytical("IB", unif0_3)),
                List.of(0.3, 0.7));

        DAG j = DAG.sequence("J",
                new Analytical("J1", unif0_1),
                new Analytical("J2", unif0_2),
                new Analytical("J3", unif0_6));

        Xor k = new Xor("K", List.of(
                DAG.sequence("KA",
                        new Analytical("KA1", unif0_3),
                        new Analytical("KA2", unif0_1)),
                DAG.sequence("KB",
                        new Analytical("KB1", unif0_2),
                        new Analytical("KB2", unif0_6))),
                List.of(0.4, 0.6));

        Analytical n = new Analytical("N", unif0_3);

        DAG o = DAG.forkJoin("O",
                DAG.sequence("YAPBP",
                        new Analytical("Y", unif0_1),
                        DAG.forkJoin("APBP",
                                new Analytical("AP", unif0_2),
                                new Analytical("BP", unif0_6))),
                DAG.sequence("ZCPDP",
                        new Analytical("Z", unif0_3),
                        DAG.forkJoin("CPDP",
                                DAG.sequence("CP",
                                        new Analytical("CP1", unif0_1),
                                        new Analytical("CP2", unif0_2)),
                                DAG.sequence("DP",
                                        new Analytical("DP1", unif0_6),
                                        new Analytical("DP2", unif0_3)))));

        o.flatten();  // to remove DAG nesting

        Analytical q = new Analytical("Q", unif0_1);
        Analytical r = new Analytical("R", unif0_2);
        Analytical s = new Analytical("S", unif0_6);

        DAG t = DAG.sequence("T",
                new Analytical("T1", unif0_3),
                new Analytical("T2", unif0_1));
        Analytical u = new Analytical("U", unif0_2);
        DAG tu = DAG.forkJoin("TU", t, u);

        DAG v = DAG.sequence("V",
                new Analytical("V1", unif0_6),
                new Analytical("V2", unif0_3));

        Analytical w = new Analytical("W", unif0_1);
        DAG x = DAG.sequence("X",
                new Analytical("X1", unif0_2),
                new Analytical("X2", unif0_6));

        DAG wx = DAG.forkJoin("WX", w, x);

        DAG p = DAG.empty("P");
        q.addPrecondition(p.begin());
        r.addPrecondition(p.begin());
        s.addPrecondition(p.begin());
        tu.addPrecondition(q, r);
        v.addPrecondition(r);
        wx.addPrecondition(r, s);
        p.end().addPrecondition(tu, v, wx);


        System.out.println("Comincia l'analisi del miniblocco WX");
        DAG wxBlock = p.nest(wx);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> wxBlockAnalysis = wxBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        wxBlockAnalysis.getSolution();
        double[] cdfWX = new double[wxBlockAnalysis.getSolution().length];
        for(int count = 0; count < wxBlockAnalysis.getSolution().length; count++){
            cdfWX[count] = wxBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco WX");
        Numerical wxApproximation = new Numerical("wx_APPROXIMATION", timeTick, 0, cdfWX.length + 1, cdfWX, approximator);
        wxBlock.replace(wxApproximation); // sostituisce il sottodag con l'approssimante

        System.out.println("Comincia l'analisi del miniblocco UT");
        DAG tuBlock = p.nest(tu);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> tuBlockAnalysis = tuBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        tuBlockAnalysis.getSolution();
        double[] cdfTU = new double[tuBlockAnalysis.getSolution().length];
        for(int count = 0; count < tuBlockAnalysis.getSolution().length; count++){
            cdfTU[count] = tuBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco UT");
        Numerical tuApproximation = new Numerical("tu_APPROXIMATION", timeTick, 0, cdfTU.length + 1, cdfTU, approximator);
        tuBlock.replace(tuApproximation); // sostituisce il sottodag con l'approssimante


        System.out.println("Cominicia l'analisi del miniblocco P");
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> pBlockAnalysis = p.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        double[] cdfP = new double[pBlockAnalysis.getSolution().length];
        for(int count = 0; count < pBlockAnalysis.getSolution().length; count++){
            cdfP[count] = pBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco P");
        Numerical pApproximation = new Numerical("P_APPROXIMATION", timeTick, 0, cdfP.length + 1, cdfP, approximator);

        Repeat e = new Repeat("E", 0.1,
                DAG.sequence("L", new Repeat("M", 0.2, pApproximation  ), n, o));

        System.out.println("Cominicia l'analisi del miniblocco E");
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> eBlockAnalysis = e.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        double[] cdfE = new double[eBlockAnalysis.getSolution().length];
        for(int count = 0; count < eBlockAnalysis.getSolution().length; count++){
            cdfE[count] = eBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco E");
        Numerical eApproximation = new Numerical("E_APPROXIMATION", timeTick, 0, cdfE.length + 1, cdfE, approximator);

        DAG main = DAG.empty("MAIN");
        a.addPrecondition(main.begin());
        b.addPrecondition(main.begin());
        c.addPrecondition(main.begin());
        d.addPrecondition(main.begin());
        eApproximation.addPrecondition(a, b);
        f.addPrecondition(b);
        g.addPrecondition(c);
        h.addPrecondition(c);
        i.addPrecondition(eApproximation, f);
        j.addPrecondition(f, g, h);
        k.addPrecondition(h, d);
        main.end().addPrecondition(i, j, k);


        System.out.println("Comincia l'analisi del miniblocco I");
        DAG iBlock = main.nest(i);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> iBlockAnalysis = iBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.0001");
        iBlockAnalysis.getSolution();
        double[] cdfI = new double[iBlockAnalysis.getSolution().length];
        for(int count = 0; count < iBlockAnalysis.getSolution().length; count++){
            cdfI[count] = iBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco I");
        Numerical iApproximation = new Numerical("I_APPROXIMATION", timeTick, 0, cdfI.length + 1, cdfI, approximator);
        iBlock.replace(iApproximation); // sostituisce il sottodag con l'approssimante

        System.out.println("Comincia l'analisi del miniblocco J");
        // Nesting node j
        DAG jBlock = main.nest(j);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> jBlockAnalysis = jBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        jBlockAnalysis.getSolution();
        double[] cdfJ = new double[jBlockAnalysis.getSolution().length];
        for(int count = 0; count < jBlockAnalysis.getSolution().length; count++){
            cdfJ[count] = jBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco J");
        Numerical jApproximation = new Numerical("J_APPROXIMATION", timeTick, 0, cdfJ.length + 1, cdfJ, approximator);
        jBlock.replace(jApproximation);

        System.out.println("Comincia l'analisi del miniblocco K");
        // Nesting node j
        DAG kBlock = main.nest(k);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> kBlockAnalysis = kBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        kBlockAnalysis.getSolution();
        double[] cdfK = new double[kBlockAnalysis.getSolution().length];
        for(int count = 0; count < kBlockAnalysis.getSolution().length; count++){
            cdfK[count] = kBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco K");
        Numerical kApproximation = new Numerical("K_APPROXIMATION", timeTick, 0, cdfK.length + 1, cdfK, approximator);
        kBlock.replace(kApproximation);

        return main;
    }

    public static Numerical modelNumericalSetup2(Approximator approximator, BigDecimal timeTick, BigDecimal timeLimit) {
        StochasticTransitionFeature unif0_2 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(2));

        StochasticTransitionFeature unif0_6 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(6));

        StochasticTransitionFeature unif0_3 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.valueOf(3));

        StochasticTransitionFeature unif0_1 =
                StochasticTransitionFeature.newUniformInstance(BigDecimal.ZERO, BigDecimal.ONE);

        Analytical a = new Analytical("A", unif0_2);
        Analytical b = new Analytical("B", unif0_6);
        Analytical c = new Analytical("C", unif0_3);
        Analytical d = new Analytical("D", unif0_1);
        Analytical f = new Analytical("F", unif0_2);

        DAG g = DAG.sequence("G",
                new Analytical("G1", unif0_6),
                new Analytical("G2", unif0_3));

        DAG h = DAG.sequence("H",
                new Analytical("H1", unif0_1),
                new Analytical("H2", unif0_2));

        Xor i = new Xor("I",
                List.of(new Analytical("IA", unif0_6),
                        new Analytical("IB", unif0_3)),
                List.of(0.3, 0.7));

        DAG j = DAG.sequence("J",
                new Analytical("J1", unif0_1),
                new Analytical("J2", unif0_2),
                new Analytical("J3", unif0_6));

        Xor k = new Xor("K", List.of(
                DAG.sequence("KA",
                        new Analytical("KA1", unif0_3),
                        new Analytical("KA2", unif0_1)),
                DAG.sequence("KB",
                        new Analytical("KB1", unif0_2),
                        new Analytical("KB2", unif0_6))),
                List.of(0.4, 0.6));

        Analytical n = new Analytical("N", unif0_3);

        Numerical o = Numerical.and(List.of(
                Numerical.seq(List.of(
                        Numerical.uniform("Y", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                        Numerical.and(List.of(
                                Numerical.uniform("AP", BigDecimal.ZERO, BigDecimal.valueOf(2), timeTick),
                                Numerical.uniform("BP", BigDecimal.valueOf(2), BigDecimal.valueOf(6), timeTick)
                        ))
                )),
                Numerical.seq(List.of(
                        Numerical.uniform("X", BigDecimal.ONE, BigDecimal.valueOf(3), timeTick),
                        Numerical.and(List.of(
                                Numerical.seq(List.of(
                                        Numerical.uniform("CP1", BigDecimal.ZERO, BigDecimal.ONE, timeTick),
                                        Numerical.uniform("CP2", BigDecimal.ZERO, BigDecimal.valueOf(2), timeTick)
                                )),
                                Numerical.seq(List.of(
                                        Numerical.uniform("DP1", BigDecimal.valueOf(2), BigDecimal.valueOf(6), timeTick),
                                        Numerical.uniform("DP2", BigDecimal.valueOf(1), BigDecimal.valueOf(3), timeTick)
                                ))

                        ))
                ))
        ));

        Analytical q = new Analytical("Q", unif0_1);
        Analytical r = new Analytical("R", unif0_2);
        Analytical s = new Analytical("S", unif0_6);

        DAG t = DAG.sequence("T",
                new Analytical("T1", unif0_3),
                new Analytical("T2", unif0_1));
        Analytical u = new Analytical("U", unif0_2);
        DAG tu = DAG.forkJoin("TU", t, u);

        DAG v = DAG.sequence("V",
                new Analytical("V1", unif0_6),
                new Analytical("V2", unif0_3));

        Analytical w = new Analytical("W", unif0_1);
        DAG x = DAG.sequence("X",
                new Analytical("X1", unif0_2),
                new Analytical("X2", unif0_6));

        DAG wx = DAG.forkJoin("WX", w, x);

        DAG p = DAG.empty("P");
        q.addPrecondition(p.begin());
        r.addPrecondition(p.begin());
        s.addPrecondition(p.begin());
        tu.addPrecondition(q, r);
        v.addPrecondition(r);
        wx.addPrecondition(r, s);
        p.end().addPrecondition(tu, v, wx);


        System.out.println("Comincia l'analisi del miniblocco WX");
        DAG wxBlock = p.nest(wx);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> wxBlockAnalysis = wxBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        wxBlockAnalysis.getSolution();
        double[] cdfWX = new double[wxBlockAnalysis.getSolution().length];
        for(int count = 0; count < wxBlockAnalysis.getSolution().length; count++){
            cdfWX[count] = wxBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco WX");


        System.out.println("Comincia l'analisi del miniblocco UT");
        DAG tuBlock = p.nest(tu);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> tuBlockAnalysis = tuBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        tuBlockAnalysis.getSolution();
        double[] cdfTU = new double[tuBlockAnalysis.getSolution().length];
        for(int count = 0; count < tuBlockAnalysis.getSolution().length; count++){
            cdfTU[count] = tuBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco UT");


        System.out.println("Comincia l'analisi del miniblocco V");
        DAG vBlock = p.nest(v);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> vBlockAnalysis = vBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        vBlockAnalysis.getSolution();
        double[] cdfV = new double[vBlockAnalysis.getSolution().length];
        for(int count = 0; count < vBlockAnalysis.getSolution().length; count++){
            cdfV[count] = vBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco V");


        Numerical pApproximation = Numerical.and(List.of(
                new Numerical("wx_APPROXIMATION", timeTick, 0, cdfWX.length + 1, cdfWX, approximator),
                new Numerical("v_APPROXIMATION", timeTick, 0, cdfV.length + 1, cdfV, approximator),
                new Numerical("tu_APPROXIMATION", timeTick, 0, cdfTU.length + 1, cdfTU, approximator)
        ));

        Repeat e = new Repeat("E", 0.1,
                DAG.sequence("L", new Repeat("M", 0.2, pApproximation ), n, o));

        System.out.println("Cominicia l'analisi del miniblocco E");
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> eBlockAnalysis = e.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        double[] cdfE = new double[eBlockAnalysis.getSolution().length];
        for(int count = 0; count < eBlockAnalysis.getSolution().length; count++){
            cdfE[count] = eBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco E");
        Numerical eApproximation = new Numerical("E_APPROXIMATION", timeTick, 0, cdfE.length + 1, cdfE, approximator);

        DAG main = DAG.empty("MAIN");
        a.addPrecondition(main.begin());
        b.addPrecondition(main.begin());
        c.addPrecondition(main.begin());
        d.addPrecondition(main.begin());
        eApproximation.addPrecondition(a, b);
        f.addPrecondition(b);
        g.addPrecondition(c);
        h.addPrecondition(c);
        i.addPrecondition(eApproximation, f);
        j.addPrecondition(f, g, h);
        k.addPrecondition(h, d);
        main.end().addPrecondition(i, j, k);


        System.out.println("Comincia l'analisi del miniblocco I");
        DAG iBlock = main.nest(i);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> iBlockAnalysis = iBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        iBlockAnalysis.getSolution();
        double[] cdfI = new double[iBlockAnalysis.getSolution().length];
        for(int count = 0; count < iBlockAnalysis.getSolution().length; count++){
            cdfI[count] = iBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco I");

        System.out.println("Comincia l'analisi del miniblocco J");
        // Nesting node j
        DAG jBlock = main.nest(j);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> jBlockAnalysis = jBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        jBlockAnalysis.getSolution();
        double[] cdfJ = new double[jBlockAnalysis.getSolution().length];
        for(int count = 0; count < jBlockAnalysis.getSolution().length; count++){
            cdfJ[count] = jBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco J");

        System.out.println("Comincia l'analisi del miniblocco K");
        // Nesting node j
        DAG kBlock = main.nest(k);
        // Get time bound from histogram supports.
        TransientSolution<DeterministicEnablingState, RewardRate> kBlockAnalysis = kBlock.analyze(timeLimit.toString(), timeTick.toString(), "0.001");
        kBlockAnalysis.getSolution();
        double[] cdfK = new double[kBlockAnalysis.getSolution().length];
        for(int count = 0; count < kBlockAnalysis.getSolution().length; count++){
            cdfK[count] = kBlockAnalysis.getSolution()[count][0][0];
        }
        System.out.println("Finita l'analisi del miniblocco K");

        return Numerical.and(List.of(
                new Numerical("K_APPROXIMATION", timeTick, 0, cdfK.length + 1, cdfK, approximator),
                new Numerical("J_APPROXIMATION", timeTick, 0, cdfJ.length + 1, cdfJ, approximator),
                new Numerical("I_APPROXIMATION", timeTick, 0, cdfI.length + 1, cdfI, approximator)
        ));
    }

    public static void test2(){
        BigDecimal timeLimit = BigDecimal.valueOf(40);
        BigDecimal timeTick = BigDecimal.valueOf(0.1);
        BigDecimal error = BigDecimal.valueOf(0.001);

        int runs = 20000;

        //Simulation
        TransientSolution<DeterministicEnablingState, RewardRate> simulation = MainHelper.simulationModelSetup2()
                .simulate(timeLimit.toString(), timeTick.toString(), runs);

        System.out.println("Comincia l'analisi del main");
        DAG analysisModel = MainHelper.modelSetup2(new EXPMixtureApproximation(), timeTick, timeLimit);
        TransientSolution<DeterministicEnablingState, RewardRate> analysis = analysisModel
                .analyze(timeLimit.toString(), timeTick.toString(), error.toString());
        System.out.println("Finita l'analisi del main");

        ActivityViewer.plot(List.of("Simulation", "Analysis"), simulation, analysis);
    }

    public static void testNumerical2() {
        BigDecimal timeLimit = BigDecimal.valueOf(40);
        BigDecimal timeTick = BigDecimal.valueOf(0.1);

        int runs = 20000;

        //Simulation
        TransientSolution<DeterministicEnablingState, RewardRate> simulation = MainHelper.simulationModelSetup2()
                .simulate(timeLimit.toString(), timeTick.toString(), runs);

        double[] simulationCDF = new double[simulation.getSolution().length];
        for(int i = 0; i < simulationCDF.length; i++){
            simulationCDF[i] = simulation.getSolution()[i][0][0];
        }

        System.out.println("Comincia l'analisi del main");
        Numerical main = MainHelper.modelNumericalSetup2(new EXPMixtureApproximation(), timeTick, timeLimit);
        System.out.println("Finita l'analisi del main");

        MainHelper.ResultWrapper simulationResult = new MainHelper.ResultWrapper(simulationCDF, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());
        MainHelper.ResultWrapper analysisResult = new MainHelper.ResultWrapper(main.getCdf(), main.min(), main.max(), timeTick.doubleValue());

        //ActivityViewer.CompareResults(List.of("Simulation", "Analysis"), simulationResult, analysisResult);
    }*/

    public static void test(String name, ModelBuilder builder, BigDecimal timeLimit, BigDecimal timeTick, BigDecimal error, int runs){
        // Simulation
        Activity simulationModel = builder.buildModelForSimulation();
        TransientSolution<DeterministicEnablingState, RewardRate> simulation = simulationModel.simulate(timeLimit.toString(), timeTick.toString(), runs);
        double[] numericalSimulation = new double[timeLimit.divide(timeTick, RoundingMode.HALF_UP).intValue()];

        for(int i = 0; i < numericalSimulation.length; i++){
            numericalSimulation[i] = simulation.getSolution()[i][0][0];
        }

        MainHelper.ResultWrapper simulationResult = new MainHelper.ResultWrapper(numericalSimulation, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());

        System.out.println("");

        // Analysis 1
        double time = System.currentTimeMillis();
        Activity analysisModel = builder.buildModelForAnalysis_Heuristic1(timeLimit, timeTick);
        double[] numericalAnalysis = new double[timeLimit.divide(timeTick, RoundingMode.HALF_UP).intValue()];

        if(analysisModel instanceof Numerical){
            for(int i = 0; i < numericalAnalysis.length; i++){
                numericalAnalysis[i] = ((Numerical) analysisModel).CDF(i);
            }
        } else {
            TransientSolution<DeterministicEnablingState, RewardRate> analysis = analysisModel.analyze(timeLimit.toString(), timeTick.toString(), error.toString());
            for(int i = 0; i < numericalAnalysis.length; i++){
                numericalAnalysis[i] = analysis.getSolution()[i][0][0];
            }
        }

        System.out.println("Analysis of " + name + " with first Heuristics took " + (System.currentTimeMillis() - time) / 1000 + " seconds");
        MainHelper.ResultWrapper analysisResult = new MainHelper.ResultWrapper(numericalAnalysis, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());

        System.out.println("");

        // Analysis 2
        time = System.currentTimeMillis();
        Activity analysisModel2 = builder.buildModelForAnalysis_Heuristic2(timeLimit, timeTick);
        double[] numericalAnalysis2 = new double[timeLimit.divide(timeTick, RoundingMode.HALF_UP).intValue()];

        if(analysisModel2 instanceof Numerical){
            for(int i = 0; i < numericalAnalysis2.length; i++){
                numericalAnalysis2[i] = ((Numerical) analysisModel2).CDF(i);
            }
        } else {
            TransientSolution<DeterministicEnablingState, RewardRate> analysis2 = analysisModel2.analyze(timeLimit.toString(), timeTick.toString(), error.toString());
            for(int i = 0; i < numericalAnalysis2.length; i++){
                numericalAnalysis2[i] = analysis2.getSolution()[i][0][0];
            }
        }

        System.out.println("Analysis of " + name + " with second Heuristics took " + (System.currentTimeMillis() - time) / 1000 + " seconds");
        MainHelper.ResultWrapper analysisResult2 = new MainHelper.ResultWrapper(numericalAnalysis2, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());

        ActivityViewer.CompareResults("Test " + name, List.of("Simulation", "Analysis1", "Analysis2"), simulationResult, analysisResult, analysisResult2);
        System.out.println("");
    }

    public static class ResultWrapper {
        private final double[] cdf;
        private final int min;
        private final int max;
        private final double step;

        public ResultWrapper(double[] cdf, int min, int max, double step){
            this.cdf = cdf;
            this.min = min;
            this.max = max;
            this.step = step;
        }

        public double[] getCdf() {
            return cdf;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        public double getStep() {
            return step;
        }

        public double[] getPdf() {
            double[] pdf = new double[cdf.length];

            pdf[0] = cdf[0] / step;

            for(int i = 1; i < pdf.length; i++){
                pdf[i] = (cdf[i] - cdf[i - 1]) / step;
            }

            return pdf;
        }

        public double jsDistance(double[] otherPDF) {
            // TODO, questa va rivista perchè deve stare tra 0 e 1
            double[] pdf = getPdf();
            if (pdf.length != otherPDF.length)
                throw new IllegalArgumentException("Should have the same number of samples");

            double result = 0.0;
            for (int t = 0; t < otherPDF.length; ++t) {
                double x = pdf[t];
                double y = otherPDF[t];
                double m = (x + y)/2.0;
                result += (klDivergence(x, m) + klDivergence(y, m)) / 2.0;
            }

            return result * step;
        }

        public double klDivergence(double px, double py) {
            if (px > 0.0 && py > 0.0) {
                return px * Math.log(px / py);
            } else if (px == 0.0 && py >= 0) {
                return 0.0;
            } else {
                return Double.POSITIVE_INFINITY;
            }
        }
    }
}
