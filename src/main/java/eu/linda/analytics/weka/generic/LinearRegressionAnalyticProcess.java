/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.generic;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.AnalyticProcess;
import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.formats.OutputFormat;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctionsSingleton;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author eleni
 */
public class LinearRegressionAnalyticProcess extends AnalyticProcess {

    HelpfulFunctionsSingleton helpfulFunctions;
    InputFormat input;

    public LinearRegressionAnalyticProcess(InputFormat input) {

        helpfulFunctions = HelpfulFunctionsSingleton.getInstance();
        helpfulFunctions.nicePrintMessage("Create analytic process for LinearRegression");
        this.input = input;

    }

    //Get LinearRegression Estimations
    @Override
    public void train(Analytics analytics) {
        helpfulFunctions.nicePrintMessage("Train LinearRegression");

        float timeToRun_analytics = 0;
        long startTimeToRun_analytics = System.currentTimeMillis();
        //clean previous eval info if exists
        helpfulFunctions.cleanPreviousInfo(analytics);
        analytics.setTimeToGet_data(0);
        analytics.setTimeToRun_analytics(0);
        analytics.setData_size(0);
        analytics.setTimeToCreate_RDF(0);

        try {

            AbstractList<Instance> data1;
            Instances data = null;

            if (helpfulFunctions.isRDFInputFormat(analytics.getTrainQuery_id())) {
                data1 = input.importData4weka(Integer.toString(analytics.getTrainQuery_id()), true, analytics);
                data = (Instances) data1;
                HashMap<String, Instances> separatedData = helpfulFunctions.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");

            } else if (!analytics.getDocument().equalsIgnoreCase("")) {

                if (helpfulFunctions.isRDFExportFormat(analytics.getExportFormat())) {
                    // remove dataset metadata (first two columns)   
                    data1 = input.importData4weka(Configuration.analyticsRepo + analytics.getDocument(), true, analytics);
                    data = (Instances) data1;
                    HashMap<String, Instances> separatedData = helpfulFunctions.separateDataFromMetadataInfo(data);
                    data = separatedData.get("newData");

                } else {
                    data1 = input.importData4weka(Configuration.analyticsRepo + analytics.getDocument(), false, analytics);
                    data = (Instances) data1;
                }

            } else {
                helpfulFunctions.updateProcessMessageToAnalyticsTable("Train Dataset in not defined. \n Could not run analytics process. \n Propably Train data were deleted after the creation of the analytic process.", analytics.getId());
                return;
            }

            System.out.println("train data lenght" + data.size());

            data.setClassIndex(data.numAttributes() - 1);
            //build model
            LinearRegression linearRegressionmodel = new LinearRegression();
            linearRegressionmodel.buildClassifier(data); //the last instance with missing    class is   not used

            helpfulFunctions.nicePrintMessage(linearRegressionmodel.toString());

            //classify the last instance
            Instance instancesToPredict = data.lastInstance();
            double price = linearRegressionmodel.classifyInstance(instancesToPredict);

            helpfulFunctions.saveModel(linearRegressionmodel, analytics);

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

        helpfulFunctions.nicePrintMessage("Eval Linear Regresion");

        AbstractList dataToReturn = null;
        HashMap<String, Instances> separatedData = null;
        try {
            //load data
            AbstractList<Instance> abstractlistdata;
            Instances data;

            if (helpfulFunctions.isRDFInputFormat(analytics.getEvaluationQuery_id())) {
                abstractlistdata = input.importData4weka(Integer.toString(analytics.getEvaluationQuery_id()), true, analytics);
                data = (Instances) abstractlistdata;

                separatedData = helpfulFunctions.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");

            } else if (!analytics.getDocument().equalsIgnoreCase("")) {

                if (helpfulFunctions.isRDFExportFormat(analytics.getExportFormat())) {
                    abstractlistdata = input.importData4weka(Configuration.analyticsRepo + analytics.getTestdocument(), true, analytics);
                    data = (Instances) abstractlistdata;

                    separatedData = helpfulFunctions.separateDataFromMetadataInfo(data);
                    data = separatedData.get("newData");
                } else {
                    abstractlistdata = input.importData4weka(Configuration.analyticsRepo + analytics.getTestdocument(), false, analytics);
                    data = (Instances) abstractlistdata;

                }
            } else {

                helpfulFunctions.updateProcessMessageToAnalyticsTable("Evaluation Dataset in not defined. \n Could not run analytics process. \n Propably Evaluation data were deleted after the creation of the analytic process. ", analytics.getId());
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
            if (helpfulFunctions.isRDFExportFormat(analytics.getExportFormat())) {
                Instances mergedData = helpfulFunctions.mergeDataAndMetadataInfo(labeled, separatedData.get("metaData"));
                dataToReturn = mergedData;

            } else {
                dataToReturn = labeled;
            }

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
