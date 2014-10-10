/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytic.formats;

/**
 *
 * @author eleni
 */
import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctions;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.model.MemValueFactory;

public class ManageOpenrdfLindaRepo {

    public void loadtotriplestore(Analytics a) {

        String sesameServerURL = "http://localhost:8080/openrdf-sesame";
        String repositoryID = "linda";
        Repository repo = new HTTPRepository(sesameServerURL, repositoryID);

        Date date = new Date();
        DateFormat formatter = new SimpleDateFormat("ddMMyyyy");
        String today = formatter.format(date);

        String datasetContextToString = "analytics" + a.getId()+ "V" + 1 + "Date" + today;
        File file = new File(Configuration.docroot + a.getResultdocument());
        String base = "http://localhost:8080/openrdf-sesame/repositories/myRepository/statements?context=:_" ;
        String baseURI = base + datasetContextToString +"/";

        try {
            repo.initialize();

            RepositoryConnection con = repo.getConnection();
            try {
                ValueFactory vf = ValueFactoryImpl.getInstance();
                Resource datasetContext = vf.createBNode(datasetContextToString);
                con.add(file, baseURI, RDFFormat.RDFXML, datasetContext);

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

    public static void main(String[] args) throws RDFParseException, IOException {

        HelpfulFunctions helpfulFunctions = new HelpfulFunctions();

        Analytics analytics = helpfulFunctions.connectToAnalyticsTable(Integer.parseInt("83"));
        ManageOpenrdfLindaRepo manageOpenrdfLindaRepo = new ManageOpenrdfLindaRepo();

        manageOpenrdfLindaRepo.loadtotriplestore(analytics);

        /*
         String sesameServerURL = "http://localhost:8080/openrdf-sesame";
         String repositoryID = "linda";
         Repository repo = new HTTPRepository(sesameServerURL, repositoryID);

         File file = new File("/home/eleni/Desktop/test.rdf");
         String baseURI = "http://localhost:8080/openrdf-sesame/repositories/LinDAnalytics/analytics_6_1.0.0_08102014/";

         try {
         repo.initialize();

         RepositoryConnection con = repo.getConnection();
         try {
         ValueFactory valueFactory = new MemValueFactory();
         //URI context = valueFactory.createURI("http://localhost:8080/openrdf-sesame/repositories/LinDAnalytics/analytics_6_1.0.0_08102014/"); 
         // Create a ValueFactory we can use to create resources and statements
         ValueFactory vf = ValueFactoryImpl.getInstance();
         Resource lala = vf.createBNode("analyticprocess");
         con.add(file, baseURI, RDFFormat.RDFXML, lala);
         //URL url = new URL("http://example.org/example/remote.rdf");
         //con.add(url, url.toString(), RDFFormat.RDFXML);
         } finally {
         con.close();
         }
         } catch (OpenRDFException e) {
         // handle exception
         }
         }*/
    }
}
