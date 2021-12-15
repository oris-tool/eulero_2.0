package org.oristool.eulero.mains;

import org.oristool.eulero.analysisheuristics.AnalysisHeuristicStrategy;
import org.oristool.eulero.graph.Activity;
import org.oristool.eulero.models.ModelBuilder;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TestCaseHandler {
    private final String testCaseName;
    private final ModelBuilder modelBuilder;
    private final List<AnalysisHeuristicStrategy> heuristics;
    private final int groundTruthRuns;
    private final int runs;
    private final String testCasePath;
    private final boolean saveResults;


    public TestCaseHandler(String testCaseName, ModelBuilder modelBuilder, List<AnalysisHeuristicStrategy> heuristics, int groundTruthRuns, int runs, String testCasePath, boolean saveResults){
        this.testCaseName = testCaseName;
        this.heuristics = heuristics;
        this.modelBuilder = modelBuilder;
        this.groundTruthRuns = groundTruthRuns;
        this.runs = runs;
        this.testCasePath = testCasePath;
        this.saveResults = saveResults;
    }

    public ArrayList<TestCaseResult> runTestCase(BigDecimal timeLimit, BigDecimal step, BigDecimal error){
        ArrayList<TestCaseResult> results = new ArrayList<>();
        // GroundTruth
        System.out.println("\nGT Simulation starts...");

        File f = new File(testCasePath + "/CDF/GroundTruth.txt");
        if(f.exists() && !f.isDirectory()) {
            System.out.println("Ground Truth already computed... Loading...");
            FileReader fr = null;
            try {
                fr = new FileReader(testCasePath + "/CDF/GroundTruth.txt");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            BufferedReader b = new BufferedReader(fr);
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
                fComputationTimes = new FileReader(testCasePath + "/times/GroundTruth.txt");
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

            long GTTime = Long.parseLong(gtTime.split("s")[0]);

            TestCaseResult GTSimulation = new TestCaseResult(
                    "GroundTruth",
                    numericalGroundTruth,
                    0,
                    numericalGroundTruth.length,
                    step.doubleValue(),
                    GTTime
            );
            results.add(GTSimulation);
        } else {
            TestCaseResult GTSimulation = runSimulation(timeLimit, step, groundTruthRuns);
            results.add(GTSimulation);
            System.out.println(String.format("GT Simulation took %.3f seconds",
                    GTSimulation.computationTime()/1e9));
        }

        // Heuristics
        long[] heuristicTimes = new long[heuristics.size()];
        for(AnalysisHeuristicStrategy heuristic: heuristics){
            System.out.println("\nHEURISTIC " + heuristics.indexOf(heuristic));
            TestCaseResult heuristicResult = runHeuristic(heuristic, timeLimit, step, error);
            results.add(heuristicResult);
            System.out.println(String.format("Evaluation took %.3f seconds",
                    heuristicResult.computationTime()/1e9));

            heuristicTimes[heuristics.indexOf(heuristic)] = (long) (heuristicResult.computationTime()/1e9);
        }

        // Simulation
        System.out.println("\nShort Simulation starts...");
        TestCaseResult shortSimulation = runSimulation(timeLimit, step, Arrays.stream(heuristicTimes).min().orElse(0));
        results.add(shortSimulation);
        System.out.println(String.format("Simulation took %.3f seconds",
                shortSimulation.computationTime()/1e9));

        return results;
    }

    public void plotResults(ArrayList<TestCaseResult> results) {
        try{
            ActivityViewer.CompareResults(testCaseName, results.stream().map(TestCaseResult::title).collect(Collectors.toList()), results);
        } catch (Exception e){
            System.out.println(e);
            System.out.println("Impossible to plot images...");
        }
    }

    public void storeResults(ArrayList<TestCaseResult> results, String savePath) {
        System.out.println("\nStoring Results...");
        String pathPrefix = savePath + "/" + testCaseName;
        File pdfFile = new File(pathPrefix + "/PDF/");
        if(!pdfFile.exists()){
            pdfFile.mkdirs();
        }

        File cdfFile = new File(pathPrefix + "/CDF/");
        if(!cdfFile.exists()){
            cdfFile.mkdirs();
        }

        File timesFile = new File(pathPrefix + "/times/");
        if(!timesFile.exists()){
            timesFile.mkdirs();
        }

        File jsFile = new File(pathPrefix + "/accuracyMeasure/");
        if(!jsFile.exists()){
            jsFile.mkdirs();
        }

        double[] groundTruth = results.get(0).pdf();

        for (int i = 0; i < results.size(); i++) {
            double[] cdf = results.get(i).cdf();
            double[] pdf = results.get(i).pdf();
            StringBuilder cdfString = new StringBuilder();
            StringBuilder pdfString = new StringBuilder();
            for(int j = 0; j < cdf.length; j++){
                BigDecimal x = BigDecimal.valueOf((results.get(i).min() + j) * results.get(i).step())
                        .setScale(BigDecimal.valueOf(results.get(i).step()).scale(), RoundingMode.HALF_DOWN);
                cdfString.append(x.toString()).append(", ").append(cdf[j]).append("\n");
                pdfString.append(x.toString()).append(", ").append(pdf[j]).append("\n");
            }

            long time = results.get(i).computationTime();// / 1e9;
            //double js = results.get(i).jsDistance(groundTruth);
            double js = results.get(i).cdfAreaDifference(results.get(0).cdf());


            // TODO ADD matplotlib code

            try {
                FileWriter cdfWriter = new FileWriter(pathPrefix + "/CDF/" + results.get(i).title() + ".txt");
                FileWriter pdfWriter = new FileWriter(pathPrefix + "/PDF/" + results.get(i).title() + ".txt");
                FileWriter timeWriter = new FileWriter(pathPrefix + "/times/" + results.get(i).title() + ".txt");
                FileWriter jsWriter = new FileWriter(pathPrefix + "/accuracyMeasure/" + results.get(i).title() + ".txt");

                timeWriter.write(String.valueOf(time));
                timeWriter.close();
                jsWriter.write(String.valueOf(js));
                jsWriter.close();
                cdfWriter.write(cdfString.toString());
                cdfWriter.close();
                pdfWriter.write(pdfString.toString());
                pdfWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }

        System.out.println("\nResults Stored");
    }

    private TestCaseResult runSimulation(BigDecimal timeLimit, BigDecimal step, int runs){
        long start = System.nanoTime();
        String caseTitle = runs == this.runs ? "Shorted Simulation" : "GroundTruth";
        Activity model = modelBuilder.buildModel();
        TransientSolution<DeterministicEnablingState, RewardRate> simulationCDF = model.simulate(timeLimit.toString(), step.toString(), runs);
        long computationTime = System.nanoTime() - start;

        return new TestCaseResult(
                caseTitle,
                simulationCDF,
                model.EFT().divide(step).intValue(),
                model.LFT().divide(step).intValue(),
                step.doubleValue(),
                computationTime);
    }

    private TestCaseResult runSimulation(BigDecimal timeLimit, BigDecimal step, long timeout){
        long start = System.nanoTime();
        String caseTitle = runs == this.runs ? "Shorted Simulation" : "GroundTruth";
        Activity model = modelBuilder.buildModel();
        TransientSolution<DeterministicEnablingState, RewardRate> simulationCDF = model.simulate(timeLimit.toString(), step.toString(), timeout);
        long computationTime = System.nanoTime() - start;

        return new TestCaseResult(
                caseTitle,
                simulationCDF,
                model.EFT().divide(step).intValue(),
                model.LFT().divide(step).intValue(),
                step.doubleValue(),
                computationTime);
    }

    private TestCaseResult runHeuristic(AnalysisHeuristicStrategy strategy, BigDecimal timeLimit, BigDecimal step, BigDecimal error){
        long start = System.nanoTime();
        Activity model = modelBuilder.buildModel();
        double[] heuristicCDF = strategy.analyze(model, timeLimit, step, error);
        long computationTime = System.nanoTime() - start;

        return new TestCaseResult(
                strategy.heuristicName(),
                heuristicCDF,
                model.EFT().divide(step).intValue(),
                model.LFT().divide(step).intValue(),
                step.doubleValue(),
                computationTime);
    }

   /* private TestCaseResult loadGroundTruth(){
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

        return new TestCaseResult(numericalGroundTruth, )

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
        MainHelper.ResultWrapper groundTruthResult = new MainHelper.ResultWrapper(numericalGroundTruth, 0, timeLimit.divide(timeTick).intValue(), timeTick.doubleValue());*/
}