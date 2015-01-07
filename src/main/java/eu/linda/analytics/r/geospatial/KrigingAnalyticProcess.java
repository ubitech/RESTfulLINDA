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
import org.rosuda.JRI.Rengine;

/**
 *
 * @author eleni
 */
public class KrigingAnalyticProcess extends AnalyticProcess {

    HelpfulFunctionsSingleton helpfulFunctions;
    InputFormat input;

    public KrigingAnalyticProcess(InputFormat input) {
        helpfulFunctions = HelpfulFunctionsSingleton.getInstance();
        helpfulFunctions.nicePrintMessage("Create analytic process for Geospatial Algorithm - Ordinary Kriging");
        this.input = input;

    }

    @Override
    public void train(Analytics analytics) {

    }

    @Override
    public void eval(Analytics analytics, OutputFormat out) {
              
        
//        
//        String startDate="";
//        String endDate="";
//        //get parameters
//        String parameters = analytics.getParameters();
//        
//        String[] splitedparameters = parameters.split("->");
//        for (String parameter : splitedparameters) {
//            System.out.println("parameter"+parameter);
//            
//            if (parameter.contains("StartDate")){
//               String[] startdateP =   parameter.split("StartDate");
//               startDate = startdateP[1].trim();
//               System.out.println("startDate"+startDate);
//            }
//             if (parameter.contains("EndDate")){
//               String[] endDateP =   parameter.split("EndDate");
//               endDate = endDateP[1].trim();
//               System.out.println("endDate"+endDate);
//            }
//        }
//        
        //clean previous eval info if exists
        helpfulFunctions.cleanPreviousInfo(analytics);
        Rengine re;
        if (helpfulFunctions.isRDFInputFormat(analytics.getTrainQuery_id())) {
            re = input.importData4R(Integer.toString(analytics.getTrainQuery_id()), true);
        } else {
            re = input.importData4R(Configuration.analyticsRepo + analytics.getDocument(), true);
        }
        String RScript ="";
        
        re.eval("library(sp)");
        RScript += "library(sp)\n";
        
        re.eval("library(gstat)");
        RScript += "library(gstat)\n";
        
        re.eval("loaded_data <- read.csv('/home/eleni/Downloads/ca21.dat');");
        RScript += " loaded_data <- read.csv('/home/eleni/Downloads/ca21.dat');\n";
        
      
        re.eval("column_number<-ncol(loaded_data);");
        RScript += "column_number<-ncol(loaded_data);\n";
        
        
        re.eval("column_to_predict <-colnames(loaded_data[column_number]);");
        RScript += "column_to_predict <-colnames(loaded_data[column_number]);\n";

        re.eval("coordinates(loaded_data) = ~x+y");
        RScript += "coordinates(loaded_data) = ~x+y\n";
        
        re.eval("v <- variogram(log(ca20)~1, loaded_data)");
        RScript += "v <- variogram(log(ca20)~1, loaded_data)\n";
        
        re.eval("m <- fit.variogram(v, vgm(1, 'Sph', 300, 1))");
        RScript += "m <- fit.variogram(v, vgm(1, 'Sph', 300, 1))\n";
       
//        re.eval("plot(v, model = m)");
//        RScript += "plot(v, model = m)\n";

        re.eval("loaded_data_eval <- read.csv('/home/eleni/Downloads/ca21Evalwithuri1.dat')");
        RScript += "loaded_data_eval <- read.csv('/home/eleni/Downloads/ca21Evalwithuri1.dat')\n";
        
//        re.eval("data_matrix<-as.matrix(loaded_data_eval);");
//        RScript += "data_matrix<-as.matrix(loaded_data_eval);";
//        RScript += "uri<-data_matrix[,c('uri')];";
//        


        re.eval("coordinates(loaded_data_eval) = ~x+y");
        RScript += "coordinates(loaded_data_eval) = ~x+y\n";

        //#sim <- krige(formula = log(ca20)~1, d, dEval, model = m, nmax = 15, beta = 5.9, nsim = 3)
        //#sim <- krige(formula = ca20~sqrt(altitude), d, dEval, model = m, nmax = 15, nsim = 3)
        re.eval("df <- krige(formula = ca20~1, loaded_data, loaded_data_eval, model = m, nmax = 15, beta = 5.9, nsim = 3)");
        RScript += "df <- krige(formula = ca20~1, loaded_data, loaded_data_eval, model = m, nmax = 15, beta = 5.9, nsim = 3)\n";

//        re.eval("spplot(df)");
//        RScript += "spplot(df)\n";

        re.eval("df_to_export<- read.csv('/home/eleni/Downloads/ca21Evalwithuri1.dat');");
        RScript += "df_to_export<- read.csv('/home/eleni/Downloads/ca21Evalwithuri1.dat')\n";
        
        re.eval("df_to_export[[column_to_predict]] <- df$sim1");
        RScript += "df_to_export[[column_to_predict]] <- df$sim1 \n";

 
        helpfulFunctions.writeToFile(RScript, "processinfo", analytics);

//       re.eval("write.csv(df_to_export, file = '"+Configuration.analyticsRepo+"tmp/tmp4processid"+analytics.getId()+".csv',row.names=FALSE);");
//       re.eval("rm(list=ls());");
       out.exportData(analytics, re);
    }

}

