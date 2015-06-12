package eu.linda.analytics.utils;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.validator.UrlValidator;
import weka.classifiers.Classifier;
import weka.core.Attribute;
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
public class Util {

    ConnectionController connectionController;

    private static Util instance = null;

    protected Util() {
        connectionController = ConnectionController.getInstance();
    }

    public static Util getInstance() {
        if (instance == null) {
            instance = new Util();
        }
        return instance;
    }

    public static Analytics saveModel(Classifier model, Analytics analytics) throws Exception {
        String[] splitedSourceFileName = analytics.getDocument().split("\\.");

        String targetFileName = ("models/analyticsID" + analytics.getId() + "_" + analytics.getAlgorithm_name() + "Model" + ".model");
        String targetFileNameTXT = ("models/analyticsID" + analytics.getId() + "_" + analytics.getAlgorithm_name() + "ModelReadable" + ".txt");

        System.out.println("targetFileName: " + targetFileName);
        try {

            // serialize && save model
            weka.core.SerializationHelper.write(Configuration.analyticsRepo + targetFileName, model);

            DBSynchronizer.updateLindaAnalyticsModel(targetFileName, analytics.getId());

            analytics.setModel(targetFileName);

            File file = new File(Configuration.analyticsRepo + targetFileNameTXT);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(model.toString());
            bw.close();

            DBSynchronizer.updateLindaAnalyticsModelReadable(targetFileNameTXT, analytics.getId());
            analytics.setModelReadable(targetFileNameTXT);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return analytics;
    }

    public static boolean writeToFile(String content, String column, Analytics analytics) {

        //prepare target file name
        String[] splitedSourceFileName = analytics.getDocument().split(".arff");

        String targetFileName = "";

        if (column.equalsIgnoreCase("processinfo")) {
            //targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_id() + "_processinfo.txt").replace("datasets", "results");
            targetFileName = ("results/analyticsID" + analytics.getId() + "_" + analytics.getAlgorithm_id() + "_processinfo.txt").replace("datasets", "results");
            String targetFileNameFullPath = Configuration.analyticsRepo + targetFileName;
            saveFile(targetFileNameFullPath, content);
            DBSynchronizer.updateLindaAnalytics(targetFileName, column, analytics.getId());

        }
        return true;
    }

    public static boolean saveFile(String targetFileNameFullPath, String content) {

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
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    public static boolean saveFile(String targetFileNameFullPath, String[] content) {

        try {

            File file = new File(targetFileNameFullPath);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for (String string : content) {
                bw.write(string + "\n");
            }

            bw.close();

        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    public static void deleteFile(String fileToDelete) {
        File file = new File(fileToDelete);

        if (file.exists()) {
            file.delete();
        }
    }

    public static boolean saveFileAsCSV(String targetFileNameFullPath, String targetFileNameCSVFull) {

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

        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public static Analytics saveModelasVector(Vector model, Analytics analytics) throws Exception {

        String targetFileName = "models/analyticsID" + analytics.getId() + "_" + analytics.getAlgorithm_name() + "Model" + ".model";

        String targetFileNameTXT = "models/analyticsID" + analytics.getId() + "_" + analytics.getAlgorithm_name() + "ModelReadable" + ".txt";

        System.out.println("targetFileName: " + targetFileName);
        try {

            // serialize && save model
            SerializationHelper.write(Configuration.analyticsRepo + targetFileName, model);
            weka.core.SerializationHelper.write(Configuration.analyticsRepo + targetFileName, model);

            DBSynchronizer.updateLindaAnalyticsModel(targetFileName, analytics.getId());

            analytics.setModel(targetFileName);

            File file = new File(Configuration.analyticsRepo + targetFileNameTXT);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(model.toString());
            bw.close();

            DBSynchronizer.updateLindaAnalyticsModelReadable(targetFileNameTXT, analytics.getId());
            analytics.setModelReadable(targetFileNameTXT);

        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);

        }
        return analytics;
    }

    public static void nicePrintMessage(String msg) {
        System.out.println("------------ " + msg + " ------------");
        System.out.println("-------------------------------------------------------");

    }

    public static HashMap separateDataFromMetadataInfo(Instances data) {
        //until to correct rdf2any converter
        String[] options1 = new String[2];
        options1[0] = "-R";    // "range"
        options1[1] = "1";
        Instances newData1 = null;
        try {
            Remove remove1 = new Remove();             // new instance of filter
            remove1.setOptions(options1);               // set options
            remove1.setInputFormat(data);              // inform filter about dataset **AFTER** setting options
            newData1 = Filter.useFilter(data, remove1); // apply filter                
        } catch (Exception ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }

        data = newData1;

        String[] options = new String[2];
        options[0] = "-R";    // "range"
        options[1] = "1";
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
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }

        HashMap resultMap = new HashMap<String, Instances>();

        resultMap.put("newData", newData);
        resultMap.put("metaData", metaData);

        return resultMap;
    }

    public static Instances mergeDataAndMetadataInfo(Instances data, Instances metadata) {

        Instances mergedData = null;

        try {
            mergedData = new Instances(data);
            // add new attributes
            mergedData.insertAttributeAt(new Attribute("uri", (List<String>) null), 0);

            for (int i = 0; i < mergedData.numInstances(); i++) {
                // fill colums with data
                mergedData.instance(i).setValue(0, metadata.get(i).stringValue(0));
            }

        } catch (Exception ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }

        return mergedData;
    }

    public static boolean isRDFExportFormat(String exportFormat) {
        return exportFormat.equalsIgnoreCase("RDFXML")
                || exportFormat.equalsIgnoreCase("TTL")
                || exportFormat.equalsIgnoreCase("N-Tripples");
    }

    public static boolean isRDFInputFormat(int analytics_id) {
        return analytics_id > 0;
    }

    public static void cleanPreviousInfo(Analytics analytics) {

        nicePrintMessage("Clean Previous Analytic Info");
        //delete result document
        deleteFile(analytics.getResultdocument());
        deleteFile(analytics.getProcessinfo());
        deleteFile(analytics.getModelReadable());
        deleteFile(analytics.getModel());
        //empty the analytic result document & processMessage
        DBSynchronizer.emptyLindaAnalyticsResultInfo(analytics.getId());

    }

    public static long manageNewPlot(Analytics analytics, String description, String filepath, String plot) {

        //add plot to db
        long plot_id = DBSynchronizer.addPlot(description, filepath);
        DBSynchronizer.updatePlot((int) plot_id, "plots/plotid" + plot_id + ".png");

        //add plot to analytics        
        DBSynchronizer.updateLindaAnalyticsPlot(analytics.getId(), plot_id, plot);

        String oldPlotFileName;
        int oldPlotID;
        if (plot.equalsIgnoreCase("plot1_id")) {
            oldPlotFileName = Configuration.analyticsRepo + "plots/plotid" + analytics.getPlot1_id() + ".png";
            oldPlotID = analytics.getPlot1_id();
        } else {
            oldPlotFileName = Configuration.analyticsRepo + "plots/plotid" + analytics.getPlot2_id() + ".png";
            oldPlotID = analytics.getPlot2_id();
        }
        deleteFile(oldPlotFileName);
        DBSynchronizer.deletePlot(oldPlotID);

        return plot_id;
    }

    public static boolean isURLResponsive(URL url) throws IOException {
        
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(10000);
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                nicePrintMessage("url http url connection failed");
                return false;
            }
            nicePrintMessage("url http url connection succesfull");
        
        return true;

    }

    public static boolean isURLValid(String url) {

        UrlValidator urlValidator = new UrlValidator();
        return urlValidator.isValid(url);
    }

    public static boolean cleanTmpFileFromDatatypes(String csvFile) {

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(csvFile));

            //after put in buffer delete file
            File file = new File(csvFile);
            file.delete();

            File fout = new File(csvFile);
            FileOutputStream fos = new FileOutputStream(fout);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] columns = line.split(cvsSplitBy);

                String newline = "";

                for (int i = 0; i < columns.length; i++) {

                    if (i == 0) {
                        newline += columns[i];
                    } else if (columns[i].contains("^^")) {
                        String[] splitedvalues = columns[i].split("\\^\\^http");
                        newline += "," + splitedvalues[0];
                    } else {
                        newline += "," + columns[i];
                    }
                }
                bw.write(newline);
                bw.newLine();

            }

            bw.close();

        }catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } 

        return true;
    }

}
