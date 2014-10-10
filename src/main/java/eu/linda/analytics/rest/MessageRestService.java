package eu.linda.analytics.rest;

import eu.linda.analytic.controller.AnalyticsController;
import eu.linda.analytic.controller.AnalyticsFactory;
import eu.linda.analytic.formats.ManageOpenrdfLindaRepo;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctions;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

//http://localhost:8080/RESTfulLINDA/rest/message/hello%20world
@Path("/analytics")
public class MessageRestService {

    @GET
    @Path("/{analytics_category}/{id}")
    public Response printMessage(@PathParam("analytics_category") String analytics_category, @PathParam("id") String analytics_id) throws Exception {

        AnalyticsFactory analyticsFactory = new AnalyticsFactory();
        AnalyticsController analyticsController = new AnalyticsController(analyticsFactory);
        Analytics analytics = analyticsController.connectToAnalyticsTable(Integer.parseInt(analytics_id));
        String[] suffixes = analytics.getDocument().split("\\.");
        String inputSuffix = suffixes[suffixes.length - 1];
        analyticsController.runAnalytics(inputSuffix, analytics.getAlgorithm_name(), analytics.getExportFormat());
        return Response.status(200).entity("Analytic Process has runned").build();

    }

    @GET
    @Path("/loadtotriplestore/{id}")
    public Response loadToTriplestore(@PathParam("id") String analytics_id) throws Exception {

        HelpfulFunctions helpfulFunctions = new HelpfulFunctions();

        Analytics analytics = helpfulFunctions.connectToAnalyticsTable(Integer.parseInt(analytics_id));
        ManageOpenrdfLindaRepo manageOpenrdfLindaRepo = new ManageOpenrdfLindaRepo();
        
        manageOpenrdfLindaRepo.loadtotriplestore(analytics);
        
        return Response.status(200).entity("Makari...Analytic Process has runned").build();

    }

}
