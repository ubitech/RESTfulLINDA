/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.formats;

import eu.linda.analytics.db.ConnectionController;
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

    HelpfulFunctionsSingleton helpfulFunctions;
    ConnectionController connectionController;

    public RDFInputFormat() {
        helpfulFunctions = HelpfulFunctionsSingleton.getInstance();
        connectionController = ConnectionController.getInstance();
    }

    @Override
    public AbstractList importData4weka(String query_id, boolean isForRDFOutput) {

        String queryURI = connectionController.getQueryURI(query_id);

        helpfulFunctions.nicePrintMessage("import data from uri " + queryURI);

        Instances data = null;
        try {

            URL url = new URL(queryURI);
            if (!helpfulFunctions.isURLResponsive(url)) {
                return null;
            }
            File tmpfile4lindaquery = File.createTempFile("tmpfile4lindaquery" + query_id, ".tmp");
            FileUtils.copyURLToFile(url, tmpfile4lindaquery);
             
            System.out.println("Downloaded File Query: "+ tmpfile4lindaquery);

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
    public Rengine importData4R(String query_id, boolean isForRDFOutput) {
        System.out.println(System.getProperty("java.library.path"));
        System.out.println("R_HOME" + System.getenv().get("R_HOME"));

        Rengine re = Rengine.getMainEngine();
        if (re == null) {
//            re = new Rengine(new String[]{"--vanilla"}, false, null);
            String newargs[] = {"--no-save"};
            re = new Rengine(newargs, false, null);

        }

        if (!re.waitForR()) {
            System.out.println("Cannot load R");
            System.out.println("is alive Rengine??" + re.isAlive());
        }

        String queryURI = connectionController.getQueryURI(query_id);

        helpfulFunctions.nicePrintMessage("import data from uri " + queryURI);
        try {
            URL url = new URL(queryURI);

            if (!helpfulFunctions.isURLResponsive(url)) {
                re.eval(" is_query_responsive <-FALSE ");
                System.out.println("is_query_responsive <-FALSE ");
                
            } else {
                re.eval("is_query_responsive <-TRUE  ");
                System.out.println("is_query_responsive <-TRUE ");


                File tmpfile4lindaquery = File.createTempFile("tmpfile4lindaquery" + query_id, ".tmp");
                FileUtils.copyURLToFile(url, tmpfile4lindaquery);

                re.eval(" loaded_data <- read.csv(file='" + tmpfile4lindaquery + "', header=TRUE, sep=',', na.strings='---');");
                System.out.println(" loaded_data <- read.csv(file='" + tmpfile4lindaquery + "', header=TRUE, sep=',', na.strings='---');");
            }

        } catch (Exception ex) {
            Logger.getLogger(ArffInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return re;
    }

}
