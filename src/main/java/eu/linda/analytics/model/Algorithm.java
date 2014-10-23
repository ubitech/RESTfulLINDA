

package eu.linda.analytics.model;


public class Algorithm {


    public Algorithm() {
    }

    public Algorithm(String name, String description, Category category_id) {
        this.name = name;
        this.description = description;
        this.category_id = category_id;
    }
  

    private Long id;

    private String name;


    private String description;

    private Category category_id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory_id() {
        return category_id;
    }

    public void setCategory_id(Category category_id) {
        this.category_id = category_id;
    }

   

}

