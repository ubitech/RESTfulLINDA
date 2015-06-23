/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author eleni
 */
public class JRITest {

    public JRITest() {
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
    @Test
    public void testArima() {

        Rengine re = Rengine.getMainEngine();
        if (re == null) {
            re = new Rengine(new String[]{"--vanilla"}, false, null);
        }

        if (!re.waitForR()) {
            System.out.println("Cannot load R");

            System.out.println("is alive??" + re.isAlive());

            return;
        }
        re.eval(" loaded_data <- read.csv(file='/home/eleni/Desktop/mydatasets/Arima/airline.csv', header=TRUE, sep=',');");
        re.eval(" column_number<-ncol(loaded_data);");
        re.eval(" rows_number<-nrow(loaded_data);");
        re.eval(" column_to_predict <-colnames(loaded_data[column_number]);");
        re.eval(" data_matrix<-as.matrix(loaded_data); ");
        re.eval(" firstdate<-as.Date(data_matrix[1]);");
        re.eval(" year_to_start <-as.numeric(format(firstdate, format='%Y'));");
        re.eval(" month_to_start <-as.numeric(format(firstdate, format='%m'));");
        re.eval(" day_to_start <-as.numeric(format(firstdate, format='%d'));");
        re.eval(" datats <- ts(loaded_data[column_number], frequency=12, start=c(year_to_start,month_to_start)); ");
        re.eval(" lastdate <- as.Date(data_matrix[rows_number]); ");
        re.eval(" add.months= function(date,n) seq(date, by = paste (n, 'months'), length = 2)[2];");
        re.eval(" date_to_start_prediction=as.Date(lastdate) ; ");
        re.eval(" add.months(date_to_start_prediction, 1);");
        re.eval(" Date = seq(date_to_start_prediction, by='months', length=12); ");
        re.eval(" m.ar2 <- arima(datats, order = c(1,1,0)); ");
        re.eval("p <- predict(m.ar2, n.ahead = 12);");
        re.eval("rounded_values <-round(p$pred, digits = 3); ");
        re.eval("df_to_export <- data.frame(Date,rounded_values); ");
        re.eval("colnames(df_to_export)[column_number] <- column_to_predict;");
        re.eval("write.csv(df_to_export, file = '/home/eleni/Desktop/mydatasets/Arima/airline2.csv',row.names=FALSE);");
        re.eval("rm(list=ls());");
    }
}
