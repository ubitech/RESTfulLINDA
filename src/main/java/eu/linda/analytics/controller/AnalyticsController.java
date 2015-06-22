/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.controller;

import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.formats.OutputFormat;
import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;
import java.util.AbstractList;

/**
 *
 * @author eleni
 */
public class AnalyticsController {

    AnalyticsFactory factory;
    Analytics analytics;

    public AnalyticsController(AnalyticsFactory factory) {
        this.factory = factory;
    }

    public void setAnalytics(Analytics analytics) {
        this.analytics = analytics;
    }

    public AnalyticsInfo runAnalytics(String inputformat, String algorithm, String ouputformat) {

        InputFormat in;
        AnalyticProcess ap;
        OutputFormat out;

        AnalyticsInfo info = factory.createAnalytics(inputformat, algorithm, ouputformat);

        in = info.getInputformat();
        ap = info.getAnalyticProcess();
        out = info.getOutputformat();

        if (analytics.isCreateModel()== false) {
            ap.train(analytics);
        }

        ap.eval(analytics, out);

        return info;

    }

}
