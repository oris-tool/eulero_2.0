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

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class MainHelper {
    private final static String SAVE_PATH = System.getProperty("user.dir") + "/results";
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

    // Deprecated?
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

    // Deprecated?
    public static ResultWrapper analyze(Numerical activity, BigDecimal timeTick){
        return new ResultWrapper(activity.getCdf(), activity.min(), activity.max(), timeTick.doubleValue());
    }

    // Deprecated?
    public static ResultWrapper analyze(DAG activity, BigDecimal timeLimit, BigDecimal timeTick, BigDecimal error){
        TransientSolution<DeterministicEnablingState, RewardRate> analysis = activity
                .analyze(timeLimit.toString(), timeTick.toString(), error.toString());

        double[] analysisCDF = new double[analysis.getSolution().length];
        for(int i = 0; i < analysisCDF.length; i++){
            analysisCDF[i] = analysis.getSolution()[i][0][0];
        }

        return new ResultWrapper(analysisCDF, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());
    }

    // TODO si può fare meglio e più componibile
    public static void test_OLD(String name, ModelBuilder builder, BigDecimal timeLimit, BigDecimal timeTick, BigDecimal error, int runs){
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

        ActivityViewer.CompareResults(SAVE_PATH, false, "Test " + name, List.of("Simulation", "Heuristic1", "Heuristic2"), simulationResult, analysisResult, analysisResult2);
        System.out.println("");
    }

    public static void test(String name, ModelBuilder builder, BigDecimal timeLimit, BigDecimal timeTick, BigDecimal error, int groundTruthRuns, int runs, boolean save){
        ArrayList<Double> computationTimes = new ArrayList<>();

        // Ground Truth
        System.out.println("GROUND_TRUTH\n");
        double time = System.currentTimeMillis();
        Activity simulationModel = builder.buildModelForSimulation();
        TransientSolution<DeterministicEnablingState, RewardRate> groundTruth = simulationModel.simulate(timeLimit.toString(), timeTick.toString(), groundTruthRuns);
        double[] numericalGroundTruth = new double[timeLimit.divide(timeTick, RoundingMode.HALF_UP).intValue()];

        for(int i = 0; i < numericalGroundTruth.length; i++){
            numericalGroundTruth[i] = groundTruth.getSolution()[i][0][0];
        }
        computationTimes.add(System.currentTimeMillis() - time);
        MainHelper.ResultWrapper groundTruthResult = new MainHelper.ResultWrapper(numericalGroundTruth, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());
        System.out.println("");

        // Simulation Test
        System.out.println("SHORT_SIMULATION\n");

        time = System.currentTimeMillis();
        TransientSolution<DeterministicEnablingState, RewardRate> shortSimulation = simulationModel.simulate(timeLimit.toString(), timeTick.toString(), runs);
        double[] numericalShortSimulation = new double[timeLimit.divide(timeTick, RoundingMode.HALF_UP).intValue()];

        for(int i = 0; i < numericalShortSimulation.length; i++){
            numericalShortSimulation[i] = shortSimulation.getSolution()[i][0][0];
        }
        computationTimes.add(System.currentTimeMillis() - time);
        MainHelper.ResultWrapper shortTimeResult = new MainHelper.ResultWrapper(numericalShortSimulation, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());
        System.out.println("");

        // Analysis
        time = System.currentTimeMillis();
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
        computationTimes.add(System.currentTimeMillis() - time);
        System.out.println("Analysis of " + name + " with first Heuristics took " + (System.currentTimeMillis() - time) / 1000 + " seconds");
        MainHelper.ResultWrapper analysisResult = new MainHelper.ResultWrapper(numericalAnalysis, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());

        System.out.println("");

        // Analysis 2
        time = System.currentTimeMillis();
        Activity analysisModel2 = builder.buildModelForAnalysis_Heuristic2(timeLimit, timeTick);
        double[] numericalAnalysis2 = new double[timeLimit.divide(timeTick, RoundingMode.HALF_UP).intValue()];

        if(!name.equals("7") && !name.equals("8")){
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
            computationTimes.add(System.currentTimeMillis() - time);
            System.out.println("Analysis of " + name + " with second Heuristics took " + (System.currentTimeMillis() - time) / 1000 + " seconds");
        } else {
            System.out.println("troppo difficile per noi");
            computationTimes.add(0.0);
        }

        MainHelper.ResultWrapper analysisResult2 = new MainHelper.ResultWrapper(numericalAnalysis2, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());


        System.out.println("");

        // Analysis 3
        time = System.currentTimeMillis();
        Activity analysisModel3 = builder.buildModelForAnalysis_Heuristic3(timeLimit, timeTick);
        double[] numericalAnalysis3 = new double[timeLimit.divide(timeTick, RoundingMode.HALF_UP).intValue()];

        if(analysisModel3 instanceof Numerical){
            for(int i = 0; i < numericalAnalysis3.length; i++){
                numericalAnalysis3[i] = ((Numerical) analysisModel3).CDF(i);
            }
        } else {
            TransientSolution<DeterministicEnablingState, RewardRate> analysis3 = analysisModel3.analyze(timeLimit.toString(), timeTick.toString(), error.toString());
            for(int i = 0; i < numericalAnalysis3.length; i++){
                numericalAnalysis3[i] = analysis3.getSolution()[i][0][0];
            }
        }
        computationTimes.add(System.currentTimeMillis() - time);
        System.out.println("Analysis of " + name + " with third Heuristics took " + (System.currentTimeMillis() - time) / 1000 + " seconds");
        MainHelper.ResultWrapper analysisResult3 = new MainHelper.ResultWrapper(numericalAnalysis3, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());

        try{
            ActivityViewer.CompareResults(SAVE_PATH, false, "Test " + name, List.of("GT", "Simulation", "Heuristic1", "Heuristic2", "Heuristic3"), groundTruthResult, shortTimeResult, analysisResult, analysisResult2, analysisResult3);
        } catch (Exception e){
            System.out.println(e);
            System.out.println("Impossible to plot images...");
        }

        if(save){
            storeResults(SAVE_PATH, "Test" + name, List.of("GroundTruth", "Simulation", "Heuristic1", "Heuristic2", "Heuristic3"), computationTimes, groundTruthResult, shortTimeResult, analysisResult, analysisResult2, analysisResult3);
        }
        System.out.println("");
    }

    public static void test(String name, ModelBuilder builder, BigDecimal timeLimit, BigDecimal timeTick, BigDecimal error, String groundTruthPath, String groundTruthTimePath, int runs, boolean save){
        ArrayList<Double> computationTimes = new ArrayList<>();

        // Ground Truth
        System.out.println("Loading Ground truth simulation\n");
        FileReader f = null;
        try {
            f = new FileReader(groundTruthPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BufferedReader b = new BufferedReader(f);
        ArrayList<Double> extractedValues = new ArrayList<>();
        boolean stringRead = false;
        while(!stringRead){
            try {
                String groundTruthString = b.readLine();
                extractedValues.add(Double.valueOf(groundTruthString.split(", ")[1]));
            } catch (Exception e) {
                System.out.println("String Read!!");
                stringRead = true;
            }
        }

        double[] numericalGroundTruth = new double[extractedValues.size()];
        for(int i = 0; i < numericalGroundTruth.length; i++){
            numericalGroundTruth[i] = extractedValues.get(i).doubleValue();
        }

        FileReader fComputationTimes = null;
        try {
            fComputationTimes = new FileReader(groundTruthTimePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader bComputationTimes = new BufferedReader(fComputationTimes);
        String gtTime = null;
        try {
            gtTime = bComputationTimes.readLine();
        } catch (Exception e) {
            System.out.println("String Read!!");
            stringRead = true;
        }

        computationTimes.add(Double.valueOf(gtTime.split("s")[0]).doubleValue() * 1000);
        ResultWrapper groundTruthResult = new ResultWrapper(numericalGroundTruth, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());

        // Simulation Test
        System.out.println("SHORT_SIMULATION\n");

        double time = System.currentTimeMillis();
        Activity simulationModel = builder.buildModelForSimulation();
        TransientSolution<DeterministicEnablingState, RewardRate> shortSimulation = simulationModel.simulate(timeLimit.toString(), timeTick.toString(), runs);
        double[] numericalShortSimulation = new double[timeLimit.divide(timeTick, RoundingMode.HALF_UP).intValue()];

        for(int i = 0; i < numericalShortSimulation.length; i++){
            numericalShortSimulation[i] = shortSimulation.getSolution()[i][0][0];
        }
        computationTimes.add(System.currentTimeMillis() - time);
        ResultWrapper shortTimeResult = new ResultWrapper(numericalShortSimulation, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());
        System.out.println("");

        // Analysis
        time = System.currentTimeMillis();
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
        computationTimes.add(System.currentTimeMillis() - time);
        System.out.println("Analysis of " + name + " with first Heuristics took " + (System.currentTimeMillis() - time) / 1000 + " seconds");
        ResultWrapper analysisResult = new ResultWrapper(numericalAnalysis, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());

        System.out.println("");

        // Analysis 2
        time = System.currentTimeMillis();
        Activity analysisModel2 = builder.buildModelForAnalysis_Heuristic2(timeLimit, timeTick);
        double[] numericalAnalysis2 = new double[timeLimit.divide(timeTick, RoundingMode.HALF_UP).intValue()];

        if(!name.equals("7") && !name.equals("8")){
            if(analysisModel2 instanceof Numerical){
                for(int i = 0; i < numericalAnalysis2.length; i++){
                    numericalAnalysis2[i] = ((Numerical) analysisModel2).CDF(i);
                }
            } else {
                TransientSolution<DeterministicEnablingState, RewardRate> analysis2 = analysisModel2.analyze(timeLimit.toString(), timeTick.toString(), error.toString());
                for (int i = 0; i < numericalAnalysis2.length; i++) {
                    numericalAnalysis2[i] = analysis2.getSolution()[i][0][0];
                }
            }
            computationTimes.add(System.currentTimeMillis() - time);

        } else {
            System.out.println("No Maria io esco..");
            System.out.println("troppo difficile per noi");
            computationTimes.add(0.0);
        }
        System.out.println("Analysis of " + name + " with second Heuristics took " + (System.currentTimeMillis() - time) / 1000 + " seconds");
        ResultWrapper analysisResult2 = new ResultWrapper(numericalAnalysis2, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());

        // Analysis 3
        time = System.currentTimeMillis();
        Activity analysisModel3 = builder.buildModelForAnalysis_Heuristic3(timeLimit, timeTick);
        double[] numericalAnalysis3 = new double[timeLimit.divide(timeTick, RoundingMode.HALF_UP).intValue()];

        if(analysisModel3 instanceof Numerical){
            for(int i = 0; i < numericalAnalysis3.length; i++){
                numericalAnalysis3[i] = ((Numerical) analysisModel3).CDF(i);
            }
        } else {
            TransientSolution<DeterministicEnablingState, RewardRate> analysis3 = analysisModel3.analyze(timeLimit.toString(), timeTick.toString(), error.toString());
            for(int i = 0; i < numericalAnalysis3.length; i++){
                numericalAnalysis3[i] = analysis3.getSolution()[i][0][0];
            }
        }
        computationTimes.add(System.currentTimeMillis() - time);
        System.out.println("Analysis of " + name + " with third Heuristics took " + (System.currentTimeMillis() - time) / 1000 + " seconds");
        ResultWrapper analysisResult3 = new ResultWrapper(numericalAnalysis3, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());

        try{
            ActivityViewer.CompareResults(SAVE_PATH, false, "Test " + name, List.of("GT", "Simulation", "Heuristic1", "Heuristic2", "Heuristic3"), groundTruthResult, shortTimeResult, analysisResult, analysisResult2, analysisResult3);
        } catch (Exception e){
            System.out.println(e);
            System.out.println("Impossible to plot images...");
        }

        if(save){
            storeResults(SAVE_PATH, "Test" + name, List.of("GroundTruth", "Simulation", "Heuristic1", "Heuristic2", "Heuristic3"), computationTimes, groundTruthResult, shortTimeResult, analysisResult, analysisResult2, analysisResult3);
        }
        System.out.println("");
    }

    public static void storeResults(String savePath, String testTitle, List<String> stringList, List<Double> computationTimes, ResultWrapper... results){
        File pdfFile = new File(savePath + "/" + testTitle + "/PDF/");
        if(!pdfFile.exists()){
            pdfFile.mkdirs();
        }

        File cdfFile = new File(savePath + "/" + testTitle + "/CDF/");
        if(!cdfFile.exists()){
            cdfFile.mkdirs();
        }

        File timesFile = new File(savePath + "/" + testTitle + "/times/");
        if(!timesFile.exists()){
            timesFile.mkdirs();
        }

        File jsFile = new File(savePath + "/" + testTitle + "/jensenShannon/");
        if(!jsFile.exists()){
            jsFile.mkdirs();
        }

        double[] groundTruth = results[0].getPdf();

        for (int i = 0; i < results.length; i++) {
            // Handle cdf
            double[] cdf = results[i].getCdf();
            StringBuilder cdfString = new StringBuilder();
            for(int j = 0; j < cdf.length; j++){
                BigDecimal x = BigDecimal.valueOf((results[i].getMin() + j) * results[i].step)
                        .setScale(BigDecimal.valueOf(results[i].step).scale(), RoundingMode.HALF_DOWN);
                cdfString.append(x.toString()).append(", ").append(cdf[j]).append("\n");
            }

            //handle pdf
            double[] pdf = results[i].getPdf();
            StringBuilder pdfString = new StringBuilder();
            for(int j = 0; j < cdf.length; j++){
                BigDecimal x = BigDecimal.valueOf((results[i].getMin() + j) * results[i].step)
                        .setScale(BigDecimal.valueOf(results[i].step).scale(), RoundingMode.HALF_DOWN);
                pdfString.append(x.toString()).append(", ").append(pdf[j]).append("\n");
            }

            double time = computationTimes.get(i) / 1000;
            double js = results[i].jsDistance(groundTruth);

            try {
                FileWriter cdfWriter = new FileWriter(savePath + "/" + testTitle + "/CDF/" + stringList.get(i) + ".txt");
                FileWriter pdfWriter = new FileWriter(savePath + "/" + testTitle + "/PDF/" + stringList.get(i) + ".txt");
                FileWriter timeWriter = new FileWriter(savePath + "/" + testTitle + "/times/" + stringList.get(i) + ".txt");
                FileWriter jsWriter = new FileWriter(savePath + "/" + testTitle + "/jensenShannon/" + stringList.get(i) + ".txt");

                if((!testTitle.contains("7") && !testTitle.contains("8")) || i != 3){
                    timeWriter.write(time + "s");
                    timeWriter.close();
                    jsWriter.write(String.valueOf(js));
                    jsWriter.close();
                } else {
                    timeWriter.write("n.a.");
                    timeWriter.close();
                    jsWriter.write("n.a.");
                    jsWriter.close();
                }

                cdfWriter.write(cdfString.toString());
                cdfWriter.close();
                pdfWriter.write(pdfString.toString());
                pdfWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }

    }

    public static String simulationSensitivityAnalysis(String name, ModelBuilder builder, BigDecimal timeLimit, BigDecimal timeTick, int runs, int runStep){
        int previousRun = runStep;
        Activity simulationModel = builder.buildModelForSimulation();

        StringBuilder myStringBuilder = new StringBuilder();
        myStringBuilder.append("Sensitivity Analysis - Test " + name + "\n");

        // FirstCase
        TransientSolution<DeterministicEnablingState, RewardRate> previousSimulation = simulationModel.simulate(timeLimit.toString(), timeTick.toString(), previousRun);
        double[] previousCDF = new double[timeLimit.divide(timeTick, RoundingMode.HALF_UP).intValue()];

        for(int i = 0; i < previousCDF.length; i++){
            previousCDF[i] = previousSimulation.getSolution()[i][0][0];
        }

        MainHelper.ResultWrapper previousResult = new MainHelper.ResultWrapper(previousCDF, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());

        int currentRun = previousRun + runStep;

        while(currentRun <= runs){
            TransientSolution<DeterministicEnablingState, RewardRate> currentSimulation = simulationModel.simulate(timeLimit.toString(), timeTick.toString(), currentRun);
            double[] currentCDF = new double[timeLimit.divide(timeTick, RoundingMode.HALF_UP).intValue()];

            for(int i = 0; i < currentCDF.length; i++){
                currentCDF[i] = currentSimulation.getSolution()[i][0][0];
            }

            MainHelper.ResultWrapper currentResult = new MainHelper.ResultWrapper(currentCDF, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());
            double js = currentResult.jsDistance(previousResult.getPdf());

            myStringBuilder.append("[" + previousRun + "vs" + currentRun + "] - " + js + "\n");

            previousRun = currentRun;
            currentRun += runStep;
            previousResult = currentResult;
        }

        myStringBuilder.append("\n");

        return myStringBuilder.toString();
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
            } else {
                return 0.0;
            }
        }
    }
}
