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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.rosuda.JRI.Rengine;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 *
 * @author eleni
 */
public class RDFInputFormat extends InputFormat {

    Util helpfulFunctions;
    ConnectionController connectionController;

    public RDFInputFormat() {
        helpfulFunctions = Util.getInstance();
        connectionController = ConnectionController.getInstance();
    }

    @Override
    public AbstractList importData4weka(String query_id, boolean isForRDFOutput, Analytics analytics) {

        String queryURI = connectionController.getQueryURI(query_id);

        helpfulFunctions.nicePrintMessage("import data from uri " + queryURI);

        Instances data = null;
        try {
            float timeToGetQuery = 0;
            long startTimeToGetQuery = System.currentTimeMillis();
            URL url = new URL(queryURI);
            if (!helpfulFunctions.isURLResponsive(url)) {
                return null;
            }
            File tmpfile4lindaquery = File.createTempFile("tmpfile4lindaquery" + query_id, ".tmp");
            FileUtils.copyURLToFile(url, tmpfile4lindaquery);

            helpfulFunctions.cleanTmpFileFromDatatypes(tmpfile4lindaquery.getAbsolutePath());
            System.out.println("tmpfile4lindaquery.getAbsolutePath()" + tmpfile4lindaquery.getAbsolutePath());

            System.out.println("Downloaded File Query: " + tmpfile4lindaquery);

            CSVLoader loader = new CSVLoader();
            loader.setSource(tmpfile4lindaquery);
            if (isForRDFOutput) {
                loader.setStringAttributes("1,2");
            }

            loader.setFieldSeparator(",");
            data = loader.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);

            FileInputStream fis = null;
            try {

                fis = new FileInputStream(tmpfile4lindaquery);
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
    public Rengine importData4R(String query_id, boolean isForRDFOutput, Analytics analytics) {
        System.out.println(System.getProperty("java.library.path"));
        System.out.println("R_HOME" + System.getenv().get("R_HOME"));

        Rengine re = Rengine.getMainEngine();
        if (re == null) {
//            re = new Rengine(new String[]{"--vanilla"}, false, null);
            String newargs[] = {"--no-save"};
            re = new Rengine(newargs, false, null);

        }

        if (!re.waitForR()) {
            System.out.println("Cannot load R");
            System.out.println("is alive Rengine??" + re.isAlive());
        }

        String queryURI = connectionController.getQueryURI(query_id);

        helpfulFunctions.nicePrintMessage("import data from uri " + queryURI);
        try {
            float timeToGetQuery = 0;
            long startTimeToGetQuery = System.currentTimeMillis();
            URL url = new URL(queryURI);

            if (!helpfulFunctions.isURLResponsive(url)) {
                re.eval(" is_query_responsive <-FALSE ");
                System.out.println("is_query_responsive <-FALSE ");

            } else {
                re.eval("is_query_responsive <-TRUE  ");
                System.out.println("is_query_responsive <-TRUE ");

                File tmpfile4lindaquery = File.createTempFile("tmpfile4lindaquery" + query_id, ".tmp");
                FileUtils.copyURLToFile(url, tmpfile4lindaquery);

                helpfulFunctions.cleanTmpFileFromDatatypes(tmpfile4lindaquery.getAbsolutePath());
                System.out.println("tmpfile4lindaquery.getAbsolutePath()" + tmpfile4lindaquery.getAbsolutePath());

                re.eval(" loaded_data <- read.csv(file='" + tmpfile4lindaquery + "', header=TRUE, sep=',', na.strings='---');");
                System.out.println(" loaded_data <- read.csv(file='" + tmpfile4lindaquery + "', header=TRUE, sep=',', na.strings='---');");

                FileInputStream fis = null;
                try {

                    fis = new FileInputStream(tmpfile4lindaquery);
                    System.out.println("fis.getChannel().size() " + fis.getChannel().size());
                    analytics.setData_size(analytics.getData_size() + fis.getChannel().size());
                } finally {
                    fis.close();
                }
            }

            // Get elapsed time in milliseconds
            long elapsedTimeToGetQueryMillis = System.currentTimeMillis() - startTimeToGetQuery;
            // Get elapsed time in seconds
            timeToGetQuery = elapsedTimeToGetQueryMillis / 1000F;
            System.out.println("timeToGetQuery" + timeToGetQuery);
            analytics.setTimeToGet_data(analytics.getTimeToGet_data() + timeToGetQuery);

            connectionController.updateLindaAnalyticsInputDataPerformanceTime(analytics);

        } catch (Exception ex) {
            Logger.getLogger(ArffInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return re;
    }
    
    @Override
    public RConnection importData4R1(String query_id, boolean isForRDFOutput, Analytics analytics) {
        System.out.println(System.getProperty("java.library.path"));
        System.out.println("R_HOME" + System.getenv().get("R_HOME"));

        RConnection re = null;
        try {
            re = new RConnection();                        
            REXP x = re.eval("R.version.string");
            System.out.println(x.asString());
        
        
        String queryURI = connectionController.getQueryURI(query_id);

        helpfulFunctions.nicePrintMessage("import data from uri " + queryURI);
        
            float timeToGetQuery = 0;
            long startTimeToGetQuery = System.currentTimeMillis();
            URL url = new URL(queryURI);

            if (!helpfulFunctions.isURLResponsive(url)) {
                re.eval(" is_query_responsive <-FALSE ");
                System.out.println("is_query_responsive <-FALSE ");

            } else {
                re.eval("is_query_responsive <-TRUE  ");
                System.out.println("is_query_responsive <-TRUE ");

                File tmpfile4lindaquery = File.createTempFile("tmpfile4lindaquery" + query_id, ".tmp");
                FileUtils.copyURLToFile(url, tmpfile4lindaquery);

                helpfulFunctions.cleanTmpFileFromDatatypes(tmpfile4lindaquery.getAbsolutePath());
                System.out.println("tmpfile4lindaquery.getAbsolutePath()" + tmpfile4lindaquery.getAbsolutePath());

                re.eval(" loaded_data <- read.csv(file='" + tmpfile4lindaquery + "', header=TRUE, sep=',', na.strings='---');");
                System.out.println(" loaded_data <- read.csv(file='" + tmpfile4lindaquery + "', header=TRUE, sep=',', na.strings='---');");

                FileInputStream fis = null;
                try {

                    fis = new FileInputStream(tmpfile4lindaquery);
                    System.out.println("fis.getChannel().size() " + fis.getChannel().size());
                    analytics.setData_size(analytics.getData_size() + fis.getChannel().size());
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(RDFInputFormat.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    fis.close();
                }
            }

            // Get elapsed time in milliseconds
            long elapsedTimeToGetQueryMillis = System.currentTimeMillis() - startTimeToGetQuery;
            // Get elapsed time in seconds
            timeToGetQuery = elapsedTimeToGetQueryMillis / 1000F;
            System.out.println("timeToGetQuery" + timeToGetQuery);
            analytics.setTimeToGet_data(analytics.getTimeToGet_data() + timeToGetQuery);

            connectionController.updateLindaAnalyticsInputDataPerformanceTime(analytics);

        } catch (RserveException ex) {
            Logger.getLogger(RDFInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(RDFInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REXPMismatchException ex) {
            Logger.getLogger(RDFInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RDFInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return re;
    }


}
