/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytics.formats;

import java.util.AbstractList;
import org.rosuda.JRI.Rengine;


/**
 *
 * @author eleni
 */

abstract public class InputFormat { 

    abstract public AbstractList importData4weka(String pathToFile,boolean isForRDFOutput);
    
    abstract public Rengine importData4R(String pathToFile,boolean isForRDFOutput);

}

