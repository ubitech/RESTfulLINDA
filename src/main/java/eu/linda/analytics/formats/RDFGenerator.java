package eu.linda.analytics.formats;

import com.hp.hpl.jena.rdf.model.Model;
import eu.linda.analytics.model.Analytics;
import java.util.AbstractList;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author eleni
 */
abstract public class RDFGenerator {

    //generateRDFModelasweka
    abstract public Model generateRDFModel(Analytics analytics, AbstractList dataToExport);

    //generateRDFModelasR   
    abstract public Model generateRDFModel(Analytics analytics, RConnection re);

}
