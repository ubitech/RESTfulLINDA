/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.formats;

import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.utils.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class CSVInputFormat extends InputFormat {

    Util helpfulFuncions;
    ConnectionController connectionController;

    public CSVInputFormat() {
        helpfulFuncions = Util.getInstance();
        connectionController = ConnectionController.getInstance();
    }

    @Override
    public AbstractList importData4weka(String trainDataset, String evaluationDataset, boolean isForRDFOutput, Analytics analytics) {

        float timeToGetQuery = 0;
        long startTimeToGetQuery = System.currentTimeMillis();
        Util.nicePrintMessage("import CSV file ");

        System.out.println("Import data from file: " + trainDataset);

        Instances data = null;
        try {
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File(trainDataset));
            if (isForRDFOutput) {
                loader.setStringAttributes("1,2");
            }

            loader.setFieldSeparator(",");
            data = loader.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);

            FileInputStream fis = null;

            fis = new FileInputStream(trainDataset);
            System.out.println("fis.getChannel().size() " + fis.getChannel().size());
            analytics.setData_size(analytics.getData_size() + fis.getChannel().size());
            fis.close();

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
    public RConnection importData4R(String trainDataset, String evaluationDataset, boolean isForRDFOutput, Analytics analytics) {

        float timeToGetQuery = 0;
        long startTimeToGetQuery = System.currentTimeMillis();
        FileInputStream trainfis = null;
        long trainfisSize = 0;
        FileInputStream evalfis = null;
        RConnection re = null;
        System.out.println(System.getProperty("java.library.path"));
        System.out.println("R_HOME" + System.getenv().get("R_HOME"));

        try {
            re = new RConnection();
            REXP x = re.eval("R.version.string");
            System.out.println(x.asString());
            re.eval("is_train_query_responsive <-TRUE  ");
            re.eval(" loaded_data <- read.csv(file='" + trainDataset + "', header=TRUE, sep=',', na.strings='---');");

            trainfis = new FileInputStream(trainDataset);
            trainfisSize = trainfis.getChannel().size();
            System.out.println("fis.getChannel().size() " + trainfisSize);
            analytics.setData_size(analytics.getData_size() + trainfisSize);
            trainfis.close();

            if (!evaluationDataset.equalsIgnoreCase("") && trainDataset.equalsIgnoreCase(evaluationDataset)) {

                re.eval("is_eval_query_responsive <-TRUE; "
                        + "loaded_data_eval <- loaded_data; ");
                System.out.println("is_eval_query_responsive <-TRUE; "
                        + "loaded_data_eval <- loaded_data; ");
            }

            if (!evaluationDataset.equalsIgnoreCase("") && !trainDataset.equalsIgnoreCase(evaluationDataset)) {

                re.eval("is_eval_query_responsive <-TRUE  ");
                System.out.println("is_eval_query_responsive <-TRUE ");

                re.eval(" loaded_data_eval <- read.csv(file='" + evaluationDataset + "', header=TRUE, sep=',', na.strings='---');");
                System.out.println(" loaded_data_eval <- read.csv(file='" + evaluationDataset + "', header=TRUE, sep=',', na.strings='---');");

                evalfis = new FileInputStream(evaluationDataset);
                System.out.println("fis.getChannel().size() " + evalfis.getChannel().size());
                analytics.setData_size(analytics.getData_size() + trainfisSize + evalfis.getChannel().size());
                evalfis.close();

            }

            // Get elapsed time in milliseconds
            long elapsedTimeToGetQueryMillis = System.currentTimeMillis() - startTimeToGetQuery;
            // Get elapsed time in seconds
            timeToGetQuery = elapsedTimeToGetQueryMillis / 1000F;
            analytics.setTimeToGet_data(analytics.getTimeToGet_data() + timeToGetQuery);
            System.out.println("timeToGetQuery" + timeToGetQuery);

            DBSynchronizer.updateLindaAnalyticsInputDataPerformanceTime(analytics);

        } catch (RserveException ex) {
            Logger.getLogger(CSVInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CSVInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CSVInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REXPMismatchException ex) {
            Logger.getLogger(CSVInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return re;
    }




}
