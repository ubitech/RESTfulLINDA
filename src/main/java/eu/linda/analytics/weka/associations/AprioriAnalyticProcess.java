package eu.linda.analytics.weka.associations;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.AnalyticProcess;
import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.formats.OutputFormat;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.utils.Util;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.associations.Apriori;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

/**
 *
 * @author eleni
 */
public class AprioriAnalyticProcess extends AnalyticProcess {


    InputFormat input;
    ConnectionController connectionController;

    public AprioriAnalyticProcess(InputFormat input) {
        Util.nicePrintMessage("Create analytic process for Apriori");
        connectionController = ConnectionController.getInstance();
        this.input = input;

    }

    @Override
    public void train(Analytics analytics) {

    }

    //Get Apriori Rules
    @Override
    public void eval(Analytics analytics, OutputFormat out) {

        try {
            float timeToRun_analytics = 0;
            long startTimeToRun_analytics = System.currentTimeMillis();
            Util.nicePrintMessage("Eval Apriori");

            //clean previous eval info if exists
            Util.cleanPreviousInfo(analytics);
            analytics.setTimeToGet_data(0);
            analytics.setTimeToRun_analytics(0);
            analytics.setData_size(0);
            analytics.setTimeToCreate_RDF(0);

            AbstractList<Instance> abstractListdata;
            Instances data;

            // remove dataset metadata (first two columns) 
            if (Util.isRDFInputFormat(analytics.getEvaluationQuery_id())) {
                abstractListdata = input.importData4weka(Integer.toString(analytics.getEvaluationQuery_id()),"", true, analytics);
                if (abstractListdata==null) return;
                data = (Instances) abstractListdata;
                HashMap<String, Instances> separatedData = Util.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");

            } else if (Util.isRDFExportFormat(analytics.getExportFormat())) {

                abstractListdata = input.importData4weka(Configuration.analyticsRepo + analytics.getDocument(),"", true, analytics);
                 if (abstractListdata==null) return;
                data = (Instances) abstractListdata;
                HashMap<String, Instances> separatedData = Util.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");
            } else {

                abstractListdata = input.importData4weka(Configuration.analyticsRepo + analytics.getDocument(),"", false, analytics);
                data = (Instances) abstractListdata;

            }

            weka.filters.unsupervised.attribute.StringToNominal ff = new weka.filters.unsupervised.attribute.StringToNominal(); // new instance of filter
            ff.setAttributeRange("1-" + data.numAttributes());
            ff.setInputFormat(data);        // inform filter about dataset **AFTER** setting options
            data = Filter.useFilter(data, ff);

            data.setClassIndex(data.numAttributes() - 1);

            // build associator
            Apriori apriori = new Apriori();
            apriori.setClassIndex(data.classIndex());
            apriori.buildAssociations(data);
            apriori.setVerbose(true);
            apriori.globalInfo();

            System.out.println(apriori);

            Util.writeToFile(apriori.toString(), "processinfo", analytics);

            long elapsedTimeToRunAnalyticsMillis = System.currentTimeMillis() - startTimeToRun_analytics;
            // Get elapsed time in seconds
            timeToRun_analytics = elapsedTimeToRunAnalyticsMillis / 1000F;
            analytics.setTimeToRun_analytics(timeToRun_analytics);
            DBSynchronizer.updateLindaAnalyticsProcessPerformanceTime(analytics);

        } catch (Exception ex) {
            Logger.getLogger(AprioriAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
             DBSynchronizer.updateLindaAnalyticsProcessMessage(ex.toString(), analytics.getId());
        }

    }

}
