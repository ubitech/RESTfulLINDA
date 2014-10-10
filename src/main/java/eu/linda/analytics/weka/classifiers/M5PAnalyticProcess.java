/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.classifiers;

import eu.linda.analytic.controller.AnalyticProcess;
import eu.linda.analytic.formats.InputFormat;
import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctions;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.trees.M5P;
import weka.core.Attribute;
import weka.core.Debug;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.SparseInstance;

/**
 *
 * @author eleni
 */
public class M5PAnalyticProcess extends AnalyticProcess {

    //M5POutput m5pOutput;
    HelpfulFunctions helpfulFunctions;
    InputFormat input;

    public M5PAnalyticProcess(InputFormat input) {
        helpfulFunctions = new HelpfulFunctions();
        helpfulFunctions.nicePrintMessage("Create analytic process for M5P");
        //m5pOutput = new M5POutput(in);
        this.input = input;

    }

//trainModelM5P
    @Override
    public void train(Analytics analytics) {
        helpfulFunctions.nicePrintMessage("Train  M5P");
        Vector M5Pmodel;
        try {
            //  M5Pmodel = m5pOutput.trainModelM5P(Configuration.docroot + analytics.getDocument());
            AbstractList<Instance> data1 = input.importData(Configuration.docroot + analytics.getDocument());
            Instances data = (Instances) data1;
            
             // remove dataset metadata (first two columns)    
            if (analytics.getExportFormat().equalsIgnoreCase("rdf")) {
                HashMap<String, Instances> separatedData = helpfulFunctions.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");
            }
            
            data.setClassIndex(data.numAttributes() - 1);

            // train M5P
            M5P cl = new M5P();
            cl.setMinNumInstances(4.0);
            cl.setUnpruned(true);
            cl.setUseUnsmoothed(true);
            cl.setBuildRegressionTree(true);
            cl.setSaveInstances(true);
            cl.buildClassifier(data);
            // save model + header
            M5Pmodel = new Vector();
            M5Pmodel.add(cl);
            M5Pmodel.add(new Instances(data, 0));

            helpfulFunctions.saveModelasVector(M5Pmodel, analytics);

        } catch (Exception ex) {
            Logger.getLogger(M5PAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//predictM5P
    @Override
    public AbstractList eval(Analytics analytics) {
        helpfulFunctions.nicePrintMessage("Eval M5P");
        HashMap<String, Instances> separatedData = null;
        AbstractList dataToReturn = null;
        try {
            //jsonresult = m5pOutput.predictM5P(analytics);

            AbstractList<Instance> data1 = input.importData(Configuration.docroot + analytics.getTestdocument());
            Instances data = (Instances) data1;
            
             if (analytics.getExportFormat().equalsIgnoreCase("rdf")) {
                separatedData = helpfulFunctions.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");
            }
            
            
            data.setClassIndex(data.numAttributes() - 1);
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
            atts.add(new Attribute("actual", 1));
            atts.add(new Attribute("predicted", 2));
            atts.add(new Attribute("error", 3));

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
                System.out.println("======(atts.get(0)" + atts.get(0));

                inst1.setValue(atts.get(0), i);
                inst1.setValue(atts.get(1), inst.classValue());
                inst1.setValue(atts.get(2), pred);
                inst1.setValue(atts.get(3), error);
                instances.add(i, inst1);

            }

            Instances preparedata = helpfulFunctions.createArffFileFromArray(atts, instances);
            
             if (analytics.getExportFormat().equalsIgnoreCase("rdf")) {
                Instances mergedData = helpfulFunctions.mergeDataAndMetadataInfo(preparedata, separatedData.get("metaData"));
                dataToReturn= mergedData;

            } else {
                 dataToReturn = preparedata;
            }
            
            
            
            helpfulFunctions.writeToFile(eval.toSummaryString(), "processinfo", analytics);

        } catch (Exception ex) {
            Logger.getLogger(M5PAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dataToReturn;

    }

}
