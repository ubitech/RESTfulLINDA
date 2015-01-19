/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.associations;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.AnalyticProcess;
import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.formats.OutputFormat;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctionsSingleton;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.associations.Apriori;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

/**
 *
 * @author eleni
 */
public class AprioriAnalyticProcess extends AnalyticProcess {

    HelpfulFunctionsSingleton helpfulFunctions;
    InputFormat input;

    public AprioriAnalyticProcess(InputFormat input) {
        helpfulFunctions = HelpfulFunctionsSingleton.getInstance();
        helpfulFunctions.nicePrintMessage("Create analytic process for Apriori");
        //aprioriOutput = new AprioriOutput(in);
        this.input = input;

    }

    @Override
    public void train(Analytics analytics) {

    }

    //Get Apriori Rules
    @Override
    public void eval(Analytics analytics, OutputFormat out) {

        try {
            helpfulFunctions.nicePrintMessage("Eval Apriori");

            //clean previous eval info if exists
            helpfulFunctions.cleanPreviousInfo(analytics);

            AbstractList<Instance> abstractListdata;
            Instances data;

            // remove dataset metadata (first two columns) 
             if (helpfulFunctions.isRDFInputFormat(analytics.getEvaluationQuery_id()))
            {
                abstractListdata = input.importData4weka(Integer.toString(analytics.getEvaluationQuery_id()), true, analytics);
                data = (Instances) abstractListdata;
                HashMap<String, Instances> separatedData = helpfulFunctions.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");

            } else if (helpfulFunctions.isRDFExportFormat(analytics.getExportFormat())) {

                abstractListdata = input.importData4weka(Configuration.analyticsRepo + analytics.getDocument(), true, analytics);
                data = (Instances) abstractListdata;
                HashMap<String, Instances> separatedData = helpfulFunctions.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");
            } else {

                abstractListdata = input.importData4weka(Configuration.analyticsRepo + analytics.getDocument(), false, analytics);
                data = (Instances) abstractListdata;

            }

            weka.filters.unsupervised.attribute.StringToNominal ff = new weka.filters.unsupervised.attribute.StringToNominal(); // new instance of filter
            ff.setAttributeRange("1-" + data.numAttributes());
            ff.setInputFormat(data);        // inform filter about dataset **AFTER** setting options
            data = Filter.useFilter(data, ff);

            data.setClassIndex(data.numAttributes() - 1);

            // build associator
            Apriori apriori = new Apriori();
            apriori.setClassIndex(data.classIndex());
            apriori.buildAssociations(data);
            apriori.setVerbose(true);
            apriori.globalInfo();

            // output associator
            System.out.println(apriori);

            helpfulFunctions.writeToFile(apriori.toString(), "processinfo", analytics);

        } catch (Exception ex) {
            Logger.getLogger(AprioriAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
            ConnectionController.getInstance().updateProcessMessageToAnalyticsTable(ex.toString(), analytics.getId());
//            helpfulFunctions.updateProcessMessageToAnalyticsTable(ex.toString(), analytics.getId());
        }

    }

}
