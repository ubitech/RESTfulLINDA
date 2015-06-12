/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.classifiers;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.AnalyticProcess;
import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.formats.OutputFormat;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.Util;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.trees.M5P;
import weka.core.Attribute;
import weka.core.Debug;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author eleni
 */
public class M5PAnalyticProcess extends AnalyticProcess {

    InputFormat input;

    public M5PAnalyticProcess(InputFormat input) {
        Util.nicePrintMessage("Create analytic process for M5P");
        //m5pOutput = new M5POutput(in);
        this.input = input;

    }

//trainModelM5P
    @Override
    public void train(Analytics analytics) {

        float timeToRun_analytics = 0;
        long startTimeToRun_analytics = System.currentTimeMillis();

        //clean previous eval info if exists
        Util.cleanPreviousInfo(analytics);
        analytics.setTimeToGet_data(0);
        analytics.setTimeToRun_analytics(0);
        analytics.setData_size(0);
        analytics.setTimeToCreate_RDF(0);

        Util.nicePrintMessage("Train  M5P");
        Vector M5Pmodel;
        try {
            //  M5Pmodel = m5pOutput.trainModelM5P(Configuration.docroot + analytics.getDocument());
            AbstractList<Instance> abstractListdata1;
            Instances data;

            // remove dataset metadata (first two columns)    
            if (Util.isRDFInputFormat(analytics.getEvaluationQuery_id())) {
                abstractListdata1 = input.importData4weka(Integer.toString(analytics.getEvaluationQuery_id()),"", true, analytics);
                data = (Instances) abstractListdata1;
                HashMap<String, Instances> separatedData = Util.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");

            } else if (Util.isRDFExportFormat(analytics.getExportFormat())) {
                abstractListdata1 = input.importData4weka(Configuration.analyticsRepo + analytics.getDocument(),"", true, analytics);
                data = (Instances) abstractListdata1;
                HashMap<String, Instances> separatedData = Util.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");
            } else {

                abstractListdata1 = input.importData4weka(Configuration.analyticsRepo + analytics.getDocument(),"", false, analytics);
                data = (Instances) abstractListdata1;

            }

            data.setClassIndex(data.numAttributes() - 1);

            // train M5P
            M5P cl = new M5P();
            cl.setMinNumInstances(4.0);
            cl.setUnpruned(true);
            cl.setUseUnsmoothed(true);
            cl.setBuildRegressionTree(true);
            cl.setSaveInstances(true);
            cl.setDebug(true);
            cl.buildClassifier(data);
            
           
            // save model + header
            M5Pmodel = new Vector();
            M5Pmodel.add(cl);
            //M5Pmodel.add(new Instances(data, 0));
            M5Pmodel.add(new Instances(data));

            //helpfulFunctions.saveModelasVector(M5Pmodel, analytics);
            cl.buildClassifier(data);
            Util.saveModel(cl, analytics);

        } catch (Exception ex) {
            Logger.getLogger(M5PAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
        // Get elapsed time in seconds
        timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
        analytics.setTimeToRun_analytics(timeToRun_analytics);
    }

//predictM5P
    @Override
    public void eval(Analytics analytics, OutputFormat out) {
        float timeToRun_analytics = 0;
        long startTimeToRun_analytics = System.currentTimeMillis();
        Util.nicePrintMessage("Eval M5P");

        HashMap<String, Instances> separatedData = null;
        AbstractList dataToReturn = null;
        try {

            AbstractList<Instance> abstractList;
            Instances data;

            if (Util.isRDFInputFormat(analytics.getEvaluationQuery_id())) {

                abstractList = input.importData4weka(Integer.toString(analytics.getTrainQuery_id()), "",true, analytics);
                data = (Instances) abstractList;
                separatedData = Util.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");

            } else if (Util.isRDFExportFormat(analytics.getExportFormat())) {
                abstractList = input.importData4weka(Configuration.analyticsRepo + analytics.getTestdocument(),"", true, analytics);
                data = (Instances) abstractList;
                separatedData = Util.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");
            } else {
                abstractList = input.importData4weka(Configuration.analyticsRepo + analytics.getTestdocument(),"", false, analytics);
                data = (Instances) abstractList;
            }

            data.setClassIndex(data.numAttributes() - 1);
           // Vector v = (Vector) SerializationHelper.read(Configuration.analyticsRepo + analytics.getModel());

            M5P model = (M5P) weka.core.SerializationHelper.read(Configuration.analyticsRepo + analytics.getModel());

            //model evaluation
            Evaluation eval = new Evaluation(data);
            StringBuffer forPredictionsPrinting = new StringBuffer();
            PlainText output = new PlainText();
            output.setBuffer(forPredictionsPrinting);
            weka.core.Range attsToOutput = null;
            Boolean outputDistribution = true;

            //Classifier cl = (Classifier) v.get(0);
            //Instances header = (Instances) v.get(1);

            eval.crossValidateModel(model, data, 10, new Debug.Random(1),
                    output, attsToOutput, outputDistribution);

            // output predictions
            ArrayList<Attribute> atts = new ArrayList<Attribute>();
            atts.add(new Attribute("inst#", 0));
            atts.add(new Attribute("actual", 1));
            atts.add(new Attribute("predicted", 2));
            atts.add(new Attribute("error", 3));

            List<Instance> instances = new ArrayList<Instance>();

            // System.out.println("inst# ,    actual, ->  predicted ,   error");
            String result = "inst# ,    actual,   predicted ,   error  \n";
            int dataLength = data.numAttributes();

            for (int i = 0; i < data.numInstances(); i++) {
                Instance curr = data.instance(i);

                // create an instance for the classifier that fits the training data
                // Instances object returned here might differ slightly from the one
                // used during training the classifier, e.g., different order of
                // nominal values, different number of attributes.
                Instance inst = data.lastInstance();
                inst.setDataset(data);
                for (int n = 0; n < data.numAttributes(); n++) {
                    Attribute att = data.attribute(data.attribute(n).name());
                    // original attribute is also present in the current dataset
                    if (att != null) {
                        if (att.isNominal()) {
                            // is this label also in the original data?
                            // Note:
                            // "numValues() > 0" is only used to avoid problems with nominal 
                            // attributes that have 0 labels, which can easily happen with
                            // data loaded from a database
                            if ((data.attribute(n).numValues() > 0) && (att.numValues() > 0)) {
                                String label = curr.stringValue(att);
                                int index = data.attribute(n).indexOfValue(label);
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
                double pred = model.classifyInstance(inst);
                //double error = pred - inst.classValue();
                curr.setClassValue(pred);

            }

            if (Util.isRDFExportFormat(analytics.getExportFormat())) {
                Instances mergedData = Util.mergeDataAndMetadataInfo(data, separatedData.get("metaData"));
                dataToReturn = mergedData;

            } else {
                dataToReturn = data;
            }

            Util.writeToFile(eval.toSummaryString(), "processinfo", analytics);

        } catch (Exception ex) {
            Logger.getLogger(M5PAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
        // Get elapsed time in seconds
        timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
        analytics.setTimeToRun_analytics(analytics.getTimeToRun_analytics() + timeToRun_analytics);
        out.exportData(analytics, dataToReturn);

        out.exportData(analytics, dataToReturn);

    }

}
