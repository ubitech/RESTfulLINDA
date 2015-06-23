package eu.linda.analytics.model;

public class Params {


    public Params() {
    }

    public Params(String name, String value, Algorithm algorithm_id) {
        this.name = name;
        this.value = value;
        this.algorithm_id = algorithm_id;
    }


    private Long id;

    private String name;

    private String value;
    
    private Algorithm algorithm_id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Algorithm getAlgorithm_id() {
        return algorithm_id;
    }

    public void setAlgorithm_id(Algorithm algorithm_id) {
        this.algorithm_id = algorithm_id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
}


