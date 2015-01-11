/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.model;

/**
 *
 * @author eleni
 */
public class Plot {

    public Plot() {
    }

    public Plot(Long id, String description, String image) {
        this.id = id;
        this.description = description;
        this.image = image;
    }

    private Long id;

    private String description;

    private String image;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
