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
        re.eval(" column_number<-ncol(loaded_data);");
        re.eval(" rows_number<-nrow(loaded_data);");
        re.eval(" column_to_predict <-colnames(loaded_data[column_number]);");
        re.eval(" data_matrix<-as.matrix(loaded_data); ");

        if (helpfulFunctions.isRDFExportFormat(analytics.getExportFormat())) {
            re.eval(" firstdate<-as.Date(data_matrix[1,3]);");
            re.eval(" lastdate <- as.Date(data_matrix[rows_number,3]); ");
        } else {
            re.eval(" firstdate<-as.Date(data_matrix[1]);");
            re.eval(" lastdate <- as.Date(data_matrix[rows_number]); ");
        }

        re.eval(" year_to_start <-as.numeric(format(firstdate, format='%Y'));");
        re.eval(" month_to_start <-as.numeric(format(firstdate, format='%m'));");
        re.eval(" day_to_start <-as.numeric(format(firstdate, format='%d'));");
        re.eval(" datats <- ts(loaded_data[column_number], frequency=12, start=c(year_to_start,month_to_start)); ");
        re.eval(" add.months= function(date,n) seq(date, by = paste (n, 'months'), length = 2)[2];");
        re.eval(" date_to_start_prediction=as.Date(lastdate) ; ");
        re.eval(" date_to_start_prediction<-add.months(date_to_start_prediction, 1);");
        re.eval(" Date = seq(date_to_start_prediction, by='months', length=12); ");
        re.eval(" m.ar2 <- arima(datats, order = c(1,1,0)); ");
        re.eval("p <- predict(m.ar2, n.ahead = 12);");
        // re.eval("rounded_values <-as.character(round(p$pred, digits = 3)); ");
        re.eval("rounded_values <-as.numeric(round(p$pred, digits = 3)); ");
        re.eval("df_to_export <- data.frame(Date,rounded_values); ");
        re.eval("result_column_number<-ncol(df_to_export); ");
       
        re.eval("colnames(df_to_export)[result_column_number] <- column_to_predict;");
        


//        re.eval("write.csv(df_to_export, file = '/home/eleni/Desktop/mydatasets/airline2.csv',row.names=FALSE);");
//        re.eval("rm(list=ls());");

        out.exportData(analytics, re);
    }

}
