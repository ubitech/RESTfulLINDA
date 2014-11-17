/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytics.formats;

import eu.linda.analytics.weka.utils.HelpfulFunctionsSingleton;
import java.io.File;
import java.net.URL;
import java.util.AbstractList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.rosuda.JRI.Rengine;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 *
 * @author eleni
 */
public class RDFInputFormat extends InputFormat {

      HelpfulFunctionsSingleton helpfulFuncions;

    public RDFInputFormat() {
        helpfulFuncions = HelpfulFunctionsSingleton.getInstance();
    }

    @Override
    public AbstractList importData4weka(String query_id, boolean isForRDFOutput) {
    
        String queryURI = helpfulFuncions.getQueryURI(query_id);
 
        helpfulFuncions.nicePrintMessage("import data from uri "+queryURI);
        
        Instances data = null;
        try {
            
            URL url = new URL(queryURI);
            File tmpfile4lindaquery = File.createTempFile("tmpfile4lindaquery"+query_id, ".tmp"); 
            FileUtils.copyURLToFile(url,tmpfile4lindaquery );
            
            CSVLoader loader = new CSVLoader();
            loader.setSource(tmpfile4lindaquery);
            if (isForRDFOutput) {
                loader.setStringAttributes("1,2");
            }

            loader.setFieldSeparator(",");
            data = loader.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);

        } catch (Exception ex) {
            Logger.getLogger(ArffInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    
    
    
    }

    @Override
    public Rengine importData4R(String pathToFile, boolean isForRDFOutput) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
