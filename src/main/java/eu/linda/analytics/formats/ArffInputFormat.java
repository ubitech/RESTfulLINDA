/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytics.formats;

import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.utils.Util;
import java.util.AbstractList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.REngine.Rserve.RConnection;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 *
 * @author eleni
 */
public class ArffInputFormat extends InputFormat {
    
    Util helpfulFuncions;

    public ArffInputFormat() {
        helpfulFuncions = Util.getInstance();
    }


    @Override
    public AbstractList importData4weka(String trainDataset, String evaluationDataset, boolean isForRDFOutput, Analytics analytics) {

    Util.nicePrintMessage("import Arff file "+trainDataset);
    

    Instances data = null;
    //Instances newData = null;
        try {
            
            data = ConverterUtils.DataSource.read(trainDataset);
            data.setClassIndex(data.numAttributes() - 1);
            
        } catch (Exception ex) {
            Logger.getLogger(ArffInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
     return data;
    
    }
    
    @Override
    public RConnection importData4R(String trainDataset,String evaluationDataset, boolean isForRDFOutput,Analytics analytics) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map importData4weka1(String trainDataset, String evaluationDataset, boolean isForRDFOutput, Analytics analytics) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    
}
