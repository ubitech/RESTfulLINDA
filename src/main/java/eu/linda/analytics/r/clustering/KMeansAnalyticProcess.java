/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.r.clustering;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.AnalyticProcess;
import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.formats.OutputFormat;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctionsSingleton;
import java.util.Vector;
import org.rosuda.JRI.RBool;
;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author eleni
 */


public class KMeansAnalyticProcess extends AnalyticProcess {

    HelpfulFunctionsSingleton helpfulFunctions;
    InputFormat input;
    ConnectionController connectionController;

    public KMeansAnalyticProcess(InputFormat input) {
        helpfulFunctions = HelpfulFunctionsSingleton.getInstance();
        helpfulFunctions.nicePrintMessage("Create analytic process for K-Means Algorithm");
        this.input = input;
        connectionController = ConnectionController.getInstance();

    }

    @Override
    public void train(Analytics analytics) {
    }

    @Override
    public void eval(Analytics analytics, OutputFormat out) {
        float timeToRun_analytics = 0;
        long startTimeToRun_analytics = System.currentTimeMillis();
        String RScript = "";
        //clean previous eval info if exists
        helpfulFunctions.cleanPreviousInfo(analytics);
        analytics.setTimeToGet_data(0);
        analytics.setTimeToRun_analytics(0);
        analytics.setData_size(0);
        Rengine re;
        if (helpfulFunctions.isRDFInputFormat(analytics.getTrainQuery_id())) {
            re = input.importData4R(Integer.toString(analytics.getTrainQuery_id()), true, analytics);

        } else {
            re = input.importData4R(Configuration.analyticsRepo + analytics.getDocument(), true, analytics);
            RScript += "loaded_data <- read.csv(file='" + Configuration.analyticsRepo + analytics.getDocument() + "', header=TRUE, sep=',');\n";

        }

        RBool is_query_responsive = re.eval("is_query_responsive").asBool();
        System.out.println("is_query_responsive:" + is_query_responsive.isTRUE());

        if (is_query_responsive.isFALSE()) {
            helpfulFunctions.updateProcessMessageToAnalyticsTable("There is a connectivity issue. Could not reach data for predefined query.\n"
                    + " Please check your connectivity and the responsiveness of the selected sparql endpoint.\n "
                    + "Then click on re-Evaluate button to try to run again the analytic process.", analytics.getId());
            re.eval("rm(list=ls());");
        } else {
            //TODO Check that all values are numeric

            re.eval("uri<-loaded_data$uri;");
            RScript += "uri<-loaded_data$uri; \n";

            re.eval("myvars <- names(loaded_data) %in% c('rowID','uri');");
            RScript += "# Prepare Data \n myvars <- names(loaded_data) %in% c('rowID','uri'); \n";

            re.eval("loaded_data <- loaded_data[!myvars]");
            RScript += "loaded_data <- loaded_data[!myvars]\n";

            re.eval("loaded_data <- na.omit(loaded_data)");
            RScript += "loaded_data <- na.omit(loaded_data) # listwise deletion of missing\n";

            re.eval("loaded_data <- scale(loaded_data)");
            RScript += "loaded_data <- scale(loaded_data) # standardize variables\n";

            //Partitioning
            re.eval("fit <- kmeans(loaded_data, 5)");
            RScript += "#Partitioning\n # K-Means Cluster Analysis \n fit <- kmeans(loaded_data, 5) # 5 cluster solution \n";

            re.eval("aggregate(loaded_data,by=list(fit$cluster),FUN=mean);");
            RScript += "# get cluster means \n aggregate(loaded_data,by=list(fit$cluster),FUN=mean) \n ";

            re.eval("loaded_data <- data.frame(loaded_data, fit$cluster);");
            RScript += "# append cluster assignment \n  loaded_data <- data.frame(loaded_data, fit$cluster) \n";

            //Visualize
            re.eval("fit <- kmeans(loaded_data, 5);");
            RScript += "# Visualize \n #Plotting Cluster Solutions \n # K-Means Clustering with 5 clusters \n fit <- kmeans(loaded_data, 5) \n";

            re.eval("library(cluster);");
            RScript += "# Cluster Plot against 1st 2 principal components  \n # vary parameters for most readable graph \n library(cluster); \n";

            long plot1_id = helpfulFunctions.manageNewPlot(analytics, "K-Means Clustering with 5 clusters", "plots/plotid" + analytics.getPlot1_id() + ".png", "plot1_id");

            re.eval("png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);");
            re.eval("print(clusplot(loaded_data, fit$cluster, color=TRUE, shade=TRUE, labels=2, lines=0));");
            re.eval("dev.off();");

            RScript += "png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);\n";
            RScript += "print(clusplot(loaded_data, fit$cluster, color=TRUE, shade=TRUE, labels=2, lines=0));\n";
            RScript += "dev.off();\n";

            re.eval("column_number<-ncol(loaded_data);");
            RScript += "column_number<-ncol(loaded_data); \n";

            re.eval("column_to_predict <-colnames(loaded_data[column_number]);");
            RScript += "column_to_predict <-colnames(loaded_data[column_number]); \n";

            re.eval("df_to_export <- data.frame(uri,loaded_data[column_to_predict]);");
            RScript += "df_to_export <- data.frame(uri,loaded_data[column_to_predict]);\n";

            helpfulFunctions.writeToFile(RScript, "processinfo", analytics);

           
            long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
            // Get elapsed time in seconds
            timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
            analytics.setTimeToRun_analytics(analytics.getTimeToRun_analytics() + timeToRun_analytics);
            connectionController.updateLindaAnalyticsProcessPerformanceTime(analytics);
            out.exportData(analytics, re);
            

        }

    }

}
