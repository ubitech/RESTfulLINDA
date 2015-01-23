/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.controller;

import eu.linda.analytics.formats.ClusteringRDFGenerator;
import eu.linda.analytics.formats.ForecastingRDFGenerator;
import eu.linda.analytics.formats.GeneralRDFGenerator;
import eu.linda.analytics.formats.RDFGenerator;
import eu.linda.analytics.model.Analytics;

/**
 *
 * @author eleni
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author eleni
 */
public class RDFGenerationFactory {

    public RDFGenerationFactory() {
    }

    public RDFGenerator createRDF(int analytics_category) {

        RDFGenerator rdfGenerator;

        //Create Instances of InputFormat
        if (analytics_category == 4) {
            rdfGenerator = new ForecastingRDFGenerator();
        } else if (analytics_category == 6) {
            rdfGenerator = new ClusteringRDFGenerator();
        } else {
            rdfGenerator = new GeneralRDFGenerator();
        }

        return rdfGenerator;

    }

}
