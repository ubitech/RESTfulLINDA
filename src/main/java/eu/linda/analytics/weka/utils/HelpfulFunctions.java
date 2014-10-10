package eu.linda.analytics.weka.utils;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Generates a little ARFF file with different attribute types.
 *
 * @author FracPete
 */
public class HelpfulFunctions {

    public HelpfulFunctions() {

    }

    public Analytics saveModel(Classifier model, Analytics analytics) throws Exception {
        String[] splitedSourceFileName = analytics.getDocument().split(".arff");

        String targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "Model" + ".model").replace("datasets", "models");

        String targetFileNameTXT = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "ModelReadable" + ".txt").replace("datasets", "models");

        System.out.println("targetFileName: " + targetFileName);
        try {

            // serialize && save model
            weka.core.SerializationHelper.write(Configuration.docroot + targetFileName, model);

            DBSynchronizer dbsynchronizer = new DBSynchronizer();
            dbsynchronizer.updateLindaAnalyticsModel(targetFileName, analytics.getId());

            analytics.setModel(targetFileName);

            File file = new File(Configuration.docroot + targetFileNameTXT);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(model.toString());
            bw.close();

            dbsynchronizer.updateLindaAnalyticsModelReadable(targetFileNameTXT, analytics.getId());
            analytics.setModelReadable(targetFileNameTXT);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return analytics;
    }

    public boolean writeToFile(String content, String column, Analytics analytics) {

        DBSynchronizer dbsynchronizer = new DBSynchronizer();
        //prepare target file name
        String[] splitedSourceFileName = analytics.getDocument().split(".arff");

        String targetFileName = "";
        /*
         if (column.equalsIgnoreCase("resultdocument")) {
         targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument.arff").replace("datasets", "results");

         String targetFileNameFullPath = Configuration.docroot + targetFileName;
         saveFile(targetFileNameFullPath, content);

         System.out.println("====analytics.getExportFormat()==="+analytics.getExportFormat());
             
         if (analytics.getExportFormat().equalsIgnoreCase("csv")) {
             
         System.out.println("====eimai mesa sto csv===");

               
         String targetFileNameCSV = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument." + analytics.getExportFormat()).replace("datasets", "results");
         String targetFileNameCSVFull = Configuration.docroot + targetFileNameCSV;

         saveFileAsCSV(targetFileNameFullPath, targetFileNameCSVFull);
         dbsynchronizer.updateLindaAnalytics(targetFileNameCSV, column, analytics.getId());

         } else {
         dbsynchronizer.updateLindaAnalytics(targetFileName, column, analytics.getId());
         }

         } else */
        if (column.equalsIgnoreCase("processinfo")) {
            targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_id() + "_processinfo.txt").replace("datasets", "results");
            String targetFileNameFullPath = Configuration.docroot + targetFileName;
            saveFile(targetFileNameFullPath, content);
            dbsynchronizer.updateLindaAnalytics(targetFileName, column, analytics.getId());

        }

        return true;
    }

    public boolean saveFile(String targetFileNameFullPath, String content) {

        try {

            File file = new File(targetFileNameFullPath);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean saveFileAsCSV(String targetFileNameFullPath, String targetFileNameCSVFull) {

        try {
            System.out.println("targetFileNameArffPath" + targetFileNameFullPath);
            System.out.println("targetFileNameCSVFull" + targetFileNameCSVFull);
            // load Arff
            ArffLoader loader = new ArffLoader();
            loader.setSource(new File(targetFileNameFullPath));
            Instances data = loader.getDataSet();

            // save CSV
            CSVSaver saver = new CSVSaver();
            saver.setInstances(data);
            saver.setFile(new File(targetFileNameCSVFull));
            saver.setDestination(new File(targetFileNameCSVFull));
            saver.writeBatch();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Instances createArffFileFromArray(ArrayList<Attribute> atts, List<Instance> instances) {
        /*
         //ArrayList<Attribute> atts = new ArrayList<Attribute>();
         List<Instance> instances = new ArrayList<Instance>();
         double[][] data = {{4058.0, 4059.0, 4060.0, 214.0, 1710.0, 2452.0, 2473.0, 2474.0, 2475.0, 2476.0, 2477.0, 2478.0, 2688.0, 2905.0, 2906.0, 2907.0, 2908.0, 2909.0, 2950.0, 2969.0, 2970.0, 3202.0, 3342.0, 3900.0, 4007.0, 4052.0, 4058.0, 4059.0, 4060.0},
         {19.0, 20.0, 21.0, 31.0, 103.0, 136.0, 141.0, 142.0, 143.0, 144.0, 145.0, 146.0, 212.0, 243.0, 244.0, 245.0, 246.0, 247.0, 261.0, 270.0, 271.0, 294.0, 302.0, 340.0, 343.0, 354.0, 356.0, 357.0, 358.0}};

         int numDimensions = 2;

         int numInstances = 3;

         for (int dim = 0; dim < numDimensions; dim++) {
         Attribute current = new Attribute("Attribute" + dim, dim);
         if (dim == 0) {
         for (int obj = 0; obj < numInstances; obj++) {
         instances.add(new SparseInstance(numDimensions));
         }
         }

         for (int obj = 0; obj < numInstances; obj++) {
         instances.get(obj).setValue(current, data[dim][obj]);
         //System.out.println("current"+current);
         }

         atts.add(current);
         }
         */
        Instances newDataset = new Instances("Dataset", atts, instances.size());

        for (Instance inst : instances) {
            System.out.println("======inst" + inst);
            newDataset.add(inst);
        }
        System.out.println("dataset arff" + newDataset.toString());

        return newDataset;
    }

    public Analytics saveModelasVector(Vector model, Analytics analytics) throws Exception {
        String[] splitedSourceFileName = analytics.getDocument().split(".arff");

        String targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "Model" + ".model").replace("datasets", "models");

        String targetFileNameTXT = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "ModelReadable" + ".txt").replace("datasets", "models");

        System.out.println("targetFileName: " + targetFileName);
        try {

            // serialize && save model
            SerializationHelper.write(Configuration.docroot + targetFileName, model);
            weka.core.SerializationHelper.write(Configuration.docroot + targetFileName, model);

            DBSynchronizer dbsynchronizer = new DBSynchronizer();
            dbsynchronizer.updateLindaAnalyticsModel(targetFileName, analytics.getId());

            analytics.setModel(targetFileName);

            File file = new File(Configuration.docroot + targetFileNameTXT);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(model.toString());
            bw.close();

            dbsynchronizer.updateLindaAnalyticsModelReadable(targetFileNameTXT, analytics.getId());
            analytics.setModelReadable(targetFileNameTXT);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return analytics;
    }

    public void nicePrintMessage(String msg) {
        System.out.println("------------ " + msg + " ------------");
        System.out.println("-------------------------------------------------------");

    }

    public HashMap separateDataFromMetadataInfo(Instances data) {
        String[] options = new String[2];
        options[0] = "-R";                              // "range"
        options[1] = "1,2";
        Instances newData = null;
        Instances metaData = null;
        try {
            Remove remove = new Remove();               // new instance of filter

            remove.setOptions(options);                 // set options

            remove.setInputFormat(data);                // inform filter about dataset **AFTER** setting options
            newData = Filter.useFilter(data, remove);   // apply filter                
            newData.setClassIndex(newData.numAttributes() - 1);

            remove.setInvertSelection(true);
            remove.setInputFormat(data);
            metaData = Filter.useFilter(data, remove);

        } catch (Exception ex) {
            Logger.getLogger(HelpfulFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }

        HashMap resultMap = new HashMap<String, Instances>();

        resultMap.put("newData", newData);
        resultMap.put("metaData", metaData);

        //System.out.println("--newData--"+newData);
        //System.out.println("--metaData--"+metaData);
        return resultMap;
    }

    public Instances mergeDataAndMetadataInfo(Instances data, Instances metadata) {

        Instances mergedData = null;

        try {
            mergedData = new Instances(data);
            // add new attributes
            mergedData.insertAttributeAt(new Attribute("basens", (List<String>) null), 0);
            //mergedData.insertAttributeAt(new Attribute("ID1", (List<String>) null), mergedData.numAttributes());
            mergedData.insertAttributeAt(new Attribute("nodeID", (List<String>) null), 1);

            for (int i = 0; i < mergedData.numInstances(); i++) {
                // fill colums with data
                mergedData.instance(i).setValue(0, metadata.get(i).stringValue(0));
                mergedData.instance(i).setValue(1, metadata.get(i).stringValue(1));
            }

           //JSONArray mergedDataArray  = new JSONArray();
        } catch (Exception ex) {
            Logger.getLogger(HelpfulFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }

        return mergedData;
    }

    public boolean isRDFExportFormat(String exportFormat) {
        if (exportFormat.equalsIgnoreCase("RDFXML")
                || exportFormat.equalsIgnoreCase("TTL")
                || exportFormat.equalsIgnoreCase("NTRIPLES")) {
            return true;
        }
        return false;
    }
    
     public Analytics connectToAnalyticsTable(int id) {
        ConnectionController connectionController = new ConnectionController();
        connectionController.readProperties();
        DBSynchronizer dbsynchronizer = new DBSynchronizer();
        Analytics analytics = dbsynchronizer.getlindaAnalytics_analytics(id);
        return analytics;
    }
    /*
     public static void main(String[] args) throws Exception {

     ArrayList<Attribute> atts = new ArrayList<Attribute>();
     List<Instance> instances = new ArrayList<Instance>();
     double[][] data = {{4058.0, 4059.0, 4060.0, 214.0, 1710.0, 2452.0, 2473.0, 2474.0, 2475.0, 2476.0, 2477.0, 2478.0, 2688.0, 2905.0, 2906.0, 2907.0, 2908.0, 2909.0, 2950.0, 2969.0, 2970.0, 3202.0, 3342.0, 3900.0, 4007.0, 4052.0, 4058.0, 4059.0, 4060.0},
     {19.0, 20.0, 21.0, 31.0, 103.0, 136.0, 141.0, 142.0, 143.0, 144.0, 145.0, 146.0, 212.0, 243.0, 244.0, 245.0, 246.0, 247.0, 261.0, 270.0, 271.0, 294.0, 302.0, 340.0, 343.0, 354.0, 356.0, 357.0, 358.0}};

     int numDimensions = 2;

     int numInstances = 3;

     for (int dim = 0; dim < numDimensions; dim++) {
     Attribute current = new Attribute("Attribute" + dim, dim);
     if (dim == 0) {
     for (int obj = 0; obj < numInstances; obj++) {
     instances.add(new SparseInstance(numDimensions));
     }
     }

     for (int obj = 0; obj < numInstances; obj++) {
     instances.get(obj).setValue(current, data[dim][obj]);
     //System.out.println("current"+current);
     }

     atts.add(current);
     }

     Instances newDataset = new Instances("Dataset", atts, instances.size());

     for (Instance inst : instances) {
     newDataset.add(inst);
     }
     System.out.println("dataset arff" + newDataset.toString());
     }
     */
}
