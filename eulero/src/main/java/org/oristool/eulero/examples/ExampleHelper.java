/* This program is called EULERO.
 * Copyright (C) 2022 The EULERO Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.oristool.eulero.examples;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.oristool.eulero.evaluation.heuristics.EvaluationResult;
import org.oristool.eulero.modeling.*;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExampleHelper {
    public static Activity jaxbXmlFileToObject(String fileName, List<StochasticTransitionFeature> features, List<BigDecimal> weights) {

        File xmlFile = new File(fileName);

        JAXBContext jaxbContext;
        try
        {
            jaxbContext = JAXBContext.newInstance(Activity.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            Activity test = (Activity) jaxbUnmarshaller.unmarshal(xmlFile);

            attachFeatures(test, new ArrayList<>(features), new ArrayList<>(weights));

            return test;
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }

        return DAG.empty("");
    }

    public static Activity jaxbXmlFileToObject(String fileName) {
        return jaxbXmlFileToObject(fileName, List.of(StochasticTransitionFeature.newUniformInstance("0", "1")), List.of(BigDecimal.ONE));
    }

    public static void attachFeatures(Activity model, ArrayList<StochasticTransitionFeature> features, ArrayList<BigDecimal> weights){
        ArrayList<StochasticTransitionFeature> beginEndFeature = new ArrayList<>();
        beginEndFeature.add(StochasticTransitionFeature.newDeterministicInstance(BigDecimal.ZERO));
        ArrayList<BigDecimal> beginEndWeights = new ArrayList<>();
        beginEndWeights.add(BigDecimal.ONE);

        if(model instanceof Simple){
            ((Simple) model).setFeatures(features);
            ((Simple) model).setWeights(weights);
        }

        if(model instanceof SEQ){
            ((Simple)((SEQ) model).begin()).setFeatures(beginEndFeature);
            ((Simple)((SEQ) model).begin()).setWeights(beginEndWeights);
            ((Simple)((SEQ) model).begin()).setFeatures(beginEndFeature);
            ((Simple)((SEQ) model).begin()).setWeights(beginEndWeights);

            ((Simple)((SEQ) model).end()).setFeatures(beginEndFeature);
            ((Simple)((SEQ) model).end()).setWeights(beginEndWeights);
            ((Simple)((SEQ) model).end()).setFeatures(beginEndFeature);
            ((Simple)((SEQ) model).end()).setWeights(beginEndWeights);

            Activity pre = ((SEQ) model).begin();
            for(Activity act: ((SEQ) model).activities()){
                attachFeatures(act, features, weights);
                act.pre().add(pre);
                pre.post().add(act);
                pre = act;
            }
            ((SEQ) model).end().pre().add(pre);
            pre.post().add(((SEQ) model).end());
        }

        if(model instanceof AND){
            ((Simple)((AND) model).begin()).setFeatures(beginEndFeature);
            ((Simple)((AND) model).begin()).setWeights(beginEndWeights);
            ((Simple)((AND) model).begin()).setFeatures(beginEndFeature);
            ((Simple)((AND) model).begin()).setWeights(beginEndWeights);

            ((Simple)((AND) model).end()).setFeatures(beginEndFeature);
            ((Simple)((AND) model).end()).setWeights(beginEndWeights);
            ((Simple)((AND) model).end()).setFeatures(beginEndFeature);
            ((Simple)((AND) model).end()).setWeights(beginEndWeights);

            Activity pre = ((AND) model).begin();
            for(Activity act: ((AND) model).activities()){
                attachFeatures(act, features, weights);
                act.pre().add(pre);
                ((AND) model).end().pre().add(act);
            }
        }

        if(model instanceof XOR){
            for(Activity act: ((XOR) model).activities()){
                attachFeatures(act, features, weights);
            }
        }

        if(model instanceof DAG){
            ((DAG) model).begin().post().clear();
            restorePreconditionToDAG(((DAG) model), features, weights);
        }
    }

    public static void restorePreconditionToDAG(DAG dag, ArrayList<StochasticTransitionFeature> features, ArrayList<BigDecimal> weights){
        List<DAGEdge> edges = dag.edges();
        for(Activity activity: dag.activities()){
            activity.post().clear();
            activity.pre().clear();
            attachFeatures(activity, features, weights);
            List<String> predecessorNames = edges.stream().filter(t -> t.getPostInt().equals(activity.name())).map(t -> t.getPreInt()).collect(Collectors.toList());
            if(predecessorNames.isEmpty()){
                activity.addPrecondition(dag.begin());
            } else {
                List<Activity> predecessorNodes = dag.activities().stream().filter(t -> predecessorNames.contains(t.name())).collect(Collectors.toList());
                activity.addPrecondition(predecessorNodes.toArray(Activity[]::new));
            }
        }

        List<String> lastNodeNames = edges.stream().map(t->t.getPreInt()).collect(Collectors.toList());
        List<Activity> lastNodes = dag.activities().stream().filter(t -> !lastNodeNames.contains(t.name())).collect(Collectors.toList());

        dag.end().pre().clear();
        dag.end().addPrecondition(lastNodes.toArray(Activity[]::new));
    }

    public static void storeResults(EvaluationResult result, String savePath){
        final boolean append = true, autoflush = true;
        PrintStream printStream = new PrintStream(System.out, autoflush);
        System.setOut(printStream);

        System.out.println("\nStoring Results...");
        String pathPrefix = savePath + "/";
        File path = new File(pathPrefix);
        if(!path.exists()){
            path.mkdirs();
        }

        double[] cdf = result.cdf();
        double[] pdf = result.pdf();

        StringBuilder cdfString = new StringBuilder();
        StringBuilder pdfString = new StringBuilder();
        cdfString.append("t,f\n");
        pdfString.append("t,f\n");
        for(int j = 0; j < cdf.length; j++){
            BigDecimal x = BigDecimal.valueOf((result.min() + j) * result.step())
                    .setScale(BigDecimal.valueOf(result.step()).scale(), RoundingMode.HALF_DOWN);
            cdfString.append(x.toString()).append(",").append(cdf[j]).append("\n");
            pdfString.append(x.toString()).append(",").append(pdf[j]).append("\n");
        }

        long time = result.computationTime();

        try {
            FileWriter cdfWriter = new FileWriter(pathPrefix + "/CDF.txt");
            FileWriter pdfWriter = new FileWriter(pathPrefix + "/PDF.txt");
            FileWriter timeWriter = new FileWriter(pathPrefix + "/computation_times.txt");

            timeWriter.write(time/1e9 + "s");
            timeWriter.close();
            cdfWriter.write(cdfString.toString());
            cdfWriter.close();
            pdfWriter.write(pdfString.toString());
            pdfWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void jaxbObjectToXML(Activity model, String testCasePath)
    {
        File folder = new File(testCasePath);
        if(!folder.exists()){
            folder.mkdirs();
        }
        try
        {
            //Create JAXB Context
            JAXBContext jaxbContext = JAXBContext.newInstance(Simple.class, SEQ.class, AND.class, XOR.class, DAG.class);

            //Create Marshaller
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            //Required formatting??
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            //Store XML to File
            File file = new File(testCasePath + "/structure_tree.xml");

            //Writes XML file to file-system
            jaxbMarshaller.marshal(model, file);
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }
    }
}
