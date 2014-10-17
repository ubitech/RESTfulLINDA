/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.formats;

import eu.linda.analytics.weka.utils.HelpfulFunctions;
import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 *
 * @author eleni
 */
public class CSVInputFormat extends InputFormat {

    HelpfulFunctions helpfulFuncions;

    public CSVInputFormat() {
        helpfulFuncions = new HelpfulFunctions();
    }

    @Override
    public AbstractList importData(String pathToFile, boolean isForRDFOutput) {

        helpfulFuncions.nicePrintMessage("import CSV file ");

        System.out.println("Import data from file: " + pathToFile);

        Instances data = null;
        try {
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File(pathToFile));
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

    public static void main(String[] args) throws Exception {
        Instances data = null;
        String[] options = new String[2];
        options[0] = "-S";        // "range"
        options[1] = "1,2";

        CSVLoader loader = new CSVLoader();
        try {
            loader.setSource(new File("/home/eleni/Desktop/mydatasets/NYRandonResearchTotest2.csv"));

            loader.setStringAttributes("1,2");
            loader.setFieldSeparator(",");

            data = loader.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);

        } catch (IOException ex) {
            Logger.getLogger(CSVInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CSVInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
