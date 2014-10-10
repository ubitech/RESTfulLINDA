/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.associations;

import eu.linda.analytic.controller.AnalyticProcess;
import eu.linda.analytic.formats.InputFormat;
import eu.linda.analytics.config.Configuration;
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

/**
 *
 * @author eleni
 */
public class AprioriAnalyticProcess extends AnalyticProcess {

    _AprioriOutput aprioriOutput;
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
    public AbstractList eval(Analytics analytics) {

        helpfulFunctions.nicePrintMessage("Eval Apriori");
        JSONArray jsonresult = null;
        try {
            //jsonresult = aprioriOutput.getAprioriRules(Configuration.docroot + analytics.getDocument());

            AbstractList<Instance> data1 = input.importData(Configuration.docroot + analytics.getDocument());
            Instances data = (Instances) data1;
            
            
            // remove dataset metadata (first two columns)    
            if (analytics.getExportFormat().equalsIgnoreCase("rdf")) {
                HashMap<String, Instances> separatedData = helpfulFunctions.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");
            }
       
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
            Logger.getLogger(_AprioriOutput.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Apriori returns an empty list
        List list = new LinkedList();
        return (AbstractList) list;

//helpfulFuncions.writeToFile(jsonresult.getString(1), "resultdocument", analytics);
        //result = jsonresult.toString();
    }

}
