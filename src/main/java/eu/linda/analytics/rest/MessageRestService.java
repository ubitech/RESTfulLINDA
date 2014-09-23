package eu.linda.analytics.rest;

import eu.linda.analytic.controller.AnalyticsController;
import eu.linda.analytic.controller.AnalyticsFactory;
import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.associations.AprioriOutput;
import eu.linda.analytics.weka.classifiers.J48Output;
import eu.linda.analytics.weka.classifiers.M5POutput;
import eu.linda.analytics.weka.generic.LinearRegressionOutput;
import java.io.File;
import java.util.Vector;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import weka.associations.Apriori;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.M5P;

//http://localhost:8080/RESTfulLINDA/rest/message/hello%20world
@Path("/analytics")
public class MessageRestService {

    @GET
    @Path("/{analytics_category}/{id}")
    public Response printMessage(@PathParam("analytics_category") String analytics_category, @PathParam("id") String analytics_id) throws Exception {
        
        
        
        AnalyticsFactory analyticsFactory =  new AnalyticsFactory();
        
        AnalyticsController analyticsController =  new AnalyticsController(analyticsFactory);
        
        Analytics analytics = analyticsController.connectToAnalyticsTable(Integer.parseInt(analytics_id));
        analyticsController.runAnalytics(analytics.getAlgorithm_name(), analytics.getExportFormat());
        /*
        System.out.println("analytics_category" + analytics_category);
        ConnectionController connectionController = new ConnectionController();
        Analytics analytics = connectionController.connectToAnalyticsTable(Integer.parseInt(analytics_id));

        //Decide Algorithm To invoke
        String result = null;
        boolean isSaved = false;
        if (analytics.getAlgorithm_name().equalsIgnoreCase("Apriori")) {
            AprioriOutput aprioryOutput = new AprioriOutput();
            Apriori aprioriResult = aprioryOutput.getAprioriRules(Configuration.docroot + analytics.getDocument());
            result = aprioriResult.toString();

            isSaved = connectionController.writeToFile(result, "processinfo", analytics);

        } else if (analytics.getAlgorithm_name().equalsIgnoreCase("J48")) {
            J48Output j48Output = new J48Output();
            if (analytics.getModel().isEmpty()) {

                System.out.println(" i construct a model");

                Classifier j48ClassifierModel = j48Output.getJ48TreeModel(Configuration.docroot + analytics.getDocument());
                result = j48ClassifierModel.toString();
                analytics = connectionController.saveModel(j48ClassifierModel, analytics);

                JSONArray jsonresult = j48Output.getJ48TreeResultDataset(analytics);

                boolean isSavedProcessInfo = connectionController.writeToFile(jsonresult.getString(0), "processinfo", analytics);

                isSaved = connectionController.writeToFile(jsonresult.getString(1), "resultdocument", analytics);

            } else {

                JSONArray jsonresult = j48Output.getJ48TreeResultDataset(analytics);
                boolean isSavedProcessInfo = connectionController.writeToFile(jsonresult.getString(0), "processinfo", analytics);
                isSaved = connectionController.writeToFile(jsonresult.getString(1), "resultdocument", analytics);
                result = jsonresult.toString();
            }

        } else if (analytics.getAlgorithm_name().equalsIgnoreCase("LinearRegression")) {

            LinearRegressionOutput linearRegressionOutput = new LinearRegressionOutput();

            LinearRegression linearRegressionmodel = linearRegressionOutput.getLinearRegressionEstimations(Configuration.docroot + analytics.getDocument());

            analytics = connectionController.saveModel(linearRegressionmodel, analytics);

            String results = linearRegressionOutput.getLinearRegressionResults(analytics);

            isSaved = connectionController.writeToFile(results, "resultdocument", analytics);

        } else if (analytics.getAlgorithm_name().equalsIgnoreCase("M5P")) {

            M5POutput m5POutput = new M5POutput();

            Vector M5Pmodel = m5POutput.trainModelM5P(Configuration.docroot + analytics.getDocument());

            analytics = connectionController.saveModelasVector(M5Pmodel, analytics);

             JSONArray jsonresult = m5POutput.predictM5P(analytics);

            boolean isSavedProcessInfo =  connectionController.writeToFile(jsonresult.getString(0), "processinfo", analytics);
            
             isSaved =  connectionController.writeToFile(jsonresult.getString(1), "resultdocument", analytics);

        }

        if (isSaved) {
            return Response.status(200).entity(result).build();
        } else {
            return Response.status(500).entity(result).build();
        }
        
        
        */
        return Response.status(200).entity("Analytic Process has runned").build();

    }

}
