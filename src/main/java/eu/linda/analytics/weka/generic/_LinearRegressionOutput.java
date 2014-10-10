/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.generic;

import eu.linda.analytic.formats.InputFormat;
import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.model.Analytics;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.AbstractList;
import org.json.JSONArray;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;

public class _LinearRegressionOutput {

    InputFormat input;

    public _LinearRegressionOutput(InputFormat input) {
        this.input = input;
    }

    public LinearRegression getLinearRegressionEstimations(String datasourcePath,boolean datasetContainsMetadataInfo) throws Exception {


        //load data
        boolean excludeMetadataInfo = datasetContainsMetadataInfo;
        AbstractList<Instance> data1 = input.importData(datasourcePath);
        Instances data = (Instances) data1;
        data.setClassIndex(data.numAttributes() - 1);
        
        //build model
        LinearRegression model = new LinearRegression();
        model.buildClassifier(data); //the last instance with missing    class is   not used
        System.out.println(model);

        //classify the last instance
        Instance instancesToPredict = data.lastInstance();
        double price = model.classifyInstance(instancesToPredict);

        return model;

    }

    public JSONArray getLinearRegressionResults(Analytics analytics) throws Exception {

        //load data
        AbstractList<Instance> data1 = input.importData(Configuration.docroot + analytics.getTestdocument());
        Instances data = (Instances) data1;
        
        boolean datasetContainsMetadataInfo = false;
        if (analytics.getExportFormat().equalsIgnoreCase("rdf")) {
            datasetContainsMetadataInfo = true;
        }


        //data.setClassIndex(data.numAttributes() - 1);
        
        //Instances data = ConverterUtils.DataSource.read(Configuration.docroot + analytics.getTestdocument());

        //load model
        LinearRegression model = (LinearRegression) weka.core.SerializationHelper.read(Configuration.docroot + analytics.getModel());

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

        JSONArray resultjson = new JSONArray();
        resultjson.put(0, "");
        resultjson.put(1, labeled.toString());

        return resultjson;

    }

    public static void main(String args[]) throws Exception {
//load data
        //Instances data = new Instances(new BufferedReader(new FileReader("/home/eleni/Desktop/house.arff")));

        Instances data = new Instances(new BufferedReader(new FileReader("/home/eleni/Desktop/mydatasets/autoMpg.arff")));

        data.setClassIndex(data.numAttributes() - 1);
//build model
        LinearRegression model = new LinearRegression();
        model.buildClassifier(data); //the last instance with missing      class is   not used
        System.out.println(model);
//classify the last instance

        Instance myHouse = data.lastInstance();
        double price = model.classifyInstance(myHouse);

        //System.out.println ("My house ("+myHouse+"): "+price);
    }
}
