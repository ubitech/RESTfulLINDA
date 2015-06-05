/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.formats;

import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.JRI.Rengine;
import org.rosuda.REngine.Rserve.RConnection;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 *
 * @author eleni
 */
public class CSVInputFormat extends InputFormat {

    Util helpfulFuncions;
    ConnectionController connectionController;

    public CSVInputFormat() {
        helpfulFuncions = Util.getInstance();
        connectionController = ConnectionController.getInstance();
    }

    @Override
    public AbstractList importData4weka(String pathToFile, boolean isForRDFOutput, Analytics analytics) {

        float timeToGetQuery = 0;
        long startTimeToGetQuery = System.currentTimeMillis();
        helpfulFuncions.nicePrintMessage("import CSV file ");

        System.out.println("Import data from file: " + pathToFile);

        Instances data = null;
        try {
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File(pathToFile));
            if (isForRDFOutput) {
                loader.setStringAttributes("1,2");
            }

            loader.setFieldSeparator(",");
            data = loader.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);

            FileInputStream fis = null;
            try {

                fis = new FileInputStream(pathToFile);
                System.out.println("fis.getChannel().size() " + fis.getChannel().size());
                analytics.setData_size(analytics.getData_size() + fis.getChannel().size());
            } finally {
                fis.close();
            }

            // Get elapsed time in milliseconds
            long elapsedTimeToGetQueryMillis = System.currentTimeMillis() - startTimeToGetQuery;
            // Get elapsed time in seconds
            timeToGetQuery = elapsedTimeToGetQueryMillis / 1000F;
            analytics.setTimeToGet_data(analytics.getTimeToGet_data() + timeToGetQuery);
            System.out.println("timeToGetQuery" + timeToGetQuery);

            connectionController.updateLindaAnalyticsInputDataPerformanceTime(analytics);

        } catch (Exception ex) {
            Logger.getLogger(ArffInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;

    }

    @Override
    public Rengine importData4R(String pathToFile, boolean isForRDFOutput, Analytics analytics) {
        float timeToGetQuery = 0;
        long startTimeToGetQuery = System.currentTimeMillis();

        System.out.println(System.getProperty("java.library.path"));
        System.out.println("R_HOME" + System.getenv().get("R_HOME"));

        Rengine re = Rengine.getMainEngine();
        if (re == null) {
            re = new Rengine(new String[]{"--vanilla"}, false, null);
        }

        if (!re.waitForR()) {
            System.out.println("Cannot load R");
            System.out.println("is alive Rengine??" + re.isAlive());
        }
        re.eval("is_query_responsive <-TRUE  ");
        re.eval(" loaded_data <- read.csv(file='" + pathToFile + "', header=TRUE, sep=',', na.strings='---');");

        FileInputStream fis = null;
        try {

            fis = new FileInputStream(pathToFile);
            System.out.println("fis.getChannel().size() " + fis.getChannel().size());
            analytics.setData_size(analytics.getData_size() + fis.getChannel().size());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CSVInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CSVInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(CSVInputFormat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Get elapsed time in milliseconds
        long elapsedTimeToGetQueryMillis = System.currentTimeMillis() - startTimeToGetQuery;
        // Get elapsed time in seconds
        timeToGetQuery = elapsedTimeToGetQueryMillis / 1000F;
        analytics.setTimeToGet_data(analytics.getTimeToGet_data() + timeToGetQuery);
        System.out.println("timeToGetQuery" + timeToGetQuery);

        connectionController.updateLindaAnalyticsInputDataPerformanceTime(analytics);

        return re;
    }
    
    @Override
    public RConnection importData4R1(String pathToFile, boolean isForRDFOutput,Analytics analytics) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void main(String[] args) throws Exception {
        Instances data = null;
        String[] options = new String[2];
        options[0] = "-S";        // "range"
        options[1] = "1,2";

        CSVLoader loader = new CSVLoader();
        try {
            loader.setSource(new File("/home/eleni/Desktop/mydatasets/NYRandonResearchTotest2.csv"));

            loader.setStringAttributes("1,2");
            loader.setFieldSeparator(",");

            data = loader.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);

        } catch (IOException ex) {
            Logger.getLogger(CSVInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CSVInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
