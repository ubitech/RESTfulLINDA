/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.r.geospatial;

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
public class MoransAnalyticProcess extends AnalyticProcess {

    Util util;
    InputFormat input;
    ConnectionController connectionController;

    public MoransAnalyticProcess(InputFormat input) {
        util = Util.getInstance();
        util.nicePrintMessage("Create analytic process for Moran's I Algorithm");
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
            util.cleanPreviousInfo(analytics);
            analytics.setTimeToGet_data(0);
            analytics.setTimeToRun_analytics(0);
            analytics.setData_size(0);
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

                //check if x y coordinates exist at the query
                re.eval("if('x' %in% colnames(loaded_data) && 'y' %in% colnames(loaded_data) ) {  exists_geo_info <-TRUE }else{   exists_geo_info <-FALSE }");
                org.rosuda.REngine.REXP exists_geo_info = re.eval("exists_geo_info");

                if (exists_geo_info.asString().equalsIgnoreCase("FALSE")) {
                    util.updateProcessMessageToAnalyticsTable("The data you provided has no geospatial information.\n Please enter a dataset or query with a x & y information or provide the adecuate parameters.", analytics.getId());
                    re.eval("rm(list=ls());");
                } else {

                    re.eval("library(ape)");
                    RScript += "library(ape)\n";

                    re.eval("loaded_data <- na.omit(loaded_data);");
                    RScript += "loaded_data <- na.omit(loaded_data);\n";

                    re.eval("column_with_uri <-colnames(loaded_data[2]);");
                    RScript += "column_with_uri <-colnames(loaded_data[2]); \n";

                    re.eval("myvars <- names(loaded_data) %in% c('rowID','basens',column_with_uri);");
                    RScript += "myvars <- names(loaded_data) %in% c('rowID','basens',column_with_uri);\n";

                    re.eval("loaded_data <- loaded_data[!myvars];");
                    RScript += "loaded_data <- loaded_data[!myvars];\n";

                    re.eval("column_number<-ncol(loaded_data);");
                    RScript += "column_number<-ncol(loaded_data);\n";

                    re.eval("rows_number<-nrow(loaded_data);");
                    RScript += "rows_number<-nrow(loaded_data);\n";

                    re.eval("column_to_predict <-colnames(loaded_data[column_number]); ");
                    RScript += "column_to_predict <-colnames(loaded_data[column_number]);\n ";
                    
                    // ---- get analyzedFieldValue ----
                    org.rosuda.REngine.REXP column_to_predict = re.eval("column_to_predict");
                    String analyzedFieldValue = column_to_predict.asString();

                    re.eval("trimmedValues<- data.frame();");
                    RScript += "trimmedValues<- data.frame();\n ";

                    re.eval("valuesToClean<-loaded_data[column_to_predict]; ");
                    RScript += "valuesToClean<-loaded_data[column_to_predict]; \n";

                    re.eval("valuesToCleanNum<-rows_number;");
                    RScript += "valuesToCleanNum<-rows_number; \n";

                    re.eval("for(i in 1:valuesToCleanNum){ valueToTrim <- as.character(valuesToClean[i,1]);  if(grepl(\"#\", valueToTrim)) {  position<-which(strsplit(valueToTrim, \"\")[[1]]==\"^\");  trimmedValues[i,1]<-substr(valueToTrim, 1, position[1]-1); }else{ trimmedValues[i,1]<-valueToTrim;}  }");
                    RScript += "for(i in 1:valuesToCleanNum){ valueToTrim <- as.character(valuesToClean[i,1]);  if(grepl(\"#\", valueToTrim)) {  position<-which(strsplit(valueToTrim, \"\")[[1]]==\"^\");  trimmedValues[i,1]<-substr(valueToTrim, 1, position[1]-1); }else{ trimmedValues[i,1]<-valueToTrim;}  }\n";

                    re.eval("result_column_number<-ncol(loaded_data); ");
                    RScript += "result_column_number<-ncol(loaded_data); \n";

                    re.eval("colnames(trimmedValues)[1]<- column_to_predict;");
                    RScript += "colnames(trimmedValues)[1]<- column_to_predict;\n";

                    re.eval("trimmedValues$" + analyzedFieldValue + "<-as.numeric(trimmedValues$" + analyzedFieldValue + ");");
                    RScript += "trimmedValues$" + analyzedFieldValue + "<-as.numeric(trimmedValues$" + analyzedFieldValue + "); \n";

                    re.eval("loaded_data[[column_to_predict]] <- trimmedValues;");
                    RScript += "loaded_data[[column_to_predict]] <- trimmedValues;\n";

                    re.eval("loaded_data.dists <- as.matrix(dist(cbind(loaded_data$x, loaded_data$y)));");
                    RScript += "loaded_data.dists <- as.matrix(dist(cbind(loaded_data$x, loaded_data$y)));\n";

                    re.eval("loaded_data.dists.inv <- 1/loaded_data.dists; ");
                    RScript += "loaded_data.dists.inv <- 1/loaded_data.dists; \n";

                    re.eval(" loaded_data.dists.inv[is.infinite(loaded_data.dists.inv)] <- 0");
                    RScript += "loaded_data.dists.inv[is.infinite(loaded_data.dists.inv)] <- 0 \n";

                    re.eval("diag(loaded_data.dists.inv) <- 0");
                    RScript += "diag(loaded_data.dists.inv) <- 0\n";

                    re.eval("morans_result<-Moran.I(loaded_data$" + analyzedFieldValue + "[1:rows_number,], loaded_data.dists.inv[1:rows_number, 1:rows_number]);");
                    RScript += "morans_result<-Moran.I(loaded_data$" + analyzedFieldValue + "[1:rows_number,], loaded_data.dists.inv[1:rows_number, 1:rows_number]);\n";

                    re.eval("if (exists('morans_result'))   {  result <-  morans_result$p;   } else {  result <- 0.0;}");
                    RScript += "if (exists('morans_result'))   {  result <-  morans_result$p;   } else {  result <- 0.0;}\n";

                    double pvalue = re.eval("result").asDouble();
                    System.out.println("pvalue:" + pvalue);

                    double moranObservedValue = re.eval("morans_result$observed").asDouble();
                    System.out.println("moranObservedValue:" + moranObservedValue);

                    String processMessage = "Moran's I Result for analyzed Field: " + analyzedFieldValue + ". \n";
                    //processMessage += "$p.value  : " + re.eval("morans_result$p").asDouble() + ".\n";
                    processMessage += "$observed.value  : " + moranObservedValue + ".\n";
                    processMessage += "$expected.value  : " + re.eval("morans_result$expected").asDouble() + ".\n";
                    processMessage += "$expected.sd  : " + re.eval("morans_result$sd").asDouble() + ".\n";

                    if (moranObservedValue > 0) {
                        processMessage += "\n There is a significant spatial autocorrelation in your data \n and you should take into account in next analytic processes \n. You could double check this result with NCF Correlogram Algorithm";
                    } else {
                        processMessage += "\n Moran's I did not detect a significant spatial autocorrelation in your data. \n You could double check this result with NCF Correlogram Algorithm";

                    }
                    util.updateProcessMessageToAnalyticsTable(processMessage, analytics.getId());

                    util.writeToFile(RScript, "processinfo", analytics);

                    re.eval("rm(list=ls());");
                    long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
                    // Get elapsed time in seconds
                    timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
                    analytics.setTimeToRun_analytics(analytics.getTimeToRun_analytics() + timeToRun_analytics);
                    connectionController.updateLindaAnalyticsProcessPerformanceTime(analytics);
                }
            }
        } catch (RserveException ex) {
            Logger.getLogger(MoransAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REXPMismatchException ex) {
            Logger.getLogger(MoransAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
