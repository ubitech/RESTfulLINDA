/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.classifiers;

import weka.associations.Apriori;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.search.SearchAlgorithm;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class BayesNetOutput {

    public BayesNetOutput() {
    }

    public BayesNet getHotSpotInfo(String datasourcePath) throws Exception {
        System.out.println("datasourcePath" + datasourcePath);
        Instances train = ConverterUtils.DataSource.read(datasourcePath);
        //Instances data = DataSource.read("/opt/weka-3-7-10/data/supermarket.arff");
        // set class index
        train.setClassIndex(train.numAttributes() - 1);

        // create test data set
        Instances test = ConverterUtils.DataSource.read(datasourcePath);
        // set class index
        test.setClassIndex(train.numAttributes() - 1);

        // build associator
        BayesNet bayesNet = new BayesNet();
        
        SearchAlgorithm searchAlgorithm =new SearchAlgorithm();
     
        bayesNet.setSearchAlgorithm(new SearchAlgorithm());
       
        // output associator
        System.out.println(bayesNet);

        return bayesNet;

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
        Instances data = ConverterUtils.DataSource.read("/opt/weka-3-7-10/data/supermarket.arff");
        data.setClassIndex(data.numAttributes() - 1);

        // build associator
        Apriori apriori = new Apriori();
        apriori.setClassIndex(data.classIndex());
        apriori.buildAssociations(data);
        apriori.getVerbose();
        apriori.globalInfo();

        // output associator
        System.out.println(apriori);

    }
}
