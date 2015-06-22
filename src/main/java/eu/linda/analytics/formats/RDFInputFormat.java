/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.formats;

import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.utils.AlsCustomException;
import eu.linda.analytics.utils.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.AbstractList;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
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

    ConnectionController connectionController;

    public RDFInputFormat() {
        connectionController = ConnectionController.getInstance();
    }

    @Override
    public AbstractList importData4weka(String train_query_id, String evaluation_query_id, boolean isForRDFOutput, Analytics analytics) {

        FileInputStream trainfis = null;
        long trainfisSize = 0;
        FileInputStream evalfis = null;

        String trainQueryURI = DBSynchronizer.getQueryURI(Integer.parseInt(train_query_id));

        Util.nicePrintMessage("import train data from uri " + trainQueryURI);

        Map data = new Hashtable();
        Instances train_data = null;
        Instances test_data = null;
        try {
            float timeToGetQuery = 0;
            long startTimeToGetQuery = System.currentTimeMillis();
            URL train_url = new URL(trainQueryURI);
            if (!Util.isURLResponsive(train_url)) {
                throw new AlsCustomException("There is a connectivity issue. Could not reach data for predefined train query.\n"
                        + " Please check your connectivity and the responsiveness of the selected sparql endpoint.\n "
                        + "Then click on re-Evaluate button to try to run again the analytic process.", analytics);

            }
            File tmpfile4lindaquery = File.createTempFile("tmpfile4lindaquery" + train_query_id, ".tmp");
            FileUtils.copyURLToFile(train_url, tmpfile4lindaquery);

            Util.cleanTmpFileFromDatatypes(tmpfile4lindaquery.getAbsolutePath());
            System.out.println("Downloaded File Query: " + tmpfile4lindaquery);

            CSVLoader loader = new CSVLoader();
            loader.setSource(tmpfile4lindaquery);
            if (isForRDFOutput) {
                loader.setStringAttributes("1,2");
            }

            loader.setFieldSeparator(",");
            train_data = loader.getDataSet();
            train_data.setClassIndex(train_data.numAttributes() - 1);

            trainfis = new FileInputStream(tmpfile4lindaquery);
            trainfisSize = trainfis.getChannel().size();
            System.out.println("fis.getChannel().size() " + trainfisSize);
            analytics.setData_size(analytics.getData_size() + trainfisSize);
            trainfis.close();

            data.put("train_data", train_data);

            if (!evaluation_query_id.equalsIgnoreCase("") && train_query_id.equalsIgnoreCase(evaluation_query_id)) {

                test_data = train_data;
                data.put("test_data", test_data);
            }

            if (!evaluation_query_id.equalsIgnoreCase("") && !train_query_id.equalsIgnoreCase(evaluation_query_id)) {

            }

            // Get elapsed time in milliseconds
            long elapsedTimeToGetQueryMillis = System.currentTimeMillis() - startTimeToGetQuery;
            // Get elapsed time in seconds
            timeToGetQuery = elapsedTimeToGetQueryMillis / 1000F;
            analytics.setTimeToGet_data(analytics.getTimeToGet_data() + timeToGetQuery);
            System.out.println("timeToGetQuery" + timeToGetQuery);

            DBSynchronizer.updateLindaAnalyticsInputDataPerformanceTime(analytics);

        } catch (IOException ex) {
            Logger.getLogger(ArffInputFormat.class.getName()).log(Level.SEVERE, null, ex);
            DBSynchronizer.updateLindaAnalyticsProcessMessage("Input Queries are not responsive. \n", analytics.getId());
        } catch (AlsCustomException ex) {
            return null;
        }
        return train_data;

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
            Util.nicePrintMessage("import data from train uri " + trainQueryURI);

            if (!Util.isURLResponsive(train_url)) {
                re.eval("rm(list=ls());");
                throw new AlsCustomException("There is a connectivity issue. Could not reach data for predefined train query.\n"
                        + " Please check your connectivity and the responsiveness of the selected sparql endpoint.\n "
                        + "Then click on re-Evaluate button to try to run again the analytic process.", analytics);

            } else {

                File traintmpfile4lindaquery = File.createTempFile("traintmpfile4lindaquery" + train_query_id, ".tmp");
                FileUtils.copyURLToFile(train_url, traintmpfile4lindaquery);

                Util.cleanTmpFileFromDatatypes(traintmpfile4lindaquery.getAbsolutePath());

                re.eval(" loaded_data <- read.csv(file='" + traintmpfile4lindaquery + "', header=TRUE, sep=',', na.strings='---');");
                System.out.println(" loaded_data <- read.csv(file='" + traintmpfile4lindaquery + "', header=TRUE, sep=',', na.strings='---');");

                trainfis = new FileInputStream(traintmpfile4lindaquery);
                System.out.println("fis.getChannel().size() " + trainfis.getChannel().size());
                trainfisSize = trainfis.getChannel().size();
                analytics.setData_size(analytics.getData_size() + trainfisSize);
                trainfis.close();

                if (!evaluation_query_id.equalsIgnoreCase("") && train_query_id.equalsIgnoreCase(evaluation_query_id)) {

                    re.eval("loaded_data_eval <- loaded_data; ");
                    System.out.println("loaded_data_eval <- loaded_data; ");
                }

                if (!evaluation_query_id.equalsIgnoreCase("") && !train_query_id.equalsIgnoreCase(evaluation_query_id)) {

                    String evalQueryURI = DBSynchronizer.getQueryURI(Integer.parseInt(evaluation_query_id));
                    Util.nicePrintMessage("import data from uri " + evalQueryURI);
                    URL eval_url = new URL(evalQueryURI);

                    if (!Util.isURLResponsive(eval_url)) {
                        re.eval("rm(list=ls());");
                        throw new AlsCustomException("There is a connectivity issue. Could not reach data for predefined evaluation query.\n"
                                + " Please check your connectivity and the responsiveness of the selected sparql endpoint.\n "
                                + "Then click on re-Evaluate button to try to run again the analytic process.", analytics);

                    } else {

                        File evaltmpfile4lindaquery = File.createTempFile("evaltmpfile4lindaquery" + evaluation_query_id, ".tmp");
                        FileUtils.copyURLToFile(eval_url, evaltmpfile4lindaquery);

                        Util.cleanTmpFileFromDatatypes(evaltmpfile4lindaquery.getAbsolutePath());

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
            DBSynchronizer.updateLindaAnalyticsProcessMessage("Rserve server not responsive. \n Reiniciate the Rserve Server or Contact the administrator.", analytics.getId());
        } catch (REXPMismatchException ex) {
            Logger.getLogger(RDFInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RDFInputFormat.class.getName()).log(Level.SEVERE, null, ex);
            DBSynchronizer.updateLindaAnalyticsProcessMessage("Input Queries are not responsive. \n", analytics.getId());
        } catch (AlsCustomException ex) {
            return null;
        }
        return re;
    }

}
