/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytics.formats;

import eu.linda.analytics.model.Analytics;
import java.util.AbstractList;
import java.util.Map;
import org.rosuda.REngine.Rserve.RConnection;


/**
 *
 * @author eleni
 */

abstract public class InputFormat { 

    abstract public AbstractList importData4weka(String trainDataset,String evaluationDataset,boolean isForRDFOutput,Analytics analytics);
      
    abstract public RConnection importData4R(String trainDataset,String evaluationDataset,boolean isForRDFOutput,Analytics analytics);

}

