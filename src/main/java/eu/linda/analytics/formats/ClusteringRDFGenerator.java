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
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;
import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.utils.Util;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 *
 * @author eleni
 */
public class ClusteringRDFGenerator extends RDFGenerator {

    @Override
    public Model generateRDFModel(Analytics analytics, AbstractList dataToExport) {

        //NOT IMPLEMENTED YET
        return null;

    }

    @Override
    public Model generateRDFModel(Analytics analytics, RConnection re) {

        // Create the model and define some prefixes (for nice serialization in RDF/XML and TTL)
        Model model = ModelFactory.createDefaultModel();

        try {
            Util.nicePrintMessage("Generate Clustering RDFModel for R algorithms ");

            int clustersNum = 5;

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

            int current_version = (analytics.getVersion() + 1);

            String analyzedFieldValue = "clusters-AnalyticsID" + analytics.getId() + "-V" + current_version;

            System.out.println("analyzedFieldValue: " + analyzedFieldValue);

            org.rosuda.REngine.REXP predictedValues = re.eval("as.character(df_to_export[[column_to_predict]]);");
            String[] predictedValuesAsDoubleArray = predictedValues.asStrings();

            Date date = new Date();
            DateFormat formatter = new SimpleDateFormat("ddMMyyyy");
            String today = formatter.format(date);
            String base = Configuration.lindaworkbenchURI;
            String datasetContextToString = "analytics" + analytics.getId() + "V" + current_version + "Date" + today;

            //openrdf + analytic_process ID_version_date
            String ds = base + datasetContextToString + "#";

            String analytics_base = Configuration.lindaworkbenchURI + "analyticsontology";
            String analytics_NS = analytics_base + "#";

            model.setNsPrefix("ds", ds);
            model.setNsPrefix("rdf", RDF.getURI());
            model.setNsPrefix("xsd", XSD.getURI());
            model.setNsPrefix("foaf", FOAF.getURI());
            model.setNsPrefix("rdfs", RDFS.getURI());
            model.setNsPrefix("prov", "http://www.w3.org/ns/prov#");
            model.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");
            model.setNsPrefix("sio", "http://semanticscience.org/ontology/sio#");
            model.setNsPrefix("an", Configuration.lindaworkbenchURI + "analyticsontology#");

            // Define local properties
            Property analyzedField = model.createProperty(ds + "analyzedField");
//            Property predictedValue = model.createProperty(ds + "predictedValue");
//            Property wasDerivedFrom = model.createProperty("http://www.w3.org/ns/prov#wasDerivedFrom");
//            Property wasGeneratedBy = model.createProperty("http://www.w3.org/ns/prov#wasGeneratedBy");
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
            Property ispartof = model.createProperty("http://purl.org/dc/elements/1.1/isPartOf");

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

            if (Util.isRDFInputFormat(analytics.getTrainQuery_id())) {

                Resource analytic_train_dataset_statement = model.createResource(Configuration.lindaworkbenchURI + "query-designer/" + analytics.getTrainQuery_id());
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

            Resource analytic_input_node = model.createResource(analytics_NS + "analytic_input_node");

            for (int i = 1; i < clustersNum + 1; i++) {

                Resource analytic_cluster = model.createResource(analytics_NS + "analytic_input_collection");
                Bag analytic_cluster_statement = model.createBag(analytics_base + "/analytic_input_collection/" + analytics.getId() + "/V" + current_version + "/" + i);

                analytic_cluster_statement.addProperty(RDF.type, analytic_cluster);
                analytic_cluster_statement.addProperty(RDFS.label, "cluster" + i);

                re.eval("sub" + i + "<-subset(df_to_export, loaded_data[column_to_predict] == " + i + ");");

                org.rosuda.REngine.REXP cluster_uriAsCharacter = re.eval("as.character(sub" + i + "[[column_with_uri]])");

                String[] urisAsStringArray = cluster_uriAsCharacter.asStrings();

                for (String string : urisAsStringArray) {

                    Resource analytic_input_node_statement = model.createResource(string);
                    analytic_input_node_statement.addProperty(RDF.type, analytic_input_node);
                    analytic_cluster_statement.add(analytic_input_node_statement);

                    analytic_input_node_statement.addProperty(ispartof, analytic_cluster_statement);

                }

            }

//            for (int i = 0; i < predictedValuesAsDoubleArray.length - 1; i++) {
//
//                Resource analytic_input_node_statement = model.createResource(analytics_base + "/analytic_input_collection/" + analytics.getId() + "/" + current_version + "/" + String.valueOf(predictedValuesAsDoubleArray[i]));
//
//                Resource analytic_result_node_statement = model.createResource(ds + "/" + i);
//                analytic_result_node_statement.addProperty(RDF.type, analytic_result_node);
//                analytic_result_node_statement.addProperty(RDFS.subClassOf, entity);
//                analytic_result_node_statement.addProperty(wasDerivedFrom, analytic_input_node_statement);
//                analytic_result_node_statement.addProperty(wasGeneratedBy, analytic_process_statement);
//                analytic_result_node_statement.addProperty(predictedValue, String.valueOf(predictedValuesAsDoubleArray[i]));
//
//            }
            re.eval("rm(list=ls());");

        } catch (RserveException ex) {
            Logger.getLogger(ClusteringRDFGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REXPMismatchException ex) {
            Logger.getLogger(ClusteringRDFGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return model;
    }

}
