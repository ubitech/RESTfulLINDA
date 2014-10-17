/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytics.formats;

import java.util.AbstractList;

/**
 *
 * @author eleni
 */

abstract public class InputFormat { 

    abstract public AbstractList importData(String pathToFile,boolean isForRDFOutput);

}

