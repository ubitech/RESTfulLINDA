/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.associations;

import eu.linda.analytic.formats.InputFormat;
import java.util.AbstractList;
import org.json.JSONArray;
import weka.associations.Apriori;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class _AprioriOutput {
    InputFormat input;

    public _AprioriOutput(InputFormat input) {
        this.input = input;
    }

    public JSONArray getAprioriRules(String datasourcePath) throws Exception {
        
        AbstractList<Instance> data1 = input.importData(datasourcePath);
        Instances data = (Instances) data1;
        
        //Instances data = DataSource.read(datasourcePath);
        //Instances data = DataSource.read("/opt/weka-3-7-10/data/supermarket.arff");
        data.setClassIndex(data.numAttributes() - 1);

        // build associator
        Apriori apriori = new Apriori();
        apriori.setClassIndex(data.classIndex());
        apriori.buildAssociations(data);
        apriori.setVerbose(true);
        apriori.globalInfo();

        // output associator
        System.out.println(apriori);
        
        JSONArray resultjson = new JSONArray();
        resultjson.put(0, apriori.toString());
        resultjson.put(1, apriori.toString());

        return resultjson;
        
   

    }
    

    /**
     * Expects a dataset as first parameter. The last attribute is used as class
     * attribute.
     *
     * @param args	the command-line parameters
     * @throws Exception	if something goes wrong
     */
    public static void main(String[] args) throws Exception {
        /**
         * Expects a dataset as first parameter. The last attribute is used as
         * class attribute.
         *
         * @param args	the command-line parameters
         * @throws Exception	if something goes wrong
         */
    // load data
        //Instances data = DataSource.read(args[0]);
        Instances data = DataSource.read("/opt/weka-3-7-10/data/supermarket.arff");
        data.setClassIndex(data.numAttributes() - 1);

        // build associator
        Apriori apriori = new Apriori();
        apriori.setClassIndex(data.classIndex());
        apriori.buildAssociations(data);

        // output associator
        System.out.println(apriori);

    }
}
