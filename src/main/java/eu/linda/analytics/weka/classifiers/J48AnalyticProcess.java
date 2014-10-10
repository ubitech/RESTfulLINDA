/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.classifiers;

import eu.linda.analytic.controller.AnalyticProcess;
import eu.linda.analytic.formats.InputFormat;
import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctions;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.trees.J48;
import weka.core.Debug;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author eleni
 */
public class J48AnalyticProcess extends AnalyticProcess {

    _J48Output j48Output;
    HelpfulFunctions helpfulFunctions;
    InputFormat input;

    public J48AnalyticProcess(InputFormat input) {
        helpfulFunctions = new HelpfulFunctions();
        helpfulFunctions.nicePrintMessage("Create analytic process for J48");
        //j48Output = new J48Output(input);
        this.input = input;

    }

    @Override
    public void train(Analytics analytics) {

        helpfulFunctions.nicePrintMessage("Train J48");
        try {
            AbstractList<Instance> data1 = input.importData(Configuration.docroot + analytics.getDocument());
            Instances data = (Instances) data1;

            //Classifier j48ClassifierModel = j48Output.getJ48TreeModel(Configuration.docroot + analytics.getDocument(),datasetContainsMetadataInfo);
            // remove dataset metadata (first two columns)    
            if (analytics.getExportFormat().equalsIgnoreCase("rdf")) {
                HashMap<String, Instances> separatedData = helpfulFunctions.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");
            }

            data.setClassIndex(data.numAttributes() - 1);
            J48 j48ClassifierModel = new J48(); // new instance of tree

            String[] options = new String[1];
            options[0] = "-C 1.0 –M 5"; // confidenceFactor = 1.0, minNumObject = 5

            j48ClassifierModel.setOptions(options);// set the options
            j48ClassifierModel.buildClassifier(data); // build classifier

            // output associator
            System.out.println(j48ClassifierModel);
            //save model
            helpfulFunctions.saveModel(j48ClassifierModel, analytics);
        } catch (Exception ex) {
            Logger.getLogger(J48AnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public AbstractList eval(Analytics analytics) {

        AbstractList dataToReturn = null;
        HashMap<String, Instances> separatedTrainData = null;
        HashMap<String, Instances> separatedEvalData = null;

        helpfulFunctions.nicePrintMessage("Eval J48");
        try {
        //jsonresult = j48Output.getJ48TreeResultDataset(analytics);

            //Train Data
            AbstractList<Instance> data1 = input.importData(Configuration.docroot + analytics.getDocument());
            Instances traindata = (Instances) data1;

            //Test data
            AbstractList<Instance> data2 = input.importData(Configuration.docroot + analytics.getDocument());
            Instances testdata = (Instances) data2;

            if (analytics.getExportFormat().equalsIgnoreCase("rdf")) {
                separatedTrainData = helpfulFunctions.separateDataFromMetadataInfo(traindata);
                traindata = separatedTrainData.get("newData");

                separatedEvalData = helpfulFunctions.separateDataFromMetadataInfo(testdata);
                testdata = separatedEvalData.get("newData");
            }

            traindata.setClassIndex(traindata.numAttributes() - 1);
            testdata.setClassIndex(testdata.numAttributes() - 1);

            //Classifier model  
            Classifier model = (Classifier) weka.core.SerializationHelper.read(Configuration.docroot + analytics.getModel());

            Evaluation eval = new Evaluation(traindata);

            // evaluation 2
            StringBuffer forPredictionsPrinting = new StringBuffer();
            PlainText output = new PlainText();
            output.setBuffer(forPredictionsPrinting);
            weka.core.Range attsToOutput = null;
            Boolean outputDistribution = new Boolean(true);
            eval.crossValidateModel(model, traindata, 10, new Debug.Random(1), output, attsToOutput, outputDistribution);

            System.out.println(eval.toSummaryString("\nResults\n======\n", false));

            // load unlabeled data
            Instances unlabeled = testdata;
            // set class attribute
            unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
            // create copy
            Instances labeled = new Instances(unlabeled);
            // label instances
            for (int i = 0; i < unlabeled.numInstances(); i++) {
                double clsLabel = model.classifyInstance(unlabeled.instance(i));
                labeled.instance(i).setClassValue(clsLabel);
            }

            dataToReturn = labeled;

            helpfulFunctions.writeToFile(eval.toSummaryString(), "processinfo", analytics);

        } catch (Exception ex) {
            Logger.getLogger(J48AnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dataToReturn;

    }

}
