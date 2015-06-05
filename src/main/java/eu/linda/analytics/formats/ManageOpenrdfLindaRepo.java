/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.formats;

/**
 *
 * @author eleni
 */
import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.Util;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

public class ManageOpenrdfLindaRepo {

    public void loadtotriplestore(Analytics a) {

        String sesameServerURL = "http://localhost:8080/openrdf-sesame";
        String repositoryID = "linda";
        Repository repo = new HTTPRepository(sesameServerURL, repositoryID);

        Date date = new Date();
        DateFormat formatter = new SimpleDateFormat("ddMMyyyy");
        String today = formatter.format(date);

        String datasetContextToString = "analytics" + a.getId() + "V" + a.getVersion() + "Date" + today;

        File file = new File(Configuration.docroot + a.getResultdocument());
        String base = "http://localhost:8080/openrdf-sesame/repositories/" + repositoryID + "/statements?context=_:";
        String baseURI = base + datasetContextToString + "#";

        try {
            repo.initialize();

            RepositoryConnection con = repo.getConnection();
            try {
                ValueFactory vf = ValueFactoryImpl.getInstance();
                Resource datasetContext = vf.createBNode(datasetContextToString);

                if (a.getExportFormat().equalsIgnoreCase("RDFXML")) {
                    con.add(file, baseURI, RDFFormat.RDFXML, datasetContext);

                } else if (a.getExportFormat().equalsIgnoreCase("TTL")) {
                    con.add(file, baseURI, RDFFormat.TURTLE, datasetContext);

                } else if (a.getExportFormat().equalsIgnoreCase("N-Tripples")) {
                    con.add(file, baseURI, RDFFormat.N3, datasetContext);
                }

                this.cleanLocalAnalyticsRepo(a, base + datasetContextToString);

            } catch (IOException ex) {
                Logger.getLogger(ManageOpenrdfLindaRepo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RDFParseException ex) {
                Logger.getLogger(ManageOpenrdfLindaRepo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RepositoryException ex) {
                Logger.getLogger(ManageOpenrdfLindaRepo.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                con.close();
            }
        } catch (OpenRDFException e) {
            // handle exception
        }

    }

    /* after loading the rdf file at linda triplestore:
     2. update publishedToTriplestore flag
     3. fill loadedRDFContext field
     */
    public void cleanLocalAnalyticsRepo(Analytics a, String rdfContextURL) {

        ConnectionController connectionController = ConnectionController.getInstance();
        connectionController.updateLindaAnalyticsRDFInfo(rdfContextURL, true, a.getId());

    }

//    public static void main(String[] args) throws RDFParseException, IOException {
//
//        HelpfulFunctionsSingleton helpfulFunctions = HelpfulFunctionsSingleton.getInstance();
//        
//        Analytics analytics = helpfulFunctions.connectToAnalyticsTable(Integer.parseInt("83"));
//        ManageOpenrdfLindaRepo manageOpenrdfLindaRepo = new ManageOpenrdfLindaRepo();
//
//        manageOpenrdfLindaRepo.loadtotriplestore(analytics);
//
//    }
}
