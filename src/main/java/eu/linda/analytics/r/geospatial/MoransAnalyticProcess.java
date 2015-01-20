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
import eu.linda.analytics.weka.utils.HelpfulFunctionsSingleton;
import java.util.HashMap;
import java.util.Vector;
import org.rosuda.JRI.RBool;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;
import weka.core.Instances;

/**
 *
 * @author eleni
 */
public class MoransAnalyticProcess extends AnalyticProcess {

    HelpfulFunctionsSingleton helpfulFunctions;
    InputFormat input;
    ConnectionController connectionController;

    public MoransAnalyticProcess(InputFormat input) {
        helpfulFunctions = HelpfulFunctionsSingleton.getInstance();
        helpfulFunctions.nicePrintMessage("Create analytic process for Moran's I Algorithm");
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

            //check if x y coordinates exist at the query
            re.eval("if('x' %in% colnames(loaded_data) && 'y' %in% colnames(loaded_data) ) {  exists_geo_info <-TRUE }else{   exists_geo_info <-FALSE }");
            RBool exists_geo_info = re.eval("exists_geo_info").asBool();
            System.out.println("exists_geo_info:" + exists_geo_info.isTRUE());

            if (exists_geo_info.isFALSE()) {
                helpfulFunctions.updateProcessMessageToAnalyticsTable("The data you provided has no geospatial information.\n Please enter a dataset or query with a x & y information or provide the adecuate parameters.", analytics.getId());
                re.eval("rm(list=ls());");
            } else {

                // ---- get analyzedFieldValue ----
                RVector dataToExportasVector = re.eval("loaded_data").asVector();
                Vector colnames = dataToExportasVector.getNames();

                String[] colnamesArray = new String[colnames.size()];
                colnames.copyInto(colnamesArray);

                String analyzedFieldValue = colnamesArray[colnames.size() - 1];
                //

                re.eval("library(ape)");
                RScript += "library(ape)\n";

                re.eval("loaded_data <- na.omit(loaded_data);");
                RScript += "loaded_data <- na.omit(loaded_data);\n";

                re.eval("myvars <- names(loaded_data) %in% c('rowID','basens','uri');");
                RScript += "myvars <- names(loaded_data) %in% c('rowID','basens','uri');\n";

                re.eval("loaded_data <- loaded_data[!myvars];");
                RScript += "loaded_data <- loaded_data[!myvars];\n";

                re.eval("column_number<-ncol(loaded_data);");
                RScript += "column_number<-ncol(loaded_data);\n";

                re.eval("rows_number<-nrow(loaded_data);");
                RScript += "rows_number<-nrow(loaded_data);\n";

                re.eval("column_to_predict <-colnames(loaded_data[column_number]); ");
                RScript += "column_to_predict <-colnames(loaded_data[column_number]);\n ";

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
                helpfulFunctions.updateProcessMessageToAnalyticsTable(processMessage, analytics.getId());

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

}
