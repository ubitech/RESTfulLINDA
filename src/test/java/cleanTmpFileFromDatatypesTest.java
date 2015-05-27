
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author eleni
 */
public class cleanTmpFileFromDatatypesTest {

//    @Test
//    public void cleanTmpFileFromDatatypesTest() {
//
//        cleanTmpFileFromDatatypes("/home/eleni/Downloads/tmpfile4lindaquery1257823751582418430325.tmp");
//    }

    public boolean cleanTmpFileFromDatatypes(String csvFile) {

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(csvFile));

            //after put in buffer delete file
            File file = new File(csvFile);
            file.delete();

            File fout = new File(csvFile);
            FileOutputStream fos = new FileOutputStream(fout);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] columns = line.split(cvsSplitBy);

                String newline = "";

                for (int i = 0; i < columns.length; i++) {
                    if (columns[i].contains("^^")) {
                        String[] splitedvalues = columns[i].split("\\^\\^http");
                        newline += "," + splitedvalues[0];
                    }
                }
                bw.write(newline);
                bw.newLine();

            }

            bw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

}
