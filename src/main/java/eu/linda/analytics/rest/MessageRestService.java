package eu.linda.analytics.rest;

import eu.linda.analytics.controller.AnalyticsController;
import eu.linda.analytics.controller.AnalyticsFactory;
import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

//Example => http://localhost:8181/RESTfulLINDA/rest/analytics/1/5
@Path("/analytics")
public class MessageRestService {

    @GET
    @Path("/{analytics_category}/{id}")
    public Response printMessage(@PathParam("analytics_category") String analytics_category, @PathParam("id") String analytics_id) throws Exception {

        AnalyticsFactory analyticsFactory = new AnalyticsFactory();
        AnalyticsController analyticsController = new AnalyticsController(analyticsFactory);
        
        ConnectionController connectionController = ConnectionController.getInstance();
        connectionController.readProperties();
        Analytics analytics = DBSynchronizer.getlindaAnalytics_analytics(Integer.parseInt(analytics_id));
        analyticsController.setAnalytics(analytics);
        
        
        String inputSuffix;
        if (analytics.getTrainQuery_id() > 0) {
            inputSuffix = "rdf";
        } else {
            String[] suffixes = analytics.getDocument().split("\\.");
            inputSuffix = suffixes[suffixes.length - 1];
        }

        analyticsController.runAnalytics(inputSuffix, analytics.getAlgorithm_name(), analytics.getExportFormat());
        
        return Response.status(200).entity("Analytic Process for analytic ID "+analytics.getId()+
                ", with description "+ analytics.getDescription()+" has runned").build();

    }

}
