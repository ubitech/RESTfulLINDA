package eu.linda.analytics.r.forecasting;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.AnalyticProcess;
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
public class ArimaAnalyticProcess extends AnalyticProcess {

    InputFormat input;

    public ArimaAnalyticProcess(InputFormat input) {
        Util.nicePrintMessage("Create analytic process for Forecasting Arima Algorithm");
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
            Util.cleanPreviousInfo(analytics);
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
                    } else if (timeGranularity.equalsIgnoreCase("days")) {
                        frequency = "365.25";
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
            if (Util.isRDFInputFormat(analytics.getTrainQuery_id())) {
                re = input.importData4R(Integer.toString(analytics.getTrainQuery_id()), "0", true, analytics);
            } else {
                re = input.importData4R(Configuration.analyticsRepo + analytics.getDocument(), "0", true, analytics);
            }
            if (re == null) {
                return;
            }

            //check if dataset contains date column
            re.eval(" if('date' %in% colnames(loaded_data)) {  exists_date <-TRUE }else{   exists_date <-FALSE }");
            org.rosuda.REngine.REXP exists_date = re.eval("exists_date");

            if (startDate.equalsIgnoreCase("") && exists_date.asString().equalsIgnoreCase("FALSE")) {
                DBSynchronizer.updateLindaAnalyticsProcessMessage("The data you provided has no information about time.\n Please enter a dataset with a date column or provide the adecuate parameters.", analytics.getId());
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

                } else if (Util.isRDFExportFormat(analytics.getExportFormat())) {
                    
                            
                    re.eval(" datecolumn <- which(colnames(loaded_data) == \"date\");");
                    RScript += "datecolumn <- which(colnames(loaded_data) == \"date\");\n";

                    re.eval(" firstdate<-as.Date(data_matrix[1,datecolumn]);");
                    RScript += "firstdate<-as.Date(data_matrix[1,datecolumn]);\n";

                    re.eval(" lastdate <- as.Date(data_matrix[rows_number,datecolumn]); ");
                    RScript += "lastdate <- as.Date(data_matrix[rows_number,datecolumn]);\n";

                } else {
                    re.eval(" firstdate<-as.Date(data_matrix[1]); "
                            + "lastdate <- as.Date(data_matrix[rows_number]); ");

                    RScript += " firstdate<-as.Date(data_matrix[1]); \n"
                            + "lastdate <- as.Date(data_matrix[rows_number]); \n";

                }

                re.eval(" year_to_start <-as.numeric(format(firstdate, format='%Y')); "
                        + "month_to_start <-as.numeric(format(firstdate, format='%m')); "
                        + "day_to_start <-as.numeric(format(firstdate, format='%d')); ");

                RScript += "year_to_start <-as.numeric(format(firstdate, format='%Y'));\n "
                        + "month_to_start <-as.numeric(format(firstdate, format='%m'));\n "
                        + "day_to_start <-as.numeric(format(firstdate, format='%d'));\n ";

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

                long plot1_id = Util.manageNewPlot(analytics, "Forecasting Time Series for analyzed field: " + analyzedFieldValue, "plots/plotid" + analytics.getPlot1_id() + ".png", "plot1_id");

                re.eval("png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600); a<-plot(forecast(m.ar2,h=" + timePredicion + ")); print(a); dev.off();");
                RScript += "png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);a<-plot(forecast(m.ar2,h=" + timePredicion + ")); print(a); dev.off();\n";

                //re.eval("p <- predict(m.ar2, n.ahead = " + timePredicion + ");");
                //RScript += "p <- predict(m.ar2, n.ahead = " + timePredicion + ");\n";
                re.eval("p <- forecast(m.ar2, h = " + timePredicion + ");");
                RScript += "p <- forecast(m.ar2, h = " + timePredicion + ");\n";

                //re.eval("rounded_values <-as.numeric(round(p$pred, digits = 3)); ");
                //RScript += "rounded_values <-as.numeric(round(p$pred, digits = 3));\n";
                re.eval("rounded_values <-as.numeric(round(p$mean, digits = 3)); ");
                RScript += "rounded_values <-as.numeric(round(p$mean, digits = 3));\n";

                re.eval("df_to_export <- data.frame(Date,rounded_values); ");
                RScript += "df_to_export <- data.frame(Date,rounded_values);\n";

                re.eval("result_column_number<-ncol(df_to_export); ");
                RScript += "result_column_number<-ncol(df_to_export);\n";

                re.eval("colnames(df_to_export)[result_column_number] <- column_to_predict;");
                RScript += "colnames(df_to_export)[result_column_number] <- column_to_predict;\n";

                Util.writeToFile(RScript, "processinfo", analytics);

                long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
                // Get elapsed time in seconds
                timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
                analytics.setTimeToRun_analytics(timeToRun_analytics);
                out.exportData(analytics, re);
            }

        } catch (RserveException ex) {
            String ex_message = "Rserve server not responsive. \n"
                    + "Propably Rserve does not acess forecast library. \n"
                    + "Reiniciate the Rserve Server or Contact the administrator.";
            Logger.getLogger(ArimaAnalyticProcess.class.getName()).log(Level.SEVERE, ex_message, ex);
            DBSynchronizer.updateLindaAnalyticsProcessMessage(ex_message, analytics.getId());
        } catch (REXPMismatchException ex) {
            Logger.getLogger(ArimaAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
