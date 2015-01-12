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
import eu.linda.analytics.weka.utils.HelpfulFunctionsSingleton;
import java.util.Vector;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author eleni
 */
public class ArimaAnalyticProcess extends AnalyticProcess {

    HelpfulFunctionsSingleton helpfulFunctions;
    InputFormat input;

    public ArimaAnalyticProcess(InputFormat input) {
        helpfulFunctions = HelpfulFunctionsSingleton.getInstance();
        helpfulFunctions.nicePrintMessage("Create analytic process for Forecasting Arima Algorithm");
        this.input = input;

    }

    @Override
    public void train(Analytics analytics) {

    }

    @Override
    public void eval(Analytics analytics, OutputFormat out) {
        String startDate = "";
        String endDate = "";
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
        }

        //clean previous eval info if exists
        helpfulFunctions.cleanPreviousInfo(analytics);
        Rengine re;
        if (helpfulFunctions.isRDFInputFormat(analytics.getTrainQuery_id())) {
            re = input.importData4R(Integer.toString(analytics.getTrainQuery_id()), true);
        } else {
            re = input.importData4R(Configuration.analyticsRepo + analytics.getDocument(), true);
        }

        //re.eval(" loaded_data <- read.csv(file='" + Configuration.docroot + analytics.getDocument() + "', header=TRUE, sep=',');");
        String RScript = "loaded_data <- read.csv(file='" + Configuration.docroot + analytics.getDocument() + "', header=TRUE, sep=',');\n";

        // ---- get analyzedFieldValue ----
        RVector dataToExportasVector = re.eval("loaded_data").asVector();
        Vector colnames = dataToExportasVector.getNames();

        String[] colnamesArray = new String[colnames.size()];
        colnames.copyInto(colnamesArray);

        String analyzedFieldValue = colnamesArray[colnames.size() - 1];
        System.out.println("analyzedFieldValue" + analyzedFieldValue);
        //

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

        re.eval(" column_number<-ncol(loaded_data);");
        RScript += "column_number<-ncol(loaded_data);\n";

        re.eval(" rows_number<-nrow(loaded_data);");
        RScript += "rows_number<-nrow(loaded_data);\n";

        re.eval(" column_to_predict <-colnames(loaded_data[column_number]);");
        RScript += "column_to_predict <-colnames(loaded_data[column_number]);\n";

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

        re.eval("valuesToCleanNum<-rows_number;");
        RScript += "valuesToCleanNum<-rows_number;\n";

        re.eval("trimmedValues<- data.frame();");
        RScript += "trimmedValues<- data.frame();\n";

        re.eval("for(i in 1:valuesToCleanNum){ valueToTrim <- as.character(valuesToClean[i,1]);  if(grepl(\"#\", valueToTrim)) {  position<-which(strsplit(valueToTrim, \"\")[[1]]==\"^\"); trimmedValues[i,1]<-substr(valueToTrim, 1, position[1]-1);  }else{ trimmedValues[i,1]<-valueToTrim; }  }");
        RScript += "for(i in 1:valuesToCleanNum){ valueToTrim <- as.character(valuesToClean[i,1]);  if(grepl(\"#\", valueToTrim)) {  position<-which(strsplit(valueToTrim, \"\")[[1]]==\"^\"); trimmedValues[i,1]<-substr(valueToTrim, 1, position[1]-1);  }else{ trimmedValues[i,1]<-valueToTrim; }  }\n";

        //re.eval(" datats <- ts(loaded_data[column_number], frequency=12, start=c(year_to_start,month_to_start)); ");
        re.eval(" datats <- ts(trimmedValues, frequency=12, start=c(year_to_start,month_to_start)); ");
        RScript += "datats <- ts(trimmedValues, frequency=12, start=c(year_to_start,month_to_start));\n";

        re.eval(" add.months= function(date,n) seq(date, by = paste (n, 'months'), length = 2)[2];");
        RScript += "add.months= function(date,n) seq(date, by = paste (n, 'months'), length = 2)[2];\n";

        re.eval(" date_to_start_prediction=as.Date(lastdate) ; ");
        RScript += "date_to_start_prediction=as.Date(lastdate) ;\n";

        re.eval(" date_to_start_prediction<-add.months(date_to_start_prediction, 1);");
        RScript += "date_to_start_prediction<-add.months(date_to_start_prediction, 1);\n";

        re.eval(" Date = seq(date_to_start_prediction, by='months', length=12); ");
        RScript += "Date = seq(date_to_start_prediction, by='months', length=12);\n";

//        re.eval(" m.ar2 <- arima(datats, order = c(1,1,0)); ");
//        RScript += "m.ar2 <- arima(datats, order = c(1,1,0));\n";
        re.eval("m.ar2 <- auto.arima(datats);");
        RScript += "m.ar2 <- auto.arima(datats);\n";

        long plot1_id = helpfulFunctions.manageNewPlot(analytics, "Forecasting Time Series for analyzed field: " + analyzedFieldValue, "plots/plotid" + analytics.getPlot1_id() + ".png", "plot1_id");

        re.eval("png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600); a<-plot(forecast(m.ar2,h=12)); print(a); dev.off();");
        RScript += "png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);a<-plot(forecast(m.ar2,h=12)); print(a); dev.off();\n";

        
        re.eval("arimaplottosave<-plot(forecast(m.ar2,h=12));");
        re.eval("png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600)");
        re.eval("arimaplottosave<-plot(forecast(m.ar2,h=12));");
        re.eval("print(arimaplottosave);");
        re.eval("dev.off();");

        RScript += "arimaplottosave<-plot(forecast(m.ar2,h=12));\n";
        RScript += "png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);\n";
        RScript += "print(arimaplottosave);\n";
        RScript += "dev.off();\n";
        
        re.eval("p <- predict(m.ar2, n.ahead = 12);");
        RScript += "p <- predict(m.ar2, n.ahead = 12);\n";

        re.eval("rounded_values <-as.numeric(round(p$pred, digits = 3)); ");
        RScript += "rounded_values <-as.numeric(round(p$pred, digits = 3));\n";

        re.eval("df_to_export <- data.frame(Date,rounded_values); ");
        RScript += "df_to_export <- data.frame(Date,rounded_values);\n";

        re.eval("result_column_number<-ncol(df_to_export); ");
        RScript += "result_column_number<-ncol(df_to_export);\n";

        re.eval("colnames(df_to_export)[result_column_number] <- column_to_predict;");
        RScript += "colnames(df_to_export)[result_column_number] <- column_to_predict;\n";

        helpfulFunctions.writeToFile(RScript, "processinfo", analytics);

//        re.eval("write.csv(df_to_export, file = '/home/eleni/Desktop/mydatasets/airline2.csv',row.names=FALSE);");
//        re.eval("rm(list=ls());");
        out.exportData(analytics, re);
    }

}
