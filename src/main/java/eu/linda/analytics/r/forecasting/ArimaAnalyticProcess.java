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

        //clean previous eval info if exists
        helpfulFunctions.cleanPreviousInfo(analytics);

        Rengine re = input.importData4R(Configuration.docroot + analytics.getDocument(), true);

        //re.eval(" loaded_data <- read.csv(file='" + Configuration.docroot + analytics.getDocument() + "', header=TRUE, sep=',');");
        String RScript = "loaded_data <- read.csv(file='" + Configuration.docroot + analytics.getDocument() + "', header=TRUE, sep=',');\n";

        re.eval(" column_number<-ncol(loaded_data);");
        RScript += "column_number<-ncol(loaded_data);\n";

        re.eval(" rows_number<-nrow(loaded_data);");
        RScript += "rows_number<-nrow(loaded_data);\n";

        re.eval(" column_to_predict <-colnames(loaded_data[column_number]);");
        RScript += "column_to_predict <-colnames(loaded_data[column_number]);\n";

        re.eval(" data_matrix<-as.matrix(loaded_data); ");
        RScript += "data_matrix<-as.matrix(loaded_data);\n";

        if (helpfulFunctions.isRDFExportFormat(analytics.getExportFormat())) {
            re.eval(" firstdate<-as.Date(data_matrix[1,3]);");
            RScript += "firstdate<-as.Date(data_matrix[1,3]);\n";

            re.eval(" lastdate <- as.Date(data_matrix[rows_number,3]); ");
            RScript += "lastdate <- as.Date(data_matrix[rows_number,3]);\n";
        } else {
            re.eval(" firstdate<-as.Date(data_matrix[1]);");
            RScript += "lastdate <- as.Date(data_matrix[1]);\n";

            re.eval(" lastdate <- as.Date(data_matrix[rows_number]); ");
            RScript += "lastdate <- as.Date(data_matrix[rows_number]);\n";
        }

        re.eval(" year_to_start <-as.numeric(format(firstdate, format='%Y'));");
        RScript += "year_to_start <-as.numeric(format(firstdate, format='%Y'));\n";

        re.eval(" month_to_start <-as.numeric(format(firstdate, format='%m'));");
        RScript += "month_to_start <-as.numeric(format(firstdate, format='%m'));\n";

        re.eval(" day_to_start <-as.numeric(format(firstdate, format='%d'));");
        RScript += "day_to_start <-as.numeric(format(firstdate, format='%d'));\n";

        re.eval(" datats <- ts(loaded_data[column_number], frequency=12, start=c(year_to_start,month_to_start)); ");
        RScript += "datats <- ts(loaded_data[column_number], frequency=12, start=c(year_to_start,month_to_start));\n";

        re.eval(" add.months= function(date,n) seq(date, by = paste (n, 'months'), length = 2)[2];");
        RScript += "add.months= function(date,n) seq(date, by = paste (n, 'months'), length = 2)[2];\n";

        re.eval(" date_to_start_prediction=as.Date(lastdate) ; ");
        RScript += "date_to_start_prediction=as.Date(lastdate) ;\n";

        re.eval(" date_to_start_prediction<-add.months(date_to_start_prediction, 1);");
        RScript += "date_to_start_prediction<-add.months(date_to_start_prediction, 1);\n";

        re.eval(" Date = seq(date_to_start_prediction, by='months', length=12); ");
        RScript += "Date = seq(date_to_start_prediction, by='months', length=12);\n";

        re.eval(" m.ar2 <- arima(datats, order = c(1,1,0)); ");
        RScript += "m.ar2 <- arima(datats, order = c(1,1,0));\n";

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
