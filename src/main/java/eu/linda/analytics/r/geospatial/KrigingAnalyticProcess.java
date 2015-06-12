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
public class KrigingAnalyticProcess extends AnalyticProcess {

    InputFormat input;

    public KrigingAnalyticProcess(InputFormat input) {
        Util.nicePrintMessage("Create analytic process for Geospatial Algorithm - Ordinary Kriging");
        this.input = input;

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
        Util.cleanPreviousInfo(analytics);
        analytics.setTimeToGet_data(0);
        analytics.setTimeToRun_analytics(0);
        analytics.setData_size(0);
        analytics.setTimeToCreate_RDF(0);
        RConnection re;
        try {
            if (Util.isRDFInputFormat(analytics.getTrainQuery_id())) {

                //import train & eval dataset
                re = input.importData4R(Integer.toString(analytics.getTrainQuery_id()), Integer.toString(analytics.getEvaluationQuery_id()), true, analytics);

                org.rosuda.REngine.REXP is_train_query_responsive = re.eval("is_train_query_responsive");
                org.rosuda.REngine.REXP is_evaluation_query_responsive = re.eval("is_eval_query_responsive");

                if (is_train_query_responsive.asString().equalsIgnoreCase("FALSE") || is_evaluation_query_responsive.asString().equalsIgnoreCase("FALSE")) {
                    Util.updateProcessMessageToAnalyticsTable("There is a connectivity issue. Could not reach data for predefined query.\n"
                            + " Please check your connectivity and the responsiveness of the selected sparql endpoint.\n "
                            + "Then click on re-Evaluate button to try to run again the analytic process.", analytics.getId());
                    re.eval("rm(list=ls());");
                    return;
                }

                re.eval("loaded_data_train <- loaded_data; "
                        + "df_to_export<- loaded_data_eval;");
                
                RScript += "loaded_data_train <- read.csv('insertqueryid" + analytics.getTrainQuery_id() + "');\n "
                        + "loaded_data_eval <- read.csv('insertqueryid" + analytics.getEvaluationQuery_id() + "')\n "
                        + "df_to_export<- read.csv('insertqueryid" + analytics.getEvaluationQuery_id() + "');\n";

            } else {
                //import train & eval dataset
                re = input.importData4R(Configuration.analyticsRepo + analytics.getDocument(),Configuration.analyticsRepo + analytics.getTestdocument(), true, analytics);
                re.eval("loaded_data_train <- loaded_data;"
                        + "loaded_data_eval <- loaded_data_eval; "
                        + "df_to_export<- loaded_data_eval;");


                RScript += "loaded_data_train <- read.csv('" + Configuration.analyticsRepo + analytics.getDocument() + "');\n "
                        + "loaded_data_eval <- read.csv('" + Configuration.analyticsRepo + analytics.getTestdocument() + "')\n "
                        + "df_to_export<- read.csv('" + Configuration.analyticsRepo + analytics.getTestdocument() + "');\n";

            }

            re.eval("xcol<-match(\"x\",names(loaded_data_train)); "
                    + "ycol<-match(\"y\",names(loaded_data_train)); "
                    + "loaded_data_train <- subset(loaded_data_train, !duplicated(loaded_data_train[xcol:ycol])); "
                    + "loaded_data_eval <- subset(loaded_data_eval, !duplicated(loaded_data_eval[xcol:ycol])); "
                    + "df_to_export <- subset(df_to_export, !duplicated(df_to_export[xcol:ycol])); "
                    + "library(sp); "
                    + "library(gstat); "
                    + "column_number<-ncol(loaded_data_train); "
                    + "column_to_predict <-colnames(loaded_data_train[column_number]); "
                    + "rows_number<-nrow(loaded_data_train); ");


            RScript += "xcol<-match(\"x\",names(loaded_data_train)); \n "
                    + "ycol<-match(\"y\",names(loaded_data_train)); \n "
                    + "#remove any duplicated coordinates\n "
                    + "loaded_data_train <- subset(loaded_data_train, !duplicated(loaded_data_train[ycol:ycol]));\n "
                    + "loaded_data_eval <- subset(loaded_data_eval, !duplicated(loaded_data_eval[xcol:ycol]));\n "
                    + "df_to_export <- subset(df_to_export, !duplicated(df_to_export[xcol:ycol]));\n "
                    + "library(sp);\n "
                    + "library(gstat);\n "
                    + "column_number<-ncol(loaded_data_train);\n "
                    + "column_to_predict <-colnames(loaded_data_train[column_number]);\n "
                    + "rows_number<-nrow(loaded_data_train);\n ";
            

            // ---- get analyzedFieldValue ----
            org.rosuda.REngine.REXP column_to_predict = re.eval("column_to_predict");
            String analyzedFieldValue = column_to_predict.asString();


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

            long plot1_id = Util.manageNewPlot(analytics, "Variogram Plot : Visualization of the spatial autocorrelation of the analyzed field: " + analyzedFieldValue, "plots/plotid" + analytics.getPlot1_id() + ".png", "plot1_id");

            re.eval("variogramplot<-plot(v, model = m)");
            re.eval("png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600)");
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

            long plot2_id = Util.manageNewPlot(analytics, "Spatial data spplot map for analyzed field :" + analyzedFieldValue, "plots/plotid" + analytics.getPlot2_id() + ".png", "plot2_id");

            re.eval("tmp <- spplot(df)");
            re.eval("png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot2_id + ".png',width=600)");
            re.eval("print(tmp)");
            re.eval("dev.off()");

            Util.writeToFile(RScript, "processinfo", analytics);

            long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
            // Get elapsed time in seconds
            timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
            analytics.setTimeToRun_analytics(timeToRun_analytics);
            out.exportData(analytics, re);

        } catch (RserveException ex) {
            Logger.getLogger(KrigingAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REXPMismatchException ex) {
            Logger.getLogger(KrigingAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
