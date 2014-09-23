/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.associations;

import weka.associations.Apriori;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class AprioriOutput {

    public AprioriOutput() {
    }

    public Apriori getAprioriRules(String datasourcePath) throws Exception {
        System.out.println("datasourcePath"+datasourcePath);
        Instances data = DataSource.read(datasourcePath);
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
        return apriori;

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
