/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.regression;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.AnalyticProcess;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.formats.OutputFormat;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.utils.Util;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.functions.LinearRegression;
import weka.core.Debug;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author eleni
 */
public class LinearRegressionAnalyticProcess extends AnalyticProcess {

    InputFormat input;

    public LinearRegressionAnalyticProcess(InputFormat input) {
        Util.nicePrintMessage("Create analytic process for LinearRegression");
        this.input = input;

    }

    //Get LinearRegression Estimations
    @Override
    public void train(Analytics analytics) {
        Util.nicePrintMessage("Train LinearRegression");

        float timeToRun_analytics = 0;
        long startTimeToRun_analytics = System.currentTimeMillis();
        //clean previous eval info if exists
        Util.cleanPreviousInfo(analytics);
        analytics.setTimeToGet_data(0);
        analytics.setTimeToRun_analytics(0);
        analytics.setData_size(0);
        analytics.setTimeToCreate_RDF(0);

        try {

            AbstractList<Instance> data1;
            Instances data = null;

            if (Util.isRDFInputFormat(analytics.getTrainQuery_id())) {
                data1 = input.importData4weka(Integer.toString(analytics.getTrainQuery_id()), "", true, analytics);
                if (data1 == null) {
                    return;
                }
                data = (Instances) data1;
                HashMap<String, Instances> separatedData = Util.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");

            } else if (!analytics.getDocument().equalsIgnoreCase("")) {

                if (Util.isRDFExportFormat(analytics.getExportFormat())) {
                    // remove dataset metadata (first two columns)   
                    data1 = input.importData4weka(Configuration.analyticsRepo + analytics.getDocument(), "", true, analytics);
                    if (data1 == null) {
                        return;
                    }
                    data = (Instances) data1;
                    HashMap<String, Instances> separatedData = Util.separateDataFromMetadataInfo(data);
                    data = separatedData.get("newData");

                } else {
                    data1 = input.importData4weka(Configuration.analyticsRepo + analytics.getDocument(), "", false, analytics);
                    if (data1 == null) {
                        return;
                    }
                    data = (Instances) data1;
                }

            } else {
                DBSynchronizer.updateLindaAnalyticsProcessMessage("Train Dataset in not defined. \n Could not run analytics process. \n Propably Train data were deleted after the creation of the analytic process.", analytics.getId());
                return;
            }

            System.out.println("train data lenght" + data.size());

            data.setClassIndex(data.numAttributes() - 1);
            //build model
            LinearRegression linearRegressionmodel = new LinearRegression();
            linearRegressionmodel.buildClassifier(data); //the last instance with missing    class is   not used

            Util.nicePrintMessage(linearRegressionmodel.toString());

            //classify the last instance
            Instance instancesToPredict = data.lastInstance();
            double price = linearRegressionmodel.classifyInstance(instancesToPredict);

            Util.saveModel(linearRegressionmodel, analytics);

        } catch (Exception ex) {
            Logger.getLogger(LinearRegressionAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
        // Get elapsed time in seconds
        timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
        analytics.setTimeToRun_analytics(timeToRun_analytics);

    }

    //Get LinearRegression Results
    @Override
    public void eval(Analytics analytics, OutputFormat out) {
        float timeToRun_analytics = 0;
        long startTimeToRun_analytics = System.currentTimeMillis();

        Util.nicePrintMessage("Eval Linear Regresion");

        AbstractList dataToReturn = null;
        HashMap<String, Instances> separatedData = null;
        try {
            //load data
            AbstractList<Instance> abstractlistdata;
            Instances data;

            if (Util.isRDFInputFormat(analytics.getEvaluationQuery_id())) {
                abstractlistdata = input.importData4weka(Integer.toString(analytics.getEvaluationQuery_id()), "", true, analytics);
                if (abstractlistdata == null) {
                    return;
                }
                data = (Instances) abstractlistdata;

                separatedData = Util.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");

            } else if (!analytics.getDocument().equalsIgnoreCase("")) {

                if (Util.isRDFExportFormat(analytics.getExportFormat())) {
                    abstractlistdata = input.importData4weka(Configuration.analyticsRepo + analytics.getTestdocument(), "", true, analytics);
                    if (abstractlistdata == null) {
                        return;
                    }
                    data = (Instances) abstractlistdata;

                    separatedData = Util.separateDataFromMetadataInfo(data);
                    data = separatedData.get("newData");
                } else {
                    abstractlistdata = input.importData4weka(Configuration.analyticsRepo + analytics.getTestdocument(), "", false, analytics);
                    if (abstractlistdata == null) {
                        return;
                    }
                    data = (Instances) abstractlistdata;

                }
            } else {

                DBSynchronizer.updateLindaAnalyticsProcessMessage("Evaluation Dataset in not defined. \n Could not run analytics process. \n Propably Evaluation data were deleted after the creation of the analytic process. ", analytics.getId());
                return;

            }

            System.out.println("evaluation data lenght" + data.size());

            //load model
            LinearRegression model = (LinearRegression) weka.core.SerializationHelper.read(Configuration.analyticsRepo + analytics.getModel());

            Instances unlabeled = data;

            // set class attribute
            unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

            // create copy
            Instances labeled = new Instances(unlabeled);

            // label instances
            for (int i = 0; i < unlabeled.numInstances(); i++) {
                double clsLabel = model.classifyInstance(unlabeled.instance(i));
                labeled.instance(i).setClassValue(clsLabel);
            }

            //<--in linear regression there is no process info text-->
            if (Util.isRDFExportFormat(analytics.getExportFormat())) {
                Instances mergedData = Util.mergeDataAndMetadataInfo(labeled, separatedData.get("metaData"));
                dataToReturn = mergedData;

            } else {
                dataToReturn = labeled;
            }

            Evaluation eval = new Evaluation(labeled);
            StringBuffer forPredictionsPrinting = new StringBuffer();
            PlainText output = new PlainText();
            output.setBuffer(forPredictionsPrinting);
            weka.core.Range attsToOutput = null;
            Boolean outputDistribution = true;
            eval.crossValidateModel(model, data, 10, new Debug.Random(1), output, attsToOutput, outputDistribution);

            Util.writeToFile(eval.toSummaryString(), "processinfo", analytics);

        } catch (Exception ex) {
            Logger.getLogger(LinearRegressionAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
        // Get elapsed time in seconds
        timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
        analytics.setTimeToRun_analytics(analytics.getTimeToRun_analytics() + timeToRun_analytics);

        out.exportData(analytics, dataToReturn);

    }

}
