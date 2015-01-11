/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.r.geospatial;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.AnalyticProcess;
import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.formats.OutputFormat;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctionsSingleton;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author eleni
 */
public class KrigingAnalyticProcess extends AnalyticProcess {

    HelpfulFunctionsSingleton helpfulFunctions;
    InputFormat input;

    public KrigingAnalyticProcess(InputFormat input) {
        helpfulFunctions = HelpfulFunctionsSingleton.getInstance();
        helpfulFunctions.nicePrintMessage("Create analytic process for Geospatial Algorithm - Ordinary Kriging");
        this.input = input;

    }

    @Override
    public void train(Analytics analytics) {

    }

    @Override
    public void eval(Analytics analytics, OutputFormat out) {
        String RScript = "";
        //clean previous eval info if exists
        helpfulFunctions.cleanPreviousInfo(analytics);
        Rengine re;
        if (helpfulFunctions.isRDFInputFormat(analytics.getTrainQuery_id())) {
            //import train dataset
            re = input.importData4R(Integer.toString(analytics.getTrainQuery_id()), true);
            re.eval("loaded_data_train <- loaded_data;");
            RScript += "loaded_data_train <- read.csv('insertqueryid" + analytics.getTrainQuery_id() + "');\n";

            re = input.importData4R(Integer.toString(analytics.getEvaluationQuery_id()), true);
            re.eval("loaded_data_eval <- loaded_data;");
            RScript += "loaded_data_eval <- read.csv('insertqueryid" + analytics.getEvaluationQuery_id() + "')\n";

            re.eval("df_to_export<- loaded_data;");
            RScript += "df_to_export<- read.csv('insertqueryid" + analytics.getEvaluationQuery_id() + "');\n";

        } else {
            //load train dataset
            re = input.importData4R(Configuration.analyticsRepo + analytics.getDocument(), true);
            re.eval("loaded_data_train <- loaded_data;");
            //re.eval("loaded_data_train <- read.csv('/home/eleni/Downloads/ca21.dat');");
            RScript += "loaded_data_train <- read.csv('" + Configuration.analyticsRepo + analytics.getDocument() + "');\n";

            //load eval dataset
            re = input.importData4R(Configuration.analyticsRepo + analytics.getTestdocument(), true);
            re.eval("loaded_data_eval <- loaded_data;");
            //re.eval("loaded_data_eval <- read.csv('/home/eleni/Downloads/ca21Evalwithuri1.dat')");
            RScript += "loaded_data_eval <- read.csv('" + Configuration.analyticsRepo + analytics.getTestdocument() + "')\n";

            re.eval("df_to_export<- loaded_data;");
            //re.eval("df_to_export<- read.csv('/home/eleni/Downloads/ca21Evalwithuri1.dat');");
            RScript += "df_to_export<- read.csv('" + Configuration.analyticsRepo + analytics.getTestdocument() + "');\n";

        }

        re.eval("loaded_data_train <- subset(loaded_data_train, !duplicated(loaded_data_train[4:5]));");
        RScript += "#remove any duplicated coordinates\n";
        RScript += "loaded_data_train <- subset(loaded_data_train, !duplicated(loaded_data_train[4:5]));\n";
        re.eval("loaded_data_eval <- subset(loaded_data_eval, !duplicated(loaded_data_eval[4:5]));");
        RScript += "loaded_data_eval <- subset(loaded_data_eval, !duplicated(loaded_data_eval[4:5]));\n";
        re.eval("df_to_export <- subset(df_to_export, !duplicated(df_to_export[4:5]));");
        RScript += "df_to_export <- subset(df_to_export, !duplicated(df_to_export[4:5]));\n";

        re.eval("library(sp)");
        RScript += "library(sp)\n";

        re.eval("library(gstat)");
        RScript += "library(gstat)\n";

        // ---- get analyzedFieldValue ----
        RVector dataToExportasVector = re.eval("loaded_data_train").asVector();
        Vector colnames = dataToExportasVector.getNames();

        String[] colnamesArray = new String[colnames.size()];
        colnames.copyInto(colnamesArray);

        String analyzedFieldValue = colnamesArray[colnames.size() - 1];
        //

        re.eval("column_number<-ncol(loaded_data_train);");
        RScript += "column_number<-ncol(loaded_data_train);\n";

        re.eval("column_to_predict <-colnames(loaded_data_train[column_number]);");
        RScript += "column_to_predict <-colnames(loaded_data_train[column_number]);\n";

        re.eval("rows_number<-nrow(loaded_data_train);");
        RScript += "rows_number<-nrow(loaded_data_train);\n";

        re.eval("valuesToClean<-loaded_data_train[column_to_predict];");
        RScript += "valuesToClean<-loaded_data_train[column_to_predict];\n";

        re.eval("valuesToCleanNum<-rows_number;");
        RScript += "valuesToCleanNum<-rows_number;\n";

        re.eval("trimmedValues<- data.frame();");
        RScript += "trimmedValues<- data.frame();\n";

        re.eval("for(i in 1:valuesToCleanNum){ valueToTrim <- as.character(valuesToClean[i,1]);  if(grepl(\"#\", valueToTrim)) {  position<-which(strsplit(valueToTrim, \"\")[[1]]==\"^\");  trimmedValues[i,1]<-substr(valueToTrim, 1, position[1]-1); }else{ trimmedValues[i,1]<-valueToTrim;}  }");
        RScript += "for(i in 1:valuesToCleanNum){ valueToTrim <- as.character(valuesToClean[i,1]);  if(grepl(\"#\", valueToTrim)) {  position<-which(strsplit(valueToTrim, \"\")[[1]]==\"^\");  trimmedValues[i,1]<-substr(valueToTrim, 1, position[1]-1); }else{ trimmedValues[i,1]<-valueToTrim;}  }\n";

        re.eval("result_column_number<-ncol(loaded_data_train);");
        RScript += "result_column_number<-ncol(loaded_data_train);\n";

        re.eval("colnames(trimmedValues)[1]<- column_to_predict;");
        RScript += "colnames(trimmedValues)[1]<- column_to_predict;\n";

        re.eval("trimmedValues$" + analyzedFieldValue + "<-as.numeric(trimmedValues$" + analyzedFieldValue + ");");
        RScript += "trimmedValues$" + analyzedFieldValue + "<-as.numeric(trimmedValues$" + analyzedFieldValue + ");\n";

        re.eval("loaded_data_train[[column_to_predict]] <- trimmedValues;");
        RScript += "loaded_data_train[[column_to_predict]] <- trimmedValues;\n";

        re.eval("loaded_data_train$" + analyzedFieldValue + "<-as.numeric(unlist(loaded_data_train$" + analyzedFieldValue + "));");
        RScript += "loaded_data_train$" + analyzedFieldValue + "<-as.numeric(unlist(loaded_data_train$" + analyzedFieldValue + "));\n";

        re.eval("loaded_data_train<-data.frame(loaded_data_train);");
        RScript += " loaded_data_train<-data.frame(loaded_data_train);\n";

        re.eval("loaded_data_eval<-data.frame(loaded_data_eval);");
        RScript += "loaded_data_eval<-data.frame(loaded_data_eval);\n";

        re.eval("coordinates(loaded_data_train) = ~x+y");
        RScript += "coordinates(loaded_data_train) = ~x+y\n";

        re.eval("v <- variogram(log(" + analyzedFieldValue + ")~1, loaded_data_train)");
        RScript += "v <- variogram(log(" + analyzedFieldValue + ")~1, loaded_data_train)\n";

        re.eval("m <- fit.variogram(v, vgm(1, 'Sph', 300, 1))");
        RScript += "m <- fit.variogram(v, vgm(1, 'Sph', 300, 1))\n";

        long plot1_id = helpfulFunctions.manageNewPlot(analytics,"Variogram Plot : Visualization of the spatial autocorrelation of the analyzed field: "+analyzedFieldValue, "plots/plotid"+analytics.getPlot1_id()+".png","plot1_id");

        
        re.eval("variogramplot<-plot(v, model = m)");
        re.eval("png(file='"+Configuration.analyticsRepo+"plots/plotid"+plot1_id+".png',width=600)");
        re.eval("print(variogramplot)");
        re.eval("dev.off()");

        re.eval("coordinates(loaded_data_eval) = ~x+y");
        RScript += "coordinates(loaded_data_eval) = ~x+y\n";

        //#sim <- krige(formula = log(ca20)~1, d, dEval, model = m, nmax = 15, beta = 5.9, nsim = 3)
        //#sim <- krige(formula = ca20~sqrt(altitude), d, dEval, model = m, nmax = 15, nsim = 3)
        re.eval("df <- krige(formula = " + analyzedFieldValue + "~1, loaded_data_train, loaded_data_eval, model = m, nmax = 15, beta = 5.9, nsim = 1)");
        RScript += "df <- krige(formula = " + analyzedFieldValue + "~1, loaded_data_train, loaded_data_eval, model = m, nmax = 15, beta = 5.9, nsim = 1)\n";

        re.eval("df_to_export[[column_to_predict]] <- df$sim1");
        RScript += "df_to_export[[column_to_predict]] <- df$sim1 \n";

         long plot2_id = helpfulFunctions.manageNewPlot(analytics,"Spatial data spplot map for analyzed field :"+analyzedFieldValue, "plots/plotid"+analytics.getPlot1_id()+".png","plot2_id");
        
        
        re.eval("tmp <- spplot(df)");
        re.eval("png(file='"+Configuration.analyticsRepo+"plots/plotid"+plot2_id+".png',width=600)");
        re.eval("print(tmp)");
        re.eval("dev.off()");
        
        

        //test  to see if plot is saved
//        String title = "R Plot in JFrame";
//        String xlab = "X Label";
//        String ylab = "Y Label";
//        re.eval("a<-c(1,2,3,4,5,6,7,8,9,10)");
//        re.eval("b<-c(1,3,2,4,5,6,7,8,9,10)");
//        re.eval("png(file=\"graph5.png\",width=1600,height=1600,res=400)");
//        re.eval("plot(a,b,type='o',col=\"Blue\",main=\"" + title + "\",xlab=\""+ xlab + "\",ylab=\"" + ylab + "\")");
//        re.eval("dev.off()");
//        
//        re.eval("png('"+Configuration.analyticsRepo+"plots/plotid"+analytics.getPlot1_id()+".png'); spplot(df); dev.off();");
//        RScript += "png('"+Configuration.analyticsRepo+"plots/plotid"+analytics.getPlot1_id()+".png');\n";      
//        re.eval("spplot(df);");
//        RScript += "spplot(df);\n";
        helpfulFunctions.writeToFile(RScript, "processinfo", analytics);

//       re.eval("write.csv(df_to_export, file = '"+Configuration.analyticsRepo+"tmp/tmp4processid"+analytics.getId()+".csv',row.names=FALSE);");
//       re.eval("rm(list=ls());");
        out.exportData(analytics, re);
    }

}
