/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.classifiers;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFuncions;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.json.JSONArray;
import weka.classifiers.*;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.trees.*;
import weka.core.*;
import weka.core.Instance;
import weka.core.converters.ConverterUtils;
import weka.experiment.*;

/**
 *
 * @author eleni
 */
public class M5POutput {
    
     HelpfulFuncions helpfulFuncions = new HelpfulFuncions();

    public M5POutput() {
        
       
    }

    public Vector trainModelM5P(String datasourcePath) throws Exception {

        System.out.println("Training...");

        /* load training data from database
         InstanceQuery query = new InstanceQuery();
         query.setDatabaseURL(URL);
         query.setUsername(USER);
         query.setPassword(PASSWORD);
         query.setQuery("select * from Results0");
         Instances data = query.retrieveInstances();
         data.setClassIndex(13);
         */
        //get datasource
        Instances data = ConverterUtils.DataSource.read(datasourcePath);
        //Instances data = DataSource.read("/opt/weka-3-7-10/data/supermarket.arff");
        data.setClassIndex(data.numAttributes() - 1);
        // train M5P
        M5P cl = new M5P();
        // further options...

        cl.setMinNumInstances(4.0);
        cl.setUnpruned(true);
        cl.setUseUnsmoothed(true);
        cl.setBuildRegressionTree(true);
        cl.setSaveInstances(true);

        cl.buildClassifier(data);

        // save model + header
        Vector v = new Vector();
        v.add(cl);
        v.add(new Instances(data, 0));
        //SerializationHelper.write(FILENAME, v);
        return v;

    }

