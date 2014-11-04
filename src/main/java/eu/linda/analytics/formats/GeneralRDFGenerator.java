/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.formats;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;
import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctionsSingleton;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.Date;
import org.rosuda.JRI.Rengine;
import weka.core.Instances;

/**
 *
 * @author eleni
 */
public class GeneralRDFGenerator extends RDFGenerator {

    HelpfulFunctionsSingleton helpfulFuncions;

    public GeneralRDFGenerator() {
        helpfulFuncions = HelpfulFunctionsSingleton.getInstance();
    }

    @Override
    public Model generateRDFModel(Analytics analytics, AbstractList dataToExport) {

        helpfulFuncions.nicePrintMessage("Generate General RDFModel for weka algorithms ");

        Date date = new Date();
        DateFormat formatter = new SimpleDateFormat("ddMMyyyy");
        String today = formatter.format(date);
        String base = "http://localhost:8080/openrdf-sesame/repositories/linda/statements?context=:_";
        String datasetContextToString = "analytics" + analytics.getId() + "V" + (analytics.getVersion() + 1) + "Date" + today;

        Instances triplets = (Instances) dataToExport;
        int tripletsAttibutesNum = triplets.numAttributes();

        // Create the model and define some prefixes (for nice serialization in RDF/XML and TTL)
        Model model = ModelFactory.createDefaultModel();
        //openrdf + analytic_process ID_version_date
        String NS = base + datasetContextToString + "#";

        model.setNsPrefix("ds", NS);
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("xsd", XSD.getURI());
        model.setNsPrefix("foaf", FOAF.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("prov", "http://www.w3.org/ns/prov#");
        model.setNsPrefix("sio", "http://semanticscience.org/ontology/sio#");

        // Define local properties
        Property analyzedField = model.createProperty(NS + "analyzedField");
        Property predictedValue = model.createProperty(NS + "predictedValue");
        Property confidence = model.createProperty(NS + "confidence");
        Property denotes = model.createProperty("http://semanticscience.org/ontology/sio#denotes");
        Property wasDerivedFrom = model.createProperty("http://www.w3.org/ns/prov#wasDerivedFrom");
        Property wasGeneratedBy = model.createProperty("http://www.w3.org/ns/prov#wasGeneratedBy");
        Property actedOnBehalfOf = model.createProperty("http://www.w3.org/ns/prov#actedOnBehalfOf");
        Property wasAssociatedWith = model.createProperty("http://www.w3.org/ns/prov#wasAssociatedWith");

        Resource entity = model.createResource("http://www.w3.org/ns/prov#Entity");
        Resource activity = model.createResource("http://www.w3.org/ns/prov#Activity");
        Resource agent = model.createResource("http://www.w3.org/ns/prov#Agent");
        Resource onlineAccount = model.createResource(FOAF.OnlineAccount);

        Resource software_statement = model.createResource(NS + "RESTfulLIDA_analytics_software");
        Resource linda_user_statement = model.createResource(NS + "eleni");

        Resource analytic_process_statement = model.createResource(NS + "analytic_process");
        analytic_process_statement.addProperty(OWL.versionInfo, "1.0.0");
        analytic_process_statement.addLiteral(analyzedField, triplets.attribute(tripletsAttibutesNum - 1).name());
        analytic_process_statement.addProperty(RDFS.subClassOf, activity);
        analytic_process_statement.addProperty(wasAssociatedWith, software_statement);
        analytic_process_statement.addProperty(RDFS.label, "linda analytic process");
        analytic_process_statement.addProperty(RDFS.comment, analytics.getDescription());

        linda_user_statement.addProperty(RDFS.subClassOf, agent);
        linda_user_statement.addProperty(RDFS.label, "linda user");

        software_statement.addProperty(RDFS.subClassOf, agent);
        software_statement.addProperty(RDFS.label, "analytics software");
        software_statement.addProperty(actedOnBehalfOf, linda_user_statement);

        linda_user_statement.addProperty(OWL.equivalentClass, FOAF.Person);

        linda_user_statement.addProperty(FOAF.holdsAccount, onlineAccount);

        linda_user_statement.addProperty(FOAF.accountName, "eleni");
        onlineAccount.addProperty(FOAF.homepage, Configuration.lindaworkbenchURI);

        // For each triplet, create a resource representing the sentence, as well as the subject, 
        // predicate, and object, and then add the triples to the model.
        for (int i = 1; i < triplets.size(); i++) {
            //for (Instance triplet : triplets) {

            Resource analytic_result_node_statement = model.createResource(NS + "/" + i);
            Resource analytic_input_node_statement = model.createResource(triplets.get(i).toString(1));

            //Resource subject = model.createResource().addProperty(RDFS.label, (String) triplet[1]);
            //Property predicate = model.createProperty(NS + URIref.encode((String) triplet[2]));
            //Resource object = model.createResource().addProperty(RDFS.label, (String) triplet[3]);
            analytic_result_node_statement.addProperty(RDFS.subClassOf, entity);
            analytic_result_node_statement.addProperty(wasDerivedFrom, analytic_input_node_statement);
            analytic_result_node_statement.addProperty(wasGeneratedBy, analytic_process_statement);
            analytic_result_node_statement.addProperty(predictedValue, triplets.get(i).toString(tripletsAttibutesNum - 1));

            //analytic_result_node_statement.addLiteral(confidence, triplet[0]);
            //analytic_result_node_statement.addProperty(RDF.subject, subject);
            //analytic_result_node_statement.addProperty(RDF.predicate, predicate);
            //analytic_result_node_statement.addProperty(RDF.object, object);
        }

        return model;

    }

    @Override
    public Model generateRDFModel(Analytics analytics, Rengine re) {

        helpfulFuncions.nicePrintMessage("Generate General RDFModel for R algorithms ");

        // Create the model and define some prefixes (for nice serialization in RDF/XML and TTL)
        Model model = ModelFactory.createDefaultModel();

        return model;
    }

}
