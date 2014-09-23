/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytic.controller;

import eu.linda.analytic.formats.ArffInputFormat;
import eu.linda.analytic.formats.ArffOutputFormat;
import eu.linda.analytic.formats.CSVOutputFormat;
import eu.linda.analytic.formats.InputFormat;
import eu.linda.analytic.formats.OutputFormat;
import eu.linda.analytics.weka.classifiers.J48AnalyticProcess;
import eu.linda.analytics.weka.classifiers.M5PAnalyticProcess;
import eu.linda.analytics.weka.generic.LinearRegressionAnalyticProcess;

/**
 *
 * @author eleni
 */
public class AnalyticsFactory {

    public AnalyticsInfo createAnalytics(String inputformat, String algorithm, String outputformat) {

        InputFormat inputFormat = null;
        AnalyticProcess analyticProcess = null;
        OutputFormat outputFormat = null;

        //Create Instances of InputFormat
        if (inputformat.equalsIgnoreCase("arff")) {
            inputFormat = new ArffInputFormat();
        }
        
        
        
        //Create AnalyticProcesses
        if (algorithm.equalsIgnoreCase("J48")) {
            analyticProcess = new J48AnalyticProcess(inputFormat);
        } else if (algorithm.equalsIgnoreCase("M5P")) {
            analyticProcess = new M5PAnalyticProcess(inputFormat);
        } else if (algorithm.equalsIgnoreCase("LinearRegression")) {
            analyticProcess = new LinearRegressionAnalyticProcess(inputFormat);
        }

        //Create Instances of OutputFormat
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
