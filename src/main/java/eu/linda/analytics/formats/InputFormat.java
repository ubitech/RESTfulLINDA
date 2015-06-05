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

abstract public class InputFormat { 

    abstract public AbstractList importData4weka(String pathToFile,boolean isForRDFOutput,Analytics analytics);
    
    abstract public Rengine importData4R(String pathToFile,boolean isForRDFOutput,Analytics analytics);
    
    abstract public RConnection importData4R1(String pathToFile,boolean isForRDFOutput,Analytics analytics);

}

