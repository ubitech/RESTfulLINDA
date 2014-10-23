/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytics.controller;

import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.formats.OutputFormat;

/**
 *
 * @author eleni
 */
public class AnalyticsInfo {
    
    InputFormat inputformat;
    AnalyticProcess analyticProcess;
    OutputFormat outputformat;

    public AnalyticsInfo() {
    }

    public InputFormat getInputformat() {
        return inputformat;
    }

    public void setInputformat(InputFormat inputformat) {
        this.inputformat = inputformat;
    }

    public AnalyticProcess getAnalyticProcess() {
        return analyticProcess;
    }

    public void setAnalyticProcess(AnalyticProcess analyticProcess) {
        this.analyticProcess = analyticProcess;
    }

    public OutputFormat getOutputformat() {
        return outputformat;
    }

    public void setOutputformat(OutputFormat outputformat) {
        this.outputformat = outputformat;
    }
    
    
      
}
