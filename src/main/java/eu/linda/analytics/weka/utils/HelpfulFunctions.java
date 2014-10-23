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
 */
public class HelpfulFunctions {
      DBSynchronizer dbsynchronizer;

    public HelpfulFunctions() {
       dbsynchronizer= new DBSynchronizer();

    }

    public Analytics saveModel(Classifier model, Analytics analytics) throws Exception {
        String[] splitedSourceFileName = analytics.getDocument().split(".arff");

        String targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "Model" + ".model").replace("datasets", "models");

        String targetFileNameTXT = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "ModelReadable" + ".txt").replace("datasets", "models");

        System.out.println("targetFileName: " + targetFileName);
        try {

            // serialize && save model
            weka.core.SerializationHelper.write(Configuration.docroot + targetFileName, model);

            
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

        //prepare target file name
        String[] splitedSourceFileName = analytics.getDocument().split(".arff");

        String targetFileName = "";

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

    public void deleteFile(String fileToDelete) {
        File file = new File(Configuration.docroot + fileToDelete);

        if (!file.exists()) {
            file.delete();
        }
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
/*
    public Instances createArffFileFromArray(ArrayList<Attribute> atts, List<Instance> instances) {

        Instances newDataset = new Instances("Dataset", atts, instances.size());

        for (Instance inst : instances) {
            System.out.println("======inst" + inst);
            newDataset.add(inst);
        }
        System.out.println("dataset arff" + newDataset.toString());

        return newDataset;
    }*/

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
        options[0] = "-R";    // "range"
        options[1] = "1,2";
        Instances newData = null;
        Instances metaData = null;
        try {
            Remove remove = new Remove();             // new instance of filter
            remove.setOptions(options);               // set options
            remove.setInputFormat(data);              // inform filter about dataset **AFTER** setting options
            newData = Filter.useFilter(data, remove); // apply filter                
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
        Analytics analytics = dbsynchronizer.getlindaAnalytics_analytics(id);
        return analytics;
    }

    public void updateProcessMessageToAnalyticsTable(String message, int id) {
        dbsynchronizer.updateLindaAnalyticsProcessMessage(message, id);

    }

    public void deleteResultDocumentFromAnalytcsTable(int id) {
        dbsynchronizer.emptyLindaAnalyticsResultDocument(id);

    }

    public void cleanPreviousInfo(Analytics analytics) {
        
        //delete result document
        deleteFile(analytics.getResultdocument());
        deleteFile(analytics.getProcessinfo());
        //empty the analytic result document
        deleteResultDocumentFromAnalytcsTable(analytics.getId());
                

    }

    

}
