/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.classifiers;

import eu.linda.analytic.formats.InputFormat;
import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.model.Analytics;
import java.util.AbstractList;
import org.json.JSONArray;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.trees.J48;
import weka.core.Debug.Random;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class J48Output {

    InputFormat input;

    public J48Output(InputFormat in) {
        this.input = in;
    }

    public Classifier getJ48TreeModel(String datasourcePath) throws Exception {
        System.out.println("datasourcePath" + datasourcePath);
        //Instances data = ConverterUtils.DataSource.read(datasourcePath);
        //Instances data = DataSource.read("/opt/weka-3-7-10/data/supermarket.arff");
        //data.setClassIndex(data.numAttributes() - 1);
        AbstractList<Instance> data1 = input.importData(datasourcePath);
        Instances data = (Instances) data1;
        data.setClassIndex(data.numAttributes() - 1);
        J48 tree = new J48(); // new instance of tree

        String[] options = new String[1];
        options[0] = "-C 1.0 –M 5"; // confidenceFactor = 1.0, minNumObject = 5

        tree.setOptions(options);// set the options
        tree.buildClassifier(data); // build classifier

        // output associator
        System.out.println(tree);
        return tree;

    }

    public JSONArray getJ48TreeResultDataset(Analytics analytics) throws Exception {
        System.out.println("datasourcePath" + Configuration.docroot + analytics.getDocument());

        //Train Data
        AbstractList<Instance> data1 = input.importData(Configuration.docroot + analytics.getDocument());
        Instances traindata = (Instances) data1;
        traindata.setClassIndex(traindata.numAttributes() - 1);
        //Instances traindata = ConverterUtils.DataSource.read(Configuration.docroot + analytics.getDocument());
        //traindata.setClassIndex(traindata.numAttributes() - 1);

        
        
        //Test data
        AbstractList<Instance> data2 = input.importData(Configuration.docroot + analytics.getDocument());
        Instances testdata = (Instances) data2;
        testdata.setClassIndex(testdata.numAttributes() - 1);
        //Instances testdata = ConverterUtils.DataSource.read(Configuration.docroot + analytics.getDocument());
        //testdata.setClassIndex(testdata.numAttributes() - 1);

        //Classifier model  
        Classifier model = (Classifier) weka.core.SerializationHelper.read(Configuration.docroot + analytics.getModel());

        Evaluation eval = new Evaluation(traindata);
        //eval.evaluateModel(model, testdata);

        //evaluation 1
        //String[] options = new String[2];
        //options[0] = "-t";
        //options[1] = Configuration.docroot + analytics.getDocument();
        //System.out.println("evaluation an other try" + Evaluation.evaluateModel(model, options));

        // evaluation 2
        StringBuffer forPredictionsPrinting = new StringBuffer();
        PlainText output = new PlainText();
        output.setBuffer(forPredictionsPrinting);
        weka.core.Range attsToOutput = null;
        Boolean outputDistribution = new Boolean(true);
        eval.crossValidateModel(model, traindata, 10, new Random(1),
                output, attsToOutput, outputDistribution);

        System.out.println(eval.toSummaryString("\nResults\n======\n", false));

        String predictionsToString = "";
        FastVector predictions = eval.predictions();
        for (int i = 0; i < eval.predictions().size(); i++) {

            predictionsToString += predictions + "\n";
        }

        //evaluation 3
        // load unlabeled data
        Instances unlabeled = testdata;

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
        resultjson.put(0, eval.toSummaryString());
        resultjson.put(1, labeled.toString());

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

        /*
         Instances data = DataSource.read("/opt/weka-3-7-10/data/ionosphere.arff");
         data.setClassIndex(data.numAttributes() - 1);

         J48 tree = new J48(); // new instance of tree

         String[] options = new String[1];
         options[0] = "-C 1.0 –M 5"; // confidenceFactor = 1.0, minNumObject = 5

         tree.setOptions(options);// set the options
         tree.buildClassifier(data); // build classifier
        
         // output associator
         System.out.println(tree);
         */
    }
}
