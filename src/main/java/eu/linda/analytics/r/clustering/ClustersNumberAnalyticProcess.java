package eu.linda.analytics.r.clustering;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.AnalyticProcess;
import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.formats.OutputFormat;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.utils.Util;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 *
 * @author eleni
 */
public class ClustersNumberAnalyticProcess extends AnalyticProcess {

    InputFormat input;
    ConnectionController connectionController;

    public ClustersNumberAnalyticProcess(InputFormat input) {
        Util.nicePrintMessage("Create analytic process for K-Means Algorithm");
        this.input = input;
        connectionController = ConnectionController.getInstance();

    }

    @Override
    public void train(Analytics analytics) {
    }

    @Override
    public void eval(Analytics analytics, OutputFormat out) {
        try {
            float timeToRun_analytics = 0;
            long startTimeToRun_analytics = System.currentTimeMillis();
            String RScript = "";
            //clean previous eval info if exists
            Util.cleanPreviousInfo(analytics);
            analytics.setTimeToGet_data(0);
            analytics.setTimeToRun_analytics(0);
            analytics.setData_size(0);
            RConnection re;
            if (Util.isRDFInputFormat(analytics.getTrainQuery_id())) {
                re = input.importData4R(Integer.toString(analytics.getTrainQuery_id()), "0", true, analytics);

            } else {
                re = input.importData4R(Configuration.analyticsRepo + analytics.getDocument(), "0", true, analytics);
                RScript += "loaded_data <- read.csv(file='" + Configuration.analyticsRepo + analytics.getDocument() + "', header=TRUE, sep=',');\n";

            }
            if (re == null) {
                return;
            }
            //TODO Check that all values are numeric

            re.eval("column_with_uri <-colnames(loaded_data[2]);");
            RScript += "# Prepare Data \n column_with_uri <-colnames(loaded_data[2]); \n";

            re.eval("myvars <- names(loaded_data) %in% c('rowID',column_with_uri);");
            RScript += "myvars <- names(loaded_data) %in% c('rowID',column_with_uri); \n";

            re.eval("loaded_data <- loaded_data[!myvars]; "
                    + "nums <- sapply(loaded_data, is.numeric); "
                    + "loaded_data<-loaded_data[ , nums]; ");

            RScript += "loaded_data <- loaded_data[!myvars]; \n"
                    + "nums <- sapply(loaded_data, is.numeric); \n "
                    + "loaded_data<-loaded_data[ , nums]; \n";

            re.eval("loaded_data <- na.omit(loaded_data)");
            RScript += "loaded_data <- na.omit(loaded_data) # listwise deletion of missing\n";

            re.eval("loaded_data <- scale(loaded_data)");
            RScript += "loaded_data <- scale(loaded_data) # standardize variables\n";

            int num_of_not_numerical_variables = re.eval("table(nums)[\"FALSE\"]").asInteger();
            if (num_of_not_numerical_variables > 0) {
                DBSynchronizer.updateLindaAnalyticsProcessMessage("Note : Input queries contain non numeric variables and these have been ignored during the analysis. \n "
                        + "If you want to analyze data that contain mixed numerical and categorical data, \n consider using the M5P or J48 algorithms. \n"
                        + "In case all your variables are categorical, consider use the K-modes algorithm", analytics.getId());
            }

            re.eval("wss <- (nrow(loaded_data)-1)*sum(apply(loaded_data,2,var));");
            RScript += "# Determine number of clusters \n wss <- (nrow(loaded_data)-1)*sum(apply(loaded_data,2,var)); \n";

            re.eval("if(nrow(loaded_data)-1>15) {cluster_num <-15;} else {cluster_num <-nrow(loaded_data)-1;}");
            RScript += "if(nrow(loaded_data)-1>15) {cluster_num <-15;} else {cluster_num <-nrow(loaded_data)-1;} \n";

            re.eval("for (i in 2:cluster_num) wss[i] <- sum(kmeans(loaded_data, centers=i)$withinss);");
            RScript += "for (i in 2:cluster_num) wss[i] <- sum(kmeans(loaded_data, centers=i)$withinss);  \n";

            long plot1_id = Util.manageNewPlot(analytics, "Definition of Clusters number", "plots/plotid" + analytics.getPlot1_id() + ".png", "plot1_id");

            re.eval("png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);");
            re.eval("plot(1:cluster_num, wss, type='b', xlab='Number of Clusters', ylab='Within groups sum of squares');");
            re.eval("dev.off();");

            RScript += "png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);\n";
            RScript += "plot(1:cluster_num, wss, type='b', xlab='Number of Clusters', ylab='Within groups sum of squares');\n";
            RScript += "dev.off();\n";

            Util.writeToFile(RScript, "processinfo", analytics);

            re.eval("rm(list=ls());");
            long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
            // Get elapsed time in seconds
            timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
            analytics.setTimeToRun_analytics(analytics.getTimeToRun_analytics() + timeToRun_analytics);
            DBSynchronizer.updateLindaAnalyticsProcessPerformanceTime(analytics);
            re.close();

        } catch (RserveException ex) {
            Logger.getLogger(ClustersNumberAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REXPMismatchException ex) {
            Logger.getLogger(ClustersNumberAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
