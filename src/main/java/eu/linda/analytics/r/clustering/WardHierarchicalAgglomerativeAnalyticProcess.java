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


public class WardHierarchicalAgglomerativeAnalyticProcess extends AnalyticProcess {

    HelpfulFunctionsSingleton helpfulFunctions;
    InputFormat input;
    ConnectionController connectionController;

    public WardHierarchicalAgglomerativeAnalyticProcess(InputFormat input) {
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

            // Ward Hierarchical Clustering
            re.eval("d <- dist(loaded_data, method = 'euclidean') # distance matrix");
            RScript += "# Ward Hierarchical Clustering \n d <- dist(loaded_data, method = 'euclidean') # distance matrix \n";

            re.eval("fit <- hclust(d, method='ward'); ");
            RScript += "fit <- hclust(d, method='ward'); \n";

            long plot1_id = helpfulFunctions.manageNewPlot(analytics, "Ward Hierarchical Clustering Dendogram", "plots/plotid" + analytics.getPlot1_id() + ".png", "plot1_id");

            re.eval("png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);");
            re.eval("print(plot(fit));");
            re.eval("dev.off();");

//            re.eval("groups <- cutree(fit, k=5);");
//            RScript += "groups <- cutree(fit, k=5); # cut tree into 5 clusters\n";
            RScript += "# display dendogram  \n ";
            RScript += "png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);\n";
            RScript += "print(plot(fit));\n";
            RScript += "dev.off();\n";

            helpfulFunctions.writeToFile(RScript, "processinfo", analytics);

            re.eval("rm(list=ls());");
            long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
            // Get elapsed time in seconds
            timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
            analytics.setTimeToRun_analytics(analytics.getTimeToRun_analytics() + timeToRun_analytics);
            connectionController.updateLindaAnalyticsProcessPerformanceTime(analytics);

        }

    }

}
