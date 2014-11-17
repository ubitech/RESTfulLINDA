
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.model;

public class Analytics {

    public Analytics() {
    }

    public Analytics(int id, int category_id, int algorithm_id, String document, String testdocument,int trainQuery_id, int evaluationQuery_id ,String model, String modelReadable, String processinfo, String resultdocument, String exportFormat, int version, String description, boolean publishedToTriplestore, String loadedRDFContext, String parameters) {
        this.id = id;
        this.category_id = category_id;
        this.algorithm_id = algorithm_id;
        this.document = document;
        this.testdocument = testdocument;
        this.trainQuery_id = trainQuery_id;
        this.evaluationQuery_id = evaluationQuery_id;
        this.model = model;
        this.modelReadable = modelReadable;
        this.processinfo = processinfo;
        this.resultdocument = resultdocument;
        this.exportFormat = exportFormat;
        this.version = version;
        this.description = description;
        this.publishedToTriplestore = publishedToTriplestore;
        this.loadedRDFContext = loadedRDFContext;
        this.parameters = parameters;
    }

    private int id;

    private int category_id;

    private int algorithm_id;

    private String algorithm_name;

    private int trainQuery_id;

    private int evaluationQuery_id;

    private String document;

    private String testdocument;

    private String model;

    private String modelReadable;

    private String processinfo;

    private String resultdocument;

    private String exportFormat;

    private int version;

    private String description;

    private boolean publishedToTriplestore;

    private String loadedRDFContext;

    private String parameters;

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

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getTestdocument() {
        return testdocument;
    }

    public void setTestdocument(String testdocument) {
        this.testdocument = testdocument;
    }

    public int getTrainQuery_id() {
        return trainQuery_id;
    }

    public void setTrainQuery_id(int trainQuery_id) {
        this.trainQuery_id = trainQuery_id;
    }

    public int getEvaluationQuery_id() {
        return evaluationQuery_id;
    }

    public void setEvaluationQuery_id(int evaluationQuery_id) {
        this.evaluationQuery_id = evaluationQuery_id;
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

    public String getModelReadable() {
        return modelReadable;
    }

    public void setModelReadable(String modelReadable) {
        this.modelReadable = modelReadable;
    }

    public String getProcessinfo() {
        return processinfo;
    }

    public void setProcessinfo(String processinfo) {
        this.processinfo = processinfo;
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublishedToTriplestore() {
        return publishedToTriplestore;
    }

    public void setPublishedToTriplestore(boolean publishedToTriplestore) {
        this.publishedToTriplestore = publishedToTriplestore;
    }

    public String getLoadedRDFContext() {
        return loadedRDFContext;
    }

    public void setLoadedRDFContext(String loadedRDFContext) {
        this.loadedRDFContext = loadedRDFContext;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

}
