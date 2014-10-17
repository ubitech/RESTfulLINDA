/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytics.controller;

import eu.linda.analytics.formats.InputFormat;
import eu.linda.analytics.model.Analytics;
import java.util.AbstractList;
import org.json.JSONArray;
import weka.core.Instances;

/**
 *
 * @author eleni
 */

abstract public class AnalyticProcess {


    abstract public void train(Analytics a);

    abstract public AbstractList eval(Analytics a);
}
