package eu.linda.analytics.r.geospatial;

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
public class NCFCorrelogramAnalyticProcess extends AnalyticProcess {

    InputFormat input;
    ConnectionController connectionController;

    public NCFCorrelogramAnalyticProcess(InputFormat input) {
        Util.nicePrintMessage("Create analytic process for Geospatial Algorithm - NCF Correlogram Kriging");
        this.input = input;
        connectionController = ConnectionController.getInstance();

    }

    @Override
    public void train(Analytics analytics) {
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
                //import train dataset
                re = input.importData4R(Integer.toString(analytics.getTrainQuery_id()),"", true, analytics);
                RScript += "loaded_data <- read.csv('insertqueryid" + analytics.getTrainQuery_id() + "');\n";

            } else {
                //load train dataset
                re = input.importData4R(Configuration.analyticsRepo + analytics.getDocument(),"", true, analytics);
                RScript += "loaded_data <- read.csv('" + Configuration.analyticsRepo + analytics.getDocument() + "');\n";

            }
            if (re==null) return;

                //check if x y coordinates exist at the query
                re.eval("if('x' %in% colnames(loaded_data) && 'y' %in% colnames(loaded_data) ) {  exists_geo_info <-TRUE }else{   exists_geo_info <-FALSE }");
                org.rosuda.REngine.REXP exists_geo_info = re.eval("exists_geo_info");

                if (exists_geo_info.asString().equalsIgnoreCase("FALSE")) {
                    DBSynchronizer.updateLindaAnalyticsProcessMessage("The data you provided has no geospatial information.\n Please enter a dataset or query with a x & y information or provide the adecuate parameters.", analytics.getId());
                    re.eval("rm(list=ls());");
                } else {
                    re.eval("library(ncf);");
                    RScript += "library(ncf);\n";

                    re.eval("column_with_uri <-colnames(loaded_data[2]);");
                    RScript += "# Prepare Data \n column_with_uri <-colnames(loaded_data[2]); \n";

                    re.eval("myvars <- names(loaded_data) %in% c('rowID','basens',column_with_uri); ");
                    RScript += "myvars <- names(loaded_data) %in% c('rowID','basens',column_with_uri); \n";

                    re.eval("loaded_data <- loaded_data[!myvars] ");
                    RScript += "loaded_data <- loaded_data[!myvars] \n";

                    re.eval("loaded_data <- na.omit(loaded_data); ");
                    RScript += "loaded_data <- na.omit(loaded_data);\n";

                    re.eval("column_number<-ncol(loaded_data);");
                    RScript += "column_number<-ncol(loaded_data);\n";

                    re.eval("column_to_predict <-colnames(loaded_data[column_number]);");
                    RScript += "column_to_predict <-colnames(loaded_data[column_number]);\n";

                    // ---- get analyzedFieldValue ----
                    org.rosuda.REngine.REXP column_to_predict = re.eval("column_to_predict");
                    String analyzedFieldValue = column_to_predict.asString();

                    re.eval("rows_number<-nrow(loaded_data);");
                    RScript += "rows_number<-nrow(loaded_data);\n";

                    re.eval("valuesToClean<-loaded_data[column_to_predict];");
                    RScript += "valuesToClean<-loaded_data[column_to_predict];\n";

                    re.eval("valuesToCleanNum<-rows_number;");
                    RScript += "valuesToCleanNum<-rows_number;\n";

                    re.eval("trimmedValues<- data.frame();");
                    RScript += "trimmedValues<- data.frame();\n";

                    re.eval("for(i in 1:valuesToCleanNum){ valueToTrim <- as.character(valuesToClean[i,1]);  if(grepl(\"#\", valueToTrim)) {  position<-which(strsplit(valueToTrim, \"\")[[1]]==\"^\");  trimmedValues[i,1]<-substr(valueToTrim, 1, position[1]-1); }else{ trimmedValues[i,1]<-valueToTrim;}  }");
                    RScript += "for(i in 1:valuesToCleanNum){ valueToTrim <- as.character(valuesToClean[i,1]);  if(grepl(\"#\", valueToTrim)) {  position<-which(strsplit(valueToTrim, \"\")[[1]]==\"^\");  trimmedValues[i,1]<-substr(valueToTrim, 1, position[1]-1); }else{ trimmedValues[i,1]<-valueToTrim;}  }\n";

                    re.eval("loaded_data[[column_to_predict]] <- trimmedValues;");
                    RScript += "loaded_data[[column_to_predict]] <- trimmedValues;\n";

                    re.eval("loaded_data$" + analyzedFieldValue + "<-as.numeric(unlist(loaded_data$" + analyzedFieldValue + "));");
                    RScript += "loaded_data$" + analyzedFieldValue + "<-as.numeric(unlist(loaded_data$" + analyzedFieldValue + "));\n";

                    re.eval("var<-as.vector(loaded_data$" + analyzedFieldValue + ");");
                    RScript += " var<-as.vector(loaded_data$" + analyzedFieldValue + ");\n";

                    re.eval("fit1 <-correlog(loaded_data$x, loaded_data$y, var, w = NULL, increment=2, resamp = 50, latlon = TRUE, na.rm = FALSE, quiet = FALSE);");
                    RScript += "fit1 <-correlog(loaded_data$x, loaded_data$y, var, w = NULL, increment=2, resamp = 50, latlon = TRUE, na.rm = FALSE, quiet = FALSE);\n";

                    long plot1_id = Util.manageNewPlot(analytics, "NCF Correlogram Plot for the analyzed field: " + analyzedFieldValue, "plots/plotid" + analytics.getPlot1_id() + ".png", "plot1_id");

                    //re.eval("correlogram<-plot(fit1)");
                    re.eval("png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);");
                    re.eval("print(plot(fit1));");
                    re.eval("dev.off();");

                    RScript += "png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);\n";
                    RScript += "print(plot(fit1));\n dev.off();\n";

                    re.eval("x.df <- data.frame(fit1$correlation);");
                    RScript += "x.df <- data.frame(fit1$correlation);\n";

                    re.eval("x.sub <- subset(x.df, fit1$correlation > 0.4);");
                    RScript += "x.sub <- subset(x.df, fit1$correlation > 0.4);\n";

                    re.eval(" sub<-nrow(x.sub); total_rows<-nrow(x.df); correlation_percent<-(sub/total_rows*100);");
                    RScript += " sub<-nrow(x.sub);\n total_rows<-nrow(x.df);\n correlation_percent<-(sub/total_rows*100);\n";

                    org.rosuda.REngine.REXP correlation_percent = re.eval("(sub/total_rows*100);");
                    System.out.println("correlation_percent" + correlation_percent);

                    Util.writeToFile(RScript, "processinfo", analytics);

                    re.eval("rm(list=ls());");
                    long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
                    // Get elapsed time in seconds
                    timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
                    analytics.setTimeToRun_analytics(timeToRun_analytics);

                    DBSynchronizer.updateLindaAnalyticsProcessPerformanceTime(analytics);
                    re.close();
                }
            
        } catch (RserveException ex) {
            Logger.getLogger(NCFCorrelogramAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REXPMismatchException ex) {
            Logger.getLogger(NCFCorrelogramAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void eval(Analytics analytics, OutputFormat out) {
    }

}
