/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.r.forecasting;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.AnalyticProcess;
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
public class ArimaAnalyticProcess extends AnalyticProcess {

    Util helpfulFunctions;
    InputFormat input;

    public ArimaAnalyticProcess(InputFormat input) {
        helpfulFunctions = Util.getInstance();
        helpfulFunctions.nicePrintMessage("Create analytic process for Forecasting Arima Algorithm");
        this.input = input;

    }

    @Override
    public void train(Analytics analytics) {

    }

    @Override
    public void eval(Analytics analytics, OutputFormat out) {
        try {
            float timeToRun_analytics = 0;
            long startTimeToRun_analytics = System.currentTimeMillis();
            String startDate = "";
            String endDate = "";
            String timeGranularity = "months";
            String frequency = "12";
            String timePredicion = "1";

            //clean previous eval info if exists
            helpfulFunctions.cleanPreviousInfo(analytics);
            analytics.setTimeToGet_data(0);
            analytics.setTimeToRun_analytics(0);
            analytics.setData_size(0);
            analytics.setTimeToCreate_RDF(0);

            //get parameters
            String parameters = analytics.getParameters();

            String[] splitedparameters = parameters.split("->");
            for (String parameter : splitedparameters) {
                System.out.println("parameter" + parameter);

                if (parameter.contains("StartDate")) {
                    String[] startdateP = parameter.split("StartDate");
                    startDate = startdateP[1].trim();
                    System.out.println("startDate" + startDate);
                }
                if (parameter.contains("EndDate")) {
                    String[] endDateP = parameter.split("EndDate");
                    endDate = endDateP[1].trim();
                    System.out.println("endDate" + endDate);
                }
                if (parameter.contains("U")) {
                    String[] timeGranularityP = parameter.split("U");
                    timeGranularity = timeGranularityP[1].trim();
                    if (timeGranularity.equalsIgnoreCase("months")) {
                        frequency = "12";
                    } else if (timeGranularity.equalsIgnoreCase("years")) {
                        frequency = "1";
                    }
                    System.out.println("timeGranularity" + timeGranularity);
                }
                if (parameter.contains("Pred")) {
                    String[] PredP = parameter.split("Pred");
                    timePredicion = PredP[1].trim();
                    System.out.println("timePredicion" + timePredicion);
                }
            }

            RConnection re;
            if (helpfulFunctions.isRDFInputFormat(analytics.getTrainQuery_id())) {
                re = input.importData4R1(Integer.toString(analytics.getTrainQuery_id()),"", true, analytics);
            } else {
                re = input.importData4R1(Configuration.analyticsRepo + analytics.getDocument(),"", true, analytics);
            }

            org.rosuda.REngine.REXP is_train_query_responsive = re.eval("is_train_query_responsive");

            if (is_train_query_responsive.asString().equalsIgnoreCase("FALSE")) {
                helpfulFunctions.updateProcessMessageToAnalyticsTable("There is a connectivity issue. Could not reach data for predefined query.\n"
                        + " Please check your connectivity and the responsiveness of the selected sparql endpoint.\n "
                        + "Then click on re-Evaluate button to try to run again the analytic process.", analytics.getId());
                re.eval("rm(list=ls());");
            } else {
                //check if dataset contains date column
                re.eval(" if('date' %in% colnames(loaded_data)) {  exists_date <-TRUE }else{   exists_date <-FALSE }");
                org.rosuda.REngine.REXP exists_date = re.eval("exists_date");

                if (startDate.equalsIgnoreCase("") && exists_date.asString().equalsIgnoreCase("FALSE")) {
                    helpfulFunctions.updateProcessMessageToAnalyticsTable("The data you provided has no information about time.\n Please enter a dataset with a date column or provide the adecuate parameters.", analytics.getId());
                    re.eval("rm(list=ls());");
                } else {
                    String RScript = "loaded_data <- read.csv(file='" + Configuration.docroot + analytics.getDocument() + "', header=TRUE, sep=',');\n";

                    re.eval(" column_number<-ncol(loaded_data);");
                    RScript += "column_number<-ncol(loaded_data);\n";

                    re.eval(" column_to_predict <-colnames(loaded_data[column_number]);");
                    RScript += "column_to_predict <-colnames(loaded_data[column_number]);\n";

                    re.eval("uri<-loaded_data[2]; ");
                    RScript += "uri<-loaded_data[2]; \n";

                    re.eval("column_with_uri <-colnames(loaded_data[2]); ");
                    RScript += "column_with_uri <-colnames(loaded_data[2]); \n";

                    // ---- get analyzedFieldValue ----
                    org.rosuda.REngine.REXP column_to_predict = re.eval("column_to_predict");
                    String analyzedFieldValue = column_to_predict.asString();

                    re.eval("library(forecast)");
                    RScript += "library(forecast)\n";

                    re.eval("library(sp)");
                    RScript += "library(sp)\n";

                    re.eval("library(gstat)");
                    RScript += "library(gstat)\n";

                    re.eval("myvars <- names(loaded_data) %in% c('rowID')");
                    RScript += "myvars <- names(loaded_data) %in% c('rowID')\n";

                    re.eval("loaded_data <- loaded_data[!myvars]");
                    RScript += "loaded_data <- loaded_data[!myvars]\n";

                    re.eval(" rows_number<-nrow(loaded_data);");
                    RScript += "rows_number<-nrow(loaded_data);\n";

                    re.eval(" data_matrix<-as.matrix(loaded_data); ");
                    RScript += "data_matrix<-as.matrix(loaded_data);\n";

                    if (!startDate.equalsIgnoreCase("") && !endDate.equalsIgnoreCase("")) {
                        re.eval(" firstdate<-as.Date('" + startDate + "');");
                        RScript += "firstdate<-as.Date('" + startDate + "');\n";

                        re.eval(" lastdate <- as.Date('" + endDate + "'); ");
                        RScript += "lastdate <- as.Date('" + endDate + "');\n";

                    } else if (helpfulFunctions.isRDFExportFormat(analytics.getExportFormat())) {
                        re.eval(" firstdate<-as.Date(data_matrix[1,3]);");
                        RScript += "firstdate<-as.Date(data_matrix[1,3]);\n";

                        re.eval(" lastdate <- as.Date(data_matrix[rows_number,3]); ");
                        RScript += "lastdate <- as.Date(data_matrix[rows_number,3]);\n";
                    } else {
                        re.eval(" firstdate<-as.Date(data_matrix[1]);");
                        RScript += " firstdate<-as.Date(data_matrix[1]);\n";

                        re.eval(" lastdate <- as.Date(data_matrix[rows_number]); ");
                        RScript += "lastdate <- as.Date(data_matrix[rows_number]);\n";
                    }

                    re.eval(" year_to_start <-as.numeric(format(firstdate, format='%Y'));");
                    RScript += "year_to_start <-as.numeric(format(firstdate, format='%Y'));\n";

                    re.eval(" month_to_start <-as.numeric(format(firstdate, format='%m'));");
                    RScript += "month_to_start <-as.numeric(format(firstdate, format='%m'));\n";

                    re.eval(" day_to_start <-as.numeric(format(firstdate, format='%d'));");
                    RScript += "day_to_start <-as.numeric(format(firstdate, format='%d'));\n";

                    //clean column to predict from sparql datatypes
                    re.eval("valuesToClean<-loaded_data[column_to_predict];");
                    RScript += "valuesToClean<-loaded_data[column_to_predict];\n";

                    re.eval(" datats <- ts(loaded_data[column_to_predict], frequency=" + frequency + ", start=c(year_to_start,month_to_start)); ");
                    RScript += "datats <- ts(loaded_data[column_to_predict], frequency=" + frequency + ", start=c(year_to_start,month_to_start));\n";

                    re.eval(" add." + timeGranularity + "= function(date,n) seq(date, by = paste (n, '" + timeGranularity + "'), length = 2)[2];");
                    RScript += "add." + timeGranularity + "= function(date,n) seq(date, by = paste (n, '" + timeGranularity + "'), length = 2)[2];\n";

                    re.eval(" date_to_start_prediction=as.Date(lastdate) ; ");
                    RScript += "date_to_start_prediction=as.Date(lastdate) ;\n";

                    re.eval(" date_to_start_prediction<-add." + timeGranularity + "(date_to_start_prediction, 1);");
                    RScript += "date_to_start_prediction<-add." + timeGranularity + "(date_to_start_prediction, 1);\n";

                    re.eval(" Date = seq(date_to_start_prediction, by='" + timeGranularity + "', length=" + timePredicion + "); ");
                    RScript += "Date = seq(date_to_start_prediction, by='" + timeGranularity + "', length=" + timePredicion + ");\n";

                    re.eval("m.ar2 <- auto.arima(datats);");
                    RScript += "m.ar2 <- auto.arima(datats);\n";

                    long plot1_id = helpfulFunctions.manageNewPlot(analytics, "Forecasting Time Series for analyzed field: " + analyzedFieldValue, "plots/plotid" + analytics.getPlot1_id() + ".png", "plot1_id");

                    re.eval("png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600); a<-plot(forecast(m.ar2,h=" + timePredicion + ")); print(a); dev.off();");
                    RScript += "png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);a<-plot(forecast(m.ar2,h=" + timePredicion + ")); print(a); dev.off();\n";

                    re.eval("png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600)");
                    re.eval("arimaplottosave<-plot(forecast(m.ar2,h=" + timePredicion + "));");
                    re.eval("print(arimaplottosave);");
                    re.eval("dev.off();");

                    RScript += "arimaplottosave<-plot(forecast(m.ar2,h=" + timePredicion + "));\n";
                    RScript += "png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);\n";
                    RScript += "print(arimaplottosave);\n";
                    RScript += "dev.off();\n";

                    re.eval("p <- predict(m.ar2, n.ahead = " + timePredicion + ");");
                    RScript += "p <- predict(m.ar2, n.ahead = " + timePredicion + ");\n";

                    re.eval("rounded_values <-as.numeric(round(p$pred, digits = 3)); ");
                    RScript += "rounded_values <-as.numeric(round(p$pred, digits = 3));\n";

                    re.eval("df_to_export <- data.frame(Date,rounded_values); ");
                    RScript += "df_to_export <- data.frame(Date,rounded_values);\n";

                    re.eval("result_column_number<-ncol(df_to_export); ");
                    RScript += "result_column_number<-ncol(df_to_export);\n";

                    re.eval("colnames(df_to_export)[result_column_number] <- column_to_predict;");
                    RScript += "colnames(df_to_export)[result_column_number] <- column_to_predict;\n";

                    helpfulFunctions.writeToFile(RScript, "processinfo", analytics);

                    long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
                    // Get elapsed time in seconds
                    timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
                    analytics.setTimeToRun_analytics(timeToRun_analytics);
                    out.exportData(analytics, re);
                }
            }
        } catch (RserveException ex) {
            Logger.getLogger(ArimaAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REXPMismatchException ex) {
            Logger.getLogger(ArimaAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
