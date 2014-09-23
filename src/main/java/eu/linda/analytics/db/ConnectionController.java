/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.db;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.logger.LoggerFactory;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.model.Classification;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVSaver;

public class ConnectionController {

    private final static Logger LOGGER = LoggerFactory.getLogger();
    private final Properties prop = new Properties();
    private InputStream input = null;

    public Analytics connectToAnalyticsTable(int id) {
        //ConnectionController connectionController = new ConnectionController();
        this.readProperties();
        DBSynchronizer dbsynchronizer = new DBSynchronizer();
        Analytics analytics = dbsynchronizer.getlindaAnalytics_analytics(id);
        return analytics;
    }

    public Classification connectToClassificationTable(int id) {
        //ConnectionController connectionController = new ConnectionController();
        this.readProperties();
        DBSynchronizer dbsynchronizer = new DBSynchronizer();
        Classification classification = dbsynchronizer.getlindaAnalytics_classification(id);
        return classification;
    }
    /*
    public boolean writeToFile(String content, String column, Analytics analytics) {

        DBSynchronizer dbsynchronizer = new DBSynchronizer();
        //prepare target file name
        String[] splitedSourceFileName = analytics.getDocument().split(".arff");

        String targetFileName = "";
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

        } else if (column.equalsIgnoreCase("processinfo")) {
            targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_id() + "_processinfo.txt").replace("datasets", "results");
            String targetFileNameFullPath = Configuration.docroot + targetFileName;
            saveFile(targetFileNameFullPath, content);
            dbsynchronizer.updateLindaAnalytics(targetFileName, column, analytics.getId());

        }

        return true;
    }*/
    /*
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

            System.out.println("Done");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean saveFileAsCSV(String targetFileNameFullPath, String targetFileNameCSVFull) {

        try {
            System.out.println("targetFileNameFullPath"+targetFileNameFullPath);
            System.out.println("targetFileNameCSVFull"+targetFileNameCSVFull);
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
    }*/

    /*
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

            System.out.println("Done");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return analytics;
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

            System.out.println("Done");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return analytics;
    }*/

    public void readProperties() {
        try {
            String path = new java.io.File(".").getCanonicalPath();
            LOGGER.log(Level.INFO, "PATH:{0}", path);

            input = this.getClass().getResourceAsStream("/RESTfulLINDA.properties");

            // load a properties file
            prop.load(input);
            Configuration.dbport = Integer.parseInt(prop.getProperty("dbport").trim());
            Configuration.dbip = prop.getProperty("dbip").trim();
            Configuration.username = prop.getProperty("username").trim();
            Configuration.password = prop.getProperty("password").trim();
            Configuration.dbname = prop.getProperty("dbname").trim();
            Configuration.docroot = prop.getProperty("docroot").trim();

        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOGGER.severe(e.getMessage());
                }
            }
        }
    }//EoM readproperties  

}//EoC

