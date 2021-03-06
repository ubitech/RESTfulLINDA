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
public class MoransAnalyticProcess extends AnalyticProcess {

    InputFormat input;
    ConnectionController connectionController;

    public MoransAnalyticProcess(InputFormat input) {
        Util.nicePrintMessage("Create analytic process for Moran's I Algorithm");
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
            Util.cleanPreviousInfo(analytics);
            analytics.setTimeToGet_data(0);
            analytics.setTimeToRun_analytics(0);
            analytics.setData_size(0);
            RConnection re;
            if (Util.isRDFInputFormat(analytics.getTrainQuery_id())) {
                re = input.importData4R(Integer.toString(analytics.getTrainQuery_id()), "0", true, analytics);

            } else {
                re = input.importData4R(Configuration.analyticsRepo + analytics.getDocument(), "0", true, analytics);
                RScript += "loaded_data <- read.csv(file='" + Configuration.analyticsRepo + analytics.getDocument() + "', header=TRUE, sep=',');\n";

            }
            if (re==null) return;

                //check if x y coordinates exist at the query
                re.eval("if('x' %in% colnames(loaded_data) && 'y' %in% colnames(loaded_data) ) {  exists_geo_info <-TRUE }else{   exists_geo_info <-FALSE }");
                org.rosuda.REngine.REXP exists_geo_info = re.eval("exists_geo_info");

                if (exists_geo_info.asString().equalsIgnoreCase("FALSE")) {
                    DBSynchronizer.updateLindaAnalyticsProcessMessage("The data you provided has no geospatial information.\n Please enter a dataset or query with a x & y information or provide the adecuate parameters.", analytics.getId());
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

                    //Save model Readable
                    String modelFileName = "models/analyticsID" + analytics.getId() + "_" + analytics.getAlgorithm_id() + "ModelReadable" + ".txt";
                    String modelFileNameFullPath = Configuration.analyticsRepo + modelFileName;

                    org.rosuda.REngine.REXP s = re.eval("capture.output(morans_result)");
                    String[] output = s.asStrings();
                    for (String string : output) {
                        System.out.println(string);
                    }

                    Util.saveFile(modelFileNameFullPath, output);
                   DBSynchronizer.updateLindaAnalytics(modelFileName, "modelReadable", analytics.getId());

                    double moranObservedValue = re.eval("morans_result$observed").asDouble();
                    System.out.println("moranObservedValue:" + moranObservedValue);


                    String processMessage = "";
                    if (moranObservedValue > 0) {
                        processMessage += "There is a significant spatial autocorrelation in your data \n and you should take into account in next analytic processes \n. You could double check this result with NCF Correlogram Algorithm";
                    } else {
                        processMessage += "Moran's I did not detect a significant spatial autocorrelation in your data. \n You could double check this result with NCF Correlogram Algorithm";

                    }
                    DBSynchronizer.updateLindaAnalyticsProcessMessage(processMessage, analytics.getId());

                    Util.writeToFile(RScript, "processinfo", analytics);

                    re.eval("rm(list=ls());");
                    long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
                    // Get elapsed time in seconds
                    timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
                    analytics.setTimeToRun_analytics(analytics.getTimeToRun_analytics() + timeToRun_analytics);
                    DBSynchronizer.updateLindaAnalyticsProcessPerformanceTime(analytics);
                    re.close();
                }
            
        } catch (RserveException ex) {
             String ex_message = "Rserve server not responsive. \n"
                    + "Propably Rserve does not acess ape library. \n"
                    + "Reiniciate the Rserve Server or Contact the administrator.";
            Logger.getLogger(MoransAnalyticProcess.class.getName()).log(Level.SEVERE, ex_message, ex);
            DBSynchronizer.updateLindaAnalyticsProcessMessage(ex_message, analytics.getId());
        } catch (REXPMismatchException ex) {
            Logger.getLogger(MoransAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
