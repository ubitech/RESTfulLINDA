/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytic.controller;

import eu.linda.analytic.output.ArffOutputFormat;
import eu.linda.analytic.output.CSVOutputFormat;
import eu.linda.analytics.weka.classifiers.J48AnalyticProcess;
import eu.linda.analytics.weka.classifiers.M5PAnalyticProcess;
import eu.linda.analytics.weka.generic.LinearRegressionAnalyticProcess;

/**
 *
 * @author eleni
 */
public class AnalyticsFactory {

    public AnalyticsInfo createAnalytics(String algorithm, String outputformat) {

        AnalyticProcess analyticProcess = null;
        OutputFormat outputFormat = null;

        if (algorithm.equalsIgnoreCase("J48")) {
            analyticProcess = new J48AnalyticProcess();
        } else if (algorithm.equalsIgnoreCase("M5P")) {
            analyticProcess = new M5PAnalyticProcess();
        } else if (algorithm.equalsIgnoreCase("LinearRegression")) {
            analyticProcess = new LinearRegressionAnalyticProcess();
        }
        if (outputformat.equalsIgnoreCase("csv")) {
            outputFormat = new CSVOutputFormat();
        } else if (outputformat.equalsIgnoreCase("arff")) {
            outputFormat = new ArffOutputFormat();
        }
        
        AnalyticsInfo analyticsInfo = new AnalyticsInfo();
        analyticsInfo.setAnalyticProcess(analyticProcess);
        analyticsInfo.setOutputformat(outputFormat);
        
        return analyticsInfo;

    }

}
