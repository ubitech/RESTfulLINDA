package eu.linda.analytics.r.clustering;

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
public class KMeansAnalyticProcess extends AnalyticProcess {

    InputFormat input;
    ConnectionController connectionController;

    public KMeansAnalyticProcess(InputFormat input) {
        Util.nicePrintMessage("Create analytic process for K-Means Algorithm");
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
            int clustersNum = 5;
            String RScript = "";
            //clean previous eval info if exists
            Util.cleanPreviousInfo(analytics);
            analytics.setTimeToGet_data(0);
            analytics.setTimeToRun_analytics(0);
            analytics.setData_size(0);

            //get parameters
            String parameters = analytics.getParameters();

            String[] splitedparameters = parameters.split("->");
            for (String parameter : splitedparameters) {
                System.out.println("parameter" + parameter);

                if (parameter.contains("k")) {
                    String[] clustersNumP = parameter.split("k");
                    clustersNum = Integer.parseInt(clustersNumP[1].trim());
                    System.out.println("clustersNum" + clustersNum);
                }
            }

            RConnection re;
            if (Util.isRDFInputFormat(analytics.getTrainQuery_id())) {
                re = input.importData4R(Integer.toString(analytics.getTrainQuery_id()), "", true, analytics);

            } else {
                re = input.importData4R(Configuration.analyticsRepo + analytics.getDocument(), "", true, analytics);
                RScript += "loaded_data <- read.csv(file='" + Configuration.analyticsRepo + analytics.getDocument() + "', header=TRUE, sep=',');\n";

            }
            if (re == null) {
                return;
            }
            //TODO Check that all values are numeric

            re.eval("uri<-loaded_data[2];");
            RScript += "# Prepare Data \n uri<-loaded_data[2]; \n";

            re.eval("column_with_uri <-colnames(loaded_data[2]);");
            RScript += "column_with_uri <-colnames(loaded_data[2]); \n";

            re.eval("myvars <- names(loaded_data) %in% c('rowID',column_with_uri);");
            RScript += "# Prepare Data \n myvars <- names(loaded_data) %in% c('rowID',column_with_uri); \n";

            re.eval("loaded_data <- loaded_data[!myvars]; "
                    + "nums <- sapply(loaded_data, is.numeric); "
                    + "loaded_data<-loaded_data[ , nums]; ");

            RScript += "loaded_data <- loaded_data[!myvars]; \n"
                    + "nums <- sapply(loaded_data, is.numeric); \n "
                    + "loaded_data<-loaded_data[ , nums]; \n";

            re.eval("loaded_data <- na.omit(loaded_data)");
            RScript += "loaded_data <- na.omit(loaded_data) # listwise deletion of missing\n";

            re.eval("loaded_data <- scale(loaded_data)");
            RScript += "loaded_data <- scale(loaded_data) # standardize variables\n";

            int num_of_not_numerical_variables = re.eval("table(nums)[\"FALSE\"]").asInteger();
            if (num_of_not_numerical_variables > 0) {
                DBSynchronizer.updateLindaAnalyticsProcessMessage("Note : Input queries contain non numeric variables and these have been ignored during the analysis. \n "
                        + "If you want to analyze data that contain mixed numerical and categorical data, consider using the M5P or J48 algorithms. \n"
                        + "In case all your variables are categorical, consider use the K-modes algorithm", analytics.getId());
            }

            //Partitioning
            re.eval("fit <- kmeans(loaded_data, " + clustersNum + ")");
            RScript += "#Partitioning\n # K-Means Cluster Analysis \n fit <- kmeans(loaded_data, " + clustersNum + ") # " + clustersNum + " cluster solution \n";

            re.eval("aggregate(loaded_data,by=list(fit$cluster),FUN=mean);");
            RScript += "# get cluster means \n aggregate(loaded_data,by=list(fit$cluster),FUN=mean) \n ";

            re.eval("loaded_data <- data.frame(loaded_data, fit$cluster);");
            RScript += "# append cluster assignment \n  loaded_data <- data.frame(loaded_data, fit$cluster) \n";

            //Visualize
            re.eval("fit <- kmeans(loaded_data, " + clustersNum + ");");
            RScript += "# Visualize \n #Plotting Cluster Solutions \n # K-Means Clustering with " + clustersNum + " clusters \n fit <- kmeans(loaded_data, " + clustersNum + ") \n";

            re.eval("library(cluster);");
            RScript += "# Cluster Plot against 1st 2 principal components  \n # vary parameters for most readable graph \n library(cluster); \n";

            long plot1_id = Util.manageNewPlot(analytics, "K-Means Clustering with " + clustersNum + " clusters", "plots/plotid" + analytics.getPlot1_id() + ".png", "plot1_id");

            re.eval("png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);");
            re.eval("print(clusplot(loaded_data, fit$cluster, color=TRUE, shade=TRUE, labels=2, lines=0));");
            re.eval("dev.off();");

            RScript += "png(file='" + Configuration.analyticsRepo + "plots/plotid" + plot1_id + ".png',width=600);\n";
            RScript += "print(clusplot(loaded_data, fit$cluster, color=TRUE, shade=TRUE, labels=2, lines=0));\n";
            RScript += "dev.off();\n";

            //Save model Readable
            String modelFileName = "models/analyticsID" + analytics.getId() + "_" + analytics.getAlgorithm_id() + "ModelReadable" + ".txt";
            String modelFileNameFullPath = Configuration.analyticsRepo + modelFileName;

            org.rosuda.REngine.REXP s = re.eval("capture.output(fit$centers)");
            String[] output = s.asStrings();
            for (String string : output) {
                System.out.println(string);
            }

            Util.saveFile(modelFileNameFullPath, output);
            DBSynchronizer.updateLindaAnalytics(modelFileName, "modelReadable", analytics.getId());

            re.eval("column_number<-ncol(loaded_data);");
            RScript += "column_number<-ncol(loaded_data); \n";

            re.eval("column_to_predict <-colnames(loaded_data[column_number]);");
            RScript += "column_to_predict <-colnames(loaded_data[column_number]); \n";

            re.eval("df_to_export <- data.frame(uri,loaded_data[column_to_predict]);");
            RScript += "df_to_export <- data.frame(uri,loaded_data[column_to_predict]);\n";

            Util.writeToFile(RScript, "processinfo", analytics);

            long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
            // Get elapsed time in seconds
            timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
            analytics.setTimeToRun_analytics(analytics.getTimeToRun_analytics() + timeToRun_analytics);
            DBSynchronizer.updateLindaAnalyticsProcessPerformanceTime(analytics);
            out.exportData(analytics, re);

        } catch (RserveException ex) {
            Logger.getLogger(KMeansAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REXPMismatchException ex) {
            Logger.getLogger(KMeansAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
