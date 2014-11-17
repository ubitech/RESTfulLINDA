/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.classifiers;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.AnalyticProcess;
import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.formats.OutputFormat;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctionsSingleton;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    HelpfulFunctionsSingleton helpfulFunctions;
    InputFormat input;

    public J48AnalyticProcess(InputFormat input) {
        helpfulFunctions = HelpfulFunctionsSingleton.getInstance();
        helpfulFunctions.nicePrintMessage("Create analytic process for J48");
        this.input = input;

    }

    @Override
    public void train(Analytics analytics) {

        String[] options = new String[1];
        if (analytics.getParameters().equalsIgnoreCase("")) {
            options[0] = "-C 1.0 –M 5"; // confidenceFactor = 1.0, minNumObject = 5

        } else {
            options[0] = analytics.getParameters();
        }

        helpfulFunctions.nicePrintMessage("Train J48 with options " + options[0]);
        try {
            AbstractList<Instance> abstractListdata;
            Instances data;

            // remove dataset metadata (first two columns)    
            if (helpfulFunctions.isRDFInputFormat(analytics.getTrainQuery_id())) {
                
                abstractListdata = input.importData4weka(Integer.toString(analytics.getTrainQuery_id()), true);
                data = (Instances) abstractListdata;
                HashMap<String, Instances> separatedData = helpfulFunctions.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");

            } else if (helpfulFunctions.isRDFExportFormat(analytics.getExportFormat())) {
               
                abstractListdata = input.importData4weka(Configuration.docroot + analytics.getDocument(), true);
                data = (Instances) abstractListdata;
                HashMap<String, Instances> separatedData = helpfulFunctions.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");
                
            } else {

                abstractListdata = input.importData4weka(Configuration.docroot + analytics.getDocument(), false);
                data = (Instances) abstractListdata;

            }

            data.setClassIndex(data.numAttributes() - 1);
            J48 j48ClassifierModel = new J48(); // new instance of tree

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
    public void eval(Analytics analytics, OutputFormat out) {

        AbstractList dataToReturn = null;
        HashMap<String, Instances> separatedTrainData = null;
        HashMap<String, Instances> separatedEvalData = null;

        helpfulFunctions.nicePrintMessage("Eval J48");

        //clean previous eval info if exists
        helpfulFunctions.cleanPreviousInfo(analytics);

        try {
        //jsonresult = j48Output.getJ48TreeResultDataset(analytics);

            //Train Data
            AbstractList<Instance> abstractListdata1;
            Instances traindata;

            //Test data
            AbstractList<Instance> abstractListdata2;
            Instances testdata;

            
            if (helpfulFunctions.isRDFInputFormat(analytics.getEvaluationQuery_id()))
            {                
                abstractListdata1 = input.importData4weka(Integer.toString(analytics.getTrainQuery_id()), true);
                traindata = (Instances) abstractListdata1;
                separatedTrainData = helpfulFunctions.separateDataFromMetadataInfo(traindata);
                traindata = separatedTrainData.get("newData");

                abstractListdata2 = input.importData4weka(Integer.toString(analytics.getEvaluationQuery_id()), true);
                testdata = (Instances) abstractListdata2;
                separatedEvalData = helpfulFunctions.separateDataFromMetadataInfo(testdata);
                testdata = separatedEvalData.get("newData"); 

            } else if (helpfulFunctions.isRDFExportFormat(analytics.getExportFormat())) {
                abstractListdata1 = input.importData4weka(Configuration.docroot + analytics.getDocument(), true);
                traindata = (Instances) abstractListdata1;
                separatedTrainData = helpfulFunctions.separateDataFromMetadataInfo(traindata);
                traindata = separatedTrainData.get("newData");

                abstractListdata2 = input.importData4weka(Configuration.docroot + analytics.getTestdocument(), true);
                testdata = (Instances) abstractListdata2;
                separatedEvalData = helpfulFunctions.separateDataFromMetadataInfo(testdata);
                testdata = separatedEvalData.get("newData");
            } else {

                abstractListdata1 = input.importData4weka(Configuration.docroot + analytics.getDocument(), false);
                traindata = (Instances) abstractListdata1;

                abstractListdata2 = input.importData4weka(Configuration.docroot + analytics.getTestdocument(), false);
                testdata = (Instances) abstractListdata2;

            }

            traindata.setClassIndex(traindata.numAttributes() - 1);
            testdata.setClassIndex(testdata.numAttributes() - 1);

            if (traindata.numAttributes() != testdata.numAttributes()) {
                helpfulFunctions.updateProcessMessageToAnalyticsTable("Train Dataset has not the same"
                        + " attributes with Evaluation dataset! Please create a new analytic process!", analytics.getId());
                return;
            }

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

            if (helpfulFunctions.isRDFExportFormat(analytics.getExportFormat())) {
                Instances mergedData = helpfulFunctions.mergeDataAndMetadataInfo(labeled, separatedEvalData.get("metaData"));
                dataToReturn = mergedData;

            } else {
                dataToReturn = labeled;
            }

            helpfulFunctions.writeToFile(eval.toSummaryString(), "processinfo", analytics);

        } catch (Exception ex) {
            Logger.getLogger(J48AnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
            helpfulFunctions.updateProcessMessageToAnalyticsTable(ex.toString(), analytics.getId());
            //return (AbstractList) new LinkedList();

        }

        out.exportData(analytics, dataToReturn);

    }

}
