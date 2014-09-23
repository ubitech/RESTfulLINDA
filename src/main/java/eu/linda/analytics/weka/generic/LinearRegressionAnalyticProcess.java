/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.generic;

import eu.linda.analytic.controller.AnalyticProcess;
import eu.linda.analytic.formats.InputFormat;
import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFuncions;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.functions.LinearRegression;

/**
 *
 * @author eleni
 */
public class LinearRegressionAnalyticProcess extends AnalyticProcess {

    LinearRegressionOutput linearRegressionOutput;
    HelpfulFuncions helpfulFuncions;

    public LinearRegressionAnalyticProcess(InputFormat in) {
        
        helpfulFuncions = new HelpfulFuncions();
        helpfulFuncions.nicePrintMessage("Create analytic process for LinearRegression");
        linearRegressionOutput = new LinearRegressionOutput(in);  
        
    }

    @Override
    public void train(Analytics analytics) {
        helpfulFuncions.nicePrintMessage("Train LinearRegression");

        try {
            LinearRegression linearRegressionmodel = linearRegressionOutput.getLinearRegressionEstimations(Configuration.docroot + analytics.getDocument());
            helpfulFuncions.saveModel(linearRegressionmodel, analytics);
            
        } catch (Exception ex) {
            Logger.getLogger(LinearRegressionAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

       
    }

    @Override
    public String eval(Analytics analytics) {

     String results = null;
        try {
            results = linearRegressionOutput.getLinearRegressionResults(analytics);
        } catch (Exception ex) {
            Logger.getLogger(LinearRegressionAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

     return results;  
     //connectionController.writeToFile(results, "resultdocument", analytics);

    
    }

}
