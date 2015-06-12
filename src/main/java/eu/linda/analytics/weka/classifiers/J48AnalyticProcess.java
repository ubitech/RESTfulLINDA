/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.classifiers;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.AnalyticProcess;
import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.formats.OutputFormat;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.utils.AlsCustomException;
import eu.linda.analytics.utils.Util;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Debug;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author eleni
 */
public class J48AnalyticProcess extends AnalyticProcess {

    InputFormat input;
    ConnectionController connectionController;

    public J48AnalyticProcess(InputFormat input) {
        connectionController = ConnectionController.getInstance();
        Util.nicePrintMessage("Create analytic process for J48");
        this.input = input;

    }

    @Override
    public void train(Analytics analytics) {

        //clean previous eval info if exists
        Util.cleanPreviousInfo(analytics);
        analytics.setTimeToGet_data(0);
        analytics.setTimeToRun_analytics(0);
        analytics.setData_size(0);
        analytics.setTimeToCreate_RDF(0);

        float timeToRun_analytics = 0;
        long startTimeToRun_analytics = System.currentTimeMillis();

        String[] options = new String[1];
        if (analytics.getParameters().equalsIgnoreCase("")) {
            options[0] = "-C 1.0 –M 5"; // confidenceFactor = 1.0, minNumObject = 5

        } else {
            options[0] = analytics.getParameters();
        }

        Util.nicePrintMessage("Train J48 with options " + options[0]);
        try {
            AbstractList<Instance> abstractListdata;
            Instances data;

            // remove dataset metadata (first two columns)    
            if (Util.isRDFInputFormat(analytics.getTrainQuery_id())) {

                abstractListdata = input.importData4weka(Integer.toString(analytics.getTrainQuery_id()), "", true, analytics);
                if (abstractListdata == null) {
                    return;
                }
                data = (Instances) abstractListdata;
                HashMap<String, Instances> separatedData = Util.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");

            } else if (Util.isRDFExportFormat(analytics.getExportFormat())) {

                abstractListdata = input.importData4weka(Configuration.analyticsRepo + analytics.getDocument(), "", true, analytics);
                if (abstractListdata == null) {
                    return;
                }
                data = (Instances) abstractListdata;
                HashMap<String, Instances> separatedData = Util.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");

            } else {

                abstractListdata = input.importData4weka(Configuration.analyticsRepo + analytics.getDocument(), "", false, analytics);
                if (abstractListdata == null) {
                    return;
                }
                data = (Instances) abstractListdata;

            }

            data.setClassIndex(data.numAttributes() - 1);

            Attribute trainClassAttribute = data.attribute(data.numAttributes() - 1);

            if (trainClassAttribute.isNumeric()) {
                throw new AlsCustomException("J48 Algorithm can not handle numeric values as Categorization Class."
                        + "\n Check that the last column of you input data is nominal.", analytics);
            }

            J48 j48ClassifierModel = new J48(); // new instance of tree

            j48ClassifierModel.setOptions(options);// set the options
            j48ClassifierModel.buildClassifier(data); // build classifier

            // output associator
            System.out.println(j48ClassifierModel);
            //save model
            Util.saveModel(j48ClassifierModel, analytics);

            long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
            // Get elapsed time in seconds
            timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
            analytics.setTimeToRun_analytics(timeToRun_analytics);
        } catch (AlsCustomException ex) {
            return;
        } catch (Exception ex) {
            Logger.getLogger(J48AnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void eval(Analytics analytics, OutputFormat out) {
        float timeToRun_analytics = 0;
        long startTimeToRun_analytics = System.currentTimeMillis();

        AbstractList dataToReturn = null;
        HashMap<String, Instances> separatedTrainData = null;
        HashMap<String, Instances> separatedEvalData = null;

        Util.nicePrintMessage("Eval J48");

        try {
            //Train Data
            AbstractList<Instance> abstractListdata1;
            Instances traindata;

            //Test data
            AbstractList<Instance> abstractListdata2;
            Instances testdata;

            if (Util.isRDFInputFormat(analytics.getEvaluationQuery_id())) {
                abstractListdata1 = input.importData4weka(Integer.toString(analytics.getTrainQuery_id()), "", true, analytics);
                if (abstractListdata1 == null) {
                    return;
                }
                traindata = (Instances) abstractListdata1;
                separatedTrainData = Util.separateDataFromMetadataInfo(traindata);
                traindata = separatedTrainData.get("newData");

                abstractListdata2 = input.importData4weka(Integer.toString(analytics.getEvaluationQuery_id()), "", true, analytics);
                testdata = (Instances) abstractListdata2;
                separatedEvalData = Util.separateDataFromMetadataInfo(testdata);
                testdata = separatedEvalData.get("newData");

            } else if (Util.isRDFExportFormat(analytics.getExportFormat())) {
                abstractListdata1 = input.importData4weka(Configuration.analyticsRepo + analytics.getDocument(), "", true, analytics);
                if (abstractListdata1 == null) {
                    return;
                }
                traindata = (Instances) abstractListdata1;
                separatedTrainData = Util.separateDataFromMetadataInfo(traindata);
                traindata = separatedTrainData.get("newData");

                abstractListdata2 = input.importData4weka(Configuration.analyticsRepo + analytics.getTestdocument(), "", true, analytics);
                testdata = (Instances) abstractListdata2;
                separatedEvalData = Util.separateDataFromMetadataInfo(testdata);
                testdata = separatedEvalData.get("newData");
            } else {

                abstractListdata1 = input.importData4weka(Configuration.analyticsRepo + analytics.getDocument(), "", false, analytics);
                if (abstractListdata1 == null) {
                    return;
                }
                traindata = (Instances) abstractListdata1;

                abstractListdata2 = input.importData4weka(Configuration.analyticsRepo + analytics.getTestdocument(), "", false, analytics);
                testdata = (Instances) abstractListdata2;

            }

            Attribute trainClassAttribute = traindata.attribute(traindata.numAttributes() - 1);
            Attribute testClassAttribute = traindata.attribute(testdata.numAttributes() - 1);
            if (trainClassAttribute.isNumeric() || testClassAttribute.isNumeric()) {

                throw new AlsCustomException("J48 Algorithm can not handle numeric values as Categorization Class."
                        + "\n Check that the last column of you input data is nominal.", analytics);
            }

            traindata.setClassIndex(traindata.numAttributes() - 1);

            testdata.setClassIndex(testdata.numAttributes() - 1);

            if (traindata.numAttributes() != testdata.numAttributes()) {
                DBSynchronizer.updateLindaAnalyticsProcessMessage("Train Dataset has not the same"
                        + " attributes with Evaluation dataset! Please create a new analytic process!", analytics.getId());
                return;
            }

            //Classifier model  
            Classifier model = (Classifier) weka.core.SerializationHelper.read(Configuration.analyticsRepo + analytics.getModel());

            Evaluation eval = new Evaluation(traindata);

            // evaluation 2
            StringBuffer forPredictionsPrinting = new StringBuffer();
            PlainText output = new PlainText();
            output.setBuffer(forPredictionsPrinting);
            weka.core.Range attsToOutput = null;
            Boolean outputDistribution = true;
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

            if (Util.isRDFExportFormat(analytics.getExportFormat())) {
                Instances mergedData = Util.mergeDataAndMetadataInfo(labeled, separatedEvalData.get("metaData"));
                dataToReturn = mergedData;

            } else {
                dataToReturn = labeled;
            }

            Util.writeToFile(eval.toSummaryString(), "processinfo", analytics);

        } catch (AlsCustomException ex) {
            return;
        } catch (Exception ex) {
            Logger.getLogger(J48AnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
            DBSynchronizer.updateLindaAnalyticsProcessMessage(ex.toString(), analytics.getId());

        }
        long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
        // Get elapsed time in seconds
        timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
        analytics.setTimeToRun_analytics(analytics.getTimeToRun_analytics() + timeToRun_analytics);
        out.exportData(analytics, dataToReturn);

    }

}
