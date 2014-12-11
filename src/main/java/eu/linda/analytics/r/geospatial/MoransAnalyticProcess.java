/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.r.geospatial;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.AnalyticProcess;
import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.formats.OutputFormat;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctionsSingleton;
import java.util.HashMap;
import org.rosuda.JRI.RBool;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import weka.core.Instances;

/**
 *
 * @author eleni
 */
public class MoransAnalyticProcess extends AnalyticProcess {

    HelpfulFunctionsSingleton helpfulFunctions;
    InputFormat input;

    public MoransAnalyticProcess(InputFormat input) {
        helpfulFunctions = HelpfulFunctionsSingleton.getInstance();
        helpfulFunctions.nicePrintMessage("Create analytic process for Moran's I Algorithm");
        this.input = input;

    }

    @Override
    public void train(Analytics analytics) {
    }

    @Override
    public void eval(Analytics analytics, OutputFormat out) {

        //clean previous eval info if exists
        helpfulFunctions.cleanPreviousInfo(analytics);
        Rengine re;
        if (helpfulFunctions.isRDFInputFormat(analytics.getTrainQuery_id())) {
            re = input.importData4R(Integer.toString(analytics.getTrainQuery_id()), true);

        } else {
            re = input.importData4R(Configuration.analyticsRepo + analytics.getDocument(), true);

        }
        System.out.println("11111111111111" + re.eval("loaded_data"));
        //String RScript = "loaded_data <- read.csv(file='" + Configuration.docroot + analytics.getDocument() + "', header=TRUE, sep=',');\n";
        String RScript = "";

        re.eval("library(ape)");
        RScript += "library(ape)\n";

        re.eval("loaded_data.dists <- as.matrix(dist(cbind(loaded_data$long, loaded_data$lat)))");
        RScript += "loaded_data.dists <- as.matrix(dist(cbind(loaded_data$long, loaded_data$lat)))\n";

        re.eval("diag(loaded_data.dists.inv) <- 0");
        RScript += "diag(loaded_data.dists.inv) <- 0\n";

        re.eval("loaded_data.dists.inv[1:5, 1:5]");
        RScript += "loaded_data.dists.inv[1:5, 1:5]\n";

        re.eval("morans_result <- Moran.I(loaded_data$percent_rate, loaded_data.dists.inv)");
        RScript += "morans_result<-Moran.I(loaded_data$percent_rate, loaded_data.dists.inv)\n";

        re.eval("morans_result$p.value");

        RScript += "----------------MORAN's RESULT-------------\n";

//        RBool lala = re.eval("morans_result$p.value>0.4").asBool();

        RScript += "if Moran's I $p.value is greater than 0.4 \n then there is a significant spatial autocorrelation in your data \n and you should take into account in next analytic processes";

//        if (lala.isTRUE()) {
//            RScript +="TRRRRRRRRRRRRRRRRUUUUUUUUUUUUUUUEEEEEEEEEEEEEEee";
//        }

        helpfulFunctions.writeToFile(RScript, "processinfo", analytics);

//        re.eval("write.csv(df_to_export, file = '/home/eleni/Desktop/mydatasets/airline2.csv',row.names=FALSE);");
//        re.eval("rm(list=ls());");
        // out.exportData(analytics, re);
    }

}
