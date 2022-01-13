package org.oristool.eulero.mains;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.apache.commons.lang3.SerializationUtils;
import org.oristool.eulero.analysisheuristics.AnalysisHeuristicStrategy;
import org.oristool.eulero.graph.*;
import org.oristool.eulero.models.ModelBuilder;
import org.oristool.eulero.ui.ActivityViewer;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

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
    private final List<AnalysisHeuristicStrategy> heuristics;
    private final int groundTruthRuns;
    private final int runs;
    private final String testCasePath;
    private final boolean saveResults;
    private final boolean verbose;
    private final Activity model;
    private final ModelBuilder modelBuilder;


    public TestCaseHandler(String testCaseName, ModelBuilder modelBuilder, List<AnalysisHeuristicStrategy> heuristics, int groundTruthRuns, int runs, String testCasePath, boolean saveResults, boolean verbose){
        this.testCaseName = testCaseName;
        this.heuristics = heuristics;
        this.groundTruthRuns = groundTruthRuns;
        this.runs = runs;
        this.testCasePath = testCasePath;
        this.saveResults = saveResults;
        this.verbose = verbose;
        this.modelBuilder = modelBuilder;
        this.model = modelBuilder.buildModel();
        jaxbObjectToXML(this.model);
    }

    public ArrayList<TestCaseResult> runTestCase(BigDecimal offset, BigDecimal step, BigDecimal error) throws FileNotFoundException {
        return runTestCase(offset, step, BigDecimal.ONE, error);
    }
    public ArrayList<TestCaseResult> runTestCase(BigDecimal offset, BigDecimal step, BigDecimal forwardReductionFactor, BigDecimal error) throws FileNotFoundException {
        ArrayList<TestCaseResult> results = new ArrayList<>();
        File logFile = new File(testCasePath + "/log/");
        if(!logFile.exists()){
            logFile.mkdirs();
        }
        PrintStream systemOut = System.out;
        // GroundTruth
        if(verbose) {
            System.out.println("\nGT Simulation starts...");
            PrintStream printWriterGT = new PrintStream(new FileOutputStream(testCasePath + "/log/GroundTruth.txt", false));
            System.setOut(printWriterGT);
            System.out.println("\nGT Simulation starts...");
        }

        File f = new File(testCasePath + "/CDF/GroundTruth.txt");
        if(f.exists() && !f.isDirectory()) {
            if(verbose){
                System.out.println("Ground Truth already computed... Loading...");

                System.setOut(systemOut);
                System.out.println("Ground Truth already computed... Loading...");
            }

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
                    if(verbose)
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
                if(verbose)
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

            TestCaseResult GTSimulation = runSimulation(offset, step, groundTruthRuns);
            results.add(GTSimulation);

            if(verbose){
                System.out.println(String.format("GT Simulation took %.3f seconds",
                        GTSimulation.computationTime()/1e9));
                System.setOut(systemOut);
                System.out.println(String.format("GT Simulation took %.3f seconds",
                        GTSimulation.computationTime()/1e9));
            }
        }

        // Heuristics
        long[] heuristicTimes = new long[heuristics.size()];
        for(AnalysisHeuristicStrategy heuristic: heuristics){
            PrintStream printWriter = new PrintStream(new FileOutputStream(testCasePath + "/log/Heuristic" + heuristics.indexOf(heuristic) + ".txt", false));

            if(verbose){
                System.out.println("\nHEURISTIC " + heuristics.indexOf(heuristic));
                System.setOut(printWriter);
                System.out.println("\nHEURISTIC " + heuristics.indexOf(heuristic));
            }

            TestCaseResult heuristicResult = runHeuristic(heuristic, offset, step, forwardReductionFactor, error);
            results.add(heuristicResult);

            if(verbose){
                System.out.println(String.format("Evaluation took %.3f seconds",
                        heuristicResult.computationTime()/1e9));
                System.setOut(systemOut);
                System.out.println(String.format("Evaluation took %.3f seconds",
                        heuristicResult.computationTime()/1e9));
            }

            heuristicTimes[heuristics.indexOf(heuristic)] = (long) (heuristicResult.computationTime()/1e9);
        }

        // Simulation
        PrintStream printWriter = new PrintStream(new FileOutputStream(testCasePath + "/log/simulation.txt", false));

        if(verbose){
            System.out.println("\nShort Simulation starts...");
            System.setOut(printWriter);
        }

        TestCaseResult shortSimulation = runSimulation(offset, step, groundTruthRuns + 1 /*heuristicTimes[0]*/);
        results.add(shortSimulation);

        if(verbose){
            System.out.printf("Simulation took %.3f seconds%n",
                    shortSimulation.computationTime()/1e9);

            System.setOut(systemOut);
            System.out.printf("Simulation took %.3f seconds%n",
                    shortSimulation.computationTime()/1e9);
        }

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
        final boolean append = true, autoflush = true;
        PrintStream printStream = new PrintStream(System.out, autoflush); // to write to console
        System.setOut(printStream);

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

    private TestCaseResult runSimulation(BigDecimal offset, BigDecimal step, int runs){
        long start = System.nanoTime();
        String caseTitle = runs != this.groundTruthRuns ? "Shorted Simulation" : "GroundTruth";
        Activity clonedModel = jaxbXmlFileToObject(this.testCasePath + "/model.xml");
        System.out.println(clonedModel.name());
        TransientSolution<DeterministicEnablingState, RewardRate> simulationCDF = clonedModel.simulate(clonedModel.LFT().add(offset).toString(), step.toString(), runs);
        double[] cdf = new double[simulationCDF.getSolution().length];
        for(int count = 0; count < simulationCDF.getSolution().length; count++){
            double accumulator = 0;
            for(int i = Math.max(count - 2, 0); i < Math.min(count + 2, cdf.length); i++){
                accumulator += simulationCDF.getSolution()[i][0][0];
            }
            accumulator /= (Math.min(count + 2, cdf.length) - Math.max(count - 2, 0));
            cdf[count] = accumulator;
        }
        long computationTime = System.nanoTime() - start;

        return new TestCaseResult(
                caseTitle,
                cdf,
                clonedModel.EFT().divide(step).intValue(),
                clonedModel.LFT().divide(step).intValue(),
                step.doubleValue(),
                computationTime);
    }

    private TestCaseResult runSimulation(BigDecimal offset, BigDecimal step, long timeout){
        int windowWidth = 10;
        long start = System.nanoTime();
        String caseTitle = runs == this.runs ? "Shorted Simulation" : "GroundTruth";
        Activity clonedModel = jaxbXmlFileToObject(this.testCasePath + "/model.xml");
        System.out.println(clonedModel.name());
        TransientSolution<DeterministicEnablingState, RewardRate> simulationCDF = clonedModel.simulate(clonedModel.LFT().add(offset).toString(), step.toString(), timeout);
        double[] cdf = new double[simulationCDF.getSolution().length];
        for(int count = 0; count < simulationCDF.getSolution().length; count++){
            double accumulator = 0;
            for(int i = Math.max(count - windowWidth, 0); i < Math.min(count + windowWidth, cdf.length); i++){
                accumulator += simulationCDF.getSolution()[i][0][0];
            }
            accumulator /= (Math.min(count + windowWidth, cdf.length) - Math.max(count - windowWidth, 0));
            cdf[count] = accumulator;
        }
        long computationTime = System.nanoTime() - start;

        return new TestCaseResult(
                caseTitle,
                cdf,
                clonedModel.EFT().divide(step).intValue(),
                clonedModel.LFT().divide(step).intValue(),
                step.doubleValue(),
                computationTime);
    }

    private TestCaseResult runHeuristic(AnalysisHeuristicStrategy strategy, BigDecimal offset, BigDecimal step, BigDecimal forwardReductionFactor, BigDecimal error){
        long start = System.nanoTime();
        Activity clonedModel = jaxbXmlFileToObject(this.testCasePath + "/model.xml");
        System.out.println(clonedModel.name());
        double[] heuristicCDF = strategy.analyze(clonedModel, clonedModel.LFT().add(offset), step, forwardReductionFactor, error);
        long computationTime = System.nanoTime() - start;

        return new TestCaseResult(
                strategy.heuristicName(),
                heuristicCDF,
                clonedModel.EFT().divide(step).intValue(),
                clonedModel.LFT().divide(step).intValue(),
                step.doubleValue(),
                computationTime);
    }

    private void jaxbObjectToXML(Activity model)
    {
        try
        {
            //Create JAXB Context
            JAXBContext jaxbContext = JAXBContext.newInstance(Analytical.class, SEQ.class, AND.class, Xor.class, DAG.class);

            //Create Marshaller
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            //Required formatting??
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            //Store XML to File
            File file = new File(this.testCasePath + "/model.xml");

            //Writes XML file to file-system
            jaxbMarshaller.marshal(model, file);
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }
    }

    private Activity jaxbXmlFileToObject(String fileName) {

        File xmlFile = new File(fileName);

        JAXBContext jaxbContext;
        try
        {
            jaxbContext = JAXBContext.newInstance(Activity.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            //System.out.println(model);
            Activity test = (Activity) jaxbUnmarshaller.unmarshal(xmlFile);

            attachFeatures(test, this.modelBuilder.getFeatures(), this.modelBuilder.getWeights());

            return test;
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }

        return this.model;
    }

    private void attachFeatures(Activity model, ArrayList<StochasticTransitionFeature> features, ArrayList<BigDecimal> weights){
        ArrayList<StochasticTransitionFeature> beginEndFeature = new ArrayList<>();
        beginEndFeature.add(StochasticTransitionFeature.newDeterministicInstance(BigDecimal.ZERO));
        ArrayList<BigDecimal> beginEndWeights = new ArrayList<>();
        beginEndWeights.add(BigDecimal.ONE);

        if(model instanceof Analytical){
            ((Analytical) model).setFeatures(features);
            ((Analytical) model).setWeights(weights);
        }

        if(model instanceof SEQ){
            ((Analytical)((SEQ) model).begin()).setFeatures(beginEndFeature);
            ((Analytical)((SEQ) model).begin()).setWeights(beginEndWeights);
            ((Analytical)((SEQ) model).begin()).setFeatures(beginEndFeature);
            ((Analytical)((SEQ) model).begin()).setWeights(beginEndWeights);

            ((Analytical)((SEQ) model).end()).setFeatures(beginEndFeature);
            ((Analytical)((SEQ) model).end()).setWeights(beginEndWeights);
            ((Analytical)((SEQ) model).end()).setFeatures(beginEndFeature);
            ((Analytical)((SEQ) model).end()).setWeights(beginEndWeights);

            for(Activity act: ((SEQ) model).activities()){
                attachFeatures(act, features, weights);
            }
        }

        if(model instanceof AND){
            ((Analytical)((AND) model).begin()).setFeatures(beginEndFeature);
            ((Analytical)((AND) model).begin()).setWeights(beginEndWeights);
            ((Analytical)((AND) model).begin()).setFeatures(beginEndFeature);
            ((Analytical)((AND) model).begin()).setWeights(beginEndWeights);

            ((Analytical)((AND) model).end()).setFeatures(beginEndFeature);
            ((Analytical)((AND) model).end()).setWeights(beginEndWeights);
            ((Analytical)((AND) model).end()).setFeatures(beginEndFeature);
            ((Analytical)((AND) model).end()).setWeights(beginEndWeights);

            for(Activity act: ((AND) model).activities()){
                attachFeatures(act, features, weights);
            }
        }

        if(model instanceof Xor){
            for(Activity act: ((Xor) model).alternatives()){
                attachFeatures(act, features, weights);
            }
        }

    }
}