/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import eu.linda.analytics.controller.AnalyticsController;
import eu.linda.analytics.controller.AnalyticsFactory;
import eu.linda.analytics.model.Analytics;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author eleni
 */
public class TestAssociationAlgorithms {
    
    public TestAssociationAlgorithms() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
 
//     @Test
//    public void testApriori() {
//
//        int[] analyticsToTest = {50, 51, 53, 55};
//
//        AnalyticsFactory analyticsFactory = new AnalyticsFactory();
//        AnalyticsController analyticsController = new AnalyticsController(analyticsFactory);
//
//        for (int analytic_id : analyticsToTest) {
//            
//            Analytics analytics = analyticsController.connectToAnalyticsTable(analytic_id);
//            System.out.println("Analytic Process "+analytics.getExportFormat()+" 2 "+analytics.getExportFormat()+" for Apriori");
//            String[] suffixes = analytics.getDocument().split("\\.");
//            String inputSuffix = suffixes[suffixes.length - 1];
//            analyticsController.runAnalytics(inputSuffix, analytics.getAlgorithm_name(), analytics.getExportFormat());
//        }
//
//        String expResult = "";
//        assertEquals(expResult, expResult);
//
//    }
}
