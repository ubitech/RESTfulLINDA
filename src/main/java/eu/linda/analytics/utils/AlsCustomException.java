/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.utils;

import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;

/**
 *
 * @author eleni
 */
public class AlsCustomException extends Exception {

    public AlsCustomException(String message, Analytics analytics) {
        DBSynchronizer.updateLindaAnalyticsProcessMessage(message, analytics.getId());

    }
}
