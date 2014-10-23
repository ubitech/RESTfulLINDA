/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.associations;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.AnalyticProcess;
import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.formats.OutputFormat;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctions;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import weka.associations.Apriori;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

/**
 *
 * @author eleni
 */
public class AprioriAnalyticProcess extends AnalyticProcess {

    HelpfulFunctions helpfulFunctions;
    InputFormat input;

    public AprioriAnalyticProcess(InputFormat input) {
        helpfulFunctions = new HelpfulFunctions();
        helpfulFunctions.nicePrintMessage("Create analytic process for Apriori");
        //aprioriOutput = new AprioriOutput(in);
        this.input = input;

    }

    @Override
    public void train(Analytics analytics) {

    }

    //Get Apriori Rules
    @Override
    public void eval(Analytics analytics,OutputFormat out) {

        try {
            helpfulFunctions.nicePrintMessage("Eval Apriori");

            AbstractList<Instance> abstractListdata;
            Instances data;

            // remove dataset metadata (first two columns)    
            if (analytics.getExportFormat().equalsIgnoreCase("rdf")) {

                abstractListdata = input.importData4weka(Configuration.docroot + analytics.getDocument(), true);
                data = (Instances) abstractListdata;
                HashMap<String, Instances> separatedData = helpfulFunctions.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");
            } else {

                abstractListdata = input.importData4weka(Configuration.docroot + analytics.getDocument(), false);
                data = (Instances) abstractListdata;

            }

            weka.filters.unsupervised.attribute.StringToNominal ff = new weka.filters.unsupervised.attribute.StringToNominal(); // new instance of filter
            ff.setAttributeRange("1-"+data.numAttributes());
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
            helpfulFunctions.updateProcessMessageToAnalyticsTable(ex.toString(),analytics.getId());
        }

        
        //Apriori returns an empty list
        // List list = new LinkedList();
        // return (AbstractList) list;

//helpfulFuncions.writeToFile(jsonresult.getString(1), "resultdocument", analytics);
        //result = jsonresult.toString();
    }

}
