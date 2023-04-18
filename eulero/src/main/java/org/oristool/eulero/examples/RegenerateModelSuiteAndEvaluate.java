package org.oristool.eulero.examples;

import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.evaluation.heuristics.EvaluationResult;
import org.oristool.eulero.modeling.deprecated.Activity;

import java.io.File;
import java.math.BigDecimal;
import java.util.Objects;

public class RegenerateModelSuiteAndEvaluate {
    public static void main(String[] args) throws Exception {
        String suiteDirectoryName;
        if(args.length > 1){
            throw new Exception("Too many arguments passed.");
        }
        if(args.length == 1){
            // this overwrites the folder
            suiteDirectoryName = args[0];
        } else {
            suiteDirectoryName = ModelSuiteGenerationParameter.directoryPath;
        }

        File modelSuiteReplica = new File(suiteDirectoryName + "_replica");
        if(!modelSuiteReplica.exists()){
            modelSuiteReplica.mkdirs();
        }

        File modelSuite = new File(suiteDirectoryName);

        for(File depth: Objects.requireNonNull(modelSuite.listFiles(File::isDirectory))){
            String depthReplicaPath = modelSuiteReplica.getAbsolutePath() + "/" + depth.getName();
            for(File modelFolder: Objects.requireNonNull(depth.listFiles(File::isDirectory))){
                Activity loadedModel = ExampleHelper.jaxbXmlFileToObject(modelFolder.getAbsolutePath() + "/structure_tree.xml");
                String modelReplicaPath = depthReplicaPath + '/' + modelFolder.getName();
                ExampleHelper.jaxbObjectToXML(loadedModel, modelReplicaPath);

                AnalysisHeuristicsVisitor strategy = ModelSuiteGenerationParameter.strategy.get(0);
                double[] cdf = strategy.analyze(loadedModel, loadedModel.max().add(BigDecimal.ONE), loadedModel.getFairTimeTick());

                EvaluationResult result = new EvaluationResult(
                        "Test",
                        cdf,
                        0,
                        cdf.length,
                        loadedModel.getFairTimeTick().doubleValue(),
                        0
                );

                ExampleHelper.storeResults(result, modelReplicaPath);
            }
        }
    }
}
