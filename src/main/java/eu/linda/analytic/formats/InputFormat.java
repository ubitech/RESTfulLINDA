/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytic.formats;

import eu.linda.analytics.model.Analytics;
import java.util.AbstractList;
import weka.core.Instances;

/**
 *
 * @author eleni
 */

abstract public class InputFormat {

    abstract public AbstractList importData(String pathToFile);

}

