package eu.linda.analytics.model;

public class Query {

    public Query(int id, String endpoint, String sparql, String description) {
        this.id = id;
        this.endpoint = endpoint;
        this.sparql = sparql;
        this.description = description;

    }

    private int id;
    private String endpoint;
    private String sparql;
    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getSparql() {
        return sparql;
    }

    public void setSparql(String sparql) {
        this.sparql = sparql;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
