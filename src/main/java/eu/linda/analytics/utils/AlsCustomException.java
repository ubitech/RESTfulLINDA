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
