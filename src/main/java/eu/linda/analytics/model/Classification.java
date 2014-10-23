
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytics.model;


public class Classification {


    public Classification() {
    }

    public Classification(int id, int category_id, int algorithm_id, String traindocument, String model, String resultdocument, String exportFormat) {
        this.id = id;
        this.category_id = category_id;
        this.algorithm_id = algorithm_id;
        this.traindocument = traindocument;
        this.model = model;
        this.resultdocument = resultdocument;
        this.exportFormat = exportFormat;
    }
    
    

 
    private int id;

    private int category_id;
    
    private int algorithm_id;
    
    private String algorithm_name;

    private String traindocument;
    
    private String model;

    private String resultdocument;


    private String exportFormat;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public int getAlgorithm_id() {
        return algorithm_id;
    }

    public void setAlgorithm_id(int algorithm_id) {
        this.algorithm_id = algorithm_id;
    }

    public String getTraindocument() {
        return traindocument;
    }

    public void setTraindocument(String traindocument) {
        this.traindocument = traindocument;
    }

   
    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }
    
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getResultdocument() {
        return resultdocument;
    }

    public void setResultdocument(String resultdocument) {
        this.resultdocument = resultdocument;
    }

    public String getAlgorithm_name() {
        return algorithm_name;
    }

    public void setAlgorithm_name(String algorithm_name) {
        this.algorithm_name = algorithm_name;
    }
    
    


}