    public JSONArray predictM5P(Analytics analytics) throws Exception {

        System.out.println("Predicting...");

        Instances data = ConverterUtils.DataSource.read(Configuration.docroot + analytics.getTestdocument());
        data.setClassIndex(data.numAttributes() - 1);

        //Classifier model  
        //Classifier model = (M5P) weka.core.SerializationHelper.read(Configuration.docroot + analytics.getModel());
        System.out.println("Predicting...");

        // read model and header
        Vector v = (Vector) SerializationHelper.read(Configuration.docroot + analytics.getModel());

        //model evaluation
        Evaluation eval = new Evaluation(data);
        StringBuffer forPredictionsPrinting = new StringBuffer();
        PlainText output = new PlainText();
        output.setBuffer(forPredictionsPrinting);
        weka.core.Range attsToOutput = null;
        Boolean outputDistribution = new Boolean(true);

        Classifier cl = (Classifier) v.get(0);
        Instances header = (Instances) v.get(1);

        eval.crossValidateModel(cl, data, 10, new Debug.Random(1),
                output, attsToOutput, outputDistribution);

        // output predictions
        ArrayList<Attribute> atts = new ArrayList<Attribute>();
        atts.add(new Attribute("inst#", 0));
        atts.add(new Attribute("actual",1));
        atts.add(new Attribute("predicted",2));
        atts.add(new Attribute("error",3));
        

        List<Instance> instances = new ArrayList<Instance>();

        System.out.println("inst# ,    actual, ->  predicted ,   error");
        String result = "inst# ,    actual,   predicted ,   error  \n";
        for (int i = 0; i < data.numInstances(); i++) {
            Instance curr = data.instance(i);

            // create an instance for the classifier that fits the training data
            // Instances object returned here might differ slightly from the one
            // used during training the classifier, e.g., different order of
            // nominal values, different number of attributes.
            Instance inst = data.lastInstance();
            inst.setDataset(header);
            for (int n = 0; n < header.numAttributes(); n++) {
                Attribute att = data.attribute(header.attribute(n).name());
                // original attribute is also present in the current dataset
                if (att != null) {
                    if (att.isNominal()) {
                        // is this label also in the original data?
                        // Note:
                        // "numValues() > 0" is only used to avoid problems with nominal 
                        // attributes that have 0 labels, which can easily happen with
                        // data loaded from a database
                        if ((header.attribute(n).numValues() > 0) && (att.numValues() > 0)) {
                            String label = curr.stringValue(att);
                            int index = header.attribute(n).indexOfValue(label);
                            if (index != -1) {
                                inst.setValue(n, index);
                            }
                        }
                    } else if (att.isNumeric()) {
                        inst.setValue(n, curr.value(att));
                    } else {
                        throw new IllegalStateException("Unhandled attribute type!");
                    }
                }
            }

            // predict class
            double pred = cl.classifyInstance(inst);
            double error = pred - inst.classValue();
            result += i + " , " + inst.classValue() + " , " + pred + " , " + error + "\n";

            // Create empty instance with three attribute values
            Instance inst1 = new SparseInstance(3);
            

           // Set instance's values for the attributes "length", "weight", and "position"
            System.out.println("======(atts.get(0)"+atts.get(0));
            System.out.println("======(atts.get(1)"+atts.get(1));

            System.out.println("======(atts.get(2)"+atts.get(2));

            System.out.println("======(atts.get(3)"+atts.get(3));

            inst1.setValue(atts.get(0), i);
            inst1.setValue(atts.get(1), inst.classValue());
            inst1.setValue(atts.get(2), pred);
            inst1.setValue(atts.get(3), error);
           
            instances.add(i, inst1);

        }
        
        
        
        JSONArray resultjson = new JSONArray();
        resultjson.put(0, eval.toSummaryString());
        resultjson.put(1, helpfulFuncions.createArffFileFromArray(atts, instances).toString());

        System.out.println("Predicting finished!");

        return resultjson;

    }
/*
    public static void main(String[] args) throws Exception {
        M5POutput m = new M5POutput();
        Vector model = m.trainModelM5P("/home/eleni/Desktop/mydatasets/forestfires.arff");
        //m.predictM5P(null);

        Instances data = ConverterUtils.DataSource.read("/home/eleni/Desktop/mydatasets/forestfires.arff");
        data.setClassIndex(data.numAttributes() - 1);

        System.out.println("Training...");

        // train M5P
        M5P cl = new M5P();
        // further options...
        cl.buildClassifier(data);

        // save model + header
        Vector v = new Vector();
        v.add(cl);
        v.add(new Instances(data, 0));
        SerializationHelper.write("/home/eleni/test.model", v);

        System.out.println("Training finished!");

        System.out.println("Predicting...");

        // read model and header
        Vector v1 = (Vector) SerializationHelper.read("/home/eleni/test.model");
        Classifier cl1 = (Classifier) v1.get(0);
        Instances header = (Instances) v1.get(1);

        // output predictions
        System.out.println("actual -> predicted");
        for (int i = 0; i < data.numInstances(); i++) {
            Instance curr = data.instance(i);
            // create an instance for the classifier that fits the training data
            // Instances object returned here might differ slightly from the one
            // used during training the classifier, e.g., different order of
            // nominal values, different number of attributes.
            Instance inst = data.lastInstance();
            inst.setDataset(header);
            for (int n = 0; n < header.numAttributes(); n++) {
                Attribute att = data.attribute(header.attribute(n).name());
                // original attribute is also present in the current dataset
                if (att != null) {
                    if (att.isNominal()) {
                        // is this label also in the original data?
                        // Note:
                        // "numValues() > 0" is only used to avoid problems with nominal 
                        // attributes that have 0 labels, which can easily happen with
                        // data loaded from a database
                        if ((header.attribute(n).numValues() > 0) && (att.numValues() > 0)) {
                            String label = curr.stringValue(att);
                            int index = header.attribute(n).indexOfValue(label);
                            if (index != -1) {
                                inst.setValue(n, index);
                            }
                        }
                    } else if (att.isNumeric()) {
                        inst.setValue(n, curr.value(att));
                    } else {
                        throw new IllegalStateException("Unhandled attribute type!");
                    }
                }
            }

            // predict class
            double pred = cl1.classifyInstance(inst);
            System.out.println(inst.classValue() + " -> " + pred);
        }

        System.out.println("Predicting finished!");
    }
    */
}
