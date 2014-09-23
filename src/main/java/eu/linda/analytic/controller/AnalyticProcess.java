/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytic.controller;

import eu.linda.analytics.model.Analytics;
import org.json.JSONArray;

/**
 *
 * @author eleni
 */

abstract public class AnalyticProcess {


    abstract public void train(Analytics a);

    abstract public String eval(Analytics a);
}
