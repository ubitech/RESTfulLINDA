/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytic.formats;

import eu.linda.analytics.weka.utils.HelpfulFuncions;
import java.io.File;
import java.util.AbstractList;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils;

/**
 *
 * @author eleni
 */
public class CSVInputFormat extends InputFormat {
    
    HelpfulFuncions helpfulFuncions;

    public CSVInputFormat() {
        helpfulFuncions = new HelpfulFuncions();
    }


    @Override
    public AbstractList importData(String pathToFile) {
        System.out.println("pathToFile"+pathToFile);

    helpfulFuncions.nicePrintMessage("import CSV file ");
    Instances data = null;
        try {
            
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File(pathToFile));
             data = loader.getDataSet();
            
            
            //data = ConverterUtils.DataSource.read(pathToFile);
            data.setClassIndex(data.numAttributes() - 1);
            
        } catch (Exception ex) {
            Logger.getLogger(ArffInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
     return data;
    
    }
    
}
