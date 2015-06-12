package eu.linda.analytics.r.clustering;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.AnalyticProcess;
import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.formats.OutputFormat;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.Util;
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
                re = input.importData4R(Integer.toString(analytics.getTrainQuery_id()),"", true, analytics);
                
            } else {
                re = input.importData4R(Configuration.analyticsRepo + analytics.getDocument(),"", true, analytics);
                RScript += "loaded_data <- read.csv(file='" + Configuration.analyticsRepo + analytics.getDocument() + "', header=TRUE, sep=',');\n";
                
            }
            
            org.rosuda.REngine.REXP is_train_query_responsive = re.eval("is_train_query_responsive");
            
            
            if (is_train_query_responsive.asString().equalsIgnoreCase("FALSE")) {
                Util.updateProcessMessageToAnalyticsTable("There is a connectivity issue. Could not reach data for predefined query.\n"
                        + " Please check your connectivity and the responsiveness of the selected sparql endpoint.\n "
                        + "Then click on re-Evaluate button to try to run again the analytic process.", analytics.getId());
                re.eval("rm(list=ls());");
            } else {
                //TODO Check that all values are numeric
                
                
                re.eval("column_with_uri <-colnames(loaded_data[2]);");
                RScript += "# Prepare Data \n column_with_uri <-colnames(loaded_data[2]); \n";
                
                
                re.eval("myvars <- names(loaded_data) %in% c('rowID',column_with_uri);");
                RScript += "myvars <- names(loaded_data) %in% c('rowID',column_with_uri); \n";
                
                re.eval("loaded_data <- loaded_data[!myvars]");
                RScript += "loaded_data <- loaded_data[!myvars]\n";
                
                re.eval("loaded_data <- na.omit(loaded_data)");
                RScript += "loaded_data <- na.omit(loaded_data) # listwise deletion of missing\n";
                
                re.eval("loaded_data <- scale(loaded_data)");
                RScript += "loaded_data <- scale(loaded_data) # standardize variables\n";
                
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
            }
        } catch (RserveException ex) {
            Logger.getLogger(ClustersNumberAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REXPMismatchException ex) {
            Logger.getLogger(ClustersNumberAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
