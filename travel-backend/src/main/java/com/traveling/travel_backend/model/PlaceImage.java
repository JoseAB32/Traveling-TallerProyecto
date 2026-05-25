package com.traveling.travel_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;

@Entity
@Table(name = "place_images")
public class PlaceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "image_url", nullable = false)
    @JsonProperty("image_url")
    private String imageUrl;

    @Column(name = "public_id")
    @JsonProperty("public_id")
    private String publicId;

    @Column(name = "alt_text")
    @JsonProperty("alt_text")
    private String altText;

    @Column(name = "display_order")
    @JsonProperty("display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_main")
    @JsonProperty("is_main")
    private Boolean isMain = false;

    private boolean state = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    @JsonIgnore
    private Place place;

    public PlaceImage() {}

    public long getId() { return id; }

    public String getImageUrl() { return imageUrl; }

    public String getPublicId() { return publicId; }

    public String getAltText() { return altText; }

    public Integer getDisplayOrder() { return displayOrder; }

    public Boolean getIsMain() { return isMain; }

    public boolean isState() { return state; }

    public Place getPlace() { return place; }

    public void setId(long id) { this.id = id; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public void setPublicId(String publicId) { this.publicId = publicId; }

    public void setAltText(String altText) { this.altText = altText; }

    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public void setIsMain(Boolean main) { isMain = main; }

    public void setState(boolean state) { this.state = state; }

    public void setPlace(Place place) { this.place = place; }
}