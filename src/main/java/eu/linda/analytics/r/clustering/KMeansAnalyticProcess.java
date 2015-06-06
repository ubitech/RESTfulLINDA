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


public class KMeansAnalyticProcess extends AnalyticProcess {

    Util util;
    InputFormat input;
    ConnectionController connectionController;

    public KMeansAnalyticProcess(InputFormat input) {
        util = Util.getInstance();
        util.nicePrintMessage("Create analytic process for K-Means Algorithm");
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
            int clustersNum = 5;
            String RScript = "";
            //clean previous eval info if exists
            util.cleanPreviousInfo(analytics);
            analytics.setTimeToGet_data(0);
            analytics.setTimeToRun_analytics(0);
            analytics.setData_size(0);
            
            //get parameters
            String parameters = analytics.getParameters();
            
            String[] splitedparameters = parameters.split("->");
            for (String parameter : splitedparameters) {
                System.out.println("parameter" + parameter);
                
                if (parameter.contains("k")) {
                    String[] clustersNumP = parameter.split("k");
                    clustersNum = Integer.parseInt(clustersNumP[1].trim());
                    System.out.println("clustersNum" + clustersNum);
                }
            }
            
            RConnection re;
            if (util.isRDFInputFormat(analytics.getTrainQuery_id())) {
                re = input.importData4R1(Integer.toString(analytics.getTrainQuery_id()),"", true, analytics);
                
            } else {
                re = input.importData4R1(Configuration.analyticsRepo + analytics.getDocument(),"", true, analytics);
                RScript += "loaded_data <- read.csv(file='" + Configuration.analyticsRepo + analytics.getDocument() + "', header=TRUE, sep=',');\n";
                
            }
            
            org.rosuda.REngine.REXP is_train_query_responsive = re.eval("is_train_query_responsive");
            
            if (is_train_query_responsive.asString().equalsIgnoreCase("FALSE")) {
                util.updateProcessMessageToAnalyticsTable("There is a connectivity issue. Could not reach data for predefined query.\n"
                        + " Please check your connectivity and the responsiveness of the selected sparql endpoint.\n "
                        + "Then click on re-Evaluate button to try to run again the analytic process.", analytics.getId());
                re.eval("rm(list=ls());");
            } else {
                //TODO Check that all values are numeric
                
                
                re.eval("uri<-loaded_data[2];");
                RScript += "# Prepare Data \n uri<-loaded_data[2]; \n";
                
                re.eval("column_with_uri <-colnames(loaded_data[2]);");
                RScript += "column_with_uri <-colnames(loaded_data[2]); \n";
                
                re.eval("myvars <- names(loaded_data) %in% c('rowID',column_with_uri);");
                RScript += "# Prepare Data \n myvars <- names(loaded_data) %in% c('rowID',column_with_uri); \n";
                
                re.eval("loaded_data <- loaded_data[!myvars]");
                RScript += "loaded_data <- loaded_data[!myvars]\n";
                
                re.eval("loaded_data <- na.omit(loaded_data)");
                RScript += "loaded_data <- na.omit(loaded_data) # listwise deletion of missing\n";
                
                re.eval("loaded_data <- scale(loaded_data)");
                RScript += "loaded_data <- scale(loaded_data) # standardize variables\n";
                
                //Partitioning
                re.eval("fit <- kmeans(loaded_data, " + clustersNum + ")");
                RScript += "#Partitioning\n # K-Means Cluster Analysis \n fit <- kmeans(loaded_data, " + clustersNum + ") # " + clustersNum + " cluster solution \n";
                
                re.eval("aggregate(loaded_data,by=list(fit$cluster),FUN=mean);");
                RScript += "# get cluster means \n aggregate(loaded_data,by=list(fit$cluster),FUN=mean) \n ";
                
                re.eval("loaded_data <- data.frame(loaded_data, fit$cluster);");
                RScript += "# append cluster assignment \n  loaded_data <- data.frame(loaded_data, fit$cluster) \n";
                
                //Visualize
                re.eval("fit <- kmeans(loaded_data, " + clustersNum + ");");
                RScript += "# Visualize \n #Plotting Cluster Solutions \n # K-Means Clustering with " + clustersNum + " clusters \n fit <- kmeans(loaded_data, " + clustersNum + ") \n";
                
                re.eval("library(cluster);");
                RScript += "# Cluster Plot against 1st 2 principal components  \n # vary parameters for most readable graph \n library(cluster); \n";
                
                long plot1_id = util.manageNewPlot(analytics, "K-Means Clustering with " + clustersNum + " clusters", "plots/plotid" + analytics.getPlot1_id() + ".png", "plot1_id");
                
                re.eval("png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);");
                re.eval("print(clusplot(loaded_data, fit$cluster, color=TRUE, shade=TRUE, labels=2, lines=0));");
                re.eval("dev.off();");
                
                RScript += "png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);\n";
                RScript += "print(clusplot(loaded_data, fit$cluster, color=TRUE, shade=TRUE, labels=2, lines=0));\n";
                RScript += "dev.off();\n";
                
                //Save model Readable
                String modelFileName = "models/analyticsID" + analytics.getId() + "_" + analytics.getAlgorithm_id() + "ModelReadable" + ".txt";
                String modelFileNameFullPath = Configuration.analyticsRepo + modelFileName;
                
                org.rosuda.REngine.REXP s = re.eval("capture.output(fit$centers)");
                String[] output = s.asStrings();
                for (String string : output) {
                    System.out.println(string);
                }
                
                util.saveFile(modelFileNameFullPath, output);
                connectionController.updateLindaAnalytics(modelFileName, "modelReadable", analytics.getId());
                
                re.eval("column_number<-ncol(loaded_data);");
                RScript += "column_number<-ncol(loaded_data); \n";
                
                re.eval("column_to_predict <-colnames(loaded_data[column_number]);");
                RScript += "column_to_predict <-colnames(loaded_data[column_number]); \n";
                
                re.eval("df_to_export <- data.frame(uri,loaded_data[column_to_predict]);");
                RScript += "df_to_export <- data.frame(uri,loaded_data[column_to_predict]);\n";
                
                util.writeToFile(RScript, "processinfo", analytics);
                
                long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
                // Get elapsed time in seconds
                timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
                analytics.setTimeToRun_analytics(analytics.getTimeToRun_analytics() + timeToRun_analytics);
                connectionController.updateLindaAnalyticsProcessPerformanceTime(analytics);
                out.exportData(analytics, re);
                
            }
        } catch (RserveException ex) {
            Logger.getLogger(KMeansAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REXPMismatchException ex) {
            Logger.getLogger(KMeansAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
