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
import java.util.Vector;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;
import weka.core.Instances;

/**
 *
 * @author eleni
 */
public class ForecastingRDFGenerator extends RDFGenerator {

    HelpfulFunctionsSingleton helpfulFunctions;

    public ForecastingRDFGenerator() {
        helpfulFunctions = HelpfulFunctionsSingleton.getInstance();
    }

    //Generate General RDF Model
    @Override
    public Model generateRDFModel(Analytics analytics, AbstractList dataToExport) {

        helpfulFunctions.nicePrintMessage("Generate Forecasting RDFModel for weka algorithms ");

        Date date = new Date();
        DateFormat formatter = new SimpleDateFormat("ddMMyyyy");
        String today = formatter.format(date);
        String base = "http://localhost:8080/openrdf-sesame/repositories/myRepository/statements?context=:_";
        String datasetContextToString = "analytics" + analytics.getId() + "V" + (analytics.getVersion() + 1) + "Date" + today;

        Instances triplets = (Instances) dataToExport;
        int tripletsAttibutesNum = triplets.numAttributes();

        // Create the model and define some prefixes (for nice serialization in RDF/XML and TTL)
        Model model = ModelFactory.createDefaultModel();
        //openrdf + analytic_process ID_version_date
        String NS = base + datasetContextToString + "#";

        String analytics_base = "http://localhost:8080/openrdf-sesame/repositories/linda/rdf-graphs/analyticsontology";
        String analytics_NS = analytics_base + "#";

        model.setNsPrefix("ds", NS);
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("xsd", XSD.getURI());
        model.setNsPrefix("foaf", FOAF.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("prov", "http://www.w3.org/ns/prov#");
        model.setNsPrefix("sio", "http://semanticscience.org/ontology/sio#");
        model.setNsPrefix("an", "http://localhost:8080/openrdf-sesame/repositories/linda/rdf-graphs/analyticsontology#");

        // Define local properties
        Property analyzedField = model.createProperty(NS + "#analyzedField");
        Property predictedValue = model.createProperty(NS + "#predictedValue");
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

        Resource software_statement = model.createResource(analytics_NS + "Software/LinDa_analytics_software");
        Resource software = model.createResource(analytics_NS + "Software");
        Resource linda_user = model.createResource(analytics_NS + "User");

        Resource analytic_process = model.createResource(analytics_NS + "analytic_process");
        Resource analytic_process_statement = model.createResource(analytics_NS + "analytic_process/" + analytics.getId() + "/" + (analytics.getVersion() + 1));
        analytic_process_statement.addProperty(RDF.type, analytic_process);
        analytic_process_statement.addProperty(OWL.versionInfo, "1.0.0");
        analytic_process_statement.addLiteral(analyzedField, triplets.attribute(tripletsAttibutesNum - 1).name());
        analytic_process_statement.addProperty(RDFS.subClassOf, activity);
        analytic_process_statement.addProperty(wasAssociatedWith, software_statement);
        analytic_process_statement.addProperty(RDFS.label, "linda analytic process");
        analytic_process_statement.addProperty(RDFS.comment, analytics.getDescription());

        Resource linda_user_statement = model.createResource(analytics_NS + "User/eleni");
        linda_user_statement.addProperty(RDF.type, linda_user);
        linda_user_statement.addProperty(RDFS.subClassOf, agent);
        linda_user_statement.addProperty(RDFS.label, "linda user");

        software_statement.addProperty(RDF.type, software);
        software_statement.addProperty(RDFS.subClassOf, agent);
        software_statement.addProperty(RDFS.label, "analytics software");
        software_statement.addProperty(actedOnBehalfOf, linda_user_statement);

        linda_user_statement.addProperty(OWL.equivalentClass, FOAF.Person);

        linda_user_statement.addProperty(FOAF.holdsAccount, onlineAccount);

        linda_user_statement.addProperty(FOAF.accountName, "eleni");
        onlineAccount.addProperty(FOAF.homepage, Configuration.lindaworkbenchURI);

        Resource analytic_result_node = model.createResource(analytics_NS + "analytics_result_node");
        Resource analytic_input_node = model.createResource(analytics_NS + "analytic_input_node");

        // For each triplet, create a resource representing the sentence, as well as the subject, 
        // predicate, and object, and then add the triples to the model.
        for (int i = 1; i < triplets.size(); i++) {

            Resource analytic_result_node_statement = model.createResource(NS + "/" + i);

            Resource analytic_input_node_statement = model.createResource(triplets.get(i).toString(1));
            analytic_input_node_statement.addProperty(RDF.type, analytic_input_node);

            analytic_result_node_statement.addProperty(RDF.type, analytic_result_node);
            analytic_result_node_statement.addProperty(RDFS.subClassOf, entity);
            analytic_result_node_statement.addProperty(wasDerivedFrom, analytic_input_node_statement);
            analytic_result_node_statement.addProperty(wasGeneratedBy, analytic_process_statement);
            analytic_result_node_statement.addProperty(predictedValue, triplets.get(i).toString(tripletsAttibutesNum - 1));
        }
        return model;

    }

    //Generate Forecasting RDF Model
    @Override
    public Model generateRDFModel(Analytics analytics, Rengine re) {

        helpfulFunctions.nicePrintMessage("Generate Forecasting RDFModel for R algorithms ");

        RVector dataToExportasVector = re.eval("df_to_export").asVector();
        Vector colnames = dataToExportasVector.getNames();
        String[] colnamesArray = new String[colnames.size()];
        colnames.copyInto(colnamesArray);

        String analyzedFieldValue = colnamesArray[colnames.size() - 1];

        //create input bag
        //re.eval("data_matrix[,c('uri')]");
        REXP basenamespace = re.eval("basens<-data_matrix[1,1]");
        String basens = basenamespace.asString();
        System.out.println("basens" + basens);

        REXP datesAsCharacter = re.eval("as.character(df_to_export$" + colnamesArray[0] + ")");

        String[] datesAsStringArray = datesAsCharacter.asStringArray();
        for (String string : datesAsStringArray) {
            System.out.println("datesAsStringArray" + string);
        }

        System.out.println("colname of predicted value" + colnamesArray[colnames.size() - 1]);
        REXP predictedValues = re.eval("df_to_export$" + colnamesArray[colnames.size() - 1]);

        double[] predictedValuesAsDoubleArray = predictedValues.asDoubleArray();

        for (double d : predictedValuesAsDoubleArray) {
            System.out.println("predictedValues:" + d);
        }

        Date date = new Date();
        DateFormat formatter = new SimpleDateFormat("ddMMyyyy");
        String today = formatter.format(date);
        String base = "http://localhost:8080/openrdf-sesame/repositories/linda/statements?context=:_";
        String datasetContextToString = "analytics" + analytics.getId() + "V" + (analytics.getVersion() + 1) + "Date" + today;

        // Create the model and define some prefixes (for nice serialization in RDF/XML and TTL)
        Model model = ModelFactory.createDefaultModel();
        //openrdf + analytic_process ID_version_date
        String ds = base + datasetContextToString + "#";

        String analytics_base = "http://localhost:8080/openrdf-sesame/repositories/linda/rdf-graphs/analyticsontology";
        String analytics_NS = analytics_base + "#";

        model.setNsPrefix("ds", ds);
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("xsd", XSD.getURI());
        model.setNsPrefix("foaf", FOAF.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("prov", "http://www.w3.org/ns/prov#");
        model.setNsPrefix("sio", "http://semanticscience.org/ontology/sio#");
        model.setNsPrefix("an", "http://localhost:8080/openrdf-sesame/repositories/linda/rdf-graphs/analyticsontology#");

        // Define local properties
        Property analyzedField = model.createProperty(ds + "analyzedField");
        Property predictedValue = model.createProperty(ds + "predictedValue");
        Property forecastingDate = model.createProperty(ds + "forecastingDate");
        Property wasDerivedFrom = model.createProperty("http://www.w3.org/ns/prov#wasDerivedFrom");
        Property wasGeneratedBy = model.createProperty("http://www.w3.org/ns/prov#wasGeneratedBy");
        Property actedOnBehalfOf = model.createProperty("http://www.w3.org/ns/prov#actedOnBehalfOf");
        Property wasAssociatedWith = model.createProperty("http://www.w3.org/ns/prov#wasAssociatedWith");

        Resource entity = model.createResource("http://www.w3.org/ns/prov#Entity");
        Resource activity = model.createResource("http://www.w3.org/ns/prov#Activity");
        Resource agent = model.createResource("http://www.w3.org/ns/prov#Agent");
        Resource onlineAccount = model.createResource(FOAF.OnlineAccount);

        Resource software_statement = model.createResource(analytics_NS + "Software/LinDa_analytics_software");
        Resource software = model.createResource(analytics_NS + "Software");
        Resource linda_user = model.createResource(analytics_NS + "User");

        Resource analytic_process = model.createResource(analytics_NS + "analytic_process");
        Resource analytic_process_statement = model.createResource(analytics_NS + "analytic_process/" + analytics.getId() + "/" + analytics.getVersion());
        analytic_process_statement.addProperty(RDF.type, analytic_process);
        analytic_process_statement.addProperty(OWL.versionInfo, "1.0.0");
        analytic_process_statement.addLiteral(analyzedField, analyzedFieldValue);
        analytic_process_statement.addProperty(RDFS.subClassOf, activity);
        analytic_process_statement.addProperty(wasAssociatedWith, software_statement);
        analytic_process_statement.addProperty(RDFS.label, "linda analytic process");
        analytic_process_statement.addProperty(RDFS.comment, analytics.getDescription());

        Resource linda_user_statement = model.createResource(analytics_NS + "User/eleni");
        linda_user_statement.addProperty(RDF.type, linda_user);
        linda_user_statement.addProperty(RDFS.subClassOf, agent);
        linda_user_statement.addProperty(RDFS.label, "linda user");

        software_statement.addProperty(RDF.type, software);
        software_statement.addProperty(RDFS.subClassOf, agent);
        software_statement.addProperty(RDFS.label, "analytics software");
        software_statement.addProperty(actedOnBehalfOf, linda_user_statement);

        linda_user_statement.addProperty(OWL.equivalentClass, FOAF.Person);
        linda_user_statement.addProperty(FOAF.holdsAccount, onlineAccount);
        linda_user_statement.addProperty(FOAF.accountName, "eleni");
        onlineAccount.addProperty(FOAF.homepage, Configuration.lindaworkbenchURI);

        Resource analytic_result_node = model.createResource(analytics_NS + "analytics_result_node");
        Resource analytic_input_node = model.createResource(analytics_NS + "analytic_input_node");

        // For each triplet, create a resource representing the sentence, as well as the subject, 
        // predicate, and object, and then add the triples to the model.
        for (int i = 0; i < predictedValuesAsDoubleArray.length - 1; i++) {

            Resource analytic_input_node_statement = model.createResource(basens);
            analytic_input_node_statement.addProperty(RDF.type, analytic_input_node);

            Resource analytic_result_node_statement = model.createResource(ds + "/" + i);
            analytic_result_node_statement.addProperty(RDF.type, analytic_result_node);
            analytic_result_node_statement.addProperty(RDFS.subClassOf, entity);
            analytic_result_node_statement.addProperty(wasDerivedFrom, analytic_input_node_statement);
            analytic_result_node_statement.addProperty(wasGeneratedBy, analytic_process_statement);
            analytic_result_node_statement.addProperty(forecastingDate, datesAsStringArray[i]);
            analytic_result_node_statement.addProperty(predictedValue, String.valueOf(predictedValuesAsDoubleArray[i]));

        }

        re.eval("rm(list=ls());");

        return model;
    }
}
