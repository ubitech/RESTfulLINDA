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
import org.json.JSONArray;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class LinearRegressionOutput {

    InputFormat in;

    public LinearRegressionOutput(InputFormat in) {
        this.in = in;
    }

    public LinearRegression getLinearRegressionEstimations(String datasourcePath) throws Exception {

        System.out.println("datasourcePath edoooo: " + datasourcePath);
        //load data
        Instances data = ConverterUtils.DataSource.read(datasourcePath);
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

    public String getLinearRegressionResults(Analytics analytics) throws Exception {

        //load data
        Instances data = ConverterUtils.DataSource.read(Configuration.docroot + analytics.getTestdocument());

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

        return labeled.toString();

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
