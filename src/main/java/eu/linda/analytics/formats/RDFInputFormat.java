/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.formats;

import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
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

        String queryURI = DBSynchronizer.getQueryURI(Integer.parseInt(query_id));

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

            DBSynchronizer.updateLindaAnalyticsInputDataPerformanceTime(analytics);

        } catch (Exception ex) {
            Logger.getLogger(ArffInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;

    }


    @Override
    public RConnection importData4R(String train_query_id, String evaluation_query_id, boolean isForRDFOutput, Analytics analytics) {
        System.out.println(System.getProperty("java.library.path"));
        System.out.println("R_HOME" + System.getenv().get("R_HOME"));

        RConnection re = null;
        FileInputStream trainfis = null;
        long trainfisSize = 0;
        FileInputStream evalfis = null;
        try {
            re = new RConnection();
            REXP x = re.eval("R.version.string");
            System.out.println(x.asString());

            float timeToGetQuery = 0;
            long startTimeToGetQuery = System.currentTimeMillis();

            String trainQueryURI = DBSynchronizer.getQueryURI(Integer.parseInt(train_query_id));
            URL train_url = new URL(trainQueryURI);
            helpfulFunctions.nicePrintMessage("import data from train uri " + trainQueryURI);

            if (!helpfulFunctions.isURLResponsive(train_url)) {
                re.eval(" is_train_query_responsive <-FALSE ");
                System.out.println("is_train_query_responsive <-FALSE ");

            } else {
                re.eval("is_train_query_responsive <-TRUE  ");
                System.out.println("is_train_query_responsive <-TRUE ");

                File traintmpfile4lindaquery = File.createTempFile("traintmpfile4lindaquery" + train_query_id, ".tmp");
                FileUtils.copyURLToFile(train_url, traintmpfile4lindaquery);

                helpfulFunctions.cleanTmpFileFromDatatypes(traintmpfile4lindaquery.getAbsolutePath());

                re.eval(" loaded_data <- read.csv(file='" + traintmpfile4lindaquery + "', header=TRUE, sep=',', na.strings='---');");
                System.out.println(" loaded_data <- read.csv(file='" + traintmpfile4lindaquery + "', header=TRUE, sep=',', na.strings='---');");

                trainfis = new FileInputStream(traintmpfile4lindaquery);
                System.out.println("fis.getChannel().size() " + trainfis.getChannel().size());
                trainfisSize = trainfis.getChannel().size();
                analytics.setData_size(analytics.getData_size() + trainfisSize);
                trainfis.close();
                
                 if (!evaluation_query_id.equalsIgnoreCase("") && train_query_id.equalsIgnoreCase(evaluation_query_id)) {

                    re.eval("is_eval_query_responsive <-TRUE; "
                            + "loaded_data_eval <- loaded_data; ");
                    System.out.println("is_eval_query_responsive <-TRUE; "
                            + "loaded_data_eval <- loaded_data; ");
                }

                if (!evaluation_query_id.equalsIgnoreCase("") && !train_query_id.equalsIgnoreCase(evaluation_query_id)) {

                    String evalQueryURI = DBSynchronizer.getQueryURI(Integer.parseInt(evaluation_query_id));
                    helpfulFunctions.nicePrintMessage("import data from uri " + evalQueryURI);
                    URL eval_url = new URL(evalQueryURI);

                    if (!helpfulFunctions.isURLResponsive(eval_url)) {
                        re.eval(" is_eval_query_responsive <-FALSE ");
                        System.out.println("is_eval_query_responsive <-FALSE ");

                    } else {
                        re.eval("is_eval_query_responsive <-TRUE  ");
                        System.out.println("is_eval_query_responsive <-TRUE ");

                        File evaltmpfile4lindaquery = File.createTempFile("evaltmpfile4lindaquery" + evaluation_query_id, ".tmp");
                        FileUtils.copyURLToFile(eval_url, evaltmpfile4lindaquery);

                        helpfulFunctions.cleanTmpFileFromDatatypes(evaltmpfile4lindaquery.getAbsolutePath());

                        re.eval(" loaded_data_eval <- read.csv(file='" + evaltmpfile4lindaquery + "', header=TRUE, sep=',', na.strings='---');");
                        System.out.println(" loaded_data_eval <- read.csv(file='" + evaltmpfile4lindaquery + "', header=TRUE, sep=',', na.strings='---');");

                        evalfis = new FileInputStream(evaltmpfile4lindaquery);
                        System.out.println("fis.getChannel().size() " + evalfis.getChannel().size());
                        analytics.setData_size(analytics.getData_size() + trainfisSize + evalfis.getChannel().size());
                        evalfis.close();
                    }

                }

               

            }

            // Get elapsed time in milliseconds
            long elapsedTimeToGetQueryMillis = System.currentTimeMillis() - startTimeToGetQuery;
            // Get elapsed time in seconds
            timeToGetQuery = elapsedTimeToGetQueryMillis / 1000F;
            System.out.println("timeToGetQuery" + timeToGetQuery);
            analytics.setTimeToGet_data(analytics.getTimeToGet_data() + timeToGetQuery);

            DBSynchronizer.updateLindaAnalyticsInputDataPerformanceTime(analytics);

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
