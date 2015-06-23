/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.formats;

import eu.linda.analytics.model.Analytics;
import java.util.AbstractList;
import org.rosuda.JRI.Rengine;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author eleni
 */
abstract public class OutputFormat {

    abstract public void exportData(Analytics analytics, AbstractList dataToExport);
    
    abstract public void exportData(Analytics analytics, RConnection dataToExport);

}
