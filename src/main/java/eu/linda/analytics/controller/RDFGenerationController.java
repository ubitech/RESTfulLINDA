/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytics.controller;


/**
 *
 * @author eleni
 */

import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.formats.OutputFormat;
import eu.linda.analytics.formats.RDFGenerator;
import eu.linda.analytics.model.Analytics;
import java.util.AbstractList;

/**
 *
 * @author eleni
 */
public class RDFGenerationController {
    
    RDFGenerationFactory factory;
    
    public RDFGenerationController(RDFGenerationFactory factory) {
        this.factory = factory;
    }
    
}

