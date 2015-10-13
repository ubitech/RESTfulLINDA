package eu.linda.analytics.r.regression;

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
public class MultipleLinearRegressionInR extends AnalyticProcess {

    InputFormat input;
    ConnectionController connectionController;
    float timeToRun_analytics = 0;
    long startTimeToRun_analytics = System.currentTimeMillis();
    String RScript = "";
    String[] output;

    public MultipleLinearRegressionInR(InputFormat input) {
        Util.nicePrintMessage("Create analytic process for Multiple LinearRegression In R Algorithm");
        this.input = input;
        connectionController = ConnectionController.getInstance();
    }

    @Override
    public void train(Analytics analytics) {

    }

    @Override
    public void eval(Analytics analytics, OutputFormat out) {

        try {

            //clean previous eval info if exists
            Util.cleanPreviousInfo(analytics);
            analytics.setTimeToGet_data(0);
            analytics.setTimeToRun_analytics(0);
            analytics.setData_size(0);
            RConnection re;
            if (Util.isRDFInputFormat(analytics.getTrainQuery_id())) {
                re = input.importData4R(Integer.toString(analytics.getTrainQuery_id()), Integer.toString(analytics.getEvaluationQuery_id()), true, analytics);

            } else {
                re = input.importData4R(Configuration.analyticsRepo + analytics.getDocument(), Configuration.analyticsRepo + analytics.getTestdocument(), true, analytics);
                RScript += "loaded_data <- read.csv(file='" + Configuration.analyticsRepo + analytics.getDocument() + "', header=TRUE, sep=',');\n";

            }
            if (re == null) {
                return;
            }

            //TODO Check that all values are numeric
            re.eval("loaded_data <- na.omit(loaded_data); ");

            RScript += "loaded_data <- na.omit(loaded_data);\n "
                    + "# listwise deletion of missing\n";

            re.eval("names(loaded_data)[2]<-\"uri\";  ");

            RScript += "names(loaded_data)[2]<-\"uri\"; \n ";

            re.eval("uri<-loaded_data[2]; ");
            RScript += "# Prepare Data \n uri<-loaded_data[2]; \n";

            re.eval("column_with_uri <-colnames(loaded_data[2]);");
            RScript += "column_with_uri <-colnames(loaded_data[2]); \n";

            re.eval("myvars <- names(loaded_data) %in% c('rowID',column_with_uri);");
            RScript += "myvars <- names(loaded_data) %in% c('rowID',column_with_uri); \n";

            re.eval("loaded_data <- loaded_data[!myvars]; "
                    + "nums <- sapply(loaded_data, is.numeric); "
                    + "loaded_data<-loaded_data[ , nums]; ");

            int num_of_not_numerical_variables = re.eval("table(nums)[\"FALSE\"]").asInteger();
            if (num_of_not_numerical_variables > 0) {
                DBSynchronizer.updateLindaAnalyticsProcessMessage("Note: Input queries contain non numeric variables and these have been ignored during the analysis. \n", analytics.getId());
            }
            RScript += "loaded_data <- loaded_data[!myvars]; \n"
                    + "nums <- sapply(loaded_data, is.numeric); \n"
                    + "loaded_data<-loaded_data[ , nums]; \n";

            re.eval("column_number<-ncol(loaded_data);");
            RScript += "column_number<-ncol(loaded_data); \n";

            re.eval("names(loaded_data)[column_number]<-\"topredict\"");
            RScript += "names(loaded_data)[column_number]<-\"topredict\" \n";

            if (analytics.isCreateModel()) {

                //Save model Readable
                String RmodelFileName = "models/analyticsID" + analytics.getId() + "_" + analytics.getAlgorithm_id() + "Model.rda";
                String RmodelFileNameFullPath = Configuration.analyticsRepo + RmodelFileName;

                re.eval("if(file.exists(\"" + RmodelFileNameFullPath + "\")) {load(\"" + RmodelFileNameFullPath + "\");} else { LMmodel <- lm(topredict ~ ., data=loaded_data);    save(LMmodel, file = \"" + RmodelFileNameFullPath + "\");} ");
                RScript += "if(file.exists(\"" + RmodelFileNameFullPath + "\")) {\nload(\"" + RmodelFileNameFullPath + "\");\n} else { \n LMmodel <- lm(topredict ~ ., data=loaded_data); \n save(LMmodel, file = \"" + RmodelFileNameFullPath + "\");}  \n";

            } else {
                re.eval("LMmodel <- lm(topredict ~ ., data=loaded_data);");
                RScript += "# Multiple Linear Regression Constuction \n LMmodel <- lm(topredict ~ ., data=loaded_data); \n";
            }

            re.eval("summary(LMmodel);");
            RScript += "# show results \n  summary(LMmodel); \n ";

            org.rosuda.REngine.REXP s = re.eval("capture.output(summary(LMmodel))");
            output = s.asStrings();
            for (String string : output) {
                System.out.println(string);
            }

            // if there is no eval dataset there is no mean of continuing
            org.rosuda.REngine.REXP exists_data_eval = re.eval("exists('loaded_data_eval')");;

            if (exists_data_eval.asString().equalsIgnoreCase("TRUE")) {
                System.out.println("exo eval dataset");

                re.eval("loaded_data_eval <- na.omit(loaded_data_eval); ");
                RScript += "loaded_data_eval <- na.omit(loaded_data_eval);  "
                        + "# listwise deletion of missing\n";

                re.eval("names(loaded_data_eval)[2]<-\"uri\" ; ");
                RScript += "names(loaded_data_eval)[2]<-\"uri\"; \n ";

                re.eval("loaded_data_eval <- loaded_data_eval[!myvars]; "
                        + "nums <- sapply(loaded_data_eval, is.numeric); "
                        + "loaded_data_eval<-loaded_data_eval[ , nums]; ");

                RScript += "loaded_data_eval <- loaded_data_eval[!myvars]; \n"
                        + "nums <- sapply(loaded_data_eval, is.numeric); \n"
                        + "loaded_data_eval<-loaded_data_eval[ , nums]; \n";

                re.eval("predictTest = predict(LMmodel, newdata=loaded_data_eval);");
                RScript += "#EVALUATE  \n predictTest = predict(LMmodel, newdata=loaded_data_eval); \n";

                re.eval("column_to_predict <-colnames(loaded_data[column_number]);");
                RScript += "column_to_predict <-colnames(loaded_data[column_number]); \n";

                re.eval("df_to_export <- data.frame(uri,loaded_data[column_to_predict]);");
                RScript += "df_to_export <- data.frame(uri,loaded_data[column_to_predict]);\n";

                updateDBandFilesystem(analytics);

                out.exportData(analytics, re);

            } else {
                System.out.println("DEN exo eval dataset");
                updateDBandFilesystem(analytics);
            }

        } catch (RserveException ex) {
            String ex_message = "Rserve server not responsive. \n"
                    + "Reiniciate the Rserve Server or Contact the administrator.";
            Logger.getLogger(MultipleLinearRegressionInR.class.getName()).log(Level.SEVERE, ex_message, ex);
            DBSynchronizer.updateLindaAnalyticsProcessMessage(ex_message, analytics.getId());
        } catch (REXPMismatchException ex) {
            Logger.getLogger(MultipleLinearRegressionInR.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void updateDBandFilesystem(Analytics analytics) {

        //Save model Readable
        String modelFileName = "models/analyticsID" + analytics.getId() + "_" + analytics.getAlgorithm_id() + "ModelReadable" + ".txt";
        String modelFileNameFullPath = Configuration.analyticsRepo + modelFileName;

        RScript += "sink(\"" + modelFileNameFullPath + "\");\n";
        RScript += "summary(LMmodel);\n";
        RScript += "sink();\n";

        Util.saveFile(modelFileNameFullPath, output);
        DBSynchronizer.updateLindaAnalytics(modelFileName, "modelReadable", analytics.getId());

        Util.writeToFile(RScript, "processinfo", analytics);

        long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
        // Get elapsed time in seconds
        timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
        analytics.setTimeToRun_analytics(analytics.getTimeToRun_analytics() + timeToRun_analytics);
        DBSynchronizer.updateLindaAnalyticsProcessPerformanceTime(analytics);

    }

}
