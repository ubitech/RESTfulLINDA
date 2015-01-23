/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.formats;

import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import static com.hp.hpl.jena.vocabulary.RDF.Bag;
import com.hp.hpl.jena.vocabulary.RDFS;
import static com.hp.hpl.jena.vocabulary.RDFS.Literal;
import com.hp.hpl.jena.vocabulary.XSD;
import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctionsSingleton;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;
import weka.core.Instances;

/**
 *
 * @author eleni
 */
public class ClusteringRDFGenerator extends RDFGenerator {

    HelpfulFunctionsSingleton helpfulFuncions;

    public ClusteringRDFGenerator() {
        helpfulFuncions = HelpfulFunctionsSingleton.getInstance();
    }

    @Override
    public Model generateRDFModel(Analytics analytics, AbstractList dataToExport) {

        //NOT IMPLEMENTED YET
        return null;

    }

    @Override
    public Model generateRDFModel(Analytics analytics, Rengine re) {

        helpfulFuncions.nicePrintMessage("Generate Clustering RDFModel for R algorithms ");

        int clustersNum = 5;
        int current_version =(analytics.getVersion() + 1);

        String analyzedFieldValue = "clusters-" + analytics.getId() + "-" + current_version;

        System.out.println("analyzedFieldValue: " + analyzedFieldValue);
//        REXP uriAsCharacter = re.eval("as.character(df_to_export$uri)");
//
//        String[] urisAsStringArray = uriAsCharacter.asStringArray();
//        for (String string : urisAsStringArray) {
//            System.out.println("urisAsStringArray" + string);
//        }

        REXP predictedValues = re.eval("as.character(df_to_export[[column_to_predict]]);");
        String[] predictedValuesAsDoubleArray = predictedValues.asStringArray();
//        for (String d : predictedValuesAsDoubleArray) {
//            System.out.println("predictedValues:" + d);
//        }

        Date date = new Date();
        DateFormat formatter = new SimpleDateFormat("ddMMyyyy");
        String today = formatter.format(date);
        String base = Configuration.lindaworkbenchURI + "openrdf-sesame/repositories/linda/statements?context=:_";
        String datasetContextToString = "analytics" + analytics.getId() + "V" + current_version + "Date" + today;

        // Create the model and define some prefixes (for nice serialization in RDF/XML and TTL)
        Model model = ModelFactory.createDefaultModel();
        //openrdf + analytic_process ID_version_date
        String ds = base + datasetContextToString + "#";

        String analytics_base = Configuration.lindaworkbenchURI + "openrdf-sesame/repositories/linda/rdf-graphs/analyticsontology";
        String analytics_NS = analytics_base + "#";

        model.setNsPrefix("ds", ds);
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("xsd", XSD.getURI());
        model.setNsPrefix("foaf", FOAF.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("prov", "http://www.w3.org/ns/prov#");
        model.setNsPrefix("sio", "http://semanticscience.org/ontology/sio#");
        model.setNsPrefix("an", Configuration.lindaworkbenchURI + "openrdf-sesame/repositories/linda/rdf-graphs/analyticsontology#");

        // Define local properties
        Property analyzedField = model.createProperty(ds + "analyzedField");
        Property predictedValue = model.createProperty(ds + "predictedValue");
        Property wasDerivedFrom = model.createProperty("http://www.w3.org/ns/prov#wasDerivedFrom");
        Property wasGeneratedBy = model.createProperty("http://www.w3.org/ns/prov#wasGeneratedBy");
        Property actedOnBehalfOf = model.createProperty("http://www.w3.org/ns/prov#actedOnBehalfOf");
        Property wasAssociatedWith = model.createProperty("http://www.w3.org/ns/prov#wasAssociatedWith");
        Property hasTrainDataset = model.createProperty(ds + "hasTrainDataset");
        Property algorithmProperty = model.createProperty(ds + "algorithm");
        Property dataSizeOfAnalyzedDataProperty = model.createProperty(ds + "dataSizeOfAnalyzedDatainBytes");
        Property timeToGetDataProperty = model.createProperty(ds + "timeToGetDataInSecs");
        Property timeToRunAnalyticsProcessProperty = model.createProperty(ds + "timeToRunAnalyticsProcessInSecs");
        Property timeToCreateRDFOutPutProperty = model.createProperty(ds + "timeToCreateRDFOutPutInSecs");
        Property performanceProperty = model.createProperty(ds + "performance");
        Property atTime = model.createProperty("http://www.w3.org/ns/prov#atTime");

        Resource entity = model.createResource("http://www.w3.org/ns/prov#Entity");
        Resource activity = model.createResource("http://www.w3.org/ns/prov#Activity");
        Resource agent = model.createResource("http://www.w3.org/ns/prov#Agent");
        Resource onlineAccount = model.createResource(FOAF.OnlineAccount);
        Resource linda_user = model.createResource(analytics_NS + "User");
        Resource software_statement = model.createResource(analytics_NS + "Software/LinDa_analytics_software");
        Resource software = model.createResource(analytics_NS + "Software");
        Resource performance = model.createResource(analytics_NS + "performance");
        Resource performance_statement = model.createResource(analytics_NS + "performance/" + analytics.getId() + "/" + current_version);

        Resource analytic_process = model.createResource(analytics_NS + "analytic_process");
        Resource analytic_process_statement = model.createResource(analytics_NS + "analytic_process/" + analytics.getId() + "/" + current_version);
        analytic_process_statement.addProperty(RDF.type, analytic_process);
        analytic_process_statement.addProperty(OWL.versionInfo, Integer.toString(current_version));
        analytic_process_statement.addLiteral(analyzedField, analyzedFieldValue);
        analytic_process_statement.addProperty(RDFS.subClassOf, activity);
        analytic_process_statement.addProperty(wasAssociatedWith, software_statement);
        analytic_process_statement.addProperty(RDFS.label, "linda analytic process");
        analytic_process_statement.addProperty(RDFS.comment, analytics.getDescription());
        analytic_process_statement.addProperty(algorithmProperty, analytics.getAlgorithm_name());

        Calendar cal = GregorianCalendar.getInstance();
        Literal value = model.createTypedLiteral(cal);
        analytic_process_statement.addProperty(atTime, value);

        performance_statement.addProperty(RDF.type, performance);
        performance_statement.addProperty(dataSizeOfAnalyzedDataProperty, Float.toString(analytics.getData_size()));
        performance_statement.addProperty(timeToGetDataProperty, Float.toString(analytics.getTimeToGet_data()));
        performance_statement.addProperty(timeToRunAnalyticsProcessProperty, Float.toString(analytics.getTimeToRun_analytics()));
        performance_statement.addProperty(timeToCreateRDFOutPutProperty, Float.toString(analytics.getTimeToCreate_RDF()));
        analytic_process_statement.addProperty(performanceProperty, performance_statement);

        if (helpfulFuncions.isRDFInputFormat(analytics.getTrainQuery_id())) {

            Resource analytic_train_dataset_statement = model.createResource(Configuration.lindaworkbenchURI + "sparql/?q_id=" + analytics.getTrainQuery_id());
            analytic_process_statement.addProperty(hasTrainDataset, analytic_train_dataset_statement);

        }

        Resource linda_user_statement = model.createResource(analytics_NS + "User/" + analytics.getUser_name());
        linda_user_statement.addProperty(RDF.type, linda_user);
        linda_user_statement.addProperty(RDFS.subClassOf, agent);
        linda_user_statement.addProperty(RDFS.label, "User");

        software_statement.addProperty(RDF.type, software);
        software_statement.addProperty(RDFS.subClassOf, agent);
        software_statement.addProperty(RDFS.label, "Software");
        software_statement.addProperty(actedOnBehalfOf, linda_user_statement);

        linda_user_statement.addProperty(OWL.equivalentClass, FOAF.Person);

        linda_user_statement.addProperty(FOAF.holdsAccount, onlineAccount);

        linda_user_statement.addProperty(FOAF.accountName, analytics.getUser_name());
        onlineAccount.addProperty(FOAF.homepage, Configuration.lindaworkbenchURI);

        Resource analytic_result_node = model.createResource(analytics_NS + "analytics_result_node");
        Resource analytic_input_node = model.createResource(analytics_NS + "analytic_input_node");

        for (int i = 1; i < clustersNum + 1; i++) {

            Resource analytic_cluster = model.createResource(analytics_NS + "analytic_input_collection");
            Bag analytic_cluster_statement = model.createBag(analytics_NS + "analytic_input_collection/" + analytics.getId() + "/" + current_version + "/" + i);

            analytic_cluster_statement.addProperty(RDF.type, analytic_cluster);
            analytic_cluster_statement.addProperty(RDFS.label, "cluster" + i);

            re.eval("sub" + i + "<-subset(df_to_export, loaded_data[column_to_predict] == " + i + ");");

            REXP cluster_uriAsCharacter = re.eval("as.character(sub" + i + "$uri)");

            String[] urisAsStringArray = cluster_uriAsCharacter.asStringArray();
            for (String string : urisAsStringArray) {
                Resource analytic_input_node_statement = model.createResource(string);
                analytic_input_node_statement.addProperty(RDF.type, analytic_input_node);
                analytic_cluster_statement.add(analytic_input_node_statement);

            }

        }

        // For each triplet, create a resource representing the sentence, as well as the subject, 
        // predicate, and object, and then add the triples to the model.
        for (int i = 0; i < predictedValuesAsDoubleArray.length - 1; i++) {

            Resource analytic_result_node_statement = model.createResource(ds + "/" + i);
            analytic_result_node_statement.addProperty(RDF.type, analytic_result_node);
            analytic_result_node_statement.addProperty(RDFS.subClassOf, entity);
            analytic_result_node_statement.addProperty(wasDerivedFrom, analytics_NS + "analytic_input_collection/" + analytics.getId() + "/" + current_version + "/" + String.valueOf(predictedValuesAsDoubleArray[i]));
            analytic_result_node_statement.addProperty(wasGeneratedBy, analytic_process_statement);
            analytic_result_node_statement.addProperty(predictedValue, String.valueOf(predictedValuesAsDoubleArray[i]));

        }

        re.eval("rm(list=ls());");

        return model;
    }

}
